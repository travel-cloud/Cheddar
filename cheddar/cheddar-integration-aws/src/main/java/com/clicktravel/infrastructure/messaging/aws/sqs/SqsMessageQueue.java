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

import com.clicktravel.cheddar.infrastructure.messaging.Message;
import com.clicktravel.cheddar.infrastructure.messaging.MessageQueue;

/**
 * Abstract AWS SQS implementation for a {@link MessageQueue}
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
    public void sendMessage(final T message) {
        sqsQueueResource.sendMessage(toSqsMessageBody(message));
    }

    @Override
    public void sendDelayedMessage(final T message, final int delaySeconds) {
        sqsQueueResource.sendDelayedMessage(toSqsMessageBody(message), delaySeconds);
    }

    @Override
    public List<T> receive() throws InterruptedException {
        return toMessages(sqsQueueResource.receiveMessages());
    }

    @Override
    public List<T> receive(final int waitTimeSeconds, final int maxMessages) throws InterruptedException {
        return toMessages(sqsQueueResource.receiveMessages(waitTimeSeconds, maxMessages));
    }

    private List<T> toMessages(final List<com.amazonaws.services.sqs.model.Message> sqsMessages) {
        final ArrayList<T> messages = new ArrayList<>();
        for (final com.amazonaws.services.sqs.model.Message sqsMessage : sqsMessages) {
            messages.add(toMessage(sqsMessage));
        }
        return messages;
    }

    @Override
    public void delete(final T message) {
        sqsQueueResource.deleteMessage(message.getReceiptHandle());
    }

    public SqsQueueResource getSqsQueue() {
        return sqsQueueResource;
    }
}
