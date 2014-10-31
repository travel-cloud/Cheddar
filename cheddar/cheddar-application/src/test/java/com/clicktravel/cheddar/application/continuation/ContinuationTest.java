package com.clicktravel.cheddar.application.continuation;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class ContinuationTest {

    @Test
    public void shouldReturnMethodResult_whenPollAndOffer() {
        // Given
        final MethodResult mockMethodResult = mock(MethodResult.class);
        final Continuation continuation = new Continuation();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                continuation.setMethodResult(mockMethodResult);
                continuation.offerMethodResult();
            }
        };
        Executors.newSingleThreadScheduledExecutor().schedule(runnable, 200, TimeUnit.MILLISECONDS);

        // When
        final MethodResult actualResult = continuation.pollForMethodResult();

        // Then
        assertSame(mockMethodResult, actualResult);
    }
}
