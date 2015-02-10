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

import com.clicktravel.cheddar.infrastructure.messaging.TypedMessageListener;

/**
 * Controls the start and shutdown of all {@link MessageListeners}. This class progresses the lifecycle status of this
 * application instance when various enterXXXstate() methods are called. These methods are called by an external trigger
 * (system event handlers) under the control of a scripted blue-green deployment procedure.
 */
@Component
public class LifecycleController implements ApplicationListener<ContextRefreshedEvent> {

    private final TypedMessageListener eventMessageListener;
    private final TypedMessageListener highPriorityEventMessageListener;
    private final TypedMessageListener systemEventMessageListener;
    private final Collection<TypedMessageListener> typedMessageListeners;
    private final CountDownLatch restAdapterStartLatch;
    private final LifecycleStatusHolder lifecycleStatusHolder;
    private final TerminationThread terminationThread;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public LifecycleController(final TypedMessageListener eventMessageListener,
            final TypedMessageListener highPriorityEventMessageListener, final TypedMessageListener systemEventMessageListener,
            final Collection<TypedMessageListener> typedMessageListeners, final CountDownLatch restAdapterStartLatch,
            final LifecycleStatusHolder lifecycleStatusHolder) {
        this.eventMessageListener = eventMessageListener;
        this.highPriorityEventMessageListener = highPriorityEventMessageListener;
        this.systemEventMessageListener = systemEventMessageListener;
        this.typedMessageListeners = typedMessageListeners;
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
            logger.debug("Application instances in this environment do not use blue-green deployment");
            processRunningState();
        } else {
            logger.debug("Application instances in this environment typically use blue-green deployment");
        }
    }

    public void enterPausedState() {
        checkAndSetLifecycleStatus(INACTIVE, PAUSED);
        logger.debug("Application instance now accepts REST requests, instance responds with positive health check. However, do not process REST requests.");
    }

    public void enterRunningState() {
        checkAndSetLifecycleStatus(PAUSED, RUNNING);
        processRunningState();
    }

    public void enterRunningStateWithoutBlueGreenDeployment() {
        checkAndSetLifecycleStatus(INACTIVE, RUNNING);
        logger.debug("Skipping blue-green deployment procedure, proceeding directly to RUNNING state");
        processRunningState();
    }

    private void processRunningState() {
        startAll(messageListenersExcept(systemEventMessageListener));
        logger.debug("Application instance now running normally; REST application service requests are accepted and processed");
        restAdapterStartLatch.countDown(); // start REST request processing
    }

    public void enterHaltingLowPriorityEventsState() {
        checkAndSetLifecycleStatus(RUNNING, HALTING_LOW_PRIORITY_EVENTS);
        logger.debug("Application instance closing down, using blue-green deployment procedure.");
        prepareAllMessageListenersForShutdown();
        logger.debug("Halting low-priority domain event handling and general application work queue handling.");
        shutdownAll(messageListenersExcept(eventMessageListener, highPriorityEventMessageListener,
                systemEventMessageListener));
    }

    public void enterDrainingRequestsState() {
        checkAndSetLifecycleStatus(HALTING_LOW_PRIORITY_EVENTS, DRAINING_REQUESTS);
        logger.debug("Application instance now responds with negative health check, diverting REST requests away; Start to drain requests in progress.");
    }

    public void enterHaltingHighPriorityEventsState() {
        checkAndSetLifecycleStatus(DRAINING_REQUESTS, HALTING_HIGH_PRIORITY_EVENTS);
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
    }

    private void processHaltingHighPriorityEventsState() {
        logger.debug("Halting high-priority domain event processing");
        shutdownAll(eventMessageListener, highPriorityEventMessageListener);
        eventMessageListener.awaitTermination();
        highPriorityEventMessageListener.awaitTermination();
    }

    private void processTerminatingState() {
        setLifecycleStatus(TERMINATING);
        logger.debug("Halting system queue processing");
        systemEventMessageListener.shutdown();
        systemEventMessageListener.awaitTermination();
    }

    private void processTerminatedState() {
        setLifecycleStatus(TERMINATED);
    }

    private void checkAndSetLifecycleStatus(final LifecycleStatus expectedCurrentStatus, final LifecycleStatus newStatus) {
        final LifecycleStatus currentStatus = lifecycleStatusHolder.getLifecycleStatus();
        if (currentStatus.equals(expectedCurrentStatus)) {
            setLifecycleStatus(newStatus);
        } else {
            logger.warn("Failed attempt to change lifecycle status from:[" + currentStatus + "] to:[" + newStatus + "]");
            throw new IllegalStateException("Cannot change status from " + currentStatus + " to " + newStatus);
        }
    }

    private void setLifecycleStatus(final LifecycleStatus lifecycleStatus) {
        logger.debug("Changing lifecycle states; from:[" + lifecycleStatusHolder.getLifecycleStatus() + "] to:["
                + lifecycleStatus + "]");
        lifecycleStatusHolder.setLifecycleStatus(lifecycleStatus);
    }

    private void startAll(final Collection<TypedMessageListener> messageListenersToStart) {
        for (final TypedMessageListener typedMessageListener : messageListenersToStart) {
            typedMessageListener.start();
        }
    }

    private void shutdownAll(final Collection<TypedMessageListener> messageListenersToShutdown) {
        for (final TypedMessageListener typedMessageListener : messageListenersToShutdown) {
            typedMessageListener.shutdown();
        }
    }

    private void shutdownAll(final TypedMessageListener... messageListenersToShutdown) {
        shutdownAll(Arrays.asList(messageListenersToShutdown));
    }

    private void prepareAllMessageListenersForShutdown() {
        for (final TypedMessageListener typedMessageListener : typedMessageListeners) {
            typedMessageListener.prepareForShutdown();
        }
    }

    private Set<TypedMessageListener> messageListenersExcept(final TypedMessageListener... exceptions) {
        final Set<TypedMessageListener> result = new HashSet<>(typedMessageListeners);
        result.removeAll(Arrays.asList(exceptions));
        return result;
    }

}
