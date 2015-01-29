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

import com.amazonaws.services.cloudsearchv2.model.*;
import com.clicktravel.common.mapper.Mapper;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.IndexDefinition;

public class IndexDefinitionToIndexFieldMapper implements Mapper<IndexDefinition, IndexField> {

    @Override
    public IndexField map(final IndexDefinition indexDefinition) {
        if (indexDefinition == null) {
            return null;
        }
        final IndexField indexfield = new IndexField().withIndexFieldName(indexDefinition.getName().toLowerCase());
        final boolean searchEnabled = indexDefinition.isSearchEnabled();
        final boolean returnEnabled = indexDefinition.isReturnEnabled();
        final boolean sortEnabled = indexDefinition.isSortEnabled();

        switch (indexDefinition.getFieldType()) {
            case DATETIME:
                indexfield.setIndexFieldType(IndexFieldType.Date);
                final DateOptions dateOptions = new DateOptions().withReturnEnabled(returnEnabled)
                        .withSearchEnabled(searchEnabled).withSortEnabled(sortEnabled).withFacetEnabled(false);
                indexfield.setDateOptions(dateOptions);
                return indexfield;
            case DATETIME_ARRAY:
                indexfield.setIndexFieldType(IndexFieldType.DateArray);
                final DateArrayOptions dateArrayOptions = new DateArrayOptions().withReturnEnabled(returnEnabled)
                        .withSearchEnabled(searchEnabled).withFacetEnabled(false);
                indexfield.setDateArrayOptions(dateArrayOptions);
                return indexfield;
            case DOUBLE:
                indexfield.setIndexFieldType(IndexFieldType.Double);
                final DoubleOptions doubleOptions = new DoubleOptions().withReturnEnabled(returnEnabled)
                        .withSearchEnabled(searchEnabled).withSortEnabled(sortEnabled).withFacetEnabled(false);
                indexfield.setDoubleOptions(doubleOptions);
                return indexfield;
            case DOUBLE_ARRAY:
                indexfield.setIndexFieldType(IndexFieldType.DoubleArray);
                final DoubleArrayOptions doubleArrayOptions = new DoubleArrayOptions().withReturnEnabled(returnEnabled)
                        .withSearchEnabled(searchEnabled).withFacetEnabled(false);
                indexfield.setDoubleArrayOptions(doubleArrayOptions);
                return indexfield;
            case INT:
                indexfield.setIndexFieldType(IndexFieldType.Int);
                final IntOptions intOptions = new IntOptions().withReturnEnabled(returnEnabled)
                        .withSearchEnabled(searchEnabled).withSortEnabled(sortEnabled).withFacetEnabled(false);
                indexfield.setIntOptions(intOptions);
                return indexfield;
            case INT_ARRAY:
                indexfield.setIndexFieldType(IndexFieldType.IntArray);
                final IntArrayOptions intArrayOptions = new IntArrayOptions().withReturnEnabled(returnEnabled)
                        .withSearchEnabled(searchEnabled).withFacetEnabled(false);
                indexfield.setIntArrayOptions(intArrayOptions);
                return indexfield;
            case LATLON:
                indexfield.setIndexFieldType(IndexFieldType.Latlon);
                final LatLonOptions latLonOptions = new LatLonOptions().withReturnEnabled(returnEnabled)
                        .withSearchEnabled(searchEnabled).withSortEnabled(sortEnabled).withFacetEnabled(false);
                indexfield.setLatLonOptions(latLonOptions);
                return indexfield;
            case LITERAL:
                indexfield.setIndexFieldType(IndexFieldType.Literal);
                final LiteralOptions literalOptions = new LiteralOptions().withReturnEnabled(returnEnabled)
                        .withSearchEnabled(searchEnabled).withSortEnabled(sortEnabled).withFacetEnabled(false);
                indexfield.setLiteralOptions(literalOptions);
                return indexfield;
            case LITERAL_ARRAY:
                indexfield.setIndexFieldType(IndexFieldType.LiteralArray);
                final LiteralArrayOptions literalArrayOptions = new LiteralArrayOptions()
                        .withReturnEnabled(returnEnabled).withSearchEnabled(searchEnabled).withFacetEnabled(false);
                indexfield.setLiteralArrayOptions(literalArrayOptions);
                return indexfield;
            case TEXT:
                indexfield.setIndexFieldType(IndexFieldType.Text);
                final TextOptions textOptions = new TextOptions().withReturnEnabled(returnEnabled).withSortEnabled(
                        sortEnabled);
                indexfield.setTextOptions(textOptions);
                return indexfield;
            case TEXT_ARRAY:
                indexfield.setIndexFieldType(IndexFieldType.TextArray);
                final TextArrayOptions textArrayOptions = new TextArrayOptions().withReturnEnabled(returnEnabled);
                indexfield.setTextArrayOptions(textArrayOptions);
                return indexfield;
            default:
                throw new IllegalStateException(String.format("Index field type is unknown. Type received was %s",
                        indexDefinition.getFieldType()));
        }
    }

}
