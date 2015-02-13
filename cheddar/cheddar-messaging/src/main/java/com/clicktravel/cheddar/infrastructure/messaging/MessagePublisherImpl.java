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

import com.clicktravel.cheddar.infrastructure.messaging.exception.MessagePublishException;

/**
 * A simple message publisher implementation which publishes all messages using an exchange
 * @param <T> Type of messages published
 */
public class MessagePublisherImpl<T extends Message> implements MessagePublisher<T> {

    private final Exchange<T> exchange;

    public MessagePublisherImpl(final Exchange<T> exchange) {
        this.exchange = exchange;
    }

    @Override
    public void publish(final T message) throws MessagePublishException {
        exchange.route(message);
    }
}
