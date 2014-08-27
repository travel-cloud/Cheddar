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

package com.clicktravel.cheddar.server.application.lifecycle;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.clicktravel.cheddar.infrastructure.messaging.MessageListener;

@Component
public class LifecycleController implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private MessageListener eventMessageListener;

    @Autowired(required = false)
    private MessageListener highPriorityEventMessageListener;

    @Autowired
    private MessageListener remoteCallMessageListener;

    @Autowired
    private MessageListener remoteResponseMessageListener;

    @Autowired
    private MessageListener systemEventMessageListener;

    @Autowired
    private Collection<MessageListener> messageListeners;

    private final Set<MessageListener> deferrableMessageListeners = new HashSet<>();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        deferrableMessageListeners.addAll(messageListeners);
        deferrableMessageListeners.removeAll(Arrays.asList(eventMessageListener, remoteCallMessageListener,
                remoteResponseMessageListener, systemEventMessageListener));
        if (highPriorityEventMessageListener != null) {
            deferrableMessageListeners.remove(highPriorityEventMessageListener);
        }
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        logger.debug("Starting MessageListeners");
        for (final MessageListener messageListener : messageListeners) {
            messageListener.start();
        }
    }
}
