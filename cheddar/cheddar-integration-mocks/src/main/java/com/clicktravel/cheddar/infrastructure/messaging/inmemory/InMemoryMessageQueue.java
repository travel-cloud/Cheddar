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
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.infrastructure.inmemory.Resettable;
import com.clicktravel.cheddar.infrastructure.messaging.Message;
import com.clicktravel.cheddar.infrastructure.messaging.MessageQueue;
import com.clicktravel.common.functional.StringUtils;

/**
 * Simple in-memory message queue, intended for use in testing.
 */
public class InMemoryMessageQueue<T extends Message> implements MessageQueue<T>, Resettable {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Queue<T> queue = new ConcurrentLinkedQueue<>();
    private final String name;
    private final InMemoryMessageQueuePoller inMemoryMessageQueuePoller;

    @SuppressWarnings("unchecked")
    public InMemoryMessageQueue(final String name, final InMemoryMessageQueuePoller inMemoryMessageQueuePoller,
            final InMemoryExchange<T>... inMemoryExchanges) {
        this.name = name;
        this.inMemoryMessageQueuePoller = inMemoryMessageQueuePoller;

        final List<String> exchangeNames = new ArrayList<>();
        for (final InMemoryExchange<T> inMemoryExchange : inMemoryExchanges) {
            inMemoryExchange.addSubscriber(this);
            exchangeNames.add(inMemoryExchange.getName());
        }
        logger.info("Using in memory message queue: " + name + " with subscriptions to these exchanges: ["
                + StringUtils.join(exchangeNames) + "]");
    }

    @Override
    public void send(final T message) {
        queue.add(message);
        inMemoryMessageQueuePoller.poll();
    }

    @Override
    public void sendDelayedMessage(final T message, final int delaySeconds) {
        send(message); // delay not supported
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<T> receive(final int waitTimeSeconds, final int maxMessages) {
        return receive();
    }

    @Override
    public List<T> receive() {
        final T message = queue.peek();
        final List<T> messages = new ArrayList<T>(1);
        if (message != null) {
            messages.add(message);
        }
        return messages;
    }

    @Override
    public void delete(final T message) {
        queue.remove(message);
    }

    @Override
    public String toString() {
        return "InMemoryMessageQueue [name=" + name + ", queue=" + queue + "]";
    }

    @Override
    public void reset() {
        queue.clear();
    }
}
