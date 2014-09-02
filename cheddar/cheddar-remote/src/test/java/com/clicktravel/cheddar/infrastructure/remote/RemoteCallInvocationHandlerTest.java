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

import static com.clicktravel.common.random.Randoms.randomInt;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class RemoteCallInvocationHandlerTest {

    private RemotingGateway mockRemotingGateway;
    private RemoteCallBuilder mockRemoteCallBuilder;
    private RemoteCall mockRemoteCall;

    @Before
    public void setUp() {
        mockRemotingGateway = mock(RemotingGateway.class);
        mockRemoteCallBuilder = mock(RemoteCallBuilder.class);
    }

    @Test
    public void shouldInvokeAsynchronously_whenInvokingAsynchronousMethod() throws Exception {
        // Given
        final String method1Parameter1 = randomString();
        final int method1Parameter2 = randomInt(100);
        final Method method = TestService.class.getMethod("method1", new Class<?>[] { String.class, int.class });
        final ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        when(mockRemoteCallBuilder.build(eq(TestService.class.getName()), eq(method), argsCaptor.capture()))
                .thenReturn(mockRemoteCall);
        final RemoteCallInvocationHandler remoteCallInvocationHandler = new RemoteCallInvocationHandler(
                mockRemotingGateway, mockRemoteCallBuilder);
        final TestService proxyTestService = remoteCallInvocationHandler.createProxy(TestService.class);

        // When
        proxyTestService.method1(method1Parameter1, method1Parameter2);

        // Then
        final Object[] actualArgs = argsCaptor.getValue();
        assertNotNull(actualArgs);
        assertEquals(2, actualArgs.length);
        assertEquals(method1Parameter1, actualArgs[0]);
        assertEquals(method1Parameter2, actualArgs[1]);
        verify(mockRemotingGateway).invokeAsynchronouslyWithoutResponse(same(mockRemoteCall));
    }

    @Test
    public void shouldInvokeSynchronously_whenInvokingSynchronousMethod() throws Throwable {
        // Given
        final TestObject testObject = new TestObject(randomString(), randomString());
        final String[] method2returnValue = new String[] { randomString() };
        final Method method = TestService.class.getMethod("method2", new Class<?>[] { TestObject.class });
        final ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        when(mockRemoteCallBuilder.build(eq(TestService.class.getName()), eq(method), argsCaptor.capture()))
                .thenReturn(mockRemoteCall);
        when(mockRemotingGateway.invokeSynchronously(mockRemoteCall)).thenReturn(method2returnValue);
        final RemoteCallInvocationHandler remoteCallInvocationHandler = new RemoteCallInvocationHandler(
                mockRemotingGateway, mockRemoteCallBuilder);
        final TestService proxyTestService = remoteCallInvocationHandler.createProxy(TestService.class);

        // When
        final String[] actualMethod2ReturnValue = proxyTestService.method2(testObject);

        // Then
        assertTrue(Arrays.equals(method2returnValue, actualMethod2ReturnValue));
        final Object[] actualArgs = argsCaptor.getValue();
        assertNotNull(actualArgs);
        assertEquals(1, actualArgs.length);
        assertSame(testObject, actualArgs[0]);
        verify(mockRemotingGateway).invokeSynchronously(mockRemoteCall);
    }
}
