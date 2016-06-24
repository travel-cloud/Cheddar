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
package com.clicktravel.cheddar.application.retry;

import static com.clicktravel.common.random.Randoms.randomIntInRange;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.joda.time.DateTime;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class RetryableAspectTest {

    @Test
    public void shouldConstructRetryableAspect() {
        // Given
        RetryableAspect retryableAspect = null;

        // When
        retryableAspect = new RetryableAspect();

        // Then
        assertNotNull(retryableAspect);
    }

    @Test
    public void shouldAttemptMethodAndNotRetry_withExceptionThrownAndRetryDisabled() throws Throwable {
        // Given
        final RetryableAspect retryableAspect = new RetryableAspect();
        final ProceedingJoinPoint mockProceedingJoinPoint = setupSimpleProceedingJoinPointMock();
        final Retryable mockRetryable = setupSimpleRetryableMock();

        RetryableConfiguration.setRetryableEnabled(false);
        when(mockProceedingJoinPoint.proceed()).thenThrow(new RetryAspectTestException());

        // When
        RetryAspectTestException actualException = null;
        try {
            retryableAspect.attemptMethodAndRetryIfNeeded(mockProceedingJoinPoint, mockRetryable);
        } catch (final RetryAspectTestException e) {
            actualException = e;
        }

        // Then
        verify(mockProceedingJoinPoint).proceed();

        assertNotNull(actualException);
    }

    @Test
    public void shouldAttemptMethodAndNotRetry_withNoExceptionThrownAndRetryEnabled() throws Throwable {
        // Given
        final RetryableAspect retryableAspect = new RetryableAspect();
        final ProceedingJoinPoint mockProceedingJoinPoint = setupSimpleProceedingJoinPointMock();
        final Retryable mockRetryable = setupSimpleRetryableMock();

        RetryableConfiguration.setRetryableEnabled(true);

        // When
        Exception actualException = null;
        try {
            retryableAspect.attemptMethodAndRetryIfNeeded(mockProceedingJoinPoint, mockRetryable);
        } catch (final RetryAspectTestException e) {
            actualException = e;
        }

        // Then
        verify(mockProceedingJoinPoint).proceed();

        assertNull(actualException);
    }

    @Test
    public void shouldAttemptMethodAndRetry_withExceptionThrownAndRetryEnabledAndMaxAttemptsAdjusted()
            throws Throwable {
        // Given
        final RetryableAspect retryableAspect = new RetryableAspect();
        final ProceedingJoinPoint mockProceedingJoinPoint = setupSimpleProceedingJoinPointMock();
        final int maxAttempts = randomIntInRange(2, 4);
        final Retryable mockRetryable = setupMockRetryable(maxAttempts, 2000);

        RetryableConfiguration.setRetryableEnabled(true);
        when(mockProceedingJoinPoint.proceed()).thenThrow(new RetryAspectTestException());

        // When
        RetryAspectTestException actualException = null;
        try {
            retryableAspect.attemptMethodAndRetryIfNeeded(mockProceedingJoinPoint, mockRetryable);
        } catch (final RetryAspectTestException e) {
            actualException = e;
        }

        // Then
        verify(mockProceedingJoinPoint, times(maxAttempts)).proceed();

        assertNotNull(actualException);
    }

    @Test
    public void shouldAttemptMethodAndFailImmedaitelyOnException_withImmediateFailureExceptionClassThrownAndRetryEnabled()
            throws Throwable {
        // Given
        final RetryableAspect retryableAspect = new RetryableAspect();
        final ProceedingJoinPoint mockProceedingJoinPoint = setupSimpleProceedingJoinPointMock();
        final int maxAttempts = 3;
        final Retryable mockRetryable = setupMockRetryable(maxAttempts, 2000);

        RetryableConfiguration.setRetryableEnabled(true);
        when(mockRetryable.failImmediatelyOn()).thenReturn(new Class[] { RetryAspectTestException.class });
        when(mockProceedingJoinPoint.proceed()).thenThrow(new RetryAspectTestException());

        // When
        RetryAspectTestException actualException = null;
        try {
            retryableAspect.attemptMethodAndRetryIfNeeded(mockProceedingJoinPoint, mockRetryable);
        } catch (final RetryAspectTestException e) {
            actualException = e;
        }

        // Then
        verify(mockProceedingJoinPoint, atMost(1)).proceed();

        assertNotNull(actualException);
    }

    @Test
    public void shouldAttemptMethodAndRetry_withExceptionThrownAndRetryEnabledAndDelayAdjusted() throws Throwable {
        // Given
        final RetryableAspect retryableAspect = new RetryableAspect();
        final ProceedingJoinPoint mockProceedingJoinPoint = setupSimpleProceedingJoinPointMock();
        final long EPSILON_MS = 300; // test timing tolerance
        final int retryDelayInMillis = 400;
        final int maxRetries = randomIntInRange(2, 6);
        final int expectedRuntime = retryDelayInMillis * (maxRetries - 1);
        final Retryable mockRetryable = setupMockRetryable(maxRetries, retryDelayInMillis);

        RetryableConfiguration.setRetryableEnabled(true);
        when(mockProceedingJoinPoint.proceed()).thenThrow(new RetryAspectTestException());

        // When
        DateTime startDateTime = null;
        RetryAspectTestException actualException = null;
        try {
            startDateTime = DateTime.now();
            retryableAspect.attemptMethodAndRetryIfNeeded(mockProceedingJoinPoint, mockRetryable);
        } catch (final RetryAspectTestException e) {
            actualException = e;
        }

        // Then
        verify(mockProceedingJoinPoint, times(maxRetries)).proceed();

        assertNotNull(actualException);
        final long elapsedMillis = DateTime.now().getMillis() - startDateTime.getMillis();
        assertTrue(Math.abs(elapsedMillis - expectedRuntime) < EPSILON_MS);
    }

    @Test
    public void shouldAttemptMethodAndCallExceptionHandler_withExceptionThrownAndExceptionHandlerNamed()
            throws Throwable {
        // Given
        final RetryableAspect retryableAspect = new RetryableAspect();
        final ProceedingJoinPoint mockProceedingJoinPoint = setupSimpleProceedingJoinPointMock();
        final Retryable mockRetryable = setupSimpleRetryableMock();
        final RetryExceptionHandler retryExceptionHandler = new RetryExceptionHandler();
        final MethodSignature mockMethodSignature = mock(MethodSignature.class);
        final String exceptionHandlerName = "joinPointMethodExceptionHandlingMethod";

        RetryableConfiguration.setRetryableEnabled(false);
        when(mockProceedingJoinPoint.proceed()).thenThrow(new RetryAspectTestException());
        when(mockProceedingJoinPoint.getThis()).thenReturn(retryExceptionHandler);
        when(mockProceedingJoinPoint.getSignature()).thenReturn(mockMethodSignature);
        when(mockProceedingJoinPoint.getArgs()).thenReturn(new Object[] { "exampleArgument" });
        when(mockMethodSignature.getParameterTypes()).thenReturn(
                RetryExceptionHandler.class.getDeclaredMethod("joinPointMethod", String.class).getParameterTypes());
        when(mockRetryable.exceptionHandlers()).thenReturn(new String[] { exceptionHandlerName });

        // When
        retryableAspect.attemptMethodAndRetryIfNeeded(mockProceedingJoinPoint, mockRetryable);

        // Then
        verify(mockProceedingJoinPoint).proceed();

        assertTrue(retryExceptionHandler.exceptionHandlerBeenCalled);
    }

    private Retryable setupMockRetryable(final int maxAttempts, final int retryDelayInMillis) {
        final Retryable mockRetryable = mock(Retryable.class);
        when(mockRetryable.maxAttempts()).thenReturn(maxAttempts);
        when(mockRetryable.failImmediatelyOn()).thenReturn(new Class[0]);
        when(mockRetryable.exceptionHandlers()).thenReturn(new String[0]);
        when(mockRetryable.retryDelayMillis()).thenReturn(retryDelayInMillis);
        return mockRetryable;
    }

    private Retryable setupSimpleRetryableMock() {
        return setupMockRetryable(5, 2000);
    }

    private ProceedingJoinPoint setupSimpleProceedingJoinPointMock() {
        final ProceedingJoinPoint mockProceedingJoinPoint = mock(ProceedingJoinPoint.class);
        final MethodSignature mockMethodSignature = mock(MethodSignature.class);

        when(mockProceedingJoinPoint.getThis()).thenReturn(mockProceedingJoinPoint);
        when(mockProceedingJoinPoint.getSignature()).thenReturn(mockMethodSignature);
        when(mockMethodSignature.getParameterTypes()).thenReturn(new Class[0]);
        return mockProceedingJoinPoint;
    }

    private class RetryAspectTestException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    @SuppressWarnings("unused")
    private class RetryExceptionHandler {

        private boolean exceptionHandlerBeenCalled = false;

        public void joinPointMethod(final String firstParameter) {

        }

        public void joinPointMethodExceptionHandlingMethod(final RetryAspectTestException retryAspectTestException,
                final String firstParameter) {
            exceptionHandlerBeenCalled = true;
        }

    }

}
