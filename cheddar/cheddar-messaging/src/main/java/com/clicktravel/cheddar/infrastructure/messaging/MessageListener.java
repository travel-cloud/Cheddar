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

/**
 * Controls the lifecycle of listening for messages on a queue and handling them.
 */
public interface MessageListener {

    /**
     * Starts the process of receiving messages from the queue and handling each message. This continues until this
     * message listener is shut down.
     */
    void start();

    /**
     * Gives a hint to this message listener that method {@link #shutdownListener()} will be invoked soon. This hint can
     * be used to reduce the time taken to complete shutdown when it occurs.
     */
    void prepareForShutdown();

    /**
     * Commences graceful shutdown of this message listener. No more messages will be received from the queue and
     * handling of already received messages will be performed and run to completion. This method does not wait for the
     * completion to occur, use method {@link #awaitShutdownComplete(long)} for that.
     */
    void shutdownListener();

    /**
     * Waits until all outstanding message handling has been completed before returning, or until a timeout occurs.
     * @param timeoutMillis Maximum time to wait for message processing to be completed
     * @return {@code true} if message processing has completed, {@code false} if timeout occurred
     */
    boolean awaitShutdownComplete(long timeoutMillis);

}
