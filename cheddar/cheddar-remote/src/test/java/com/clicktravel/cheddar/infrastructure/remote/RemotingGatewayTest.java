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

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.clicktravel.common.random.Randoms;
import com.clicktravel.cheddar.infrastructure.remote.RemoteCall;
import com.clicktravel.cheddar.infrastructure.remote.RemoteCallSender;
import com.clicktravel.cheddar.infrastructure.remote.RemoteResponse;
import com.clicktravel.cheddar.infrastructure.remote.RemotingGateway;

public class RemotingGatewayTest {

    private String callId;
    private RemoteCall mockRemoteCall;
    private RemoteCallSender mockRemoteCallSender;
    private RemotingGateway remotingGateway;
    private RemoteResponse mockRemoteResponse;

    @Before
    public void setUp() {
        // Given
        callId = Randoms.randomId();
        mockRemoteCall = mock(RemoteCall.class);
        when(mockRemoteCall.getCallId()).thenReturn(callId);
        mockRemoteCallSender = mock(RemoteCallSender.class);
        remotingGateway = new RemotingGateway(mockRemoteCallSender);
        mockRemoteResponse = mock(RemoteResponse.class);
        when(mockRemoteResponse.getCallId()).thenReturn(callId);
    }

    @Test
    public void shouldSendRemoteCall_withAsyncInvoke() {
        // When
        remotingGateway.invokeAsynchronouslyWithoutResponse(mockRemoteCall);

        // Then
        verify(mockRemoteCallSender).sendRemoteCall(mockRemoteCall);
    }

    @Test(timeout = 2000)
    public void shouldSendRemoteCallAndHandleResponse_withSyncInvokeAndReturnValueResponse() throws Throwable {
        Object returnValue = new Object();
        when(mockRemoteResponse.returnValue()).thenReturn(returnValue);

        // When
        new Thread() {
            @Override
            public void run() {
                remotingGateway.handle(mockRemoteResponse);
            }
        }.start();

        final Object actualReturnValue = remotingGateway.invokeSynchronously(mockRemoteCall);

        // Then
        verify(mockRemoteCallSender).sendRemoteCall(mockRemoteCall);
        assertSame(returnValue, actualReturnValue);
    }

    @Test(timeout = 2000)
    public void shouldSendRemoteCallAndHandleResponse_withSyncInvokeAndExceptionResponse() throws Throwable {
        Exception expectedException = new RuntimeException();
        when(mockRemoteResponse.returnValue()).thenThrow(expectedException);

        // When
        new Thread() {
            @Override
            public void run() {
                remotingGateway.handle(mockRemoteResponse);
            }
        }.start();

        Exception actualException = null;
        try {
            remotingGateway.invokeSynchronously(mockRemoteCall);
        } catch (Exception e) {
            actualException = e;
        }

        // Then
        verify(mockRemoteCallSender).sendRemoteCall(mockRemoteCall);
        assertSame(expectedException, actualException);
    }
}
