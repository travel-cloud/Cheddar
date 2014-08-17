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

import com.clicktravel.common.functional.Equals;

public class IndexDefinition {

    private final String name;
    private final IndexFieldType indexFieldType;
    private final boolean searchEnabled;
    private final boolean returnEnabled;
    private final boolean sortEnabled;

    public IndexDefinition(final String name, final IndexFieldType indexFieldType, final boolean searchEnabled,
            final boolean returnEnabled, final boolean sortEnabled) {
        if (Equals.isNullOrBlank(name)) {
            throw new IllegalArgumentException("Index property name cannot be empty");
        }
        if (indexFieldType == null) {
            throw new IllegalArgumentException("Index field type cannot be null");
        }
        if ((indexFieldType == IndexFieldType.TEXT || indexFieldType == IndexFieldType.TEXT_ARRAY) && !searchEnabled) {
            throw new IllegalArgumentException("Index fields of type TEXT or TEXT_ARRAY must be searchable");
        }
        if (indexFieldType.isArray() && sortEnabled) {
            throw new IllegalArgumentException("Cannot sort on index field of array type");
        }

        this.name = name;
        this.indexFieldType = indexFieldType;
        this.searchEnabled = searchEnabled;
        this.returnEnabled = returnEnabled;
        this.sortEnabled = sortEnabled;
    }

    public IndexDefinition(final String name, final IndexFieldType indexFieldType) {
        this(name, indexFieldType, true, true, true);
    }

    public String getName() {
        return name;
    }

    public IndexFieldType getFieldType() {
        return indexFieldType;
    }

    public boolean isSortEnabled() {
        return sortEnabled;
    }

    public boolean isSearchEnabled() {
        return searchEnabled;
    }

    public boolean isReturnEnabled() {
        return returnEnabled;
    }

}
