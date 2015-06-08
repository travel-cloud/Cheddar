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

import static com.clicktravel.common.random.Randoms.randomId;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

public class PendingResultsHolderTest {

    private PendingResultsHolder pendingResultsHolder;

    @Before
    public void setUp() {
        pendingResultsHolder = new PendingResultsHolder();
    }

    @Test
    public void shouldReturnId_onCreate() {
        // When
        final String id = pendingResultsHolder.create();

        // Then
        assertNotNull(id);
    }

    @Test
    public void shouldReturnPendingResult_onCreateThenGet() {
        // Given
        final String id = pendingResultsHolder.create();

        // When
        final PendingResult pendingResult = pendingResultsHolder.get(id);

        // Then
        assertNotNull(pendingResult);
    }

    @Test
    public void shouldNotReturnPendingResult_withUnknownId() {
        // Given
        final String id = randomId();

        // When
        final PendingResult pendingResult = pendingResultsHolder.get(id);

        // Then
        assertNull(pendingResult);
    }

    @Test
    public void shouldNotReturnPendingResult_afterRemove() {
        // Given
        final String id = pendingResultsHolder.create();

        // When
        pendingResultsHolder.remove(id);

        // Then
        final PendingResult pendingResult = pendingResultsHolder.get(id);
        assertNull(pendingResult);
    }
}
