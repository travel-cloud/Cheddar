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

import java.util.Collection;
import java.util.Collections;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultInMemoryMessageForwardingControl implements InMemoryMessageForwardingControl {

    @Autowired(required = false)
    private Collection<InMemoryMessageSender<?>> inMemoryMessageSenders;
    @Autowired(required = false)
    private Collection<InMemoryMessagePublisher<?>> inMemoryMessagePublishers;

    @PostConstruct
    public void init() {
        if (inMemoryMessageSenders == null) {
            inMemoryMessageSenders = Collections.emptySet();
        }
        if (inMemoryMessagePublishers == null) {
            inMemoryMessagePublishers = Collections.emptySet();
        }
    }

    @Override
    public void setForwardAllSentMessagesToQueues(final boolean forwardMessages) {
        for (final InMemoryMessageSender<?> inMemoryMessageSender : inMemoryMessageSenders) {
            inMemoryMessageSender.setForwardMessagesToQueue(forwardMessages);
        }
    }

    @Override
    public void setForwardAllPublishedMessagesToExchanges(final boolean forwardMessages) {
        for (final InMemoryMessagePublisher<?> inMemoryMessagePublisher : inMemoryMessagePublishers) {
            inMemoryMessagePublisher.setForwardMessagesToExchange(forwardMessages);
        }
    }

    @Override
    public void setForwardAllMessages(final boolean forwardMessages) {
        setForwardAllSentMessagesToQueues(forwardMessages);
        setForwardAllPublishedMessagesToExchanges(forwardMessages);
    }

}
