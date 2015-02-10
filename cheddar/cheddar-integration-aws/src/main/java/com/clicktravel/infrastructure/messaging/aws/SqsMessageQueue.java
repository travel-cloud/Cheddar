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
public class SqsMessageQueue<T extends Message> implements MessageQueue<T> {

    private final SqsMessageAdapter<T> sqsMessageAdapter;
    private final SqsQueue sqsQueue;

    public SqsMessageQueue(final SqsQueue sqsQueue, final SqsMessageAdapter<T> sqsMessageAdapter) {
        this.sqsMessageAdapter = sqsMessageAdapter;
        this.sqsQueue = sqsQueue;
    }

    @Override
    public String getName() {
        return sqsQueue.getQueueName();
    }

    @Override
    public void sendMessage(final T message) {
        final String messageBody = sqsMessageAdapter.toSqsMessageBody(message);
        sqsQueue.sendMessage(messageBody);
    }

    @Override
    public void sendDelayedMessage(final T message, final int delaySeconds) {
        final String messageBody = sqsMessageAdapter.toSqsMessageBody(message);
        sqsQueue.sendDelayedMessage(messageBody, delaySeconds);
    }

    @Override
    public List<T> receive() throws InterruptedException {
        return toMessages(sqsQueue.receiveMessages());
    }

    @Override
    public List<T> receive(final int waitTimeSeconds, final int maxMessages) throws InterruptedException {
        return toMessages(sqsQueue.receiveMessages(waitTimeSeconds, maxMessages));
    }

    private List<T> toMessages(final List<com.amazonaws.services.sqs.model.Message> sqsMessages) {
        final ArrayList<T> messages = new ArrayList<>();
        for (final com.amazonaws.services.sqs.model.Message sqsMessage : sqsMessages) {
            messages.add(sqsMessageAdapter.toMessage(sqsMessage));
        }
        return messages;
    }

    @Override
    public void delete(final T message) {
        sqsQueue.deleteMessage(message.getReceiptHandle());
    }

    public SqsQueue getSqsQueue() {
        return sqsQueue;
    }
}
