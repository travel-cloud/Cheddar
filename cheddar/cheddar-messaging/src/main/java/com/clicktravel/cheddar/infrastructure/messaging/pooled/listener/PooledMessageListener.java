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
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageDeleteException;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageReceiveException;
import com.clicktravel.common.concurrent.RateLimiter;

public abstract class PooledMessageListener<T extends Message> implements MessageListener, Runnable {

    /**
     * Maximum number of messages to receive from the queue at a time. Using larger numbers decreases the number of
     * calls to receive and thus increases throughput, at the possible expense of latency.
     */
    protected static final int DEFAULT_MAX_RECEIVED_MESSAGES = 10;

    /**
     * Controls when messages are received from the queue by setting an ideal minimum number of runnable tasks for each
     * thread. This minimum includes the currently executing tasks and those on the thread pool work queue. When the
     * number of runnable tasks dips below the ideal, more messages are received.
     */
    protected static final int IDEAL_RUNNABLES_PER_THREAD = 2; // Each thread has 1 executing + 1 queued runnable

    /**
     * The default number of worker threads to use in a fixed size thread pool
     */
    protected static final int DEFAULT_NUM_WORKER_THREADS = 10;

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
     * Time (in milliseconds) to pause when receive message request returns an error
     */
    private static final long RECEIVE_MESSAGE_ERROR_PAUSE_MILLIS = 500;

    /**
     * Maximum number of attempts to delete message from queue
     */
    private static final int MAX_DELETE_MESSAGE_ATTEMPTS = 5;

    /**
     * Time (in milliseconds) to pause when delete message request returns an error
     */
    private static final long DELETE_MESSAGE_ERROR_PAUSE_MILLIS = 1500;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final MessageQueue<T> messageQueue;
    private final ThreadPoolExecutor threadPoolExecutor;
    private final RateLimiter rateLimiter;
    private final Semaphore semaphore;
    private final int maxReceivedMessages;
    private volatile boolean started;
    private volatile boolean shutdownRequested;
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

    protected abstract void listenerStarted();

    @Override
    public void start() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            started = true;
            listenerStarted();
            final String limiterSummary = rateLimiter != null ? ("using " + rateLimiter.toString())
                    : "not rate limited";
            logger.debug(String.format("Listener for queue [%s] has pool of %d threads and is %s", queueName(),
                    threadPoolExecutor.getMaximumPoolSize(), limiterSummary));
            processMessagesUntilShutdownRequested();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (final Throwable e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            logger.debug(String.format(
                    "Message listener for queue [%s] has stopped receiving messages. Initiating shutdown of task executor",
                    queueName()));
            threadPoolExecutor.shutdown();
        }
    }

    private void processMessagesUntilShutdownRequested() throws InterruptedException {
        while (!shutdownRequested) {
            // Block until there is capacity to handle up to maxReceivedMessages
            semaphore.acquire(maxReceivedMessages);
            List<T> messages = Collections.emptyList();
            try {
                if (!shutdownRequested) {
                    final int pollSeconds = shutdownRequestImminent ? SHORT_POLL_DURATION_SECONDS
                            : LONG_POLL_DURATION_SECONDS;
                    try {
                        messages = messageQueue.receive(pollSeconds, maxReceivedMessages);
                    } catch (final MessageReceiveException e) {
                        logger.warn("Error receiving messages on queue:[" + queueName() + "]", e);
                        Thread.sleep(RECEIVE_MESSAGE_ERROR_PAUSE_MILLIS);
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
    private void processMessage(final T message) throws InterruptedException {
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
    public void completeMessageProcessing(final T message) throws InterruptedException {
        deleteMessage(message);
        semaphore.release();
    }

    private void deleteMessage(final T message) throws InterruptedException {
        for (int attempts = 0; attempts < MAX_DELETE_MESSAGE_ATTEMPTS; attempts++) {
            try {
                messageQueue.delete(message);
                return;
            } catch (final MessageDeleteException e) {
                logger.warn(String.format("Failed attempt to delete message with id [%s] from queue [%s]",
                        message.getMessageId(), queueName()), e);
                Thread.sleep(DELETE_MESSAGE_ERROR_PAUSE_MILLIS);
            }
        }
        logger.error(String.format("Failed all attempts to delete message with id [%s] from queue [%s]",
                message.getMessageId(), queueName()));
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

    protected String queueName() {
        return messageQueue.getName();
    }

    @Override
    public void prepareForShutdown() {
        logger.debug(String.format(
                "Message listener for queue [%s] is preparing for imminent shutdown. Reducing queue poll time.",
                queueName()));
        shutdownRequestImminent = true;
    }

    @Override
    public void shutdownListener() {
        logger.debug(String.format("Message listener for queue [%s] is shutting down", queueName()));
        shutdownRequested = true;
        if (!started) {
            threadPoolExecutor.shutdown();
        }
    }

    @Override
    public boolean awaitShutdownComplete(final long timeoutMillis) {
        boolean terminated = false;
        try {
            terminated = threadPoolExecutor.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS);
            if (terminated) {
                logger.debug(String.format("Message listener for queue [%s] shutdown has completed", queueName()));
            } else {
                logger.warn(String.format(
                        "Message listener for queue [%s] has not shutdown as message handler worker threads have not completed",
                        queueName()));
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return terminated;
    }
}
