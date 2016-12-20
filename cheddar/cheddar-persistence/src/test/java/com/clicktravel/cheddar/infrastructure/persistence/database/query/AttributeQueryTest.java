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
package com.clicktravel.cheddar.infrastructure.persistence.database.query;

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.persistence.database.StubItem;

public class AttributeQueryTest {

    @Test
    public void shouldGetAttributeType_withItemClass() {
        // Given
        final String attributeName = "id";
        final Condition mockCondition = mock(Condition.class);
        final AttributeQuery attributeQuery = new AttributeQuery(attributeName, mockCondition);

        // When
        final Class<?> attributeType = attributeQuery.getAttributeType(StubItem.class);

        // Then
        assertEquals(String.class, attributeType);
    }

    @Test
    public void shouldNotGetAttributeType_withAttributeNotInItemClass() {
        // Given
        final String attributeName = randomString(10);
        final Condition mockCondition = mock(Condition.class);
        final AttributeQuery attributeQuery = new AttributeQuery(attributeName, mockCondition);

        // When
        NonExistentAttributeException expectedException = null;
        try {
            attributeQuery.getAttributeType(StubItem.class);
        } catch (final NonExistentAttributeException e) {
            expectedException = e;
        }

        // Then
        assertNotNull(expectedException);
        assertTrue(expectedException.getMessage().contains(attributeName));
        assertTrue(expectedException.getMessage().contains(StubItem.class.getCanonicalName()));
    }
}
