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
package com.clicktravel.infrastructure.persistence.aws.cloudsearch.manager;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.hamcrest.core.Is;
import org.junit.Test;

import com.amazonaws.services.cloudsearchv2.model.IndexField;
import com.clicktravel.common.mapper.Mapper;
import com.clicktravel.common.random.Randoms;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.IndexDefinition;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.IndexFieldType;

public class IndexDefinitionToIndexFieldMapperTest {

    @Test
    public void shouldMapToIndexFeild_withIndexDefinitionOfTypeText() {
        // Given
        final String indexDefinitionName = Randoms.randomString();
        final String indexDefinitionNameAsLowerCase = indexDefinitionName.toLowerCase();
        final IndexFieldType indexFeildType = IndexFieldType.TEXT;
        final boolean searchEnabled = true;
        final boolean returnEnabled = Randoms.randomBoolean();
        final boolean sortEnabled = Randoms.randomBoolean();
        final IndexDefinition indexDefinition = new IndexDefinition(indexDefinitionName, indexFeildType, searchEnabled,
                returnEnabled, sortEnabled);
        final Mapper<IndexDefinition, IndexField> indexDefinitionToIndexFieldMapper = new IndexDefinitionToIndexFieldMapper();

        // When
        final IndexField indexField = indexDefinitionToIndexFieldMapper.map(indexDefinition);

        // Then
        assertNotNull(indexField);
        assertThat(indexField.getIndexFieldName(), is(indexDefinitionNameAsLowerCase));
        assertThat(indexField.getIndexFieldType(),
                Is.is(com.amazonaws.services.cloudsearchv2.model.IndexFieldType.Text.toString()));
        assertNotNull(indexField.getTextOptions());
        assertThat(indexField.getTextOptions().getReturnEnabled(), Is.is(returnEnabled));
        assertThat(indexField.getTextOptions().getSortEnabled(), Is.is(sortEnabled));
        assertNull(indexField.getDateArrayOptions());
        assertNull(indexField.getDateOptions());
        assertNull(indexField.getDoubleArrayOptions());
        assertNull(indexField.getDoubleOptions());
        assertNull(indexField.getIntArrayOptions());
        assertNull(indexField.getIntOptions());
        assertNull(indexField.getLatLonOptions());
        assertNull(indexField.getLiteralArrayOptions());
        assertNull(indexField.getLiteralOptions());
        assertNull(indexField.getTextArrayOptions());
    }

    @Test
    public void shouldMapToIndexFeild_withIndexDefinitionOfTypeTextArray() {
        // Given
        final String indexDefinitionName = Randoms.randomString();
        final String indexDefinitionNameAsLowerCase = indexDefinitionName.toLowerCase();
        final IndexFieldType indexFeildType = IndexFieldType.TEXT_ARRAY;
        final boolean searchEnabled = true;
        final boolean returnEnabled = Randoms.randomBoolean();
        final boolean sortEnabled = false;
        final IndexDefinition indexDefinition = new IndexDefinition(indexDefinitionName, indexFeildType, searchEnabled,
                returnEnabled, sortEnabled);
        final Mapper<IndexDefinition, IndexField> indexDefinitionToIndexFieldMapper = new IndexDefinitionToIndexFieldMapper();

        // When
        final IndexField indexField = indexDefinitionToIndexFieldMapper.map(indexDefinition);

        // Then
        assertNotNull(indexField);
        assertThat(indexField.getIndexFieldName(), is(indexDefinitionNameAsLowerCase));
        assertThat(indexField.getIndexFieldType(),
                Is.is(com.amazonaws.services.cloudsearchv2.model.IndexFieldType.TextArray.toString()));
        assertNotNull(indexField.getTextArrayOptions());
        assertThat(indexField.getTextArrayOptions().getReturnEnabled(), Is.is(returnEnabled));
        assertNull(indexField.getDateArrayOptions());
        assertNull(indexField.getDateOptions());
        assertNull(indexField.getDoubleArrayOptions());
        assertNull(indexField.getDoubleOptions());
        assertNull(indexField.getIntArrayOptions());
        assertNull(indexField.getIntOptions());
        assertNull(indexField.getLatLonOptions());
        assertNull(indexField.getLiteralArrayOptions());
        assertNull(indexField.getLiteralOptions());
        assertNull(indexField.getTextOptions());
    }

