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
package com.clicktravel.infrastructure.messaging.inmemory;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.infrastructure.messaging.Exchange;
import com.clicktravel.cheddar.infrastructure.messaging.Message;
import com.clicktravel.cheddar.infrastructure.messaging.MessageQueue;

/**
 * Simple in-memory mock {@link Exchange} implementation. Intended for use with in-memory queues.
 */
public class InMemoryExchange<T extends Message> implements Exchange<T> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String name;
    private final Set<MessageQueue<T>> subscribers = new HashSet<>();

    public InMemoryExchange(final String name) {
        this.name = name;
        logger.info("Using in memory exchange: " + name);
    }

    public void addSubscriber(final MessageQueue<T> messageQueue) {
        subscribers.add(messageQueue);
    }

    @Override
    public void route(final T message) {
        for (final MessageQueue<T> subscriber : subscribers) {
            subscriber.send(message);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "InMemoryExchange [name=" + name + ", subscribers=" + subscribers + "]";
    }
}
