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
import java.util.concurrent.*;

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
     * Maximum duration (in seconds) to wait for messages on the queue. If there is at least one message on the queue,
     * the actual duration will be shorter.
     */
    private static final int LONG_POLL_DURATION_SECONDS = 20;
    private static final int SHORT_POLL_DURATION_SECONDS = 2;
    private static final int MAX_RECEIVED_MESSAGES = 10;
    private static final int NUM_THREADS = 10;
    private static final int MAX_RUNNABLES = NUM_THREADS * 2;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String queueUrl;
    private final AmazonSQS amazonSqsClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, MessageHandler> messageHandlers;
    private final ExecutorService executorService;
    private final RateLimiter rateLimiter;
    private final Semaphore semaphore;
    private volatile boolean shutdownRequested;
    private volatile boolean shutdownRequestImminent;

    public SqsMessageProcessor(final AmazonSQS amazonSqsClient, final String queueName,
            final Map<String, MessageHandler> messageHandlers, final RateLimiter rateLimiter) {
        this.amazonSqsClient = amazonSqsClient;
        this.rateLimiter = rateLimiter;
        queueUrl = amazonSqsClient.getQueueUrl(new GetQueueUrlRequest(queueName)).getQueueUrl();
        this.messageHandlers = new HashMap<>(messageHandlers);
        final LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
        semaphore = new Semaphore(MAX_RUNNABLES);
        executorService = new ThreadPoolExecutor(NUM_THREADS, NUM_THREADS, 0L, TimeUnit.SECONDS, workQueue);
    }

    @Override
    public void run() {
        try {
            processMessagesUntilShutdownRequested();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executorService.shutdown();
        }
    }

    private void processMessagesUntilShutdownRequested() throws InterruptedException {
        while (!shutdownRequested) {
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
            executorService.execute(new MessageHandlingWorker(message, messageHandler, amazonSqsClient,
                    deleteMessageRequest, semaphore));
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

    public void shutdownImminent() {
        shutdownRequestImminent = true;
    }

    public void shutdown() {
        shutdownRequested = true;
    }

    public boolean isProcessing() {
        return !shutdownRequested;
    }
}