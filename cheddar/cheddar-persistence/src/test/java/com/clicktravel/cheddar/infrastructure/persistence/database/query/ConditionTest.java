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

import static com.clicktravel.common.random.Randoms.randomBoolean;
import static com.clicktravel.common.random.Randoms.randomEnum;
import static com.clicktravel.common.random.Randoms.randomEnumInSet;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

public class ConditionTest {

    @Test
    public void shouldCreateCondition_withComparisonOperatorAndValue() throws Exception {
        // Given
        final Operators randomComparisonOperator = randomEnum(Operators.class);
        final String value = randomString(10);

        // When
        final Condition condition = new Condition(randomComparisonOperator, value);

        // Then
        assertNotNull(condition);
        assertEquals(randomComparisonOperator, condition.getComparisonOperator());
        assertEquals(1, condition.getValues().size());
        assertThat(condition.getValues(), hasItem(value));
    }

    @Test
    public void shouldCreateCondition_withComparisonOperatorAndValueSet() throws Exception {
        // Given
        final Operators randomComparisonOperator = randomEnum(Operators.class);
        final Set<String> values = Sets.newSet(randomString(10), randomString(10), randomString(10));

        // When
        final Condition condition = new Condition(randomComparisonOperator, values);

        // Then
        assertNotNull(condition);
        assertEquals(randomComparisonOperator, condition.getComparisonOperator());
        assertEquals(values.size(), condition.getValues().size());
    }

    @Test
    public void shouldCreateCondition_withComparisonOperator() throws Exception {
        // Given
        final Operators randomComparisonOperator = randomEnum(Operators.class);

        // When
        final Condition condition = new Condition(randomComparisonOperator);

        // Then
        assertNotNull(condition);
        assertEquals(randomComparisonOperator, condition.getComparisonOperator());
        assertEquals(0, condition.getValues().size());
    }

    @Test
    public void shouldReturnHasMissingComparisonValues_withConditionWhichRequiresComparisonValuesAndNullOrEmptyValues() {
        // Given
        final Set<Operators> operatorsWhichRequireNonNullOrEmptyValues = Sets.newSet(Operators.EQUALS,
                Operators.GREATER_THAN_OR_EQUALS, Operators.LESS_THAN_OR_EQUALS);
        final Operators operatorWhichRequiresNonNullOrEmptyValue = randomEnumInSet(
                operatorsWhichRequireNonNullOrEmptyValues);
        final String value = randomBoolean() ? null : "";

        final Condition condition = new Condition(operatorWhichRequiresNonNullOrEmptyValue, value);

        // When
        final boolean hasMissingComparisonValues = condition.hasMissingComparisonValues();

        // Then
        assertTrue(hasMissingComparisonValues);
    }

    @Test
    public void shouldReturnDoesNotHaveMissingComparisonValues_withConditionWhichRequiresComparisonValuesAndNonNullOrEmptyValues() {
        // Given
        final Set<Operators> operatorsWhichRequireNonNullOrEmptyValues = Sets.newSet(Operators.EQUALS,
                Operators.GREATER_THAN_OR_EQUALS, Operators.LESS_THAN_OR_EQUALS);
        final Operators operatorWhichRequiresNonNullOrEmptyValue = randomEnumInSet(
                operatorsWhichRequireNonNullOrEmptyValues);
        final String value = randomString(10);

        final Condition condition = new Condition(operatorWhichRequiresNonNullOrEmptyValue, value);

        // When
        final boolean hasMissingComparisonValues = condition.hasMissingComparisonValues();

        // Then
        assertFalse(hasMissingComparisonValues);
    }

    @Test
    public void shouldReturnDoesNotHaveMissingComparisonValues_withConditionWhichDoesNotRequireComparisonValuesAndNullOrEmptyValues() {
        // Given
        final Set<Operators> operatorsWhichDoNotRequireNonNullOrEmptyValues = Sets.newSet(Operators.NOT_NULL,
                Operators.NULL);
        final Operators operatorWhichRequiresNonNullOrEmptyValue = randomEnumInSet(
                operatorsWhichDoNotRequireNonNullOrEmptyValues);
        final String value = randomBoolean() ? null : "";

        final Condition condition = new Condition(operatorWhichRequiresNonNullOrEmptyValue, value);

        // When
        final boolean hasMissingComparisonValues = condition.hasMissingComparisonValues();

        // Then
        assertFalse(hasMissingComparisonValues);
    }

    @Test
    public void shouldReturnDoesNotHaveMissingComparisonValues_withConditionWhichDoesNotRequireComparisonValuesAndNonNullOrEmptyValues() {
        // Given
        final Set<Operators> operatorsWhichDoNotRequireNonNullOrEmptyValues = Sets.newSet(Operators.NOT_NULL,
                Operators.NULL);
        final Operators operatorWhichRequiresNonNullOrEmptyValue = randomEnumInSet(
                operatorsWhichDoNotRequireNonNullOrEmptyValues);
        final String value = randomString();

        final Condition condition = new Condition(operatorWhichRequiresNonNullOrEmptyValue, value);

        // When
        final boolean hasMissingComparisonValues = condition.hasMissingComparisonValues();

        // Then
        assertFalse(hasMissingComparisonValues);
    }

    @Test
    public void shouldNotReturnHasMissingComparisonValues_withNullOperator() {
        // Given
        final Operators operators = null;
        final String value = randomString();

        final Condition condition = new Condition(operators, value);

        // When
        InvalidConditionOperatorException thrownException = null;
        try {
            condition.hasMissingComparisonValues();
        } catch (final InvalidConditionOperatorException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }
}
