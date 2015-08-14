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
package com.clicktravel.cheddar.application.pending.result;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class PendingResultTest {

    @Test
    public void shouldReturnResult_whenPollAndOffer() throws Exception {
        // Given
        final Result mockResult = mock(Result.class);
        final PendingResult pendingResult = new PendingResult();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                pendingResult.offerResult(mockResult);
            }
        };
        Executors.newSingleThreadScheduledExecutor().schedule(runnable, 200, TimeUnit.MILLISECONDS);

        // When
        final Result actualResult = pendingResult.pollResult();

        // Then
        assertSame(mockResult, actualResult);
    }
}
