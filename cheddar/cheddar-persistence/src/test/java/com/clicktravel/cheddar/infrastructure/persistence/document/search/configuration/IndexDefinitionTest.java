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
package com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration;

import static com.clicktravel.common.random.Randoms.randomBoolean;
import static com.clicktravel.common.random.Randoms.randomEnum;
import static com.clicktravel.common.random.Randoms.randomInt;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IndexDefinitionTest {

    @Test
    public void shouldCreateIndexDefinition_withMandatoryParameters() {
        // Given
        final String name = randomString(10);
        final IndexFieldType indexFieldType = IndexFieldType.INT;

        // When
        final IndexDefinition indexDefinition = new IndexDefinition(name, indexFieldType);

        // Then
        assertNotNull(indexDefinition);
        assertEquals(name, indexDefinition.getName());
        assertEquals(indexFieldType, indexDefinition.getFieldType());
        assertTrue(indexDefinition.isSearchEnabled());
        assertTrue(indexDefinition.isReturnEnabled());
        assertTrue(indexDefinition.isSortEnabled());
    }

    @Test
    public void shouldCreateIndexDefinition_withFullParameters() {
        // Given
        final String name = randomString(10);
        final IndexFieldType indexFieldType = IndexFieldType.INT;
        final boolean searchEnabled = randomBoolean();
        final boolean returnEnabled = randomBoolean();
        final boolean sortEnabled = randomBoolean();

        // When
        final IndexDefinition indexDefinition = new IndexDefinition(name, indexFieldType, searchEnabled, returnEnabled,
                sortEnabled);

        // Then
        assertNotNull(indexDefinition);
        assertEquals(name, indexDefinition.getName());
        assertEquals(indexFieldType, indexDefinition.getFieldType());
        assertEquals(searchEnabled, indexDefinition.isSearchEnabled());
        assertEquals(returnEnabled, indexDefinition.isReturnEnabled());
        assertEquals(sortEnabled, indexDefinition.isSortEnabled());
    }

    @Test
    public void shouldNotCreateIndexDefinition_withEmptyName() {
        // Given
        final String name = "";
        final IndexFieldType indexFieldType = randomEnum(IndexFieldType.class);

        // When
        IllegalArgumentException actualException = null;
        try {
            new IndexDefinition(name, indexFieldType);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotCreateIndexDefinition_withNullName() {
        // Given
        final String name = null;
        final IndexFieldType indexFieldType = randomEnum(IndexFieldType.class);

        // When
        IllegalArgumentException actualException = null;
        try {
            new IndexDefinition(name, indexFieldType);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotCreateIndexDefinition_withNullIndexFieldType() {
        // Given
        final String name = randomString(10);
        final IndexFieldType indexFieldType = null;

        // When
        IllegalArgumentException actualException = null;
        try {
            new IndexDefinition(name, indexFieldType);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotCreateIndexDefinition_withTextFieldNotSearchEnabled() {
        // Given
        final String name = randomString(10);
        final IndexFieldType indexFieldType = IndexFieldType.TEXT;
        final boolean searchEnabled = false;
        final boolean returnEnabled = randomBoolean();
        final boolean sortEnabled = randomBoolean();

        // When
        IllegalArgumentException actualException = null;
        try {
            new IndexDefinition(name, indexFieldType, searchEnabled, returnEnabled, sortEnabled);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotCreateIndexDefinition_withTextArrayFieldNotSearchEnabled() {
        // Given
        final String name = randomString(10);
        final IndexFieldType indexFieldType = IndexFieldType.TEXT_ARRAY;
        final boolean searchEnabled = false;
        final boolean returnEnabled = randomBoolean();
        final boolean sortEnabled = randomBoolean();

        // When
        IllegalArgumentException actualException = null;
        try {
            new IndexDefinition(name, indexFieldType, searchEnabled, returnEnabled, sortEnabled);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotCreateIndexDefinition_withArrayFieldSortEnabled() {
        // Given
        final String name = randomString(10);
        final IndexFieldType indexFieldType = randomArrayFieldType();
        final boolean searchEnabled = true;
        final boolean returnEnabled = randomBoolean();
        final boolean sortEnabled = true;

        // When
        IllegalArgumentException actualException = null;
        try {
            new IndexDefinition(name, indexFieldType, searchEnabled, returnEnabled, sortEnabled);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    private IndexFieldType randomArrayFieldType() {
        final IndexFieldType[] arrayTypes = new IndexFieldType[] { IndexFieldType.DATETIME_ARRAY,
                IndexFieldType.DOUBLE_ARRAY, IndexFieldType.INT_ARRAY, IndexFieldType.LITERAL_ARRAY,
                IndexFieldType.TEXT_ARRAY };
        return arrayTypes[randomInt(arrayTypes.length)];
    }
}
