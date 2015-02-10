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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.infrastructure.messaging.Message;
import com.clicktravel.cheddar.infrastructure.messaging.MessageHandler;
import com.clicktravel.cheddar.infrastructure.messaging.MessageListener;
import com.clicktravel.cheddar.infrastructure.messaging.MessageQueue;
import com.clicktravel.common.concurrent.RateLimiter;

public abstract class PooledMessageListener<T extends Message> implements MessageListener, Runnable {

    /**
     * Maximum duration (in seconds) to wait for messages on the queue during normal processing. If there is at least
     * one message on the queue, the actual duration will be shorter.
     */
    private static final int LONG_POLL_DURATION_SECONDS = 20;

    /**
     * Maximum duration (in seconds) to wait for messages on the queue during handing over to a new application instance
     * in a blue-green deployment. This is shorter to enable prompt termination of this message processor.
     */
    private static final int SHORT_POLL_DURATION_SECONDS = 2;

    /**
     * Minimum number of empty responses for consecutive ReceiveMessageRequests to conclude queue is empty
     */
    private static final int QUEUE_DRAINED_THRESHOLD = 2;

    /**
     * Maximum time (in seconds) to wait for executor to complete termination
     */
    private static final int TERMINATION_TIMEOUT_SECONDS = 300;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final MessageQueue<T> messageQueue;
    private final ThreadPoolExecutor threadPoolExecutor;
    private final RateLimiter rateLimiter;
    private final Semaphore semaphore;
    private final int maxReceivedMessages;
    private int noReceivedMessagesCount;
    private volatile boolean shutdownRequested;
    private volatile boolean shutdownWhenQueueDrainedRequested;
    private volatile boolean shutdownRequestImminent;

    public PooledMessageListener(final MessageQueue<T> messageQueue, final RateLimiter rateLimiter,
            final ThreadPoolExecutor threadPoolExecutor, final Semaphore semaphore, final int maxReceivedMessages) {
        this.messageQueue = messageQueue;
        this.rateLimiter = rateLimiter;
        this.threadPoolExecutor = threadPoolExecutor;
        this.semaphore = semaphore;
        this.maxReceivedMessages = maxReceivedMessages;
    }

    protected abstract MessageHandler<T> getHandlerForMessage(T message);

    @Override
    public void start() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        logger.debug("Message listener for queue [" + queueName() + "] is starting to receive messages");
        try {
            processMessagesUntilShutdownRequested();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (final Throwable e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            logger.debug("Message listener for queue [" + queueName()
                    + "] has stopped receiving messages. Initiating shutdown of task executor");
            threadPoolExecutor.shutdown();
        }
    }

    private void processMessagesUntilShutdownRequested() throws InterruptedException {
        while (!(shutdownRequested || shutdownWhenQueueDrainedRequested && queueDrained())) {
            // Block until there is capacity to handle up to MAX_RECEIVED_MESSAGES
            semaphore.acquire(maxReceivedMessages);
            List<T> messages = Collections.emptyList();
            try {
                if (!shutdownRequested) {
                    final int pollSeconds = shutdownRequestImminent ? SHORT_POLL_DURATION_SECONDS
                            : LONG_POLL_DURATION_SECONDS;
                    messages = messageQueue.receive(pollSeconds, maxReceivedMessages);
                    if (shutdownWhenQueueDrainedRequested) {
                        noReceivedMessagesCount = messages.isEmpty() ? (noReceivedMessagesCount + 1) : 0;
                    }
                }
            } finally {
                // Release over-allocated permits
                semaphore.release(maxReceivedMessages - messages.size());
            }
            for (final T message : messages) {
                processMessage(message); // Must complete processing each message to release permit
            }
        }
    }

    /**
     * Processes a message by getting the appropriate message handler and scheduling a task to execute the handler. In
     * case of problems (e.g. the message cannot be parsed), the message processing is completed to ensure the message
     * is deleted from the queue and the associated permit is released.
     * @param message {@link Message} to process
     */
    private void processMessage(final T message) {
        boolean workerAssigned = false;
        try {
            final MessageHandler<T> messageHandler = getHandlerForMessage(message);
            if (messageHandler != null) {
                applyRateLimiter();
                threadPoolExecutor.execute(new MessageHandlerWorker<T>(this, message, messageHandler));
                workerAssigned = true;
            }
        } catch (final Exception e) {
            logger.error("Unable to process received message", e);
        }

        if (!workerAssigned) {
            completeMessageProcessing(message);
        }
    }

    /**
     * Completes message processing by deleting it from the queue and releasing the associated permit.
     * @param message {@link Message} to complete processing
     */
    public void completeMessageProcessing(final T message) {
        messageQueue.delete(message);
        semaphore.release();
    }

    private void applyRateLimiter() {
        if (rateLimiter != null) {
            try {
                rateLimiter.takeToken();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean queueDrained() {
        return noReceivedMessagesCount >= QUEUE_DRAINED_THRESHOLD;
    }

    @Override
    public String queueName() {
        return messageQueue.getName();
    }

    @Override
    public void prepareForShutdown() {
        logger.debug("Message listener for queue [" + queueName()
                + "] is preparing for imminent shutdown. Reducing queue poll time.");
        shutdownRequestImminent = true;
    }

    @Override
    public void shutdown() {
        logger.debug("Message listener for queue [" + queueName() + "] is shutting down");
        shutdownRequested = true;
    }

    @Override
    public void shutdownAfterQueueDrained() {
        logger.debug("Message listener for queue [" + queueName() + "] will shutdown when queue is drained");
        shutdownWhenQueueDrainedRequested = true;
    }

    @Override
    public boolean hasTerminated() {
        return threadPoolExecutor.isTerminated();
    }

    @Override
    public void awaitTermination() {
        logger.debug("Message listener for queue [" + queueName()
                + "] is waiting for message handler worker threads to complete before terminating");
        try {
            if (threadPoolExecutor.awaitTermination(TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                logger.debug("Message listener for queue [" + queueName() + "] has terminated");
            } else {
                logger.warn("Message listener for queue [" + queueName()
                        + "] has not terminated as message handler worker threads have not completed after "
                        + TERMINATION_TIMEOUT_SECONDS + "seconds");
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
