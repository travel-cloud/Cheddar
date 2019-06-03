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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.messaging.MessageQueue;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;

@SuppressWarnings("unchecked")
public class InMemoryMessageSenderTest {

    private MessageQueue<TypedMessage> mockMessageQueue;
    private InMemoryMessageSender<TypedMessage> inMemoryMessageSender;

    @Before
    public void setUp() {
        mockMessageQueue = mock(MessageQueue.class);
        inMemoryMessageSender = new InMemoryMessageSender<TypedMessage>(mockMessageQueue);
    }

    @Test
    public void shouldReturnNoMessagesAfterConstruction_onGetSentMessages() {
        // When
        final List<TypedMessage> sentMessages = inMemoryMessageSender.getSentMessages();

        // Then
        assertTrue(sentMessages.isEmpty());
    }

    @Test
    public void shouldReturnSentMessages_onGetSentMessages() {
        // Given
        final List<TypedMessage> messages = new ArrayList<>();
        final int messageCount = 2 + randomInt(3);
        for (int n = 0; n < messageCount; n++) {
            final TypedMessage message = mock(TypedMessage.class);
            messages.add(message);
            inMemoryMessageSender.send(message);
        }

        // When
        final List<TypedMessage> returnedMessages = inMemoryMessageSender.getSentMessages();

        // Then
        assertEquals(returnedMessages, messages);
    }

    @Test
    public void shouldNotForwardMessageToQueueByDefault_onSendMessage() {
        // Given
        final TypedMessage mockMessage = mock(TypedMessage.class);

        // When
        inMemoryMessageSender.send(mockMessage);

        // Then
        verify(mockMessageQueue, never()).send(any(TypedMessage.class));
    }

    @Test
    public void shouldForwardMessageToQueueAfterSetForwardMessagesToQueue_onSendMessage() {
        // Given
        inMemoryMessageSender.setForwardMessagesToQueue(true);
        final TypedMessage mockMessage = mock(TypedMessage.class);

        // When
        inMemoryMessageSender.send(mockMessage);

        // Then
        verify(mockMessageQueue).send(mockMessage);
    }

    @Test
    public void shouldReturnDelayedSentMessages_onGetSentMessages() {
        // Given
        final TypedMessage message = mock(TypedMessage.class);
        inMemoryMessageSender.send(message);

        // When
        final List<TypedMessage> returnedMessages = inMemoryMessageSender.getSentMessages();

        // Then
        assertEquals(1, returnedMessages.size());
        assertEquals(returnedMessages.get(0), message);
    }

}
