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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.persistence.document.search.sort.SortOrder;

public class SearchOptionsTest {

    @Test
    public void shouldCreateDefaultSearchOptions() {
        final SearchOptions options = SearchOptions.DEFAULT;
        assertEquals(SortOrder.DEFAULT, options.getSortOrder());
        assertTrue(options.getExpressions().isEmpty());
    }

    @Test
    public void shouldCreateDefaultSearchOptionsWithNewSortOrder() {
        final SortOrder order = new SortOrder();
        final SearchOptions options = new SearchOptions().withSortOrder(order);
        assertEquals(order, options.getSortOrder());
        assertNotEquals(SortOrder.DEFAULT, options.getSortOrder());
    }

    @Test
    public void shouldCreateDefaultSearchOptionsWithNewExpressions() {
        final Map<String, String> expressions = new HashMap<String, String>();
        expressions.put("key", "expression");
        final SearchOptions options = new SearchOptions().withExpressions(expressions);
        assertEquals(expressions, options.getExpressions());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailCreateDefaultSearchOptionsWithNullSortOrder() {
        new SearchOptions().withSortOrder(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailCreateDefaultSearchOptionsWithNullExpressions() {
        new SearchOptions().withExpressions(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailSetDefaultSearchOptionsWithNullSortOrder() {
        final SearchOptions options = new SearchOptions();
        options.setExpressions(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailSetDefaultSearchOptionsWithNullExpressions() {
        final SearchOptions options = new SearchOptions();
        options.setExpressions(null);
    }

}
