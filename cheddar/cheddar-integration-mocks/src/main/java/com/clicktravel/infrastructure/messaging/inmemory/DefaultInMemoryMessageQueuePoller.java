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
package com.clicktravel.infrastructure.messaging.inmemory;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.clicktravel.cheddar.infrastructure.tx.TransactionalResourceManager;

@Component
public class DefaultInMemoryMessageQueuePoller implements InMemoryMessageQueuePoller {

    private final Set<InMemoryMessageListener<?>> inMemoryMessageListeners = new HashSet<>();
    private TransactionalResourceManager transactionalResourceManager;
    private boolean polling;

    @Override
    public void setTransactionalResourceManager(final TransactionalResourceManager transactionalResourceManager) {
        this.transactionalResourceManager = transactionalResourceManager;
    }

    @Override
    public void register(final InMemoryMessageListener<?> inMemoryMessageListener) {
        inMemoryMessageListeners.add(inMemoryMessageListener);
    }

    @Override
    public void poll() {
        if (!polling && !transactionalResourceManager.inTransaction()) {
            polling = true;
            pollForAllMessages();
            polling = false;
        }
    }

    private void pollForAllMessages() {
        boolean anyListenerReceivedMessage;
        do {
            anyListenerReceivedMessage = false;
            for (final InMemoryMessageListener<?> inMemoryMessageListener : inMemoryMessageListeners) {
                final boolean listenerReceivedMessage = inMemoryMessageListener.receiveAndHandleMessages();
                anyListenerReceivedMessage |= listenerReceivedMessage;
            }
        } while (anyListenerReceivedMessage);
    }

    @Override
    public String toString() {
        return "DefaultInMemoryMessageQueuePoller [polling=" + polling + ", inMemoryMessageListeners="
                + inMemoryMessageListeners + "]";
    }

}
