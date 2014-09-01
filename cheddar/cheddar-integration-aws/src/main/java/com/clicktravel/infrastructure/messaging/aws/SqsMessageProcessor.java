/*
 * Copyright 2014 Click Travel Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.clicktravel.infrastructure.messaging.aws;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.clicktravel.cheddar.infrastructure.messaging.Message;
import com.clicktravel.cheddar.infrastructure.messaging.MessageHandler;
import com.clicktravel.cheddar.infrastructure.messaging.SimpleMessage;
import com.clicktravel.common.concurrent.RateLimiter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SqsMessageProcessor implements Runnable {

    /**
     * Maximum duration (in seconds) to wait for messages on the queue during normal processing. If there is at least
     * one message on the queue, the actual duration will be shorter.
     */
    private static final int LONG_POLL_DURATION_SECONDS = 20;

    /**
     * Maximum duration (in seconds) to wait for messages on the queue during handing over to a new application instance
     * in a blue-green deployment. This is shorter to enable prompt termination of this message processor.
     */
    private static final int SHORT_POLL_DURATION_SECONDS = 2;

    /**
     * Maximum number of messages to receive per call to SQS. Using larger numbers decreases the number of network calls
     * and thus increases throughput, at the possible expense of continuous performance.
     */
    private static final int MAX_RECEIVED_MESSAGES = 10;

    /**
     * Controls when messages are received from the SQS queue by setting an ideal minimum number of runnable tasks for
     * each thread. This minimum includes the currently executing task and those on the thread pool work queue. When the
     * number of runnable tasks dips below the ideal, more messages are received.
     */
    private static final int IDEAL_RUNNABLES_PER_THREAD = 2; // Each thread has 1 executing + 1 queued runnable

    /**
     * Minimum number of empty responses for consecutive ReceiveMessageRequests to conclude queue is empty
     */
    private static final int QUEUE_DRAINED_THRESHOLD = 2;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String queueName;
    private final String queueUrl;
    private final AmazonSQS amazonSqsClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, MessageHandler> messageHandlers;
    private final SqsMessageProcessorExecutor executor;
    private final RateLimiter rateLimiter;
    private final Semaphore semaphore;
    private int noReceivedMessagesCount;
    private volatile boolean shutdownRequested;
    private volatile boolean shutdownWhenQueueDrainedRequested;
    private volatile boolean shutdownRequestImminent;

    public SqsMessageProcessor(final AmazonSQS amazonSqsClient, final String queueName,
            final Map<String, MessageHandler> messageHandlers, final RateLimiter rateLimiter,
            final SqsMessageProcessorExecutor executor) {
        this.amazonSqsClient = amazonSqsClient;
        this.queueName = queueName;
        this.rateLimiter = rateLimiter;
        this.executor = executor;
        queueUrl = amazonSqsClient.getQueueUrl(new GetQueueUrlRequest(queueName)).getQueueUrl();
        this.messageHandlers = new HashMap<>(messageHandlers);
        final int minRunnables = executor.getMaximumPoolSize() * IDEAL_RUNNABLES_PER_THREAD;
        final int maxRunnables = minRunnables + MAX_RECEIVED_MESSAGES - 1;
        semaphore = new Semaphore(maxRunnables);
    }

    @Override
    public void run() {
        try {
            processMessagesUntilShutdownRequested();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            logger.debug("Stopped receiving messages from queue [" + queueName
                    + "]; Initiating shutdown of task executor");
            executor.shutdown();
        }
    }

    private void processMessagesUntilShutdownRequested() throws InterruptedException {
        while (!(shutdownRequested || (shutdownWhenQueueDrainedRequested && queueDrained()))) {
            // Block until there is capacity to handle up to MAX_RECEIVED_MESSAGES
            semaphore.acquire(MAX_RECEIVED_MESSAGES);
            List<com.amazonaws.services.sqs.model.Message> sqsMessages = Collections.emptyList();
            try {
                // Check again if message processing is not being shut down
                if (!shutdownRequested) {
                    final int pollSeconds = shutdownRequestImminent ? SHORT_POLL_DURATION_SECONDS
                            : LONG_POLL_DURATION_SECONDS;
                    final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl)
                            .withWaitTimeSeconds(pollSeconds).withMaxNumberOfMessages(MAX_RECEIVED_MESSAGES);
                    sqsMessages = amazonSqsClient.receiveMessage(receiveMessageRequest).getMessages();
                    if (shutdownWhenQueueDrainedRequested) {
                        noReceivedMessagesCount = sqsMessages.isEmpty() ? (noReceivedMessagesCount + 1) : 0;
                    }
                }
            } finally {
                semaphore.release(MAX_RECEIVED_MESSAGES - sqsMessages.size());
            }
            for (final com.amazonaws.services.sqs.model.Message sqsMessage : sqsMessages) {
                processSqsMessage(sqsMessage); // Must process each received message
            }
        }
    }

    private void processSqsMessage(final com.amazonaws.services.sqs.model.Message sqsMessage) {
        final DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest().withQueueUrl(queueUrl)
                .withReceiptHandle(sqsMessage.getReceiptHandle());
        Message message;
        MessageHandler messageHandler;
        // TODO Replace the following hack to generalise message listener to handle any message, including foreign ones
        if (messageHandlers.containsKey("")) {
            // Handle foreign message
            message = new SimpleMessage("", sqsMessage.getBody());
            messageHandler = messageHandlers.get("");
        } else {
            // Handle native Cheddar message
            message = getMessage(sqsMessage);
            messageHandler = messageHandlers.get(message.getType());
        }
        if (messageHandler != null) {
            applyRateLimiter();
            executor.execute(new MessageHandlingWorker(message, messageHandler, amazonSqsClient, deleteMessageRequest,
                    semaphore));
        } else {
            logger.debug("Unsupported message type: " + message.getType());
            amazonSqsClient.deleteMessage(deleteMessageRequest);
            semaphore.release();
        }
    }

    private Message getMessage(final com.amazonaws.services.sqs.model.Message sqsMessage) {
        try {
            final JsonNode jsonNode = mapper.readTree(sqsMessage.getBody());
            final String messageType = jsonNode.get("Subject").textValue();
            final String messagePayload = jsonNode.get("Message").textValue();
            return new SimpleMessage(messageType, messagePayload);
        } catch (final Exception e) {
            throw new IllegalStateException("Could not parse message from SQS message: " + sqsMessage.getBody(), e);
        }
    }

    private void applyRateLimiter() {
        if (rateLimiter != null) {
            try {
                rateLimiter.takeToken();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean queueDrained() {
        return noReceivedMessagesCount >= QUEUE_DRAINED_THRESHOLD;
    }

    public void shutdownImminent() {
        shutdownRequestImminent = true;
    }

    public void shutdown() {
        shutdownRequested = true;
    }

    public void shutdownWhenQueueDrained() {
        shutdownWhenQueueDrainedRequested = true;
    }

    public void awaitTermnation() {
        try {
            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                logger.warn("SqsMessageProcessor executor for queue [" + queueName + "] did not terminate");
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}