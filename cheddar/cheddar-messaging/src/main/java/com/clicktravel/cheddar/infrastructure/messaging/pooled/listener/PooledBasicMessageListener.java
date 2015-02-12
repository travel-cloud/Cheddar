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
package com.clicktravel.cheddar.infrastructure.messaging.pooled.listener;

import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.infrastructure.messaging.BasicMessage;
import com.clicktravel.cheddar.infrastructure.messaging.MessageHandler;
import com.clicktravel.cheddar.infrastructure.messaging.MessageQueue;
import com.clicktravel.common.concurrent.RateLimiter;

public class PooledBasicMessageListener extends PooledMessageListener<BasicMessage> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final MessageHandler<BasicMessage> messageHandler;

    /**
     * Convenience constructor, uses all defaults with no rate limiter
     * @param basicMessageQueue The basic message queue to listen to
     * @param messageHandler The handler used for all messages that are received
     */
    public PooledBasicMessageListener(final MessageQueue<BasicMessage> basicMessageQueue,
            final MessageHandler<BasicMessage> messageHandler) {
        this(basicMessageQueue, messageHandler, DEFAULT_NUM_WORKER_THREADS);
    }

    /**
     * Convenience constructor, uses defaults based on specified number of threads
     * @param basicMessageQueue The basic message queue to listen to
     * @param messageHandler The handler used for all messages that are received
     * @param numWorkerThreads The number of worker threads to use
     */
    public PooledBasicMessageListener(final MessageQueue<BasicMessage> basicMessageQueue,
            final MessageHandler<BasicMessage> messageHandler, final int numWorkerThreads) {
        this(basicMessageQueue, messageHandler, numWorkerThreads, null);
    }

    /**
     * Convenience constructor, uses defaults based on specified number of threads
     * @param basicMessageQueue The basic message queue to listen to
     * @param messageHandler The handler used for all messages that are received
     * @param numWorkerThreads The number of worker threads to use
     * @param rateLimiter An optional {@link RateLimiter} used to limit the message throughput
     */
    public PooledBasicMessageListener(final MessageQueue<BasicMessage> basicMessageQueue,
            final MessageHandler<BasicMessage> messageHandler, final int numWorkerThreads, final RateLimiter rateLimiter) {
        this(basicMessageQueue, messageHandler, rateLimiter, new MessageHandlerExecutor(basicMessageQueue.getName(),
                numWorkerThreads), new Semaphore((numWorkerThreads * IDEAL_RUNNABLES_PER_THREAD)
                + DEFAULT_MAX_RECEIVED_MESSAGES - 1), DEFAULT_MAX_RECEIVED_MESSAGES);
    }

    /**
     * Most general constructor, allows for greatest flexibility
     * @param basicMessageQueue The basic message queue to listen to
     * @param messageHandler The handler used for all messages that are received
     * @param rateLimiter An optional {@link RateLimiter} used to limit the message throughput
     * @param threadPoolExecutor {@link ThreadPoolExecutor} for a fixed-size thread pool for message handler tasks
     * @param semaphore {@link Semaphore} used to regulate number of in-flight messages to keep all worker threads busy
     * @param maxReceivedMessages Maximum number of messages to receive from the queue at a time
     */
    public PooledBasicMessageListener(final MessageQueue<BasicMessage> basicMessageQueue,
            final MessageHandler<BasicMessage> messageHandler, final RateLimiter rateLimiter,
            final ThreadPoolExecutor threadPoolExecutor, final Semaphore semaphore, final int maxReceivedMessages) {
        super(basicMessageQueue, rateLimiter, threadPoolExecutor, semaphore, maxReceivedMessages);
        this.messageHandler = messageHandler;
    }

    @Override
    protected MessageHandler<BasicMessage> getHandlerForMessage(final BasicMessage message) {
        return messageHandler;
    }

    @Override
    protected void listenerStarted() {
        logger.info("Starting to listen for and handle messages on basic message queue [" + queueName() + "]");
    }

}
