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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageHandlingException;

public class MessageRouter implements MessageHandler, ApplicationListener<ContextRefreshedEvent> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, Set<MessageSender>> routes = new HashMap<>();;
    private final MessageListener messageListener;

    public MessageRouter(final MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public void addRoute(final String messageType, final MessageSender messageSender) {
        Set<MessageSender> routesForMessageType = routes.get(messageType);
        if (routesForMessageType == null) {
            routesForMessageType = new HashSet<>();
            routes.put(messageType, routesForMessageType);
            messageListener.registerMessageHandler(messageType, this);
        }
        routesForMessageType.add(messageSender);
    }

    @Override
    public void handle(final Message message) throws MessageHandlingException {
        final Set<MessageSender> routesForMessageType = routes.get(message.getType());
        if (routesForMessageType != null) {
            for (final MessageSender messageSender : routesForMessageType) {
                messageSender.sendMessage(message);
            }
        } else {
            logger.warn("Unable to route message of type : " + message.getType());
        }
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        final StringBuilder sb = new StringBuilder();
        for (final String route : routes.keySet()) {
            if (sb.length() != 0) {
                sb.append(", ");
            }
            sb.append(route);
        }
        logger.debug("Started MessageRouter for these message types: [" + sb.toString() + "]");
    }
}
