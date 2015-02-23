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
package com.clicktravel.cheddar.infrastructure.messaging.inmemory;

import static com.clicktravel.common.random.Randoms.randomInt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.messaging.Exchange;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;

@SuppressWarnings("unchecked")
public class InMemoryMessagePublisherTest {

    private Exchange<TypedMessage> mockExchange;
    private InMemoryMessagePublisher<TypedMessage> inMemoryMessagePublisher;

    @Before
    public void setUp() {
        mockExchange = mock(Exchange.class);
        inMemoryMessagePublisher = new InMemoryMessagePublisher<TypedMessage>(mockExchange);
    }

    @Test
    public void shouldReturnNoPublishedMessages_afterConstruction() {
        // When
        final List<TypedMessage> typedMessages = inMemoryMessagePublisher.getPublishedMessages();

        // Then
        assertTrue(typedMessages.isEmpty());
    }

    @Test
    public void shouldReturnPublishedMessages_afterPublishMessage() {
        // Given
        final List<TypedMessage> typedMessages = new ArrayList<>();
        final int messageCount = 2 + randomInt(3);
        for (int n = 0; n < messageCount; n++) {
            final TypedMessage typedMessage = mock(TypedMessage.class);
            typedMessages.add(typedMessage);
            inMemoryMessagePublisher.publish(typedMessage);
        }

        // When
        final List<TypedMessage> returnedMessages = inMemoryMessagePublisher.getPublishedMessages();

        // Then
        assertEquals(returnedMessages, typedMessages);
    }

    @Test
    public void shouldNotForwardMessageToExchange_afterConstruction() {
        // Given
        final TypedMessage mockTypedMessage = mock(TypedMessage.class);

        // When
        inMemoryMessagePublisher.publish(mockTypedMessage);

        // Then
        verify(mockExchange, never()).route(any(TypedMessage.class));
    }

    @Test
    public void shouldForwardMessageToExchange_afterForwardingSet() {
        // Given
        inMemoryMessagePublisher.setForwardMessagesToExchange(true);
        final TypedMessage mockTypedMessage = mock(TypedMessage.class);

        // When
        inMemoryMessagePublisher.publish(mockTypedMessage);

        // Then
        verify(mockExchange).route(mockTypedMessage);
    }
}