    @Test
    public void shouldMapToIndexFeild_withIndexDefinitionOfTypeDateTime() {
        // Given
        final String indexDefinitionName = Randoms.randomString();
        final String indexDefinitionNameAsLowerCase = indexDefinitionName.toLowerCase();
        final IndexFieldType indexFeildType = IndexFieldType.DATETIME;
        final boolean searchEnabled = Randoms.randomBoolean();
        final boolean returnEnabled = Randoms.randomBoolean();
        final boolean sortEnabled = Randoms.randomBoolean();
        final IndexDefinition indexDefinition = new IndexDefinition(indexDefinitionName, indexFeildType, searchEnabled,
                returnEnabled, sortEnabled);
        final Mapper<IndexDefinition, IndexField> indexDefinitionToIndexFieldMapper = new IndexDefinitionToIndexFieldMapper();

        // When
        final IndexField indexField = indexDefinitionToIndexFieldMapper.map(indexDefinition);

        // Then
        assertNotNull(indexField);
        assertThat(indexField.getIndexFieldName(), is(indexDefinitionNameAsLowerCase));
        assertThat(indexField.getIndexFieldType(),
                Is.is(com.amazonaws.services.cloudsearchv2.model.IndexFieldType.Date.toString()));
        assertNotNull(indexField.getDateOptions());
        assertThat(indexField.getDateOptions().getReturnEnabled(), Is.is(returnEnabled));
        assertThat(indexField.getDateOptions().getSortEnabled(), Is.is(sortEnabled));
        assertThat(indexField.getDateOptions().getSearchEnabled(), Is.is(searchEnabled));
        assertThat(indexField.getDateOptions().getFacetEnabled(), Is.is(false));
        assertNull(indexField.getDateArrayOptions());
        assertNull(indexField.getDoubleArrayOptions());
        assertNull(indexField.getDoubleOptions());
        assertNull(indexField.getIntArrayOptions());
        assertNull(indexField.getIntOptions());
        assertNull(indexField.getLatLonOptions());
        assertNull(indexField.getLiteralArrayOptions());
        assertNull(indexField.getLiteralOptions());
        assertNull(indexField.getTextArrayOptions());
        assertNull(indexField.getTextOptions());
    }

    @Test
    public void shouldMapToIndexFeild_withIndexDefinitionOfTypeDateTimeArray() {
        // Given
        final String indexDefinitionName = Randoms.randomString();
        final String indexDefinitionNameAsLowerCase = indexDefinitionName.toLowerCase();
        final IndexFieldType indexFeildType = IndexFieldType.DATETIME_ARRAY;
        final boolean searchEnabled = Randoms.randomBoolean();
        final boolean returnEnabled = Randoms.randomBoolean();
        final boolean sortEnabled = false;
        final IndexDefinition indexDefinition = new IndexDefinition(indexDefinitionName, indexFeildType, searchEnabled,
                returnEnabled, sortEnabled);
        final Mapper<IndexDefinition, IndexField> indexDefinitionToIndexFieldMapper = new IndexDefinitionToIndexFieldMapper();

        // When
        final IndexField indexField = indexDefinitionToIndexFieldMapper.map(indexDefinition);

        // Then
        assertNotNull(indexField);
        assertThat(indexField.getIndexFieldName(), is(indexDefinitionNameAsLowerCase));
        assertThat(indexField.getIndexFieldType(),
                Is.is(com.amazonaws.services.cloudsearchv2.model.IndexFieldType.DateArray.toString()));
        assertNotNull(indexField.getDateArrayOptions());
        assertThat(indexField.getDateArrayOptions().getReturnEnabled(), Is.is(returnEnabled));
        assertThat(indexField.getDateArrayOptions().getSearchEnabled(), Is.is(searchEnabled));
        assertThat(indexField.getDateArrayOptions().getFacetEnabled(), Is.is(false));
        assertNull(indexField.getDateOptions());
        assertNull(indexField.getDoubleOptions());
        assertNull(indexField.getDoubleArrayOptions());
        assertNull(indexField.getIntArrayOptions());
        assertNull(indexField.getIntOptions());
        assertNull(indexField.getLatLonOptions());
        assertNull(indexField.getLiteralArrayOptions());
        assertNull(indexField.getLiteralOptions());
        assertNull(indexField.getTextArrayOptions());
        assertNull(indexField.getTextOptions());
    }

