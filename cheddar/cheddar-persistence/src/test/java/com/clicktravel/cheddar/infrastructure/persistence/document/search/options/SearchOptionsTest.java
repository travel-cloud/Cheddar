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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.persistence.document.search.sort.SortOrder;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.sort.SortingOption;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.sort.SortingOption.Direction;
import com.clicktravel.common.random.Randoms;

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
        assertTrue(SortOrder.DEFAULT != options.getSortOrder());
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

    @Test
    public void shouldSetSortOrder() {
        final SearchOptions options = new SearchOptions();
        final SortOrder order = new SortOrder();
        options.setSortOrder(order);
        assertEquals(order, options.getSortOrder());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailSetDefaultSearchOptionsWithNullSortOrder() {
        final SearchOptions options = new SearchOptions();
        options.setSortOrder(null);
    }

    @Test
    public void shouldSetExpressions() {
        final SearchOptions options = new SearchOptions();
        final Map<String, String> expressions = new HashMap<String, String>();
        options.setExpressions(expressions);
        assertEquals(expressions, options.getExpressions());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailSetDefaultSearchOptionsWithNullExpressions() {
        final SearchOptions options = new SearchOptions();
        options.setExpressions(null);
    }

    @Test
    public void shouldGetSameHashCode_withDifferentObjects() {

        final SearchOptions searchOptions001 = new SearchOptions();
        final SearchOptions searchOptions002 = new SearchOptions();

        assertEquals(searchOptions001.hashCode(), searchOptions002.hashCode());
    }

    @Test
    public void shouldEquals_withSameObject() {
        final SearchOptions searchOptions = new SearchOptions();

        assertTrue(searchOptions.equals(searchOptions));
    }

    @Test
    public void shouldNotEquals_withNull() {
        final SearchOptions searchOptions = new SearchOptions();

        assertFalse(searchOptions.equals(null));
    }

    @Test
    public void shouldNotEquals_withDifferentClass() {
        final SearchOptions searchOptions = new SearchOptions();

        assertFalse(searchOptions.equals(Randoms.randomString(20)));
    }

    @Test
    public void shouldNotEquals_withDifferentExpressions() {

        final SearchOptions searchOptions001 = new SearchOptions();

        final Map<String, String> expressions = new HashMap<String, String>();
        expressions.put(Randoms.randomString(20), Randoms.randomString(20));
        final SearchOptions searchOptions002 = new SearchOptions().withExpressions(expressions);

        assertFalse(searchOptions001.equals(searchOptions002));
        assertFalse(searchOptions002.equals(searchOptions001));
    }

    @Test
    public void shouldNotEquals_withDifferentSortOrders() {

        final SearchOptions searchOptions001 = new SearchOptions();

        final SortingOption sortingOption = new SortingOption(Randoms.randomString(20),
                Randoms.randomEnum(Direction.class));
        final SortOrder sortOrder = new SortOrder();
        sortOrder.addSortingOption(sortingOption);
        final SearchOptions searchOptions002 = new SearchOptions().withSortOrder(sortOrder);

        assertFalse(searchOptions001.equals(searchOptions002));
        assertFalse(searchOptions002.equals(searchOptions001));
    }

    @Test
    public void shouldEquals() {

        final SearchOptions searchOptions001 = new SearchOptions();

        final SearchOptions searchOptions002 = new SearchOptions();

        assertTrue(searchOptions001.equals(searchOptions002));
        assertTrue(searchOptions002.equals(searchOptions001));
    }
}
