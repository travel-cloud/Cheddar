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
package com.clicktravel.cheddar.infrastructure.persistence.document.search.options;

import java.util.HashMap;
import java.util.Map;

import com.clicktravel.cheddar.infrastructure.persistence.document.search.sort.SortOrder;

/**
 * SearchOptions are custom values that can be passed to a search query such as sorting or expressions
 */
public class SearchOptions {

    public static final SearchOptions DEFAULT = new SearchOptions();

    /**
     * Sorting option applied to the query defaults to the default value and cannot be null
     */
    private SortOrder sortOrder;

    /**
     * Map of expressions that you wish the search engine to evaluate as part of the request
     */
    private Map<String, String> expressions;

    public SearchOptions() {
        sortOrder = SortOrder.DEFAULT;
        expressions = new HashMap<String, String>();
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(final SortOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("SortOrder cannot be null");
        }
        sortOrder = order;
    }

    public Map<String, String> getExpressions() {
        return expressions;
    }

    public void setExpressions(final Map<String, String> expressions) {
        if (expressions == null) {
            throw new IllegalArgumentException("Expressions cannot be null but can be empty");
        }
        this.expressions = expressions;
    }

    public SearchOptions withSortOrder(final SortOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("SortOrder cannot be null");
        }
        sortOrder = order;
        return this;
    }

    public SearchOptions withExpressions(final Map<String, String> expressions) {
        if (expressions == null) {
            throw new IllegalArgumentException("Expressions cannot be null but can be empty");
        }
        this.expressions = expressions;
        return this;
    }

}
