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
package com.clicktravel.infrastructure.persistence.aws.dynamodb;

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.amazonaws.services.dynamodbv2.document.KeyConditions;
import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Operators;
import com.clicktravel.common.random.Randoms;
import com.clicktravel.common.validation.ValidationException;

public class RangeKeyConditionBuilderTest {

    @Test
    public void shouldBuildRangeKeyCondition_withAttributeNameConditionValueAndEqualComparisonOperator() {
        // Given
        final String attributeName = randomString();
        final String conditionValue = randomString();
        final Operators operator = Operators.EQUALS;

        // When
        final RangeKeyCondition rangeKeyCondition = RangeKeyConditionBuilder.build(attributeName, conditionValue,
                operator);

        // Then
        assertEquals(attributeName, rangeKeyCondition.getAttrName());
        assertTrue(rangeKeyCondition.getValues().length == 1);
        assertEquals(conditionValue, rangeKeyCondition.getValues()[0]);
        assertEquals(KeyConditions.EQ, rangeKeyCondition.getKeyCondition());
    }

    @Test
    public void shouldBuildRangeKeyCondition_withAttributeNameConditionValueAndLessThanOrEqualToComparisonOperator() {
        // Given
        final String attributeName = randomString();
        final String conditionValue = randomString();
        final Operators operator = Operators.LESS_THAN_OR_EQUALS;

        // When
        final RangeKeyCondition rangeKeyCondition = RangeKeyConditionBuilder.build(attributeName, conditionValue,
                operator);

        // Then
        assertEquals(attributeName, rangeKeyCondition.getAttrName());
        assertTrue(rangeKeyCondition.getValues().length == 1);
        assertEquals(conditionValue, rangeKeyCondition.getValues()[0]);
        assertEquals(KeyConditions.LE, rangeKeyCondition.getKeyCondition());
    }

    @Test
    public void shouldBuildRangeKeyCondition_withAttributeNameConditionValueAndGreaterThanOrEqualToComparisonOperator() {
        // Given
        final String attributeName = randomString();
        final String conditionValue = randomString();
        final Operators operator = Operators.GREATER_THAN_OR_EQUALS;

        // When
        final RangeKeyCondition rangeKeyCondition = RangeKeyConditionBuilder.build(attributeName, conditionValue,
                operator);

        // Then
        assertEquals(attributeName, rangeKeyCondition.getAttrName());
        assertTrue(rangeKeyCondition.getValues().length == 1);
        assertEquals(conditionValue, rangeKeyCondition.getValues()[0]);
        assertEquals(KeyConditions.GE, rangeKeyCondition.getKeyCondition());
    }

    @Test
    public void shouldNotBuildRangeKeyCondition_withInvalidComparisonOperator() {
        // Given
        final String attributeName = randomString();
        final String conditionValue = randomString();
        final Set<Operators> validOperators = new HashSet<>();
        validOperators.add(Operators.NOT_NULL);
        validOperators.add(Operators.NULL);
        final Operators operator = Randoms.randomEnumInSet(validOperators);

        // When
        InvalidConditionOperatorException expectedException = null;
        try {
            RangeKeyConditionBuilder.build(attributeName, conditionValue, operator);
        } catch (final InvalidConditionOperatorException e) {
            expectedException = e;
        }

        // Then
        assertNotNull(expectedException);
    }

    @Test
    public void shouldNotBuildRangeKeyCondition_withEmptyAttributeName() {
        // Given
        final String attributeName = "";
        final String conditionValue = randomString();
        final Set<Operators> invalidOperators = buildValidOperatorsSet();
        final Operators operator = Randoms.randomEnumInSet(invalidOperators);

        // When
        ValidationException expectedException = null;
        try {
            RangeKeyConditionBuilder.build(attributeName, conditionValue, operator);
        } catch (final ValidationException e) {
            expectedException = e;
        }

        // Then
        assertNotNull(expectedException);
        assertEquals("attributeName", expectedException.getFields()[0]);
    }

    @Test
    public void shouldNotBuildRangeKeyCondition_withNullAttributeName() {
        // Given
        final String attributeName = null;
        final String conditionValue = randomString();
        final Set<Operators> invalidOperators = buildValidOperatorsSet();
        final Operators operator = Randoms.randomEnumInSet(invalidOperators);

        // When
        ValidationException expectedException = null;
        try {
            RangeKeyConditionBuilder.build(attributeName, conditionValue, operator);
        } catch (final ValidationException e) {
            expectedException = e;
        }

        // Then
        assertNotNull(expectedException);
        assertEquals("attributeName", expectedException.getFields()[0]);
    }

    @Test
    public void shouldNotBuildRangeKeyCondition_withNullConditionValue() {
        // Given
        final String attributeName = randomString();
        final String conditionValue = null;
        final Set<Operators> invalidOperators = buildValidOperatorsSet();
        final Operators operator = Randoms.randomEnumInSet(invalidOperators);

        // When
        ValidationException expectedException = null;
        try {
            RangeKeyConditionBuilder.build(attributeName, conditionValue, operator);
        } catch (final ValidationException e) {
            expectedException = e;
        }

        // Then
        assertNotNull(expectedException);
        assertEquals("conditionValue", expectedException.getFields()[0]);
    }

    private Set<Operators> buildValidOperatorsSet() {
        final Set<Operators> invalidOperators = new HashSet<>();
        invalidOperators.add(Operators.EQUALS);
        invalidOperators.add(Operators.GREATER_THAN_OR_EQUALS);
        invalidOperators.add(Operators.LESS_THAN_OR_EQUALS);
        return invalidOperators;
    }
}