    @Test
    public void shouldMapToIndexFeild_withIndexDefinitionOfTypeDouble() {
        // Given
        final String indexDefinitionName = Randoms.randomString();
        final String indexDefinitionNameAsLowerCase = indexDefinitionName.toLowerCase();
        final IndexFieldType indexFeildType = IndexFieldType.DOUBLE;
        final boolean searchEnabled = Randoms.randomBoolean();
        final boolean returnEnabled = Randoms.randomBoolean();
        final boolean sortEnabled = Randoms.randomBoolean();
        final IndexDefinition indexDefinition = new IndexDefinition(indexDefinitionName, indexFeildType, searchEnabled,
                returnEnabled, sortEnabled);
        final Mapper<IndexDefinition, IndexField> indexDefinitionToIndexFieldMapper = new IndexDefinitionToIndexFieldMapper();

        // When
        final IndexField indexField = indexDefinitionToIndexFieldMapper.map(indexDefinition);

        // Then
        assertNotNull(indexField);
        assertThat(indexField.getIndexFieldName(), is(indexDefinitionNameAsLowerCase));
        assertThat(indexField.getIndexFieldType(),
                Is.is(com.amazonaws.services.cloudsearchv2.model.IndexFieldType.Double.toString()));
        assertNotNull(indexField.getDoubleOptions());
        assertThat(indexField.getDoubleOptions().getReturnEnabled(), Is.is(returnEnabled));
        assertThat(indexField.getDoubleOptions().getSortEnabled(), Is.is(sortEnabled));
        assertThat(indexField.getDoubleOptions().getSearchEnabled(), Is.is(searchEnabled));
        assertThat(indexField.getDoubleOptions().getFacetEnabled(), Is.is(false));
        assertNull(indexField.getDateOptions());
        assertNull(indexField.getDateArrayOptions());
        assertNull(indexField.getDoubleArrayOptions());
        assertNull(indexField.getIntArrayOptions());
        assertNull(indexField.getIntOptions());
        assertNull(indexField.getLatLonOptions());
        assertNull(indexField.getLiteralArrayOptions());
        assertNull(indexField.getLiteralOptions());
        assertNull(indexField.getTextArrayOptions());
        assertNull(indexField.getTextOptions());
    }

    @Test
    public void shouldMapToIndexFeild_withIndexDefinitionOfTypeDoubleArray() {
        // Given
        final String indexDefinitionName = Randoms.randomString();
        final String indexDefinitionNameAsLowerCase = indexDefinitionName.toLowerCase();
        final IndexFieldType indexFeildType = IndexFieldType.DOUBLE_ARRAY;
        final boolean searchEnabled = Randoms.randomBoolean();
        final boolean returnEnabled = Randoms.randomBoolean();
        final boolean sortEnabled = false;
        final IndexDefinition indexDefinition = new IndexDefinition(indexDefinitionName, indexFeildType, searchEnabled,
                returnEnabled, sortEnabled);
        final Mapper<IndexDefinition, IndexField> indexDefinitionToIndexFieldMapper = new IndexDefinitionToIndexFieldMapper();

        // When
        final IndexField indexField = indexDefinitionToIndexFieldMapper.map(indexDefinition);

        // Then
        assertNotNull(indexField);
        assertThat(indexField.getIndexFieldName(), is(indexDefinitionNameAsLowerCase));
        assertThat(indexField.getIndexFieldType(),
                Is.is(com.amazonaws.services.cloudsearchv2.model.IndexFieldType.DoubleArray.toString()));
        assertNotNull(indexField.getDoubleArrayOptions());
        assertThat(indexField.getDoubleArrayOptions().getReturnEnabled(), Is.is(returnEnabled));
        assertThat(indexField.getDoubleArrayOptions().getSearchEnabled(), Is.is(searchEnabled));
        assertThat(indexField.getDoubleArrayOptions().getFacetEnabled(), Is.is(false));
        assertNull(indexField.getDateOptions());
        assertNull(indexField.getDoubleOptions());
        assertNull(indexField.getIntArrayOptions());
        assertNull(indexField.getIntOptions());
        assertNull(indexField.getLatLonOptions());
        assertNull(indexField.getLiteralArrayOptions());
        assertNull(indexField.getLiteralOptions());
        assertNull(indexField.getTextArrayOptions());
        assertNull(indexField.getTextOptions());
        assertNull(indexField.getDateArrayOptions());
    }

