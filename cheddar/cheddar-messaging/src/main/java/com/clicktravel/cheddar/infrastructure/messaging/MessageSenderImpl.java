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
package com.clicktravel.cheddar.infrastructure.messaging;

import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageSendException;

/**
 * A simple message sender implementation which sends all messages to a queue
 * @param <T> Type of messages sent
 */
public class MessageSenderImpl<T extends Message> implements MessageSender<T> {

    private final MessageQueue<T> messageQueue;

    public MessageSenderImpl(final MessageQueue<T> messageQueue) {
        this.messageQueue = messageQueue;
    }

    @Override
    public void send(final T message) throws MessageSendException {
        messageQueue.send(message);
    }

    @Override
    public void sendDelayedMessage(final T message, final int delaySeconds) throws MessageSendException {
        messageQueue.sendDelayedMessage(message, delaySeconds);
    }

}
