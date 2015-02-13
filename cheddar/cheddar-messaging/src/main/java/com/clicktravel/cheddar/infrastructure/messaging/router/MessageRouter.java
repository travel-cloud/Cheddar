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
package com.clicktravel.cheddar.infrastructure.messaging.router;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.clicktravel.cheddar.infrastructure.messaging.MessageHandler;
import com.clicktravel.cheddar.infrastructure.messaging.MessageSender;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessageListener;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageHandlingException;
import com.clicktravel.common.functional.StringUtils;

public class MessageRouter implements MessageHandler<TypedMessage>, ApplicationListener<ContextRefreshedEvent> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, Set<MessageSender<TypedMessage>>> routes = new HashMap<>();;
    private final TypedMessageListener typedMessageListener;

    public MessageRouter(final TypedMessageListener typedMessageListener) {
        this.typedMessageListener = typedMessageListener;
    }

    public void addRoute(final String messageType, final MessageSender<TypedMessage> messageSender) {
        Set<MessageSender<TypedMessage>> routesForMessageType = routes.get(messageType);
        if (routesForMessageType == null) {
            routesForMessageType = new HashSet<>();
            routes.put(messageType, routesForMessageType);
            typedMessageListener.registerMessageHandler(messageType, this);
        }
        routesForMessageType.add(messageSender);
    }

    @Override
    public void handle(final TypedMessage typedMessage) throws MessageHandlingException {
        final Set<MessageSender<TypedMessage>> routesForMessageType = routes.get(typedMessage.getType());
        if (routesForMessageType != null) {
            for (final MessageSender<TypedMessage> messageSender : routesForMessageType) {
                messageSender.send(typedMessage);
            }
        } else {
            logger.warn("Unable to route message of type : " + typedMessage.getType());
        }
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        final String messageTypes = StringUtils.join(new ArrayList<>(routes.keySet()));
        logger.debug("Started MessageRouter for these message types: [" + messageTypes + "]");
    }
}
