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
