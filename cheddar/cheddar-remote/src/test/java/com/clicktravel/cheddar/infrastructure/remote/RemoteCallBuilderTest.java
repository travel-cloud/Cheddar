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
package com.clicktravel.cheddar.infrastructure.remote;

import static com.clicktravel.common.random.Randoms.randomInt;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.clicktravel.cheddar.infrastructure.messaging.MessageListener;
import com.clicktravel.cheddar.request.context.SecurityContextHolder;
import com.clicktravel.common.random.Randoms;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ RemoteCallBuilder.class, SecurityContextHolder.class })
public class RemoteCallBuilderTest {

    private MessageListener mockEventMessageListener;
    private MessageListener mockHighPriorityEventMessageListener;
    private MessageListener mockRemoteCallMessageListener;
    private MessageListener mockRemoteResponseMessageListener;
    private MessageListener mockSystemEventMessageListener;
    private final Collection<MessageListener> allMockMessageListeners = new ArrayList<>();
    private RemoteCallBuilder remoteCallBuilder;
    private String principal;

    @Before
    public void setUp() {
        principal = randomString();
        mockStatic(SecurityContextHolder.class);
        when(SecurityContextHolder.getPrincipal()).thenReturn(principal);

        mockEventMessageListener = mock(MessageListener.class);
        mockHighPriorityEventMessageListener = mock(MessageListener.class);
        mockRemoteCallMessageListener = mock(MessageListener.class);
        mockRemoteResponseMessageListener = mock(MessageListener.class);
        mockSystemEventMessageListener = mock(MessageListener.class);
        for (final MessageListener mockMessageListener : new MessageListener[] { mockEventMessageListener,
                mockHighPriorityEventMessageListener, mockRemoteCallMessageListener, mockRemoteResponseMessageListener,
                mockSystemEventMessageListener }) {
            allMockMessageListeners.add(mockMessageListener);
            when(mockMessageListener.queueName()).thenReturn(randomString());
        }
        remoteCallBuilder = new RemoteCallBuilder(mockEventMessageListener, mockHighPriorityEventMessageListener,
                mockRemoteCallMessageListener, mockRemoteResponseMessageListener, mockSystemEventMessageListener);
    }

    @Test
    public void shouldBuild_withParameters() throws Exception {
        // Given
        prepareMockThreadWithName(randomString());
        final String interfaceName = randomString();
        final Method method = TestService.class.getMethod("method1", new Class<?>[] { String.class, int.class });
        final Object[] args = new Object[] { randomString(), randomInt(100) };

        // When
        final RemoteCall remoteCall = remoteCallBuilder.build(interfaceName, method, args);

        // Then
        assertNotNull(remoteCall);
        assertNotNull(remoteCall.getCallId());
        assertEquals(interfaceName, remoteCall.getInterfaceName());
        assertEquals("method1", remoteCall.getMethodName());
        assertNotNull(remoteCall.getMethodParameterTypes());
        assertEquals(2, remoteCall.getMethodParameterTypes().length);
        assertEquals("java.lang.String", remoteCall.getMethodParameterTypes()[0]);
        assertEquals("int", remoteCall.getMethodParameterTypes()[1]);
        assertNotNull(remoteCall.getParameters());
        assertEquals(2, remoteCall.getParameters().length);
        assertEquals(args[0], remoteCall.getParameters()[0]);
        assertEquals(args[1], remoteCall.getParameters()[1]);
        assertEquals(principal, remoteCall.getPrincipal());
    }

    @Test
    public void shouldBuildTaggedRemoteCall_forGeneralMessageQueueProcessor() throws Exception {
        // Given
        prepareMockThreadWithName("MessageProcessor:" + randomString() + ":" + Randoms.randomInt(10));
        final String interfaceName = randomString();
        final Method method = TestService.class.getMethod("method1", new Class<?>[] { String.class, int.class });
        final Object[] args = new Object[] { randomString(), randomInt(100) };

        // When
        final RemoteCall remoteCall = remoteCallBuilder.build(interfaceName, method, args);

        // Then
        assertTrue(remoteCall.hasTag());
    }

    @Test
    public void shouldBuildNonTaggedRemoteCall_forNonTaggedMessageQueueProcessor() throws Exception {
        for (final MessageListener mockMessageListener : allMockMessageListeners) {
            final String threadName = "MessageProcessor:" + mockMessageListener.queueName() + ":"
                    + Randoms.randomInt(10);
            shouldBuildNonTaggedRemoteCall_inThreadWithName(threadName);
        }
    }

    @Test
    public void shouldBuildNonTaggedRemoteCall_forNonMessageQueueProcessorThread() throws Exception {
        shouldBuildNonTaggedRemoteCall_inThreadWithName(randomString());
    }

    private void shouldBuildNonTaggedRemoteCall_inThreadWithName(final String threadName) throws Exception {
        // Given
        prepareMockThreadWithName(threadName);
        final String interfaceName = randomString();
        final Method method = TestService.class.getMethod("method1", new Class<?>[] { String.class, int.class });
        final Object[] args = new Object[] { randomString(), randomInt(100) };

        // When
        final RemoteCall remoteCall = remoteCallBuilder.build(interfaceName, method, args);

        // Then
        assertFalse(remoteCall.hasTag());
    }

    private void prepareMockThreadWithName(final String threadName) {
        final Thread mockThread = mock(Thread.class);
        when(mockThread.getName()).thenReturn(threadName);
        mockStatic(Thread.class);
        when(Thread.currentThread()).thenReturn(mockThread);
    }
}
