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
package com.clicktravel.infrastructure.messaging.inmemory;

import static com.clicktravel.common.random.Randoms.randomInt;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;

import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;
import com.clicktravel.cheddar.infrastructure.tx.TransactionalResourceManager;

@SuppressWarnings("unchecked")
public class DefaultInMemoryMessageQueuePollerTest {

    private DefaultInMemoryMessageQueuePoller poller;
    private TransactionalResourceManager mockTransactionalResourceManager;

    @Before
    public void setUp() {
        mockTransactionalResourceManager = mock(TransactionalResourceManager.class);
        poller = new DefaultInMemoryMessageQueuePoller();
        poller.setTransactionalResourceManager(mockTransactionalResourceManager);
    }

    @Test
    public void shouldReturnIfNoListenersRegistered_onPoll() {
        // Given
        when(mockTransactionalResourceManager.inTransaction()).thenReturn(false);

        // When
        poller.poll();

        // Then
        // poll() method has returned
    }

    @Test
    public void shouldPollIfNotInTransaction_onPoll() {
        // Given
        when(mockTransactionalResourceManager.inTransaction()).thenReturn(false);
        final InMemoryMessageListener<TypedMessage> mockListener = mock(InMemoryMessageListener.class);
        when(mockListener.receiveAndHandleMessages()).thenReturn(false);
        poller.register(mockListener);

        // When
        poller.poll();

        // Then
        verify(mockListener).receiveAndHandleMessages();
    }

    @Test
    public void shouldNotPollIfInTransaction_onPoll() {
        // Given
        when(mockTransactionalResourceManager.inTransaction()).thenReturn(true);
        final InMemoryMessageListener<TypedMessage> mockListener = mock(InMemoryMessageListener.class);
        when(mockListener.receiveAndHandleMessages()).thenReturn(false);
        poller.register(mockListener);

        // When
        poller.poll();

        // Then
        verify(mockListener, never()).receiveAndHandleMessages();
    }

    @Test
    public void shouldPollOnceIfAllQueuesEmpty_onPoll() {
        // Given
        when(mockTransactionalResourceManager.inTransaction()).thenReturn(false);
        final Set<InMemoryMessageListener<TypedMessage>> typedMessageListeners = new HashSet<>();
        for (int n = 0; n < 3; n++) {
            final InMemoryMessageListener<TypedMessage> mockListener = mock(InMemoryMessageListener.class);
            when(mockListener.receiveAndHandleMessages()).thenReturn(false);
            typedMessageListeners.add(mockListener);
            poller.register(mockListener);
        }

        // When
        poller.poll();

        // Then
        for (final InMemoryMessageListener<TypedMessage> mockListener : typedMessageListeners) {
            verify(mockListener).receiveAndHandleMessages(); // exactly once
        }
    }

    @Test
    public void shouldPollUntilQueueEmpty_onPoll() {
        // Given
        when(mockTransactionalResourceManager.inTransaction()).thenReturn(false);
        final int messageCount = randomInt(5) + 1;
        final InMemoryMessageListener<TypedMessage> mockMemoryMessageListener = mock(InMemoryMessageListener.class);
        poller.register(mockMemoryMessageListener);

        // pollForMessage() should return true for messageCount times
        OngoingStubbing<Boolean> ongoingStubbing = when(mockMemoryMessageListener.receiveAndHandleMessages());
        for (int n = 0; n < messageCount; n++) {
            ongoingStubbing = ongoingStubbing.thenReturn(true);
        }
        ongoingStubbing.thenReturn(false);

        // When
        poller.poll();

        // Then
        verify(mockMemoryMessageListener, times(messageCount + 1)).receiveAndHandleMessages();
    }

    @Test
    public void shouldNotPollIfNestedCall_onPoll() {
        // Given
        when(mockTransactionalResourceManager.inTransaction()).thenReturn(false);
        final InMemoryMessageListener<TypedMessage> mockMemoryMessageListener = mock(InMemoryMessageListener.class);
        poller.register(mockMemoryMessageListener);
        when(mockMemoryMessageListener.receiveAndHandleMessages()).thenAnswer(new Answer<Boolean>() {
            private boolean firstPoll = true;

            @Override
            public Boolean answer(final InvocationOnMock invocation) throws Throwable {
                if (firstPoll) {
                    firstPoll = false;
                    poller.poll(); // Should not invoke pollForMessage again
                    return true;
                } else {
                    return false;
                }
            }
        });

        // When
        poller.poll();

        // Then

        // Expecting exactly 2 invocations
        // - First invocation to get first message
        // - Second invocation to discover queue is empty
        verify(mockMemoryMessageListener, times(2)).receiveAndHandleMessages();
    }
}
