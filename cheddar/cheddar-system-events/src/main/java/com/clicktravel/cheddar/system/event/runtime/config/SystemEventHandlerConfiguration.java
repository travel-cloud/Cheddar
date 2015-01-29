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
package com.clicktravel.cheddar.system.event.runtime.config;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.clicktravel.cheddar.event.EventHandler;
import com.clicktravel.cheddar.event.EventHandlerRegistry;
import com.clicktravel.cheddar.event.EventMessageHandler;
import com.clicktravel.cheddar.infrastructure.messaging.MessageListener;
import com.clicktravel.cheddar.system.event.SystemEvent;
import com.clicktravel.cheddar.system.event.handler.SystemEventHandler;

@Configuration
public class SystemEventHandlerConfiguration {

    @Autowired(required = false)
    private Collection<SystemEventHandler> systemEventHandlers;

    @Bean
    @Autowired
    public EventHandlerRegistry<SystemEvent> systemEventHandlerRegistry(final MessageListener systemEventMessageListener) {
        final EventHandlerRegistry<SystemEvent> eventHandlerRegistry = new EventHandlerRegistry<SystemEvent>(
                systemEventMessageListener, new EventMessageHandler<SystemEvent>(),
                castSystemEventHandlers(systemEventHandlers));
        eventHandlerRegistry.init();
        return eventHandlerRegistry;
    }

    /**
     * Cast Collection<? extends SystemEventHandler> to Collection<EventHandler<SystemEvent>>
     * @param systemEventHandlers as Collection<? extends SystemEventHandler>
     * @return systemEventHandlers as Collection<EventHandler<SystemEvent>>
     */
    public static Collection<EventHandler<SystemEvent>> castSystemEventHandlers(
            final Collection<? extends SystemEventHandler> systemEventHandlers) {
        final Collection<EventHandler<SystemEvent>> handlers = new ArrayList<>();
        if (systemEventHandlers != null) {
            for (final SystemEventHandler systemEventHandler : systemEventHandlers) {
                handlers.add(systemEventHandler);
            }
        }
        return handlers;
    }

}
