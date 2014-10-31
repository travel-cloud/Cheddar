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
