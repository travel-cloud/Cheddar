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
package com.clicktravel.cheddar.infrastructure.remote;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.clicktravel.common.random.Randoms;
import com.clicktravel.cheddar.infrastructure.messaging.HoldableMessageSender;
import com.clicktravel.cheddar.infrastructure.messaging.Message;
import com.clicktravel.cheddar.infrastructure.remote.RemoteCall;
import com.clicktravel.cheddar.infrastructure.remote.RemoteCallSender;

public class RemoteCallSenderTest {

    RemoteCall mockRemoteCall;
    String serialized;
    HoldableMessageSender holdableMessageSender;
    RemoteCallSender remoteCallSender;

    @Before
    public void setUp() {
        // Given
        mockRemoteCall = mock(RemoteCall.class);
        serialized = Randoms.randomString();
        when(mockRemoteCall.serialize()).thenReturn(serialized);
        holdableMessageSender = mock(HoldableMessageSender.class);
        remoteCallSender = new RemoteCallSender(holdableMessageSender);
    }

    @Test
    public void shouldSendMessage_withRemoteCall() {
        // When
        remoteCallSender.sendRemoteCall(mockRemoteCall);

        // Then
        checkSendMessage(0);
    }

    @Test
    public void shouldSendDelayedMessage_withRemoteCallAndDelay() {
        // When
        final int commandDelaySeconds = 1 + Randoms.randomInt(10);
        remoteCallSender.sendDelayedRemoteCall(mockRemoteCall, commandDelaySeconds);

        // Then
        checkSendMessage(commandDelaySeconds);
    }

    @Test
    public void shouldHoldMessages_withPause() {
        // When
        remoteCallSender.pause();

        // Then
        verify(holdableMessageSender).holdMessages();
    }

    @Test
    public void shouldForwardMessages_withResume() {
        // When
        remoteCallSender.resume();

        // Then
        verify(holdableMessageSender).forwardMessages();
    }

    private void checkSendMessage(final int messageDelaySeconds) {
        final String messageType = mockRemoteCall.getClass().getSimpleName();
        final ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(holdableMessageSender).sendDelayedMessage(messageCaptor.capture(), eq(messageDelaySeconds));
        final Message actualMessage = messageCaptor.getValue();
        assertEquals(messageType, actualMessage.getType());
        assertEquals(serialized, actualMessage.getPayload());
    }
}
