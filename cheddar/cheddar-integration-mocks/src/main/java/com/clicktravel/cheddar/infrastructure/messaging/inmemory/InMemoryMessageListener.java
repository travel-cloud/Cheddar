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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.infrastructure.messaging.Message;
import com.clicktravel.cheddar.infrastructure.messaging.MessageHandler;
import com.clicktravel.cheddar.infrastructure.messaging.MessageListener;
import com.clicktravel.cheddar.infrastructure.messaging.MessageQueue;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageListenerException;

/**
 * Simple mock {@link MessageListener} that uses in-memory queues.<br />
 * This implementation does not use extra threads for message listening. Instead, instances of this class must be
 * registered with a {@link InMemoryMessageQueuePoller}. Invoke {@link InMemoryMessageQueuePoller#poll()} to cause
 * registered listeners to poll their queues for messages.
 */
public abstract class InMemoryMessageListener<T extends Message> implements MessageListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final MessageQueue<T> messageQueue;

    public InMemoryMessageListener(final MessageQueue<T> inMemoryMessageQueue,
            final InMemoryMessageQueuePoller inMemoryMessageQueuePoller) {
        messageQueue = inMemoryMessageQueue;
        inMemoryMessageQueuePoller.register(this);
    }

    protected abstract MessageHandler<T> getHandlerForMessage(T message);

    /**
     * Receives a single message (if any available) and delegates to the relevant registered message handler
     * @return {@code true} if message was received from the queue
     */
    public boolean receiveAndHandleMessages() {
        boolean didReceiveMessage = false;
        List<T> messages;
        try {
            messages = messageQueue.receive();
        } catch (final InterruptedException e) {
            throw new IllegalStateException(e);
        }
        for (final T message : messages) {
            didReceiveMessage = true;
            final MessageHandler<T> messageHandler = getHandlerForMessage(message);
            if (messageHandler != null) {
                try {
                    logger.debug("Handling received message; queue:[" + messageQueue.getName() + "] message:["
                            + message + "]");
                    messageHandler.handle(message);
                } catch (final Exception exception) {
                    logger.error("Error handling message: " + message, exception);
                }
            } else {
                logger.debug("No handler for received message [" + message + "]");
            }
            messageQueue.delete(message);
        }
        return didReceiveMessage;
    }

    @Override
    public void start() throws MessageListenerException {
        // Nothing to do
    }

    @Override
    public void shutdown() {
        // Nothing to do
    }

    @Override
    public void prepareForShutdown() {
        // Nothing to do
    }

    @Override
    public void shutdownAfterQueueDrained() {
        // Nothing to do
    }

    @Override
    public boolean hasTerminated() {
        return false;
    }

    @Override
    public void awaitTermination() {
        // Nothing to do
    }

    @Override
    public String toString() {
        return "InMemoryMessageListener [messageQueue=" + messageQueue + "]";
    }

}
