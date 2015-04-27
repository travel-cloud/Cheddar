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
package com.clicktravel.infrastructure.messaging.aws.sqs;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;

/**
 * Represents an actual AWS SQS queue that exists in the AWS environment. Provides some convenience methods for working
 * with the actual AWS SQS queue.
 */
public class SqsQueueResource {

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
    public void sendMessage(final String messageBody) throws AmazonClientException {
        final SendMessageRequest sendMessageRequest = new SendMessageRequest(queueUrl, messageBody);
        doSendRequest(sendMessageRequest);
    }

    /**
     * Sends a delayed message to the AWS SQS queue
     * @param messageBody Body of the message to send
     * @param delaySeconds Number of seconds to delay visibility of the sent message
     */
    public void sendDelayedMessage(final String messageBody, final int delaySeconds) throws AmazonClientException {
        final SendMessageRequest sendMessageRequest = new SendMessageRequest(queueUrl, messageBody)
                .withDelaySeconds(delaySeconds);
        doSendRequest(sendMessageRequest);
    }

    private void doSendRequest(final SendMessageRequest sendMessageRequest) throws AmazonClientException {
        amazonSqsClient.sendMessage(sendMessageRequest);
        logger.trace("Successfully sent message: Payload=" + sendMessageRequest.getMessageBody() + "] to SQS queue: ["
                + queueName + "]");
    }

    /**
     * Receive up to 10 messages from the AWS SQS queue, using short polling. If an error occurs performing the SQS
     * ReceiveMessageRequest, the request is retried until it succeeds.
     * @return {@code List<com.amazonaws.services.sqs.model.Message>} received messages
     * @throws InterruptedException
     */
    public List<Message> receiveMessages() throws AmazonClientException {
        return doReceiveRequest(new ReceiveMessageRequest(queueUrl));
    }

    /**
     * Receive up to the given number of messages from the AWS SQS queue, waiting the specified time for messages to
     * appear.
     * @param waitTimeSeconds Number of seconds to wait for messages to appear
     * @param maxMessages Maximum number of messages returned, may be up to 10.
     * @return {@code List<com.amazonaws.services.sqs.model.Message>} received messages
     * @throws InterruptedException
     */
    public List<Message> receiveMessages(final int waitTimeSeconds, final int maxMessages) throws AmazonClientException {
        final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl).withWaitTimeSeconds(
                waitTimeSeconds).withMaxNumberOfMessages(maxMessages);
        return doReceiveRequest(receiveMessageRequest);
    }

    private List<Message> doReceiveRequest(final ReceiveMessageRequest receiveMessageRequest)
            throws AmazonClientException {
        return amazonSqsClient.receiveMessage(receiveMessageRequest).getMessages();
    }

    /**
     * Delete a previously received message from the AWS SQS queue. Up to {@link #MAX_SQS_DELETE_MESSAGE_ATTEMPTS}
     * attempts are made to perform the SQS {@link DeleteMessageRequest}.
     * @param receiptHandle Identifier of message to delete, given with receipt of the message
     */
    public void deleteMessage(final String receiptHandle) throws AmazonClientException {
        final DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest(queueUrl, receiptHandle);
        amazonSqsClient.deleteMessage(deleteMessageRequest);
    }

    /**
     * Sets the {@link Policy} of the AWS SQS queue
     * @param policy {@link Policy} to set
     */
    public void setPolicy(final Policy policy) throws AmazonClientException {
        final Map<String, String> queueAttributes = Collections.singletonMap(AWS_POLICY_ATTRIBUTE, policy.toJson());
        amazonSqsClient.setQueueAttributes(new SetQueueAttributesRequest(queueUrl, queueAttributes));
    }

    /**
     * @return The ARN of the AWS SQS queue
     */
    public String queueArn() throws AmazonClientException {
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