    @Test
    public void shouldMapToIndexFeild_withIndexDefinitionOfTypeInt() {
        // Given
        final String indexDefinitionName = Randoms.randomString();
        final String indexDefinitionNameAsLowerCase = indexDefinitionName.toLowerCase();
        final IndexFieldType indexFeildType = IndexFieldType.INT;
        final boolean searchEnabled = Randoms.randomBoolean();
        final boolean returnEnabled = Randoms.randomBoolean();
        final boolean sortEnabled = Randoms.randomBoolean();
        final IndexDefinition indexDefinition = new IndexDefinition(indexDefinitionName, indexFeildType, searchEnabled,
                returnEnabled, sortEnabled);
        final Mapper<IndexDefinition, IndexField> indexDefinitionToIndexFieldMapper = new IndexDefinitionToIndexFieldMapper();

        // When
        final IndexField indexField = indexDefinitionToIndexFieldMapper.map(indexDefinition);

        // Then
        assertNotNull(indexField);
        assertThat(indexField.getIndexFieldName(), is(indexDefinitionNameAsLowerCase));
        assertThat(indexField.getIndexFieldType(),
                Is.is(com.amazonaws.services.cloudsearchv2.model.IndexFieldType.Int.toString()));
        assertNotNull(indexField.getIntOptions());
        assertThat(indexField.getIntOptions().getReturnEnabled(), Is.is(returnEnabled));
        assertThat(indexField.getIntOptions().getSortEnabled(), Is.is(sortEnabled));
        assertThat(indexField.getIntOptions().getSearchEnabled(), Is.is(searchEnabled));
        assertThat(indexField.getIntOptions().getFacetEnabled(), Is.is(false));
        assertNull(indexField.getDateOptions());
        assertNull(indexField.getDateArrayOptions());
        assertNull(indexField.getDoubleArrayOptions());
        assertNull(indexField.getIntArrayOptions());
        assertNull(indexField.getLatLonOptions());
        assertNull(indexField.getLiteralArrayOptions());
        assertNull(indexField.getLiteralOptions());
        assertNull(indexField.getTextArrayOptions());
        assertNull(indexField.getTextOptions());
        assertNull(indexField.getDoubleOptions());
    }

