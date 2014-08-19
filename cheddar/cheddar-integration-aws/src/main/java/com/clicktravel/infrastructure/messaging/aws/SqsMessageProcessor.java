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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
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
     * Maximum duration (in seconds) to wait for messages on the queue. If there is at least one message on the queue,
     * the actual duration will be shorter.
     */
    private static final int LONG_POLL_DURATION_SECONDS = 20;
    private static final int MAX_THREADS = 10;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ReceiveMessageRequest receiveMessageRequest;
    private final String queueUrl;
    private final AmazonSQS amazonSqsClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, MessageHandler> messageHandlers;
    private volatile boolean processing = false;
    private final ExecutorService executorService;
    private final RateLimiter rateLimiter;

    public SqsMessageProcessor(final AmazonSQS amazonSqsClient, final String queueName,
            final Map<String, MessageHandler> messageHandlers, final RateLimiter rateLimiter) {
        this.amazonSqsClient = amazonSqsClient;
        this.rateLimiter = rateLimiter;
        queueUrl = amazonSqsClient.getQueueUrl(new GetQueueUrlRequest(queueName)).getQueueUrl();
        receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
        receiveMessageRequest.setWaitTimeSeconds(LONG_POLL_DURATION_SECONDS);
        this.messageHandlers = new HashMap<>(messageHandlers);
        final MessageHandlingWorkerRejectedExecutionHandler rejectedExecutionHandler = new MessageHandlingWorkerRejectedExecutionHandler(
                this);
        final ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(MAX_THREADS * 2);
        executorService = new ThreadPoolExecutor(MAX_THREADS, MAX_THREADS, 1L, TimeUnit.SECONDS, workQueue,
                rejectedExecutionHandler);
    }

    @Override
    public void run() {
        processing = true;
        while (processing) {
            final List<com.amazonaws.services.sqs.model.Message> sqsMessages = amazonSqsClient.receiveMessage(
                    receiveMessageRequest).getMessages();
            for (final com.amazonaws.services.sqs.model.Message sqsMessage : sqsMessages) {
                processSqsMessage(sqsMessage);
            }
        }
    }

    private void processSqsMessage(final com.amazonaws.services.sqs.model.Message sqsMessage) {
        final DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest().withQueueUrl(queueUrl)
                .withReceiptHandle(sqsMessage.getReceiptHandle());
        final Message message = getMessage(sqsMessage);
        final MessageHandler messageHandler = messageHandlers.get(message.getType());
        if (messageHandler != null) {
            applyRateLimiter();
            executorService.execute(new MessageHandlingWorker(message, messageHandler,
                    amazonSqsClient, deleteMessageRequest));
        } else {
            logger.debug("Unsupported message type: " + message.getType());
            amazonSqsClient.deleteMessage(deleteMessageRequest);
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

    public void stopProcessing() {
        processing = false;
        executorService.shutdown();
    }

    public boolean isProcessing() {
        return processing;
    }

}