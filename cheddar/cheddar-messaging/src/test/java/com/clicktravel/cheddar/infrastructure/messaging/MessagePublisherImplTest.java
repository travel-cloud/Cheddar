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
package com.clicktravel.cheddar.infrastructure.messaging;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.messaging.exception.MessagePublishException;

@SuppressWarnings("unchecked")
public class MessagePublisherImplTest {

    private MessagePublisherImpl<Message> messagePublisherImpl;
    private Exchange<Message> mockExchange;
    private Message mockMessage;

    @Before
    public void setUp() {
        mockExchange = mock(Exchange.class);
        messagePublisherImpl = new MessagePublisherImpl<>(mockExchange);
        mockMessage = mock(Message.class);
    }

    @Test
    public void shouldPublish_withMessage() {
        // When
        messagePublisherImpl.publish(mockMessage);

        // Then
        verify(mockExchange).route(mockMessage);
    }

    @Test
    public void shouldThrowMessagePublishException_onExceptionFromRoute() {
        // Given
        final MessagePublishException mockMessagePublishException = mock(MessagePublishException.class);
        doThrow(mockMessagePublishException).when(mockExchange).route(any(Message.class));

        // When
        MessagePublishException thrownException = null;
        try {
            messagePublisherImpl.publish(mockMessage);
        } catch (final MessagePublishException e) {
            thrownException = e;
        }

        // Then
        assertSame(mockMessagePublishException, thrownException);
    }
}
