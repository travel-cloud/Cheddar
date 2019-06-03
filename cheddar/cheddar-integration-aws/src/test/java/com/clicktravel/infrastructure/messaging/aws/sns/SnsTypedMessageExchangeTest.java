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
package com.clicktravel.infrastructure.messaging.aws.sns;

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.AmazonClientException;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessagePublishException;

public class SnsTypedMessageExchangeTest {

    private SnsTypedMessageExchange snsTypedMessageExchange;
    private SnsTopicResource mockSnsTopicResource;

    @Before
    public void setUp() {
        mockSnsTopicResource = mock(SnsTopicResource.class);
        snsTypedMessageExchange = new SnsTypedMessageExchange(mockSnsTopicResource);
    }

    @Test
    public void shouldRoute_withMessage() {
        // Given
        final TypedMessage mockTypedMessage = mock(TypedMessage.class);
        final String messageType = randomString();
        final String payload = randomString();
        when(mockTypedMessage.getType()).thenReturn(messageType);
        when(mockTypedMessage.getPayload()).thenReturn(payload);

        // When
        snsTypedMessageExchange.route(mockTypedMessage);

        // Then
        verify(mockSnsTopicResource).publish(messageType, payload);
    }

    @Test
    public void shouldThrowMessagePublishException_onAmazonClientExceptionFromPublish() {
        // Given
        final TypedMessage mockTypedMessage = mock(TypedMessage.class);
        final String messageType = randomString();
        final String payload = randomString();
        when(mockTypedMessage.getType()).thenReturn(messageType);
        when(mockTypedMessage.getPayload()).thenReturn(payload);
        doThrow(AmazonClientException.class).when(mockSnsTopicResource).publish(anyString(), anyString());

        // When
        MessagePublishException thrownException = null;
        try {
            snsTypedMessageExchange.route(mockTypedMessage);
        } catch (final MessagePublishException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldReturnName() {
        // Given
        final String topicName = randomString();
        when(mockSnsTopicResource.getTopicName()).thenReturn(topicName);

        // When
        final String returnedName = snsTypedMessageExchange.getName();

        // Then
        assertEquals(topicName, returnedName);
    }
}
