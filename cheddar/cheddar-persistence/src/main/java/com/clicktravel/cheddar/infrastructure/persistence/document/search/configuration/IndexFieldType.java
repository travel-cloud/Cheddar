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

public enum IndexFieldType {
    DATETIME,
    DATETIME_ARRAY,
    DOUBLE,
    DOUBLE_ARRAY,
    INT,
    INT_ARRAY,
    LATLON,
    LITERAL,
    LITERAL_ARRAY,
    TEXT,
    TEXT_ARRAY;

    public boolean isArray() {
        return this == DATETIME_ARRAY || this == DOUBLE_ARRAY || this == INT_ARRAY || this == TEXT_ARRAY
                || this == LITERAL_ARRAY;
    }
}
