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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonServiceException.ErrorType;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageSendException;

/**
 * Represents an actual AWS SQS queue that exists in the AWS environment. Provides some convenience methods for working
 * with the actual AWS SQS queue.
 */
public class SqsQueueResource {

    /**
     * Time to pause SQS request has service error (5xx) response, in milliseconds
     */
    private static final long SQS_SERVICE_ERROR_PAUSE_MILLIS = 500;

    private static final String AWS_POLICY_ATTRIBUTE = QueueAttributeName.Policy.toString();
    private static final String SQS_QUEUE_ARN_ATTRIBUTE = QueueAttributeName.QueueArn.toString();

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String queueName;
    private final AmazonSQS amazonSqsClient;
    private final String queueUrl;
    private String queueArn; // lazily initialised

    public SqsQueueResource(final String queueName, final String queueUrl, final AmazonSQS amazonSqsClient) {
        this.queueName = queueName;
        this.queueUrl = queueUrl;
        this.amazonSqsClient = amazonSqsClient;
    }

    /**
     * Sends a message to the AWS SQS queue
     * @param messageBody Body of the message to send
     */
    public void sendMessage(final String messageBody) {
        final SendMessageRequest sendMessageRequest = new SendMessageRequest(queueUrl, messageBody);
        doSendRequest(sendMessageRequest);
    }

    /**
     * Sends a delayed message to the AWS SQS queue
     * @param messageBody Body of the message to send
     * @param delaySeconds Number of seconds to delay visibility of the sent message
     */
    public void sendDelayedMessage(final String messageBody, final int delaySeconds) {
        final SendMessageRequest sendMessageRequest = new SendMessageRequest(queueUrl, messageBody)
                .withDelaySeconds(delaySeconds);
        doSendRequest(sendMessageRequest);
    }

    private void doSendRequest(final SendMessageRequest sendMessageRequest) {
        try {
            amazonSqsClient.sendMessage(sendMessageRequest);
            logger.trace("Successfully sent message: Payload=" + sendMessageRequest.getMessageBody()
                    + "] to SQS queue: [" + queueName + "]");
        } catch (final Exception e) {
            throw new MessageSendException("Could not send message to SQS queue " + queueName, e);
        }
    }

    /**
     * Receive up to 10 messages from the AWS SQS queue, using short polling.
     * @return {@code List<com.amazonaws.services.sqs.model.Message>} received messages
     * @throws InterruptedException
     */
    public List<com.amazonaws.services.sqs.model.Message> receiveMessages() throws InterruptedException {
        final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
        return doReceiveRequest(receiveMessageRequest);
    }

    /**
     * Receive up to the given number of messages from the AWS SQS queue, waiting the specified time for messages to
     * appear.
     * @param waitTimeSeconds Number of seconds to wait for messages to appear
     * @param maxMessages Maximum number of messages returned, may be up to 10.
     * @return {@code List<com.amazonaws.services.sqs.model.Message>} received messages
     * @throws InterruptedException
     */
    public List<com.amazonaws.services.sqs.model.Message> receiveMessages(final int waitTimeSeconds,
            final int maxMessages) throws InterruptedException {
        final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl).withWaitTimeSeconds(
                waitTimeSeconds).withMaxNumberOfMessages(maxMessages);
        return doReceiveRequest(receiveMessageRequest);
    }

    private List<com.amazonaws.services.sqs.model.Message> doReceiveRequest(
            final ReceiveMessageRequest receiveMessageRequest) throws InterruptedException {
        while (true) {
            try {
                return amazonSqsClient.receiveMessage(receiveMessageRequest).getMessages();
            } catch (final AmazonServiceException amazonServiceException) {
                logger.error("Error receiving SQS messages on queue:[" + queueName + "]", amazonServiceException);
                // Ignore service errors (5xx) and hope SQS recovers
                if (amazonServiceException.getErrorType().equals(ErrorType.Service)) {
                    Thread.sleep(SQS_SERVICE_ERROR_PAUSE_MILLIS);
                } else {
                    throw amazonServiceException;
                }
            }
        }
    }

    /**
     * Delete a previously received message from the AWS SQS queue.
     * @param receiptHandle Identifier given with receipt of message
     */
    public void deleteMessage(final String receiptHandle) {
        final DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest(queueUrl, receiptHandle);
        amazonSqsClient.deleteMessage(deleteMessageRequest);
    }

    /**
     * Sets the {@link Policy} of the AWS SQS queue
     * @param policy {@link Policy} to set
     */
    public void setPolicy(final Policy policy) {
        final Map<String, String> queueAttributes = Collections.singletonMap(AWS_POLICY_ATTRIBUTE, policy.toJson());
        amazonSqsClient.setQueueAttributes(new SetQueueAttributesRequest(queueUrl, queueAttributes));
    }

    /**
     * @return The ARN of the AWS SQS queue
     */
    public String queueArn() {
        if (queueArn == null) {
            final GetQueueAttributesRequest request = new GetQueueAttributesRequest(queueUrl)
                    .withAttributeNames(SQS_QUEUE_ARN_ATTRIBUTE);
            final GetQueueAttributesResult getQueueAttributesResult = amazonSqsClient.getQueueAttributes(request);
            queueArn = getQueueAttributesResult.getAttributes().get(SQS_QUEUE_ARN_ATTRIBUTE);
        }
        return queueArn;
    }

    /**
     * @return The AWS SQS queue name
     */
    public String getQueueName() {
        return queueName;
    }
}
