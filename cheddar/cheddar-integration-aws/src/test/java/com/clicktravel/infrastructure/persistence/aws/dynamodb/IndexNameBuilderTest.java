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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.persistence.database.query.AttributeQuery;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.CompoundAttributeQuery;

public class IndexNameBuilderTest {

    @Test
    public void shouldBuild_withAttributeQuery() {
        // Given
        final String attributeName = randomString(10);
        final AttributeQuery mockAttributeQuery = mock(AttributeQuery.class);
        final String expectedIndexName = attributeName + "_idx";

        when(mockAttributeQuery.getAttributeName()).thenReturn(attributeName);

        // When
        final String indexName = IndexNameBuilder.build(mockAttributeQuery);

        // Then
        assertEquals(expectedIndexName, indexName);
    }

    @Test
    public void shouldBuild_withCompositeAttributeQuery() {
        // Given
        final String attributeName = randomString(10);
        final String supportingAttributeName = randomString(10);
        final CompoundAttributeQuery mockCompoundAttributeQuery = mock(CompoundAttributeQuery.class);
        final String expectedIndexName = attributeName + "_" + supportingAttributeName + "_idx";

        when(mockCompoundAttributeQuery.getAttributeName()).thenReturn(attributeName);
        when(mockCompoundAttributeQuery.getSupportingAttributeName()).thenReturn(supportingAttributeName);

        // When
        final String indexName = IndexNameBuilder.build(mockCompoundAttributeQuery);

        // Then
        assertEquals(expectedIndexName, indexName);
    }
}
