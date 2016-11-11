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

import java.io.IOException;
import java.util.*;

import org.joda.time.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.infrastructure.messaging.MessageListener;
import com.clicktravel.cheddar.server.rest.RestServer;

/**
 * Controls the start and shutdown of the {@link RestServer} and all {@link MessageListener}s.
 */
public class ApplicationLifecycleController {

    /**
     * Maximum overall time to allow for graceful shutdown of REST server and message listeners, in milliseconds. This
     * time must cover the completion of processing current REST requests. This time must also cover the maximum queue
     * poll time for message listeners and completion of processing for all messages already received.
     */
    static final long SHUTDOWN_TIMEOUT_MILLIS = 50000; // = 50s

    private final RestServer restServer;
    private final MessageListener systemEventMessageListener;
    private final Collection<MessageListener> messageListeners;
    private long shutdownDeadline;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ApplicationLifecycleController(final RestServer restServer, final MessageListener systemEventMessageListener,
            final Collection<MessageListener> messageListeners) {
        this.restServer = restServer;
        this.systemEventMessageListener = systemEventMessageListener;
        this.messageListeners = messageListeners;
    }

    public void startApplication(final int servicePort, final int statusPort, final String bindAddress)
            throws IOException {
        logger.info("Starting application");
        logger.info("Starting system event message listener");
        systemEventMessageListener.start();
        logger.info("Starting remaining message listeners");
        messageListenersExcept(systemEventMessageListener).stream().forEach(MessageListener::start);
        restServer.start(servicePort, statusPort, bindAddress);
        logger.info("Application started");
    }

    public void shutdownApplication() {
        logger.info("Shutting down application");
        shutdownDeadline = DateTimeUtils.currentTimeMillis() + SHUTDOWN_TIMEOUT_MILLIS;
        messageListeners.stream().forEach(MessageListener::prepareForShutdown);
        restServer.shutdownAndAwait(millisToShutdownDeadline());
        logger.info("Shutting down message listeners (except system message listener)");
        shutdownAndAwait(messageListenersExcept(systemEventMessageListener));
        logger.info("Shutting down system event message listener");
        shutdownAndAwait(Collections.singleton(systemEventMessageListener));
        logger.info("Application shutdown completed");
    }

    private void shutdownAndAwait(final Set<MessageListener> messageListenersToShutdown) {
        messageListenersToShutdown.stream().forEach(MessageListener::shutdownListener);
        for (final MessageListener messageListener : messageListenersToShutdown) {
            messageListener.awaitShutdownComplete(millisToShutdownDeadline());
        }
    }

    private long millisToShutdownDeadline() {
        return Math.max(0, shutdownDeadline - DateTimeUtils.currentTimeMillis());
    }

    private Set<MessageListener> messageListenersExcept(final MessageListener... exceptions) {
        final Set<MessageListener> result = new HashSet<>(messageListeners);
        result.removeAll(Arrays.asList(exceptions));
        return result;
    }

}
