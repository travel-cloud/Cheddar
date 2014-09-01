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

import static com.clicktravel.common.random.Randoms.randomBoolean;
import static com.clicktravel.common.random.Randoms.randomInt;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.clicktravel.cheddar.request.context.SecurityContextHolder;
import com.clicktravel.common.random.Randoms;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SecurityContextHolder.class)
public class RemoteCallInvocationHandlerTest {

    private RemotingGateway mockRemotingGateway;
    private String principal;
    private RemoteCallTagLogic mockRemoteCallTagLogic;

    @Before
    public void setUp() {
        mockRemotingGateway = mock(RemotingGateway.class);
        principal = Randoms.randomString();
        mockStatic(SecurityContextHolder.class);
        when(SecurityContextHolder.getPrincipal()).thenReturn(principal);
        mockRemoteCallTagLogic = mock(RemoteCallTagLogic.class);
    }

    @Test
    public void shouldInvokeAsynchronously_whenInvokingAsynchronousMethod() throws Exception {
        // Given
        final String method1Parameter1 = randomString();
        final int method1Parameter2 = randomInt(100);
        final RemoteCallInvocationHandler remoteCallInvocationHandler = new RemoteCallInvocationHandler(
                mockRemotingGateway, mockRemoteCallTagLogic);
        final boolean tag = randomBoolean();
        when(mockRemoteCallTagLogic.shouldTagRemoteCall()).thenReturn(tag);

        // When
        final TestService proxyTestService = remoteCallInvocationHandler.createProxy(TestService.class);
        proxyTestService.method1(method1Parameter1, method1Parameter2);

        // Then
        final ArgumentCaptor<RemoteCall> remoteCallCaptor = ArgumentCaptor.forClass(RemoteCall.class);
        verify(mockRemotingGateway).invokeAsynchronouslyWithoutResponse(remoteCallCaptor.capture());
        final RemoteCall actualRemoteCall = remoteCallCaptor.getValue();
        assertNotNull(actualRemoteCall.getCallId());
        assertFalse(actualRemoteCall.getCallId().isEmpty());
        assertEquals(TestService.class.getName(), actualRemoteCall.getInterfaceName());
        assertEquals("method1", actualRemoteCall.getMethodName());
        final String[] expectedMethodParameterTypes = new String[] { String.class.getName(), int.class.getName() };
        assertTrue(Arrays.equals(expectedMethodParameterTypes, actualRemoteCall.getMethodParameterTypes()));
        final Object[] expectedParameters = new Object[] { method1Parameter1, method1Parameter2 };
        assertTrue(Arrays.equals(expectedParameters, actualRemoteCall.getParameters()));
        assertTrue(actualRemoteCall.getAttemptsRemaining() > 0);
        assertEquals(principal, actualRemoteCall.getPrincipal());
        assertEquals(tag, actualRemoteCall.hasTag());
    }

    @Test
    public void shouldInvokeSynchronously_whenInvokingSynchronousMethod() throws Throwable {
        // Given
        final TestObject testObject = new TestObject(randomString(), randomString());
        final String[] method2returnValue = new String[] { randomString() };
        when(mockRemotingGateway.invokeSynchronously(any(RemoteCall.class))).thenReturn(method2returnValue);
        final RemoteCallInvocationHandler remoteCallInvocationHandler = new RemoteCallInvocationHandler(
                mockRemotingGateway, mockRemoteCallTagLogic);
        final boolean tag = randomBoolean();
        when(mockRemoteCallTagLogic.shouldTagRemoteCall()).thenReturn(tag);

        // When
        final TestService proxyTestService = remoteCallInvocationHandler.createProxy(TestService.class);
        final String[] actualMethod2ReturnValue = proxyTestService.method2(testObject);

        // Then
        assertTrue(Arrays.equals(method2returnValue, actualMethod2ReturnValue));
        final ArgumentCaptor<RemoteCall> remoteCallCaptor = ArgumentCaptor.forClass(RemoteCall.class);
        verify(mockRemotingGateway).invokeSynchronously(remoteCallCaptor.capture());
        final RemoteCall actualRemoteCall = remoteCallCaptor.getValue();
        assertNotNull(actualRemoteCall.getCallId());
        assertFalse(actualRemoteCall.getCallId().isEmpty());
        assertEquals(TestService.class.getName(), actualRemoteCall.getInterfaceName());
        assertEquals("method2", actualRemoteCall.getMethodName());
        final String[] expectedMethodParameterTypes = new String[] { TestObject.class.getName() };
        assertTrue(Arrays.equals(expectedMethodParameterTypes, actualRemoteCall.getMethodParameterTypes()));
        final Object[] expectedParameters = new Object[] { testObject };
        assertTrue(Arrays.equals(expectedParameters, actualRemoteCall.getParameters()));
        assertTrue(actualRemoteCall.getAttemptsRemaining() > 0);
        assertEquals(principal, actualRemoteCall.getPrincipal());
        assertEquals(tag, actualRemoteCall.hasTag());
    }
}
