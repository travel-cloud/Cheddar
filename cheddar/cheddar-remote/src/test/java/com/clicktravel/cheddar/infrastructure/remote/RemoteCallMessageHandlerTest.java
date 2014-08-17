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

import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.clicktravel.cheddar.infrastructure.messaging.Message;
import com.clicktravel.cheddar.infrastructure.remote.RemoteCall;
import com.clicktravel.cheddar.infrastructure.remote.RemoteCallHandler;
import com.clicktravel.cheddar.infrastructure.remote.RemoteCallMessageHandler;
import com.clicktravel.common.random.Randoms;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RemoteCall.class)
public class RemoteCallMessageHandlerTest {

    @Test
    public void shouldHandleRemoteCall_withMessage() throws Exception {
        // Given
        RemoteCallHandler mockRemoteCallHandler = mock(RemoteCallHandler.class);
        final RemoteCallMessageHandler remoteCallMessageHandler = new RemoteCallMessageHandler(mockRemoteCallHandler);
        final Message message = mock(Message.class);
        String payload = Randoms.randomString();
        when(message.getPayload()).thenReturn(payload);
        mockStatic(RemoteCall.class);
        RemoteCall mockRemoteCall = mock(RemoteCall.class);
        when(RemoteCall.deserialize(payload)).thenReturn(mockRemoteCall);

        // When
        remoteCallMessageHandler.handle(message);

        // Then
        verify(mockRemoteCallHandler).handle(mockRemoteCall);
    }
}
