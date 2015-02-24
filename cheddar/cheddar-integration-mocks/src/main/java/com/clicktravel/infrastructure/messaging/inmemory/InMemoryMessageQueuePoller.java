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

import com.clicktravel.cheddar.infrastructure.tx.TransactionalResourceManager;

/**
 * Convenience class to cause all registered {@link InMemoryMessageListener}s to poll for all messages on their message
 * queues. In-memory queues are designed to be used in a single-threaded environment. They do not use extra threads to
 * receive and handle messages. Instead, they rely on manual polling to perform message processing.
 */
public interface InMemoryMessageQueuePoller {

    /**
     * Ensures that all messages are polled for by registered listeners. This method returns immediately if either a
     * poll is already in progress or a global transaction has been started. This is to prevent transactional failures
     * due to recursive calls to this method.
     */
    void poll();

    /**
     * Register an {@link InMemoryMessageListener} to participate in message queue polling. Listeners should be
     * registered soon after construction of this class. This is done after construction to break circular Spring bean
     * wiring dependencies.
     * @param inMemoryMessageListener {@link InMemoryMessageListener} to register
     */
    void register(InMemoryMessageListener<?> inMemoryMessageListener);

    /**
     * Set the transactional resource manager to use. This is set after construction to break circular Spring bean
     * wiring dependencies.
     * @param transactionalResourceManager
     */
    void setTransactionalResourceManager(TransactionalResourceManager transactionalResourceManager);
}
