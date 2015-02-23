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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.messaging.MessageQueue;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;

@SuppressWarnings("unchecked")
public class InMemoryExchangeTest {

    @Test
    public void shouldReturnName() {
        // Given
        final String name = randomString();
        final InMemoryExchange<TypedMessage> inMemoryExchange = new InMemoryExchange<TypedMessage>(name);

        // When
        final String returnedName = inMemoryExchange.getName();

        // Then
        assertEquals(name, returnedName);
    }

    @Test
    public void shouldSendMessageToSubscriberOnRoute_withMessage() {
        // Given
        final MessageQueue<TypedMessage> mockSubscriber = mock(MessageQueue.class);
        final InMemoryExchange<TypedMessage> inMemoryExchange = new InMemoryExchange<TypedMessage>(randomString());
        inMemoryExchange.addSubscriber(mockSubscriber);
        final TypedMessage mockTypedMessage = mock(TypedMessage.class);

        // When
        inMemoryExchange.route(mockTypedMessage);

        // Then
        verify(mockSubscriber).send(mockTypedMessage);
    }

    @Test
    public void addSubscriberIsIdempotent() {
        // Given
        final MessageQueue<TypedMessage> mockSubscriber = mock(MessageQueue.class);
        final InMemoryExchange<TypedMessage> inMemoryExchange = new InMemoryExchange<TypedMessage>(randomString());
        inMemoryExchange.addSubscriber(mockSubscriber);
        inMemoryExchange.addSubscriber(mockSubscriber);
        inMemoryExchange.addSubscriber(mockSubscriber);
        final TypedMessage mockTypedMessage = mock(TypedMessage.class);

        // When
        inMemoryExchange.route(mockTypedMessage);

        // Then
        verify(mockSubscriber).send(mockTypedMessage); // exactly once
    }

    @Test
    public void shouldSendMessageToAllSubscribersOnRoute_withMessage() {
        // Given
        final InMemoryExchange<TypedMessage> inMemoryExchange = new InMemoryExchange<TypedMessage>(randomString());
        final Collection<MessageQueue<TypedMessage>> mockSubscribers = new ArrayList<>();
        final int subscriberCount = 2 + randomInt(3);
        for (int n = 0; n < subscriberCount; n++) {
            final MessageQueue<TypedMessage> mockSubscriber = mock(MessageQueue.class);
            mockSubscribers.add(mockSubscriber);
            inMemoryExchange.addSubscriber(mockSubscriber);
        }
        final TypedMessage mockTypedMessage = mock(TypedMessage.class);

        // When
        inMemoryExchange.route(mockTypedMessage);

        // Then
        for (final MessageQueue<TypedMessage> mockSubscriber : mockSubscribers) {
            verify(mockSubscriber).send(mockTypedMessage);
        }
    }
}
