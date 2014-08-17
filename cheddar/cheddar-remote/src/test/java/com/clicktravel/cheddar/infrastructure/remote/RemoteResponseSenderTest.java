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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.clicktravel.cheddar.infrastructure.messaging.Message;
import com.clicktravel.cheddar.infrastructure.messaging.MessageSender;
import com.clicktravel.cheddar.infrastructure.remote.RemoteResponse;
import com.clicktravel.cheddar.infrastructure.remote.RemoteResponseSender;
import com.clicktravel.common.random.Randoms;

public class RemoteResponseSenderTest {

    @Test
    public void shouldSendMessage_withRemoteResponse() {
        // Given
        final RemoteResponse mockRemoteResponse = mock(RemoteResponse.class);
        final String serialized = Randoms.randomString();
        when(mockRemoteResponse.serialize()).thenReturn(serialized);
        final MessageSender messageSender = mock(MessageSender.class);
        final RemoteResponseSender remoteResponseSender = new RemoteResponseSender(messageSender);

        // When
        remoteResponseSender.sendRemoteResponse(mockRemoteResponse);

        // Then
        final String messageType = mockRemoteResponse.getClass().getSimpleName();
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageSender).sendMessage(messageCaptor.capture());
        final Message actualMessage = messageCaptor.getValue();
        assertEquals(messageType, actualMessage.getType());
        assertEquals(serialized, actualMessage.getPayload());
    }
}
