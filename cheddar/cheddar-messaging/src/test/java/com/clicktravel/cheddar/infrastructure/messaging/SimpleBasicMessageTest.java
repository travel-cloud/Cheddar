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

import static com.clicktravel.common.random.Randoms.randomId;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class SimpleBasicMessageTest {

    @Test
    public void shouldReturnMessageId() {
        // Given
        final String body = randomString();
        final String messageId = randomId();
        final String receiptHandle = randomId();
        final SimpleBasicMessage message = new SimpleBasicMessage(body, messageId, receiptHandle);

        // When
        final String result = message.getMessageId();

        // Then
        assertEquals(messageId, result);
    }

    @Test
    public void shouldReturnReceiptHandle() {
        // Given
        final String body = randomString();
        final String messageId = randomId();
        final String receiptHandle = randomId();
        final SimpleBasicMessage message = new SimpleBasicMessage(body, messageId, receiptHandle);

        // When
        final String result = message.getReceiptHandle();

        // Then
        assertEquals(receiptHandle, result);
    }

    @Test
    public void shouldReturnBody() {
        // Given
        final String body = randomString();
        final String messageId = randomId();
        final String receiptHandle = randomId();
        final SimpleBasicMessage message = new SimpleBasicMessage(body, messageId, receiptHandle);

        // When
        final String result = message.getBody();

        // Then
        assertEquals(body, result);
    }

    @Test
    public void shouldConstruct_withBody() {
        // Given
        final String body = randomString();

        // When
        final SimpleBasicMessage message = new SimpleBasicMessage(body);

        // Then
        assertNull(message.getMessageId());
        assertNull(message.getReceiptHandle());
        assertEquals(body, message.getBody());
    }
}
