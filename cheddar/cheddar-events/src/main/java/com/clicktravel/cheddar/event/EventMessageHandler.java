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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.infrastructure.messaging.MessageHandler;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageHandlingException;

public class EventMessageHandler<E extends Event> implements MessageHandler<TypedMessage> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, Set<EventHandler<E>>> eventHandlers;

    public EventMessageHandler() {
        eventHandlers = new ConcurrentHashMap<>();
    }

    @Override
    public void handle(final TypedMessage typedMessage) throws MessageHandlingException {
        try {
            final String messageType = typedMessage.getType();
            logger.debug("Handling: " + messageType);
            for (final EventHandler<E> eventHandler : eventHandlers.get(messageType)) {
                final E event = AbstractEvent.newEvent(eventHandler.getEventClass(), typedMessage.getPayload());
                if (!eventHandler.getEventClass().isAssignableFrom(event.getClass())) {
                    throw new IllegalStateException("Event of type " + event.getClass() + " is not compatible with "
                            + eventHandler.getEventClass() + " in event handler");
                }
                eventHandler.handle(event);
            }
        } catch (final Exception e) {
            throw new MessageHandlingException(e.getMessage(), e);
        }
    }

    public void registerEventHandler(final String eventType, final EventHandler<E> eventHandler) {
        Set<EventHandler<E>> eventHandlersForType = eventHandlers.get(eventType);
        if (eventHandlersForType == null) {
            eventHandlersForType = new HashSet<>();
            eventHandlers.put(eventType, eventHandlersForType);
        }
        eventHandlersForType.add(eventHandler);
    }

}
