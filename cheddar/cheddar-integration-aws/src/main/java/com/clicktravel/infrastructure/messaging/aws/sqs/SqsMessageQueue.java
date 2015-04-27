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

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.clicktravel.cheddar.infrastructure.messaging.Message;
import com.clicktravel.cheddar.infrastructure.messaging.MessageQueue;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageDeleteException;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageReceiveException;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageSendException;

/**
 * Abstract AWS SQS implementation for a {@link MessageQueue}. This class is implemented as an adapter for a
 * {@link SqsQueueResource}.
 * @param <T> message type accepted by this queue
 * @see SqsTypedMessageQueue
 * @see SqsBasicMessageQueue
 */
public abstract class SqsMessageQueue<T extends Message> implements MessageQueue<T> {

    private final SqsQueueResource sqsQueueResource;

    public SqsMessageQueue(final SqsQueueResource sqsQueueResource) {
        this.sqsQueueResource = sqsQueueResource;
    }

    protected abstract String toSqsMessageBody(final T message);

    protected abstract T toMessage(final com.amazonaws.services.sqs.model.Message sqsMessage);

    @Override
    public String getName() {
        return sqsQueueResource.getQueueName();
    }

    @Override
    public void send(final T message) throws MessageSendException {
        try {
            sqsQueueResource.sendMessage(toSqsMessageBody(message));
        } catch (final AmazonClientException e) {
            throw new MessageSendException("Unable to send message on SQS queue:[" + sqsQueueResource.getQueueName()
                    + "]", e);
        }
    }

    @Override
    public void sendDelayedMessage(final T message, final int delaySeconds) throws MessageSendException {
        try {
            sqsQueueResource.sendDelayedMessage(toSqsMessageBody(message), delaySeconds);
        } catch (final AmazonClientException e) {
            throw new MessageSendException("Unable to send message on SQS queue:[" + sqsQueueResource.getQueueName()
                    + "]", e);
        }
    }

    @Override
    public List<T> receive() throws MessageReceiveException {
        try {
            return toMessages(sqsQueueResource.receiveMessages());
        } catch (final AmazonClientException e) {
            throw new MessageReceiveException("Unable to receive messages on SQS queue:["
                    + sqsQueueResource.getQueueName() + "]", e);
        }
    }

    @Override
    public List<T> receive(final int waitTimeSeconds, final int maxMessages) throws MessageReceiveException {
        try {
            return toMessages(sqsQueueResource.receiveMessages(waitTimeSeconds, maxMessages));
        } catch (final AmazonClientException e) {
            throw new MessageReceiveException("Unable to receive messages on SQS queue:["
                    + sqsQueueResource.getQueueName() + "]", e);
        }
    }

    private List<T> toMessages(final List<com.amazonaws.services.sqs.model.Message> sqsMessages) {
        final ArrayList<T> messages = new ArrayList<>();
        for (final com.amazonaws.services.sqs.model.Message sqsMessage : sqsMessages) {
            messages.add(toMessage(sqsMessage));
        }
        return messages;
    }

    @Override
    public void delete(final T message) throws MessageDeleteException {
        try {
            sqsQueueResource.deleteMessage(message.getReceiptHandle());
        } catch (final AmazonClientException e) {
            throw new MessageDeleteException("Unable to delete message on SQS queue:["
                    + sqsQueueResource.getQueueName() + "]", e);
        }
    }

    public SqsQueueResource getSqsQueue() {
        return sqsQueueResource;
    }
}
