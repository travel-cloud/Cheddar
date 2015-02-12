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
package com.clicktravel.cheddar.domain.event.runtime.config;

import java.util.ArrayList;
import java.util.Collection;

import com.clicktravel.cheddar.domain.event.DomainEvent;
import com.clicktravel.cheddar.domain.event.DomainEventHandler;
import com.clicktravel.cheddar.event.EventHandler;
import com.clicktravel.cheddar.infrastructure.messaging.MessageSender;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;
import com.clicktravel.cheddar.infrastructure.messaging.router.MessageRouter;

public class DomainEventHandlerConfigurationUtils {

    /**
     * Adds routes for handled event messages to go to a specified queue. This is used to route event messages to the
     * appropriate prioritised queue(s) (low, high or both) for handling.
     * @param messageRouter Message router that routes messages to low and high priority queues
     * @param domainEventHandlers DomainEventHandlers that handles all messages for one of the queues (either high or
     *            low)
     * @param messageSender MessageSender to the appropriate queue (either high or low)
     */
    public static void addRoutes(final MessageRouter messageRouter,
            final Collection<? extends DomainEventHandler> domainEventHandlers,
            final MessageSender<TypedMessage> messageSender) {
        final Collection<EventHandler<DomainEvent>> eventHandlers = castDomainEventHandlers(domainEventHandlers);
        for (final EventHandler<DomainEvent> eventHandler : eventHandlers) {
            try {
                final DomainEvent prototypeDomainEvent = eventHandler.getEventClass().newInstance();
                messageRouter.addRoute(prototypeDomainEvent.type(), messageSender);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Could not add message route for event handler "
                        + eventHandler.getClass().getName(), e);
            }
        }
    }

    /**
     * Cast Collection<? extends DomainEventHandler> to Collection<EventHandler<DomainEvent>>
     * @param domainEventHandlers as Collection<? extends DomainEventHandler>
     * @return domainEventHandlers as Collection<EventHandler<DomainEvent>>
     */
    public static Collection<EventHandler<DomainEvent>> castDomainEventHandlers(
            final Collection<? extends DomainEventHandler> domainEventHandlers) {
        final Collection<EventHandler<DomainEvent>> handlers = new ArrayList<>();
        if (domainEventHandlers != null) {
            for (final DomainEventHandler domainEventHandler : domainEventHandlers) {
                handlers.add(domainEventHandler);
            }
        }
        return handlers;
    }

}
