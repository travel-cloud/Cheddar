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

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.clicktravel.cheddar.infrastructure.messaging.MessageListener;
import com.clicktravel.common.random.Randoms;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RemoteCallTagLogic.class)
public class RemoteCallTagLogicTest {

    private MessageListener mockEventMessageListener;
    private MessageListener mockHighPriorityEventMessageListener;
    private MessageListener mockRemoteCallMessageListener;
    private MessageListener mockRemoteResponseMessageListener;
    private MessageListener mockSystemEventMessageListener;
    private final Collection<MessageListener> allMockMessageListeners = new ArrayList<>();

    @Before
    public void setUp() {
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
    }

    @Test
    public void shouldTagRemoteCall_forGeneralMessageQueueProcessor() {
        shouldReturnExpectedResult("MessageProcessor:" + randomString() + ":" + Randoms.randomInt(10), true);
    }

    @Test
    public void shouldNotTagRemoteCall_forNonTaggedMessageQueueProcessor() {
        for (final MessageListener mockMessageListener : allMockMessageListeners) {
            shouldReturnExpectedResult(
                    "MessageProcessor:" + mockMessageListener.queueName() + ":" + Randoms.randomInt(10), false);
        }
    }

    @Test
    public void shouldNotTagRemoteCall_forNonMessageQueueProcessorThread() {
        shouldReturnExpectedResult(randomString(), false);
    }

    private void shouldReturnExpectedResult(final String threadName, final boolean expectedResult) {
        // Given
        final Thread mockThread = mock(Thread.class);
        when(mockThread.getName()).thenReturn(threadName);
        mockStatic(Thread.class);
        when(Thread.currentThread()).thenReturn(mockThread);
        final RemoteCallTagLogic remoteCallTagLogic = new RemoteCallTagLogic(mockEventMessageListener,
                mockHighPriorityEventMessageListener, mockRemoteCallMessageListener, mockRemoteResponseMessageListener,
                mockSystemEventMessageListener);

        // When
        final boolean result = remoteCallTagLogic.shouldTagRemoteCall();

        // Then
        assertEquals(expectedResult, result);
    }
}