    @Test
    public void shouldMapToIndexFeild_withIndexDefinitionOfTypeIntArray() {
        // Given
        final String indexDefinitionName = Randoms.randomString();
        final String indexDefinitionNameAsLowerCase = indexDefinitionName.toLowerCase();
        final IndexFieldType indexFeildType = IndexFieldType.INT_ARRAY;
        final boolean searchEnabled = Randoms.randomBoolean();
        final boolean returnEnabled = Randoms.randomBoolean();
        final boolean sortEnabled = false;
        final IndexDefinition indexDefinition = new IndexDefinition(indexDefinitionName, indexFeildType, searchEnabled,
                returnEnabled, sortEnabled);
        final Mapper<IndexDefinition, IndexField> indexDefinitionToIndexFieldMapper = new IndexDefinitionToIndexFieldMapper();

        // When
        final IndexField indexField = indexDefinitionToIndexFieldMapper.map(indexDefinition);

        // Then
        assertNotNull(indexField);
        assertThat(indexField.getIndexFieldName(), is(indexDefinitionNameAsLowerCase));
        assertThat(indexField.getIndexFieldType(),
                Is.is(com.amazonaws.services.cloudsearchv2.model.IndexFieldType.IntArray.toString()));
        assertNotNull(indexField.getIntArrayOptions());
        assertThat(indexField.getIntArrayOptions().getReturnEnabled(), Is.is(returnEnabled));
        assertThat(indexField.getIntArrayOptions().getSearchEnabled(), Is.is(searchEnabled));
        assertThat(indexField.getIntArrayOptions().getFacetEnabled(), Is.is(false));
        assertNull(indexField.getDateOptions());
        assertNull(indexField.getDoubleOptions());
        assertNull(indexField.getIntOptions());
        assertNull(indexField.getLatLonOptions());
        assertNull(indexField.getLiteralArrayOptions());
        assertNull(indexField.getLiteralOptions());
        assertNull(indexField.getTextArrayOptions());
        assertNull(indexField.getTextOptions());
        assertNull(indexField.getDateArrayOptions());
        assertNull(indexField.getDoubleArrayOptions());
    }

    @Test
    public void shouldMapToIndexFeild_withIndexDefinitionOfTypeLatLon() {
        // Given
        final String indexDefinitionName = Randoms.randomString();
        final String indexDefinitionNameAsLowerCase = indexDefinitionName.toLowerCase();
        final IndexFieldType indexFeildType = IndexFieldType.LATLON;
        final boolean searchEnabled = Randoms.randomBoolean();
        final boolean returnEnabled = Randoms.randomBoolean();
        final boolean sortEnabled = Randoms.randomBoolean();
        final IndexDefinition indexDefinition = new IndexDefinition(indexDefinitionName, indexFeildType, searchEnabled,
                returnEnabled, sortEnabled);
        final Mapper<IndexDefinition, IndexField> indexDefinitionToIndexFieldMapper = new IndexDefinitionToIndexFieldMapper();

        // When
        final IndexField indexField = indexDefinitionToIndexFieldMapper.map(indexDefinition);

        // Then
        assertNotNull(indexField);
        assertThat(indexField.getIndexFieldName(), is(indexDefinitionNameAsLowerCase));
        assertThat(indexField.getIndexFieldType(),
                Is.is(com.amazonaws.services.cloudsearchv2.model.IndexFieldType.Latlon.toString()));
        assertNotNull(indexField.getLatLonOptions());
        assertThat(indexField.getLatLonOptions().getReturnEnabled(), Is.is(returnEnabled));
        assertThat(indexField.getLatLonOptions().getSortEnabled(), Is.is(sortEnabled));
        assertThat(indexField.getLatLonOptions().getSearchEnabled(), Is.is(searchEnabled));
        assertThat(indexField.getLatLonOptions().getFacetEnabled(), Is.is(false));
        assertNull(indexField.getDateOptions());
        assertNull(indexField.getDateArrayOptions());
        assertNull(indexField.getDoubleArrayOptions());
        assertNull(indexField.getIntArrayOptions());
        assertNull(indexField.getLiteralArrayOptions());
        assertNull(indexField.getLiteralOptions());
        assertNull(indexField.getTextArrayOptions());
        assertNull(indexField.getTextOptions());
        assertNull(indexField.getDoubleOptions());
        assertNull(indexField.getIntOptions());
    }

