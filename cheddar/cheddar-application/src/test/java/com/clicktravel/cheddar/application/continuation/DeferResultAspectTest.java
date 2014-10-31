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
package com.clicktravel.cheddar.application.continuation;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.mockito.InOrder;

public class DeferResultAspectTest {

    @Test
    public void shouldReturnContinueValue_whenInvokingDeferMethod() throws Throwable {
        // Given
        final Object expectedMethodReturnValue = new Object();
        final ContinuationHandler mockContinuationHandler = mock(ContinuationHandler.class);
        when(mockContinuationHandler.pollForMethodReturnValue()).thenReturn(expectedMethodReturnValue);
        final DeferResultAspect deferResultAspect = new DeferResultAspect(mockContinuationHandler);
        final ProceedingJoinPoint mockProceedingJoinPoint = mock(ProceedingJoinPoint.class);
        final InOrder inOrder = inOrder(mockContinuationHandler, mockProceedingJoinPoint);

        // When
        final Object actualReturnValue = deferResultAspect.invokeAndDeferResult(mockProceedingJoinPoint);

        // Then
        inOrder.verify(mockContinuationHandler).createContinuation();
        inOrder.verify(mockProceedingJoinPoint).proceed();
        inOrder.verify(mockContinuationHandler).pollForMethodReturnValue();
        assertSame(expectedMethodReturnValue, actualReturnValue);
    }
}
