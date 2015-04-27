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

import static com.clicktravel.common.random.Randoms.randomInt;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageSendException;

@SuppressWarnings("unchecked")
public class MessageSenderImplTest {

    private MessageSenderImpl<Message> messageSenderImpl;
    private MessageQueue<Message> mockMessageQueue;
    private Message mockMessage;

    @Before
    public void setUp() {
        mockMessageQueue = mock(MessageQueue.class);
        messageSenderImpl = new MessageSenderImpl<>(mockMessageQueue);
        mockMessage = mock(Message.class);
    }

    @Test
    public void shouldSend_withMessage() {
        // When
        messageSenderImpl.send(mockMessage);

        // Then
        verify(mockMessageQueue).send(mockMessage);
    }

    @Test
    public void shouldThrowMessageSendException_onExceptionFromSend() {
        // Given
        final MessageSendException messageSendException = mock(MessageSendException.class);
        doThrow(messageSendException).when(mockMessageQueue).send(any(Message.class));

        // When
        MessageSendException thrownException = null;
        try {
            messageSenderImpl.send(mockMessage);
        } catch (final MessageSendException e) {
            thrownException = e;
        }

        // Then
        assertSame(messageSendException, thrownException);
    }

    @Test
    public void shouldSendDelayedMessage_withMessageAndDelay() {
        // Given
        final int delaySeconds = randomInt(100);

        // When
        messageSenderImpl.sendDelayedMessage(mockMessage, delaySeconds);

        // Then
        verify(mockMessageQueue).sendDelayedMessage(mockMessage, delaySeconds);
    }

    @Test
    public void shouldThrowMessageSendException_onExceptionFromSendDelayedMessage() {
        // Given
        final int delaySeconds = randomInt(100);
        final MessageSendException messageSendException = mock(MessageSendException.class);
        doThrow(messageSendException).when(mockMessageQueue).sendDelayedMessage(any(Message.class), anyInt());

        // When
        MessageSendException thrownException = null;
        try {
            messageSenderImpl.sendDelayedMessage(mockMessage, delaySeconds);
        } catch (final MessageSendException e) {
            thrownException = e;
        }

        // Then
        assertSame(messageSendException, thrownException);
    }

}
