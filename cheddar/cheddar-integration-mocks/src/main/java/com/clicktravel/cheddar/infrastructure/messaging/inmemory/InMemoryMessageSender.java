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
package com.clicktravel.cheddar.infrastructure.messaging.inmemory;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.infrastructure.inmemory.Resettable;
import com.clicktravel.cheddar.infrastructure.messaging.Message;
import com.clicktravel.cheddar.infrastructure.messaging.MessageQueue;
import com.clicktravel.cheddar.infrastructure.messaging.MessageSender;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageSendException;

/**
 * Simple mock {@link MessageSender} that uses in-memory queues.<br />
 * This implementation stores all sent messages, for the purpose of test verification.<br />
 * There is an option to forward all sent messages to a queue. This is used where a test relies on sent messages being
 * received by a {@link InMemoryMessageListener}.
 */
public class InMemoryMessageSender<T extends Message> implements MessageSender<T>, Resettable {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final MessageQueue<T> messageQueue;
    private final List<T> sentMessages = new ArrayList<>();
    private boolean forwardMessagesToQueue;

    public InMemoryMessageSender(final MessageQueue<T> messageQueue) {
        this.messageQueue = messageQueue;
    }

    @Override
    public void send(final T message) throws MessageSendException {
        sentMessages.add(message);
        logger.debug("Message sent; message:[" + message + "] queue:[" + messageQueue.getName() + "]");
        if (forwardMessagesToQueue) {
            messageQueue.send(message);
        }
    }

    @Override
    public void sendDelayedMessage(final T message, final int delaySeconds) throws MessageSendException {
        send(message); // delays are not supported
    }

    public List<T> getSentMessages() {
        return sentMessages;
    }

    public void setForwardMessagesToQueue(final boolean forwardMessagesToQueue) {
        this.forwardMessagesToQueue = forwardMessagesToQueue;
    }

    @Override
    public String toString() {
        return "InMemoryMessageSender [messageQueue=" + messageQueue + ", sentMessages=" + sentMessages
                + ", forwardMessagesToQueue=" + forwardMessagesToQueue + "]";
    }

    @Override
    public void reset() {
        sentMessages.clear();
        forwardMessagesToQueue = false;
    }
}
