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
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageParseException;

public class InvalidBasicMessageTest {

    private String messageId;
    private String receiptHandle;
    private MessageParseException messageParseException;
    private InvalidBasicMessage message;

    @Before
    public void setUp() {
        messageId = randomId();
        receiptHandle = randomId();
        messageParseException = new MessageParseException(randomString());
        message = new InvalidBasicMessage(messageId, receiptHandle, messageParseException);
    }

    @Test
    public void shouldReturnMessageId() {
        // When
        final String result = message.getMessageId();

        // Then
        assertEquals(messageId, result);
    }

    @Test
    public void shouldReturnReceiptHandle() {
        // When
        final String result = message.getReceiptHandle();

        // Then
        assertEquals(receiptHandle, result);
    }

    @Test
    public void shouldNotReturnBody() {
        // When
        MessageParseException thrownException = null;
        try {
            message.getBody();
        } catch (final MessageParseException e) {
            thrownException = e;
        }

        // Then
        assertSame(messageParseException, thrownException);
    }

}
