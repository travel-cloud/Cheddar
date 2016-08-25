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

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.messaging.MessageHandler;
import com.clicktravel.cheddar.infrastructure.messaging.MessageQueue;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageHandlingException;
import com.clicktravel.common.random.Randoms;

@SuppressWarnings("unchecked")
public class InMemoryTypedMessageListenerTest {

    private MessageQueue<TypedMessage> mockMessageQueue;
    private InMemoryMessageQueuePoller mockInMemoryMessageQueuePoller;
    private InMemoryTypedMessageListener inMemoryMessageListener;

    @Before
    public void setUp() {
        mockMessageQueue = mock(InMemoryMessageQueue.class);
        mockInMemoryMessageQueuePoller = mock(InMemoryMessageQueuePoller.class);
        inMemoryMessageListener = new InMemoryTypedMessageListener(mockMessageQueue, mockInMemoryMessageQueuePoller);
    }

    @Test
    public void shouldRegister_onConstruction() {
        // When
        // setUp()

        // Then
        verify(mockInMemoryMessageQueuePoller).register(inMemoryMessageListener);
    }

    @Test
    public void shouldHandleAndDeleteMessageForRegisteredType_onReceiveAndHandleMessages() throws Exception {
        // Given
        final String messageType = randomString();
        final TypedMessage mockTypedMessage = mock(TypedMessage.class);
        when(mockTypedMessage.getType()).thenReturn(messageType);
        final MessageHandler<TypedMessage> mockMessageHandler = mock(MessageHandler.class);
        inMemoryMessageListener.registerMessageHandler(messageType, mockMessageHandler);
        when(mockMessageQueue.receive()).thenReturn(Collections.singletonList(mockTypedMessage));

        // When
        final boolean polledMessage = inMemoryMessageListener.receiveAndHandleMessages();

        // Then
        assertTrue(polledMessage);
        verify(mockMessageHandler).handle(mockTypedMessage);
        verify(mockMessageQueue).delete(mockTypedMessage);
    }

    @Test
    public void shouldDeleteMessageForUnsupportedMessageType_onReceiveAndHandleMessages() throws Exception {
        // Given
        final String messageType = randomString();
        final TypedMessage mockTypedMessage = mock(TypedMessage.class);
        when(mockTypedMessage.getType()).thenReturn(messageType);
        when(mockMessageQueue.receive()).thenReturn(Collections.singletonList(mockTypedMessage));

        // When
        final boolean polledMessage = inMemoryMessageListener.receiveAndHandleMessages();

        // Then
        assertTrue(polledMessage);
        verify(mockMessageQueue).delete(mockTypedMessage);
    }

    @Test
    public void shouldDeleteMessageWhenHandlerThrowsException_onReceiveAndHandleMessages() throws Exception {
        // Given
        final String messageType = randomString();
        final TypedMessage mockTypedMessage = mock(TypedMessage.class);
        when(mockTypedMessage.getType()).thenReturn(messageType);
        final MessageHandler<TypedMessage> mockMessageHandler = mock(MessageHandler.class);
        inMemoryMessageListener.registerMessageHandler(messageType, mockMessageHandler);
        when(mockMessageQueue.receive()).thenReturn(Collections.singletonList(mockTypedMessage));
        doThrow(MessageHandlingException.class).when(mockMessageHandler).handle(mockTypedMessage);

        // When
        final boolean polledMessage = inMemoryMessageListener.receiveAndHandleMessages();

        // Then
        assertTrue(polledMessage);
        verify(mockMessageHandler).handle(mockTypedMessage);
        verify(mockMessageQueue).delete(mockTypedMessage);
    }

    @Test
    public void shouldReturnForEmptyQueue_onPollForMessage() throws Exception {
        // Given
        when(mockMessageQueue.receive()).thenReturn(Collections.<TypedMessage> emptyList());

        // When
        final boolean polledMessage = inMemoryMessageListener.receiveAndHandleMessages();

        // Then
        assertFalse(polledMessage);
    }

    @Test
    public void shouldReturn_onStart() {
        // When
        inMemoryMessageListener.start();

        // Then
        // Method returns
    }

    @Test
    public void shouldReturn_onShutdown() {
        // When
        inMemoryMessageListener.shutdownListener();

        // Then
        // Method returns
    }

    @Test
    public void shouldReturn_onPrepareForShutdown() {
        // When
        inMemoryMessageListener.prepareForShutdown();

        // Then
        // Method returns
    }

    @Test
    public void shouldReturn_onAwaitShutdownComplete() {
        // When
        inMemoryMessageListener.awaitShutdownComplete(Randoms.randomLong());

        // Then
        // Method returns
    }
}
