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

import static com.clicktravel.cheddar.server.application.lifecycle.ApplicationLifecycleController.SHUTDOWN_TIMEOUT_MILLIS;
import static com.clicktravel.common.random.Randoms.randomInt;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;
import java.util.Collection;

import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.clicktravel.cheddar.infrastructure.messaging.MessageListener;
import com.clicktravel.cheddar.server.rest.RestServer;
import com.clicktravel.common.random.Randoms;

public class ApplicationLifecycleControllerTest {

    private ApplicationLifecycleController applicationLifecycleController;
    private RestServer mockRestServer;
    private MessageListener mockSystemEventMessageListener;
    private MessageListener mockOtherMessageListener;

    @Before
    public void setUp() {
        mockRestServer = mock(RestServer.class);
        mockSystemEventMessageListener = mock(MessageListener.class);
        mockOtherMessageListener = mock(MessageListener.class);
        final Collection<MessageListener> mockMessageListeners = Arrays.asList(mockSystemEventMessageListener,
                mockOtherMessageListener);
        applicationLifecycleController = new ApplicationLifecycleController(mockRestServer,
                mockSystemEventMessageListener, mockMessageListeners);
        DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis());
    }

    @After
    public void tearDown() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void shouldStartApplication_withServicePortStatusPortAndBindAddress() throws Exception {
        // Given
        final int servicePort = randomInt(16384);
        final String bindAddress = randomString();
        final int workerThreads = Randoms.randomIntInRange(2, 16);

        // When
        applicationLifecycleController.startApplication(servicePort, bindAddress, workerThreads);

        // Then
        final InOrder inOrder = inOrder(mockRestServer, mockSystemEventMessageListener, mockOtherMessageListener);
        inOrder.verify(mockSystemEventMessageListener).start();
        inOrder.verify(mockOtherMessageListener).start();
        verifyNoMoreInteractions(mockSystemEventMessageListener, mockOtherMessageListener);
        inOrder.verify(mockRestServer).start(servicePort, bindAddress, workerThreads);
    }

    @Test
    public void shouldShutdownApplication() {
        // When
        applicationLifecycleController.shutdownApplication();

        // Then
        verifyShutdownRestServerBeforeMessageListener(mockSystemEventMessageListener);
        verifyShutdownRestServerBeforeMessageListener(mockOtherMessageListener);
        verifyShutdownSystemMessageListenerLast();
    }

    private void verifyShutdownRestServerBeforeMessageListener(final MessageListener mockMessageListener) {
        final InOrder inOrder = inOrder(mockRestServer, mockMessageListener);
        inOrder.verify(mockMessageListener).prepareForShutdown();
        inOrder.verify(mockRestServer).shutdownAndAwait(SHUTDOWN_TIMEOUT_MILLIS);
        inOrder.verify(mockMessageListener).shutdownListener();
        inOrder.verify(mockMessageListener).awaitShutdownComplete(SHUTDOWN_TIMEOUT_MILLIS);
    }

    private void verifyShutdownSystemMessageListenerLast() {
        final InOrder inOrder = inOrder(mockSystemEventMessageListener, mockOtherMessageListener);
        inOrder.verify(mockOtherMessageListener).shutdownListener();
        inOrder.verify(mockOtherMessageListener).awaitShutdownComplete(SHUTDOWN_TIMEOUT_MILLIS);
        inOrder.verify(mockSystemEventMessageListener).shutdownListener();
        inOrder.verify(mockSystemEventMessageListener).awaitShutdownComplete(SHUTDOWN_TIMEOUT_MILLIS);
    }
}