    @Test
    public void shouldMapToIndexFeild_withIndexDefinitionOfTypeLiteral() {
        // Given
        final String indexDefinitionName = Randoms.randomString();
        final String indexDefinitionNameAsLowerCase = indexDefinitionName.toLowerCase();
        final IndexFieldType indexFeildType = IndexFieldType.LITERAL;
        final boolean searchEnabled = Randoms.randomBoolean();
        final boolean returnEnabled = Randoms.randomBoolean();
        final boolean sortEnabled = Randoms.randomBoolean();
        final IndexDefinition indexDefinition = new IndexDefinition(indexDefinitionName, indexFeildType, searchEnabled,
                returnEnabled, sortEnabled);
        final Mapper<IndexDefinition, IndexField> indexDefinitionToIndexFieldMapper = new IndexDefinitionToIndexFieldMapper();

        // When
        final IndexField indexField = indexDefinitionToIndexFieldMapper.map(indexDefinition);

        // Then
        assertNotNull(indexField);
        assertThat(indexField.getIndexFieldName(), is(indexDefinitionNameAsLowerCase));
        assertThat(indexField.getIndexFieldType(),
                Is.is(com.amazonaws.services.cloudsearchv2.model.IndexFieldType.Literal.toString()));
        assertNotNull(indexField.getLiteralOptions());
        assertThat(indexField.getLiteralOptions().getReturnEnabled(), Is.is(returnEnabled));
        assertThat(indexField.getLiteralOptions().getSortEnabled(), Is.is(sortEnabled));
        assertThat(indexField.getLiteralOptions().getSearchEnabled(), Is.is(searchEnabled));
        assertThat(indexField.getLiteralOptions().getFacetEnabled(), Is.is(false));
        assertNull(indexField.getDateOptions());
        assertNull(indexField.getDateArrayOptions());
        assertNull(indexField.getDoubleArrayOptions());
        assertNull(indexField.getIntArrayOptions());
        assertNull(indexField.getLiteralArrayOptions());
        assertNull(indexField.getTextArrayOptions());
        assertNull(indexField.getTextOptions());
        assertNull(indexField.getDoubleOptions());
        assertNull(indexField.getIntOptions());
        assertNull(indexField.getLatLonOptions());
    }

    @Test
    public void shouldMapToIndexFeild_withIndexDefinitionOfTypeLiteralArray() {
        // Given
        final String indexDefinitionName = Randoms.randomString();
        final String indexDefinitionNameAsLowerCase = indexDefinitionName.toLowerCase();
        final IndexFieldType indexFeildType = IndexFieldType.LITERAL_ARRAY;
        final boolean searchEnabled = Randoms.randomBoolean();
        final boolean returnEnabled = Randoms.randomBoolean();
        final boolean sortEnabled = false;
        final IndexDefinition indexDefinition = new IndexDefinition(indexDefinitionName, indexFeildType, searchEnabled,
                returnEnabled, sortEnabled);
        final Mapper<IndexDefinition, IndexField> indexDefinitionToIndexFieldMapper = new IndexDefinitionToIndexFieldMapper();

        // When
        final IndexField indexField = indexDefinitionToIndexFieldMapper.map(indexDefinition);

        // Then
        assertNotNull(indexField);
        assertThat(indexField.getIndexFieldName(), is(indexDefinitionNameAsLowerCase));
        assertThat(indexField.getIndexFieldType(),
                Is.is(com.amazonaws.services.cloudsearchv2.model.IndexFieldType.LiteralArray.toString()));
        assertNotNull(indexField.getLiteralArrayOptions());
        assertThat(indexField.getLiteralArrayOptions().getReturnEnabled(), Is.is(returnEnabled));
        assertThat(indexField.getLiteralArrayOptions().getSearchEnabled(), Is.is(searchEnabled));
        assertThat(indexField.getLiteralArrayOptions().getFacetEnabled(), Is.is(false));
        assertNull(indexField.getDateOptions());
        assertNull(indexField.getDoubleOptions());
        assertNull(indexField.getIntOptions());
        assertNull(indexField.getLatLonOptions());
        assertNull(indexField.getLiteralOptions());
        assertNull(indexField.getTextArrayOptions());
        assertNull(indexField.getTextOptions());
        assertNull(indexField.getDateArrayOptions());
        assertNull(indexField.getDoubleArrayOptions());
        assertNull(indexField.getIntArrayOptions());
    }

    @Test
    public void shouldNotMapToIndexFeild_withNullIndexDefinition() {
        // Given
        final IndexDefinition indexDefinition = null;
        final Mapper<IndexDefinition, IndexField> indexDefinitionToIndexFieldMapper = new IndexDefinitionToIndexFieldMapper();

        // When
        final IndexField indexField = indexDefinitionToIndexFieldMapper.map(indexDefinition);

        // Then
        assertNull(indexField);
    }

}
