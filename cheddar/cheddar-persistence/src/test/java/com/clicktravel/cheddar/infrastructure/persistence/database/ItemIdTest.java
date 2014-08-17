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
package com.clicktravel.cheddar.infrastructure.persistence.database;

import static com.clicktravel.common.random.Randoms.randomId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ItemIdTest {

    @Test
    public void shouldCreateItemId_withId() throws Exception {
        // Given
        final String itemIdStr = randomId();

        // When
        final ItemId itemId = new ItemId(itemIdStr);

        // Then
        assertNotNull(itemId);
        assertEquals(itemIdStr, itemId.value());
    }

    @Test
    public void shouldNotCreateItemId_withNullId() throws Exception {
        // Given
        final String itemIdStr = null;

        // When
        IllegalArgumentException actualException = null;
        try {
            new ItemId(itemIdStr);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldReturnEqualItemId_withSameValue() throws Exception {
        // Given
        final String itemIdStr = randomId();
        final ItemId itemId1 = new ItemId(itemIdStr);
        final ItemId itemId2 = new ItemId(itemIdStr);

        // When
        final boolean itemId1Equals2 = itemId1.equals(itemId2);

        // Then
        assertTrue(itemId1Equals2);

        // When
        final boolean itemId2Equals1 = itemId2.equals(itemId1);

        // Then
        assertTrue(itemId2Equals1);
    }

}
