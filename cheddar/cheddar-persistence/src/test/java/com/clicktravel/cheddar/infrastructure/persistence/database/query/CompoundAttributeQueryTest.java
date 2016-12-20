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
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.clicktravel.common.validation.ValidationException;

public class CompoundAttributeQueryTest {

    @Test
    public void canCreateCompoundAttributeQuery_withAttributeNameAndConditionAndSupportingAttributeNameAndSupportingCondition() {
        // Given
        final String attributeName = randomString(10);
        final Condition mockCondition = mock(Condition.class);
        final String supportingAttributeName = randomString(10);
        final Condition mockSupportingCondition = mock(Condition.class);

        // When
        final CompoundAttributeQuery compoundAttributeQuery = new CompoundAttributeQuery(attributeName, mockCondition,
                supportingAttributeName, mockSupportingCondition);

        // Then
        assertEquals(attributeName, compoundAttributeQuery.getAttributeName());
        assertEquals(mockCondition, compoundAttributeQuery.getCondition());
        assertEquals(supportingAttributeName, compoundAttributeQuery.getSupportingAttributeName());
        assertEquals(mockSupportingCondition, compoundAttributeQuery.getSupportingCondition());
    }

    @Test
    public void canNotCreateCompoundAttributeQuery_withEmptySupportingAttributeName() {
        // Given
        final String attributeName = randomString(10);
        final Condition mockCondition = mock(Condition.class);
        final String supportingAttributeName = "";
        final Condition mockSupportingCondition = mock(Condition.class);

        // When
        ValidationException expectedException = null;
        try {
            new CompoundAttributeQuery(attributeName, mockCondition, supportingAttributeName, mockSupportingCondition);
        } catch (final ValidationException e) {
            expectedException = e;
        }

        // Then
        assertNotNull(expectedException);
        assertEquals("supportingAttributeName", expectedException.getFields()[0]);
    }

    @Test
    public void canNotCreateCompoundAttributeQuery_withNullSupportingAttributeName() {
        // Given
        final String attributeName = randomString(10);
        final Condition mockCondition = mock(Condition.class);
        final String supportingAttributeName = null;
        final Condition mockSupportingCondition = mock(Condition.class);

        // When
        ValidationException expectedException = null;
        try {
            new CompoundAttributeQuery(attributeName, mockCondition, supportingAttributeName, mockSupportingCondition);
        } catch (final ValidationException e) {
            expectedException = e;
        }

        // Then
        assertNotNull(expectedException);
        assertEquals("supportingAttributeName", expectedException.getFields()[0]);
    }

    @Test
    public void canNotCreateCompoundAttributeQuery_withNullSupportingCondition() {
        // Given
        final String attributeName = randomString(10);
        final Condition mockCondition = mock(Condition.class);
        final String supportingAttributeName = randomString(10);
        final Condition mockSupportingCondition = null;

        // When
        ValidationException expectedException = null;
        try {
            new CompoundAttributeQuery(attributeName, mockCondition, supportingAttributeName, mockSupportingCondition);
        } catch (final ValidationException e) {
            expectedException = e;
        }

        // Then
        assertNotNull(expectedException);
        assertEquals("supportingCondition", expectedException.getFields()[0]);
    }
}
