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

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.ListableBeanFactory;

import com.clicktravel.cheddar.infrastructure.persistence.database.exception.PersistenceResourceFailureException;
import com.clicktravel.cheddar.request.context.SecurityContextHolder;
import com.clicktravel.common.random.Randoms;
import com.clicktravel.common.validation.ValidationException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SecurityContextHolder.class, DefaultTestService.class })
public class RemoteCallHandlerTest {

    private ListableBeanFactory mockListableBeanFactory;
    private RemoteCallSender mockRemoteCallSender;
    private RemoteResponseSender mockRemoteResponseSender;
    private RemoteCallHandler remoteCallHandler;
    private RemoteCall mockRemoteCall;
    private DefaultTestService defaultTestService;
    private String callId;
    private int attemptsRemaining;
    private String principal;
    private boolean tag;
    private RemoteCallContextHolder mockRemoteCallContextHolder;
    private TaggedRemoteCallStatusHolderImpl mockTaggedRemoteCallStatusHolder;

    @Before
    public void setUp() {
        mockListableBeanFactory = mock(ListableBeanFactory.class);
        mockRemoteCallSender = mock(RemoteCallSender.class);
        mockRemoteResponseSender = mock(RemoteResponseSender.class);
        mockRemoteCallContextHolder = mock(RemoteCallContextHolder.class);
        mockTaggedRemoteCallStatusHolder = mock(TaggedRemoteCallStatusHolderImpl.class);
        remoteCallHandler = new RemoteCallHandler(mockListableBeanFactory, mockRemoteCallSender,
                mockRemoteResponseSender, mockRemoteCallContextHolder, mockTaggedRemoteCallStatusHolder);
        final Map<String, TestService> beanMap = new HashMap<>();
        defaultTestService = new DefaultTestService();
        beanMap.put("defaultTestService", defaultTestService);
        when(mockListableBeanFactory.getBeansOfType(TestService.class)).thenReturn(beanMap);
        mockRemoteCall = mock(RemoteCall.class);
        callId = Randoms.randomId();
        principal = Randoms.randomString();
        when(mockRemoteCall.getCallId()).thenReturn(callId);
        when(mockRemoteCall.getPrincipal()).thenReturn(principal);
        when(mockRemoteCall.getAttemptsRemaining()).then(new Answer<Integer>() {
            @Override
            public Integer answer(final InvocationOnMock invocation) throws Throwable {
                return attemptsRemaining;
            }
        });
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                if (attemptsRemaining > 0) {
                    attemptsRemaining--;
                }
                return null;
            }
        }).when(mockRemoteCall).decrementAttemptsRemaining();
        mockStatic(SecurityContextHolder.class);
        tag = Randoms.randomBoolean();
    }

    @Test
    public void shouldAttemptCallAndNotSendResponse_withAsyncRemoteCallAndSuccess() {
        // Given
        attemptsRemaining = 2 + Randoms.randomInt(5);
        setUpMethod1RemoteCall();

        // When
        remoteCallHandler.handle(mockRemoteCall);

        // Then
        assertTrue(defaultTestService.isMethod1Called());
        PowerMockito.verifyStatic();
        SecurityContextHolder.setPrincipal(principal);
        PowerMockito.verifyStatic();
        SecurityContextHolder.clearPrincipal();
        verify(mockRemoteResponseSender, never()).sendRemoteResponse(any(RemoteResponse.class));
    }

    @Test
    public void shouldNotCallTaggedRemoteCallStatusHolder_withUntaggedRemoteCall() {
        // Given
        attemptsRemaining = 2 + Randoms.randomInt(5);
        tag = false;
        setUpMethod1RemoteCall();

        // When
        remoteCallHandler.handle(mockRemoteCall);

        // Then
        verifyZeroInteractions(mockTaggedRemoteCallStatusHolder);
    }

    @Test
    public void shouldCallTaggedRemoteCallStatusHolder_withTaggedRemoteCall() {
        // Given
        attemptsRemaining = 2 + Randoms.randomInt(5);
        tag = true;
        setUpMethod1RemoteCall();

        // When
        remoteCallHandler.handle(mockRemoteCall);

        // Then
        verify(mockTaggedRemoteCallStatusHolder).taggedRemoteCallStarted();
        verify(mockTaggedRemoteCallStatusHolder).taggedRemoteCallCompleted();
        verifyNoMoreInteractions(mockTaggedRemoteCallStatusHolder);
    }

    @Test
    public void shouldAttemptCallAndSendResponse_withSyncRemoteCallAndSuccess() {
        // Given
        attemptsRemaining = 2 + Randoms.randomInt(5);
        setUpMethod2RemoteCall();
        final String[] method2ReturnValue = new String[] { Randoms.randomString() };
        defaultTestService.setMethod2ReturnValue(method2ReturnValue);

        // When
        remoteCallHandler.handle(mockRemoteCall);

        // Then
        assertTrue(defaultTestService.isMethod2Called());
        PowerMockito.verifyStatic();
        SecurityContextHolder.setPrincipal(principal);
        PowerMockito.verifyStatic();
        SecurityContextHolder.clearPrincipal();
        final ArgumentCaptor<RemoteResponse> remoteResponseCaptor = ArgumentCaptor.forClass(RemoteResponse.class);
        verify(mockRemoteResponseSender).sendRemoteResponse(remoteResponseCaptor.capture());
        final RemoteResponse remoteResponse = remoteResponseCaptor.getValue();
        assertEquals(callId, remoteResponse.getCallId());
        assertSame(method2ReturnValue, remoteResponse.getReturnValue());
        assertNull(remoteResponse.getThrownException());
    }

    @Test
    public void shouldAttemptCallAndNotSendResponse_withSuspendedSyncRemoteCallAndSuccess() {
        // Given
        attemptsRemaining = 2 + Randoms.randomInt(5);
        setUpMethod2RemoteCall();
        final String[] method2ReturnValue = new String[] { Randoms.randomString() };
        defaultTestService.setMethod2ReturnValue(method2ReturnValue);
        when(mockRemoteCallContextHolder.isResponseSuspended()).thenReturn(true);

        // When
        remoteCallHandler.handle(mockRemoteCall);

        // Then
        assertTrue(defaultTestService.isMethod2Called());
        PowerMockito.verifyStatic();
        SecurityContextHolder.setPrincipal(principal);
        PowerMockito.verifyStatic();
        SecurityContextHolder.clearPrincipal();
        verify(mockRemoteResponseSender, never()).sendRemoteResponse(any(RemoteResponse.class));
    }

    @Test
    public void shouldAttemptCallAndResend_withRemoteCallAndAttemptFailure() {
        // Given
        attemptsRemaining = 2 + Randoms.randomInt(5);
        setUpMethod1RemoteCall();
        defaultTestService.setExceptionToThrow(new RuntimeException());

        // When
        remoteCallHandler.handle(mockRemoteCall);

        // Then
        PowerMockito.verifyStatic();
        SecurityContextHolder.setPrincipal(principal);
        PowerMockito.verifyStatic();
        SecurityContextHolder.clearPrincipal();
        verify(mockRemoteCall).decrementAttemptsRemaining();
        verify(mockRemoteCallSender).sendDelayedRemoteCall(eq(mockRemoteCall), anyInt());
        verify(mockRemoteResponseSender, never()).sendRemoteResponse(any(RemoteResponse.class));
    }

    @Test
    public void shouldAttemptCallAndReturnException_withSyncRemoteCallAndValidationException() {
        // Given
        attemptsRemaining = 2 + Randoms.randomInt(5);
        setUpMethod2RemoteCall();
        final ValidationException thrownException = new ValidationException("message", "field");
        defaultTestService.setExceptionToThrow(thrownException);

        // When
        remoteCallHandler.handle(mockRemoteCall);

        // Then
        PowerMockito.verifyStatic();
        SecurityContextHolder.setPrincipal(principal);
        PowerMockito.verifyStatic();
        SecurityContextHolder.clearPrincipal();
        final ArgumentCaptor<RemoteResponse> remoteResponseCaptor = ArgumentCaptor.forClass(RemoteResponse.class);
        verify(mockRemoteResponseSender).sendRemoteResponse(remoteResponseCaptor.capture());
        final RemoteResponse remoteResponse = remoteResponseCaptor.getValue();
        assertEquals(callId, remoteResponse.getCallId());
        assertNull(remoteResponse.getReturnValue());
        assertSame(thrownException, remoteResponse.getThrownException());
    }

    @Test
    public void shouldAttemptCallAndReturnException_withSyncRemoteCallAndPersistenceResourceFailureException() {
        // Given
        attemptsRemaining = 2 + Randoms.randomInt(5);
        setUpMethod2RemoteCall();
        final PersistenceResourceFailureException thrownException = mock(PersistenceResourceFailureException.class);
        defaultTestService.setExceptionToThrow(thrownException);

        // When
        remoteCallHandler.handle(mockRemoteCall);

        // Then
        PowerMockito.verifyStatic();
        SecurityContextHolder.setPrincipal(principal);
        PowerMockito.verifyStatic();
        SecurityContextHolder.clearPrincipal();
        final ArgumentCaptor<RemoteResponse> remoteResponseCaptor = ArgumentCaptor.forClass(RemoteResponse.class);
        verify(mockRemoteResponseSender).sendRemoteResponse(remoteResponseCaptor.capture());
        final RemoteResponse remoteResponse = remoteResponseCaptor.getValue();
        assertEquals(callId, remoteResponse.getCallId());
        assertNull(remoteResponse.getReturnValue());
        assertSame(thrownException, remoteResponse.getThrownException());
    }

    @Test
    public void shouldAttemptCallAndReturnException_withSyncRemoteCallAndImmediateFailException() {
        // Given
        attemptsRemaining = 2 + Randoms.randomInt(5);
        setUpMethod2RemoteCall();
        final TestDerivedException thrownException = new TestDerivedException();
        defaultTestService.setExceptionToThrow(thrownException);

        // When
        remoteCallHandler.handle(mockRemoteCall);

        // Then
        PowerMockito.verifyStatic();
        SecurityContextHolder.setPrincipal(principal);
        PowerMockito.verifyStatic();
        SecurityContextHolder.clearPrincipal();
        final ArgumentCaptor<RemoteResponse> remoteResponseCaptor = ArgumentCaptor.forClass(RemoteResponse.class);
        verify(mockRemoteResponseSender).sendRemoteResponse(remoteResponseCaptor.capture());
        final RemoteResponse remoteResponse = remoteResponseCaptor.getValue();
        assertEquals(callId, remoteResponse.getCallId());
        assertNull(remoteResponse.getReturnValue());
        assertSame(thrownException, remoteResponse.getThrownException());
    }

    @Test
    public void shouldAttemptCallAndReturnException_withSyncRemoteCallAndLastFailure() {
        // Given
        attemptsRemaining = 1;
        setUpMethod2RemoteCall();
        final RuntimeException thrownException = new RuntimeException();
        defaultTestService.setExceptionToThrow(thrownException);

        // When
        remoteCallHandler.handle(mockRemoteCall);

        // Then
        PowerMockito.verifyStatic();
        SecurityContextHolder.setPrincipal(principal);
        PowerMockito.verifyStatic();
        SecurityContextHolder.clearPrincipal();
        final ArgumentCaptor<RemoteResponse> remoteResponseCaptor = ArgumentCaptor.forClass(RemoteResponse.class);
        verify(mockRemoteResponseSender).sendRemoteResponse(remoteResponseCaptor.capture());
        final RemoteResponse remoteResponse = remoteResponseCaptor.getValue();
        assertEquals(callId, remoteResponse.getCallId());
        assertNull(remoteResponse.getReturnValue());
        assertSame(thrownException, remoteResponse.getThrownException());
    }

    @Test
    public void shouldAttemptCallAndReturnException_withSyncRemoteCallAndLastFailureAndNoExceptionHandler() {
        // Given
        attemptsRemaining = 1;
        setUpMethod1RemoteCall();
        final RuntimeException thrownException = new RuntimeException();
        defaultTestService.setExceptionToThrow(thrownException);

        // When
        remoteCallHandler.handle(mockRemoteCall);

        // Then
        PowerMockito.verifyStatic();
        SecurityContextHolder.setPrincipal(principal);
        PowerMockito.verifyStatic();
        SecurityContextHolder.clearPrincipal();
        assertTrue(defaultTestService.isMethod1Called());
        final ArgumentCaptor<RemoteResponse> remoteResponseCaptor = ArgumentCaptor.forClass(RemoteResponse.class);
        verify(mockRemoteResponseSender).sendRemoteResponse(remoteResponseCaptor.capture());
        final RemoteResponse remoteResponse = remoteResponseCaptor.getValue();
        assertEquals(callId, remoteResponse.getCallId());
        assertNull(remoteResponse.getReturnValue());
        assertSame(thrownException, remoteResponse.getThrownException());
    }

    @Test
    public void shouldAttemptCallAndReturnNoException_withSyncRemoteCallAndLastFailureAndExceptionHandler() {
        // Given
        attemptsRemaining = 1;
        setUpMethod3RemoteCall();
        final RuntimeException thrownException = new RuntimeException();
        defaultTestService.setExceptionToThrow(thrownException);
        final Map<String, TestService> beanMap = new HashMap<>();
        beanMap.put("defaultTestService", defaultTestService);
        when(mockListableBeanFactory.getBeansOfType(TestService.class)).thenReturn(beanMap);

        // When
        remoteCallHandler.handle(mockRemoteCall);

        // Then
        PowerMockito.verifyStatic();
        SecurityContextHolder.setPrincipal(principal);
        PowerMockito.verifyStatic();
        SecurityContextHolder.clearPrincipal();
        assertTrue(defaultTestService.isMethod3Called());
        assertTrue(defaultTestService.isMethod4Called());
        final ArgumentCaptor<RemoteResponse> remoteResponseCaptor = ArgumentCaptor.forClass(RemoteResponse.class);
        verify(mockRemoteResponseSender).sendRemoteResponse(remoteResponseCaptor.capture());
        final RemoteResponse remoteResponse = remoteResponseCaptor.getValue();
        assertEquals(callId, remoteResponse.getCallId());
        assertNull(remoteResponse.getReturnValue());
        assertNull(remoteResponse.getThrownException());
    }

    @Test
    public void shouldAttemptCallAndReturnException_withSyncRemoteCallAndLastFailureAndExceptionHandlerThrowingException() {
        // Given
        attemptsRemaining = 1;
        setUpMethod3RemoteCall();
        final RuntimeException thrownException = new RuntimeException();
        final RuntimeException exceptionToThrowOnExceptionHandle = new RuntimeException();
        defaultTestService.setExceptionToThrow(thrownException);
        defaultTestService.setExceptionToThrowOnExceptionHandle(exceptionToThrowOnExceptionHandle);
        final Map<String, TestService> beanMap = new HashMap<>();
        beanMap.put("defaultTestService", defaultTestService);
        when(mockListableBeanFactory.getBeansOfType(TestService.class)).thenReturn(beanMap);

        // When
        remoteCallHandler.handle(mockRemoteCall);

        // Then
        PowerMockito.verifyStatic();
        SecurityContextHolder.setPrincipal(principal);
        PowerMockito.verifyStatic();
        SecurityContextHolder.clearPrincipal();
        assertTrue(defaultTestService.isMethod3Called());
        assertTrue(defaultTestService.isMethod4Called());
        final ArgumentCaptor<RemoteResponse> remoteResponseCaptor = ArgumentCaptor.forClass(RemoteResponse.class);
        verify(mockRemoteResponseSender).sendRemoteResponse(remoteResponseCaptor.capture());
        final RemoteResponse remoteResponse = remoteResponseCaptor.getValue();
        assertEquals(callId, remoteResponse.getCallId());
        assertNull(remoteResponse.getReturnValue());
        assertSame(exceptionToThrowOnExceptionHandle, remoteResponse.getThrownException());
    }

    @Test
    public void shouldNotAttemptCall_withUnknownInterface() {
        // Given
        when(mockRemoteCall.getInterfaceName()).thenReturn("org.Unknown");

        // When
        RemotingException actualException = null;
        try {
            remoteCallHandler.handle(mockRemoteCall);
        } catch (final RemotingException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotAttemptCall_withUnknownMethod() {
        // Given
        when(mockRemoteCall.getInterfaceName()).thenReturn(TestService.class.getName());
        when(mockRemoteCall.getMethodName()).thenReturn("unknown");
        when(mockRemoteCall.getMethodParameterTypes()).thenReturn(new String[] {});

        // When
        RemotingException actualException = null;
        try {
            remoteCallHandler.handle(mockRemoteCall);
        } catch (final RemotingException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotAttemptCall_withParameterTypeMismatch() {
        // Given
        final String parameter1 = Randoms.randomString();
        final String incorectParameter2 = Randoms.randomString();
        when(mockRemoteCall.getInterfaceName()).thenReturn(TestService.class.getName());
        when(mockRemoteCall.getMethodName()).thenReturn("method1");
        when(mockRemoteCall.getMethodParameterTypes()).thenReturn(
                new String[] { String.class.getName(), int.class.getName() });
        when(mockRemoteCall.getParameters()).thenReturn(new Object[] { parameter1, incorectParameter2 });
        when(mockRemoteCallContextHolder.isResponseSuspended()).thenReturn(false);

        // When
        RemotingException actualException = null;
        try {
            remoteCallHandler.handle(mockRemoteCall);
        } catch (final RemotingException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    private void setUpMethod1RemoteCall() {
        final String parameter1 = Randoms.randomString();
        final int parameter2 = Randoms.randomInt(100);
        when(mockRemoteCall.getInterfaceName()).thenReturn(TestService.class.getName());
        when(mockRemoteCall.getMethodName()).thenReturn("method1");
        when(mockRemoteCall.getMethodParameterTypes()).thenReturn(
                new String[] { String.class.getName(), int.class.getName() });
        when(mockRemoteCall.getParameters()).thenReturn(new Object[] { parameter1, parameter2 });
        when(mockRemoteCall.hasTag()).thenReturn(tag);
        when(mockRemoteCallContextHolder.isResponseSuspended()).thenReturn(false);
    }

    private void setUpMethod2RemoteCall() {
        final TestObject testObject = new TestObject(Randoms.randomString(), Randoms.randomString());
        when(mockRemoteCall.getInterfaceName()).thenReturn(TestService.class.getName());
        when(mockRemoteCall.getMethodName()).thenReturn("method2");
        when(mockRemoteCall.getMethodParameterTypes()).thenReturn(new String[] { TestObject.class.getName() });
        when(mockRemoteCall.getParameters()).thenReturn(new Object[] { testObject });
        when(mockRemoteCall.hasTag()).thenReturn(tag);
        when(mockRemoteCallContextHolder.isResponseSuspended()).thenReturn(false);
    }

    private void setUpMethod3RemoteCall() {
        final TestObject testObject = new TestObject(Randoms.randomString(), Randoms.randomString());
        when(mockRemoteCall.getInterfaceName()).thenReturn(TestService.class.getName());
        when(mockRemoteCall.getMethodName()).thenReturn("method3");
        when(mockRemoteCall.getMethodParameterTypes()).thenReturn(new String[] { TestObject.class.getName() });
        when(mockRemoteCall.getParameters()).thenReturn(new Object[] { testObject });
        when(mockRemoteCall.hasTag()).thenReturn(tag);
        when(mockRemoteCallContextHolder.isResponseSuspended()).thenReturn(false);
    }
}
