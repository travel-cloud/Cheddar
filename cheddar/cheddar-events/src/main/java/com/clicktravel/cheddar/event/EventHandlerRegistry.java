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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.infrastructure.messaging.TypedMessageListener;

/**
 * Registers message and event handlers associated with a given message listener
 * 
 * @param <E> Base class for all events associated with this message listener
 */
public class EventHandlerRegistry<E extends Event> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final TypedMessageListener eventMessageListener;
    private final EventMessageHandler<E> eventMessageHandler;
    private final Set<EventHandler<E>> eventHandlers;

    /**
     * Constructs and configures event handler registry
     * @param eventMessageListener {@link MessageListener} on which all event messages arrive
     * @param eventMessageHandler {@link EventMessageHandler} on which all event handlers will be registered
     * @param eventHandlers Collection of {@link EventHandler}&lt;E&gt; that will handle all events
     */
    public EventHandlerRegistry(final TypedMessageListener eventMessageListener,
            final EventMessageHandler<E> eventMessageHandler, final Collection<EventHandler<E>> eventHandlers) {
        this.eventMessageListener = eventMessageListener;
        this.eventMessageHandler = eventMessageHandler;
        this.eventHandlers = new HashSet<>(eventHandlers);
    }

    public void init() {
        for (final EventHandler<E> eventHandler : eventHandlers) {
            try {
                final E domainEventPrototype = eventHandler.getEventClass().newInstance();
                final String eventType = domainEventPrototype.type();
                eventMessageHandler.registerEventHandler(eventType, eventHandler);
                eventMessageListener.registerMessageHandler(eventType, eventMessageHandler);
            } catch (final Exception e) {
                logger.warn("Unable to register event handler: [" + eventHandler.getClass() + "]", e);
            }
        }
    }

}
