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

import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageSendException;

/**
 * Methods for sending {@link Message}s to a {@link MessageQueue}
 * @param <T> message type accepted by these methods
 */
public interface MessageSender<T extends Message> {

    /**
     * Send a message to a message queue
     * @param message Message to send
     * @throws MessageSendException
     */
    void sendMessage(T message) throws MessageSendException;

    /**
     * Send a message to a message queue; the message is not visible to receivers for the specified delay duration
     * @param message Message to send
     * @param delaySeconds Duration for which sent message is invisible to receivers
     * @throws MessageSendException
     */
    void sendDelayedMessage(T message, int delaySeconds) throws MessageSendException;
}
