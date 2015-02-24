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

/**
 * Utility for setting message forwarding option on all {@link InMemoryMessagePublisher}s and
 * {@link InMemoryMessageSender}s
 */
public interface InMemoryMessageForwardingControl {

    /**
     * Sets the forward messages option on all {@link InMemoryMessageSender}s
     * @param forwardMessages Set {@code true} to cause all messages sent on a {@link InMemoryMessageSender} to be
     *            forwarded on to its associated {@link InMemoryMessageQueue}
     */
    void setForwardAllSentMessagesToQueues(boolean forwardMessages);

    /**
     * Sets the forward messages option on all {@link InMemoryMessagePublisher}s
     * @param forwardMessages Set {@code true} to cause all messages published on a {@link InMemoryMessagePublisher} to
     *            be forwarded on to its associated {@link InMemoryExchange}
     */
    void setForwardAllPublishedMessagesToExchanges(boolean forwardMessages);

    /**
     * Sets the forward messages option on all {@link InMemoryMessageSender}s and {@link InMemoryMessagePublisher}s
     * @param forwardMessages Set {@code true} to cause all sent and published messages to be forwarded to the
     *            appropriate {@link InMemoryMessageQueue} or {@link InMemoryExchange}
     */
    void setForwardAllMessages(boolean forwardMessages);
}
