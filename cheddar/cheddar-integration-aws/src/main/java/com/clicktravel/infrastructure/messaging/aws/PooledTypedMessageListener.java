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
package com.clicktravel.infrastructure.messaging.aws;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.infrastructure.messaging.MessageHandler;
import com.clicktravel.cheddar.infrastructure.messaging.MessageQueue;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessageListener;
import com.clicktravel.common.concurrent.RateLimiter;

public class PooledTypedMessageListener extends PooledMessageListener<TypedMessage> implements TypedMessageListener {

    /**
     * Maximum number of messages to receive from the queue at a time. Using larger numbers decreases the number of
     * calls to receive and thus increases throughput, at the possible expense of latency.
     */
    private static final int DEFAULT_MAX_RECEIVED_MESSAGES = 10;

    /**
     * Controls when messages are received from the queue by setting an ideal minimum number of runnable tasks for each
     * thread. This minimum includes the currently executing tasks and those on the thread pool work queue. When the
     * number of runnable tasks dips below the ideal, more messages are received.
     */
    private static final int IDEAL_RUNNABLES_PER_THREAD = 2; // Each thread has 1 executing + 1 queued runnable

    /**
     * The default number of worker threads to use in a fixed size thread pool
     */
    private static final int DEFAULT_NUM_WORKER_THREADS = 10;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, MessageHandler<TypedMessage>> messageHandlers = new HashMap<>();

    /**
     * Convenience constructor, uses all defaults with no rate limiter
     * @param typedMessageQueue The typed message queue to listen to
     */
    public PooledTypedMessageListener(final MessageQueue<TypedMessage> typedMessageQueue) {
        this(typedMessageQueue, DEFAULT_NUM_WORKER_THREADS);
    }

    /**
     * Convenience constructor, uses defaults based on specified number of threads
     * @param typedMessageQueue The typed message queue to listen to
     * @param numWorkerThreads The number of worker threads to use
     */
    public PooledTypedMessageListener(final MessageQueue<TypedMessage> typedMessageQueue, final int numWorkerThreads) {
        this(typedMessageQueue, numWorkerThreads, null);
    }

    /**
     * Convenience constructor, uses defaults based on specified number of threads
     * @param typedMessageQueue The typed message queue to listen to
     * @param numWorkerThreads The number of worker threads to use
     * @param rateLimiter An optional {@link RateLimiter} used to limit the message throughput
     */
    public PooledTypedMessageListener(final MessageQueue<TypedMessage> typedMessageQueue, final int numWorkerThreads,
            final RateLimiter rateLimiter) {
        this(typedMessageQueue, rateLimiter, new MessageHandlerExecutor(typedMessageQueue.getName(), numWorkerThreads),
                new Semaphore((numWorkerThreads * IDEAL_RUNNABLES_PER_THREAD) + DEFAULT_MAX_RECEIVED_MESSAGES - 1),
                DEFAULT_MAX_RECEIVED_MESSAGES);
    }

    /**
     * Most general constructor, allows for greatest flexibility
     * @param typedMessageQueue The typed message queue to listen to
     * @param rateLimiter An optional {@link RateLimiter} used to limit the message throughput
     * @param threadPoolExecutor {@link ThreadPoolExecutor} for a fixed-size thread pool for message handler tasks
     * @param semaphore {@link Semaphore} used to regulate number of in-flight messages to keep all worker threads busy
     * @param maxReceivedMessages Maximum number of messages to receive from the queue at a time
     */
    public PooledTypedMessageListener(final MessageQueue<TypedMessage> typedMessageQueue,
            final RateLimiter rateLimiter, final ThreadPoolExecutor threadPoolExecutor, final Semaphore semaphore,
            final int maxReceivedMessages) {
        super(typedMessageQueue, rateLimiter, threadPoolExecutor, semaphore, maxReceivedMessages);
        // logger.debug("Message listener for queue [" + queueName() + "] configured to handle these message types :["
        // + StringUtils.join(new ArrayList<>(messageHandlers.keySet())) + "]");
    }

    @Override
    protected MessageHandler<TypedMessage> getHandlerForMessage(final TypedMessage typedMessage) {
        final MessageHandler<TypedMessage> messageHandler = messageHandlers.get(typedMessage.getType());
        if (messageHandler == null) {
            logger.trace("Unsupported message type: " + typedMessage.getType());
        }
        return messageHandler;
    }

    @Override
    public void registerMessageHandler(final String messageType, final MessageHandler<TypedMessage> messageHandler) {
        messageHandlers.put(messageType, messageHandler);
    }

}
