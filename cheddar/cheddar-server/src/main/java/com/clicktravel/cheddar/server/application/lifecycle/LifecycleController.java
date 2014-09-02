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

package com.clicktravel.cheddar.server.application.lifecycle;

import static com.clicktravel.cheddar.server.application.lifecycle.LifecycleStatus.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.clicktravel.cheddar.infrastructure.messaging.MessageListener;

@Component
public class LifecycleController implements ApplicationListener<ContextRefreshedEvent> {

    private final MessageListener eventMessageListener;
    private final MessageListener highPriorityEventMessageListener;
    private final MessageListener remoteCallMessageListener;
    private final MessageListener remoteResponseMessageListener;
    private final MessageListener systemEventMessageListener;
    private final Collection<MessageListener> messageListeners;
    private final CountDownLatch restAdapterStartLatch;
    private final LifecycleStatusHolder lifecycleStatusHolder;
    private final TerminationThread terminationThread;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public LifecycleController(final MessageListener eventMessageListener,
            final MessageListener highPriorityEventMessageListener, final MessageListener remoteCallMessageListener,
            final MessageListener remoteResponseMessageListener, final MessageListener systemEventMessageListener,
            final Collection<MessageListener> messageListeners, final CountDownLatch restAdapterStartLatch,
            final LifecycleStatusHolder lifecycleStatusHolder) {
        this.eventMessageListener = eventMessageListener;
        this.highPriorityEventMessageListener = highPriorityEventMessageListener;
        this.remoteCallMessageListener = remoteCallMessageListener;
        this.remoteResponseMessageListener = remoteResponseMessageListener;
        this.systemEventMessageListener = systemEventMessageListener;
        this.messageListeners = messageListeners;
        this.restAdapterStartLatch = restAdapterStartLatch;
        this.lifecycleStatusHolder = lifecycleStatusHolder;
        terminationThread = new TerminationThread();
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        final LifecycleStatus currentLifecycleStatus = lifecycleStatusHolder.getLifecycleStatus();
        logger.debug("Initial lifecycle status: " + currentLifecycleStatus);
        systemEventMessageListener.start();
        if (currentLifecycleStatus.equals(RUNNING)) {
            logger.debug("Application instance NOT using blue-green deployment procedure");
            processRunningState();
        } else {
            logger.debug("Application instance using blue-green deployment procedure");
        }
    }

    public void enterPausedState() {
        checkAndChangeLifecycleStatus(INACTIVE, PAUSED);
        logger.debug("Application instance now accepts REST requests, instance responds with positive health check. However, do not process REST requests.");
    }

    public void enterRunningState() {
        checkAndChangeLifecycleStatus(PAUSED, RUNNING);
        processRunningState();
    }

    private void processRunningState() {
        startAll(messageListenersExcept(systemEventMessageListener));
        logger.debug("Application instance now running normally; REST application service requests are accepted and processed");
        restAdapterStartLatch.countDown(); // start REST request processing
    }

    public void enterHaltingLowPriorityEventsState() {
        checkAndChangeLifecycleStatus(RUNNING, HALTING_LOW_PRIORITY_EVENTS);
        logger.debug("Application instance closing down, using blue-green deployment procedure.");
        shutdownImminentlyAllMessageListeners();
        logger.debug("Halting low-priority domain event handling and general application work queue handling.");
        shutdownAll(messageListenersExcept(eventMessageListener, highPriorityEventMessageListener,
                remoteCallMessageListener, remoteResponseMessageListener, systemEventMessageListener));
    }

    public void enterDrainingRequestsState() {
        checkAndChangeLifecycleStatus(HALTING_LOW_PRIORITY_EVENTS, DRAINING_REQUESTS);
        logger.debug("Application instance now responds with negative health check, diverting REST requests away; Start to drain requests in progress.");
    }

    public void enterHaltingHighPriorityEventsState() {
        terminationThread.start();
    }

    private class TerminationThread extends Thread {
        @Override
        public void run() {
            processHaltingHighPriorityEventsState();
            processTerminatingState();
            processTerminatedState();
            logger.info("Application instance terminated");
        }

        private void processHaltingHighPriorityEventsState() {
            checkAndChangeLifecycleStatus(DRAINING_REQUESTS, HALTING_HIGH_PRIORITY_EVENTS);
            logger.debug("Halting high-priority domain event processing");
            shutdownAll(eventMessageListener, highPriorityEventMessageListener);
            eventMessageListener.awaitTermination();
            highPriorityEventMessageListener.awaitTermination();
        }

        private void processTerminatingState() {
            checkAndChangeLifecycleStatus(HALTING_HIGH_PRIORITY_EVENTS, TERMINATING);
            logger.debug("Draining command queue");
            remoteCallMessageListener.shutdownAfterQueueDrained();
            remoteCallMessageListener.awaitTermination();
            logger.debug("Draining command response queue");
            remoteResponseMessageListener.shutdownAfterQueueDrained();
            remoteResponseMessageListener.awaitTermination();
        }

        private void processTerminatedState() {
            checkAndChangeLifecycleStatus(TERMINATING, TERMINATED);
            logger.debug("Halting system queue processing");
            systemEventMessageListener.shutdown();
            systemEventMessageListener.awaitTermination();
        }
    }

    private void checkAndChangeLifecycleStatus(final LifecycleStatus expectedCurrentStatus,
            final LifecycleStatus newStatus) {
        final LifecycleStatus currentStatus = lifecycleStatusHolder.getLifecycleStatus();
        if (currentStatus.equals(expectedCurrentStatus)) {
            changeLifecycleStatus(currentStatus, newStatus);
        } else {
            warnFailedLifecycleStatusChange(currentStatus, newStatus);
        }
    }

    private void warnFailedLifecycleStatusChange(final LifecycleStatus currentStatus, final LifecycleStatus newStatus) {
        logger.warn("Failed attempt to change lifecycle status from:[" + currentStatus + "] to:[" + newStatus + "]");
        throw new IllegalStateException("Cannot change status from " + currentStatus + " to " + newStatus);
    }

    private void changeLifecycleStatus(final LifecycleStatus currentStatus, final LifecycleStatus newStatus) {
        logger.debug("Changing lifecycle states; from:[" + currentStatus + "] to:[" + newStatus + "]");
        lifecycleStatusHolder.setLifecycleStatus(newStatus);
    }

    private void startAll(final Collection<MessageListener> messageListenersToStart) {
        for (final MessageListener messageListener : messageListenersToStart) {
            messageListener.start();
        }
    }

    private void shutdownAll(final Collection<MessageListener> messageListenersToShutdown) {
        for (final MessageListener messageListener : messageListenersToShutdown) {
            messageListener.shutdown();
        }
    }

    private void shutdownAll(final MessageListener... messageListenersToShutdown) {
        shutdownAll(Arrays.asList(messageListenersToShutdown));
    }

    private void shutdownImminentlyAllMessageListeners() {
        for (final MessageListener messageListener : messageListeners) {
            messageListener.prepareForShutdown();
        }
    }

    private Set<MessageListener> messageListenersExcept(final MessageListener... exceptions) {
        final Set<MessageListener> result = new HashSet<>(messageListeners);
        result.removeAll(Arrays.asList(exceptions));
        return result;
    }

}
