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

import static com.clicktravel.common.random.Randoms.randomEnum;
import static com.clicktravel.common.random.Randoms.randomIntInRange;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.AttributeQuery;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.CompoundAttributeQuery;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Condition;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Operators;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RangeKeyConditionBuilder.class)
public class QuerySpecBuilderTest {

    @Before
    public void setup() {
        mockStatic(RangeKeyConditionBuilder.class);
    }

    @Test
    public void shouldBuild_withAttributeQuery() {
        // Given
        final String attributeName = randomString(10);
        final Condition mockCondition = randomCondition(1);
        final String expectedValue = mockCondition.getValues().iterator().next();
        final AttributeQuery mockAttributeQuery = mock(AttributeQuery.class);

        when(mockAttributeQuery.getAttributeName()).thenReturn(attributeName);
        when(mockAttributeQuery.getCondition()).thenReturn(mockCondition);

        // When
        final QuerySpec querySpec = QuerySpecBuilder.build(mockAttributeQuery, StubItem.class);

        // Then
        assertEquals(attributeName, querySpec.getHashKey().getName());
        assertEquals(expectedValue, querySpec.getHashKey().getValue());
        assertNull(querySpec.getRangeKeyCondition());
    }

    @Test
    public void shouldBuild_withCompoundAttributeQuery() {
        // Given
        final String attributeName = randomString(10);
        final String supportingAttributeName = randomString(10);
        final Condition condition = randomCondition(1);
        final Condition supportingCondition = randomCondition(1);
        final CompoundAttributeQuery compoundAttributeQuery = mock(CompoundAttributeQuery.class);
        final RangeKeyCondition mockRangeKeyCondition = mock(RangeKeyCondition.class);
        final String expectedValue = condition.getValues().iterator().next();
        final String expectedSupportingValue = supportingCondition.getValues().iterator().next();
        final Operators expectedSupportingComparisonOperator = supportingCondition.getComparisonOperator();

        when(compoundAttributeQuery.getAttributeName()).thenReturn(attributeName);
        when(compoundAttributeQuery.getCondition()).thenReturn(condition);
        when(compoundAttributeQuery.getSupportingCondition()).thenReturn(supportingCondition);
        Mockito.<Class<?>> when(compoundAttributeQuery.getSupportingAttributeType(any())).thenReturn(String.class);
        when(compoundAttributeQuery.getSupportingAttributeName()).thenReturn(supportingAttributeName);
        when(RangeKeyConditionBuilder.build(supportingAttributeName, expectedSupportingValue,
                expectedSupportingComparisonOperator)).thenReturn(mockRangeKeyCondition);

        // When
        final QuerySpec querySpec = QuerySpecBuilder.build(compoundAttributeQuery,
                StubWithGlobalSecondaryIndexItem.class);

        // Then
        assertEquals(attributeName, querySpec.getHashKey().getName());
        assertEquals(expectedValue, querySpec.getHashKey().getValue());
        assertEquals(mockRangeKeyCondition, querySpec.getRangeKeyCondition());
    }

    @Test
    public void shouldBuild_withCompoundAttributeQueryAndMultipleSupportingValues() {
        // Given
        final Condition supportingCondition = randomCondition(randomIntInRange(2, 10));
        final CompoundAttributeQuery compoundAttributeQuery = mock(CompoundAttributeQuery.class);

        when(compoundAttributeQuery.getSupportingCondition()).thenReturn(supportingCondition);

        // When
        InvalidConditionValuesException expectedException = null;
        try {
            QuerySpecBuilder.build(compoundAttributeQuery, StubWithGlobalSecondaryIndexItem.class);
        } catch (final InvalidConditionValuesException e) {
            expectedException = e;
        }

        // Then
        assertNotNull(expectedException);
    }

    private Condition randomCondition(final int numberOfValues) {
        final Operators operators = randomEnum(Operators.class);
        final Condition mockCondition = mock(Condition.class);
        final Set<String> values = new HashSet<String>();

        for (int i = 0; i < numberOfValues; i++) {
            final String value = randomString(10);
            values.add(value);
        }

        when(mockCondition.getValues()).thenReturn(values);
        when(mockCondition.getComparisonOperator()).thenReturn(operators);

        return mockCondition;
    }
}
