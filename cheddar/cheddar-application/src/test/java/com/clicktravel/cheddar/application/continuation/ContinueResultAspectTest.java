package com.clicktravel.cheddar.application.continuation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class ContinueResultAspectTest {

    @Test
    public void shouldOfferMethodResult_onMethodReturn() {
        // Given
        final ContinuationHandler mockContinuationHandler = mock(ContinuationHandler.class);
        final ContinueResultAspect continueResultAspect = new ContinueResultAspect(mockContinuationHandler);

        // When
        continueResultAspect.returnMethodResult();

        // Then
        verify(mockContinuationHandler).offerMethodResult();
    }
}
