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
import com.clicktravel.cheddar.infrastructure.messaging.Exchange;
import com.clicktravel.cheddar.infrastructure.messaging.Message;
import com.clicktravel.cheddar.infrastructure.messaging.MessagePublisher;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessagePublishException;

/**
 * Simple in-memory mock implementation of {@link MessagePublisher} for use in testing. This retains all published
 * messages for test verification purposes. Optionally forwards published messages to an {@link Exchange}.
 */
public class InMemoryMessagePublisher<T extends Message> implements MessagePublisher<T>, Resettable {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Exchange<T> exchange;
    private final List<T> publishedMessages = new ArrayList<>();
    private boolean forwardMessagesToExchange;

    public InMemoryMessagePublisher(final Exchange<T> exchange) {
        this.exchange = exchange;
    }

    @Override
    public void publish(final T message) throws MessagePublishException {
        publishedMessages.add(message);
        logger.debug("Published message; message:[" + message + "] exchange:[" + exchange.getName() + "]");
        if (forwardMessagesToExchange) {
            exchange.route(message);
        }
    }

    public void setForwardMessagesToExchange(final boolean forwardMessagesToExchange) {
        this.forwardMessagesToExchange = forwardMessagesToExchange;
    }

    public List<T> getPublishedMessages() {
        return publishedMessages;
    }

    @Override
    public String toString() {
        return "InMemoryMessagePublisher [exchange=" + exchange + ", publishedMessages=" + publishedMessages
                + ", forwardMessagesToExchange=" + forwardMessagesToExchange + "]";
    }

    @Override
    public void reset() {
        publishedMessages.clear();
        forwardMessagesToExchange = false;
    }

}
