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
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;

@SuppressWarnings("unchecked")
public class InMemoryMessageQueueTest {

    private InMemoryMessageQueuePoller mockInMemoryMessageQueuePoller;

    @Before
    public void setUp() {
        mockInMemoryMessageQueuePoller = mock(InMemoryMessageQueuePoller.class);
    }

    @Test
    public void shouldPollQueues_onSend() {
        final InMemoryMessageQueue<TypedMessage> inMemoryMessageQueue = new InMemoryMessageQueue<TypedMessage>(
                randomString(), mockInMemoryMessageQueuePoller);
        final TypedMessage mockMessage = mock(TypedMessage.class);

        // When
        inMemoryMessageQueue.send(mockMessage);

        // Then
        verify(mockInMemoryMessageQueuePoller).poll();
    }

    @Test
    public void shouldReturnMessagesAfterSend_onReceive() {
        // Given
        final InMemoryMessageQueue<TypedMessage> inMemoryMessageQueue = new InMemoryMessageQueue<TypedMessage>(
                randomString(), mockInMemoryMessageQueuePoller);
        final List<TypedMessage> messages = new ArrayList<>();
        final int messageCount = 2 + randomInt(3);
        for (int n = 0; n < messageCount; n++) {
            final TypedMessage message = mock(TypedMessage.class);
            messages.add(message);
            inMemoryMessageQueue.send(message);
        }

        // When
        final List<TypedMessage> allReceivedMessages = new ArrayList<>();
        while (true) {
            final List<TypedMessage> receivedMessages = inMemoryMessageQueue.receive();
            if (receivedMessages.isEmpty()) {
                break;
            }
            for (final TypedMessage receivedMessage : receivedMessages) {
                allReceivedMessages.add(receivedMessage);
                inMemoryMessageQueue.delete(receivedMessage);
            }
        }

        // Then
        assertEquals(messages, allReceivedMessages);
    }

    @Test
    public void shouldReturnMessageAfterSendDelayedMessage_onReceive() {
        // Given
        final InMemoryMessageQueue<TypedMessage> inMemoryMessageQueue = new InMemoryMessageQueue<TypedMessage>(
                randomString(), mockInMemoryMessageQueuePoller);
        final TypedMessage message = mock(TypedMessage.class);
        inMemoryMessageQueue.sendDelayedMessage(message, randomInt(5));

        // When
        final List<TypedMessage> receivedMessages = inMemoryMessageQueue.receive();

        // Then
        assertNotNull(receivedMessages);
        assertEquals(Collections.singletonList(message), receivedMessages);
    }

    @Test
    public void shouldReturnName() {
        // Given
        final String name = randomString();
        final InMemoryMessageQueue<TypedMessage> inMemoryMessageQueue = new InMemoryMessageQueue<TypedMessage>(name,
                mockInMemoryMessageQueuePoller);

        // When
        final String returnedName = inMemoryMessageQueue.getName();

        // Then
        assertEquals(name, returnedName);
    }
}
