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
package com.clicktravel.cheddar.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.infrastructure.messaging.Message;
import com.clicktravel.cheddar.infrastructure.messaging.MessagePublisher;
import com.clicktravel.cheddar.infrastructure.messaging.SimpleMessage;

public abstract class EventPublisher<E extends Event> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final MessagePublisher messagePublisher;

    protected EventPublisher(final MessagePublisher messagePublisher) {
        this.messagePublisher = messagePublisher;
    }

    public void publishEvent(final E event) {
        logger.debug("Publishing event: [" + event.type() + "]");
        final Message message = new SimpleMessage(event.type(), event.serialize());
        messagePublisher.publishMessage(message);
    }

}
