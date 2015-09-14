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
package com.clicktravel.cheddar.infrastructure.persistence.document.search.sort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.persistence.document.search.sort.SortingOption.Direction;
import com.clicktravel.common.random.Randoms;

public class SortOrderTest {

    @Test
    public void shouldConstruct() {
        final SortOrder sortOrder = new SortOrder();

        assertNotNull(sortOrder);
    }

    @Test
    public void shouldGetDefault() {
        final SortOrder sortOrder = SortOrder.DEFAULT;

        assertNotNull(sortOrder);
    }

    @Test
    public void shouldGetSameDefault() {
        final SortOrder sortOrder001 = SortOrder.DEFAULT;
        final SortOrder sortOrder002 = SortOrder.DEFAULT;

        assertEquals(sortOrder001, sortOrder002);
    }

    @Test
    public void shouldGetSortingOption() {
        final SortOrder sortOrder = new SortOrder();

        final List<SortingOption> sortingOptions = sortOrder.sortingOptions();

        assertEquals(0, sortingOptions.size());
    }

    @Test
    public void shouldAddSortingOption() {
        final SortingOption sortingOption = new SortingOption(Randoms.randomString(20));
        final SortOrder sortOrder = new SortOrder();

        sortOrder.addSortingOption(sortingOption);

        assertEquals(1, sortOrder.sortingOptions().size());
    }

    @Test
    public void shouldGetSameHashCode_withDifferentObjects() {
        final String expectedKey = Randoms.randomString(20);
        final Direction expectedDirection = Randoms.randomEnum(Direction.class);

        final SortingOption sortingOption001 = new SortingOption(expectedKey, expectedDirection);
        final SortingOption sortingOption002 = new SortingOption(expectedKey, expectedDirection);

        final SortOrder sortOrder001 = new SortOrder();
        final SortOrder sortOrder002 = new SortOrder();

        sortOrder001.addSortingOption(sortingOption001);
        sortOrder002.addSortingOption(sortingOption002);

        assertEquals(sortOrder001.hashCode(), sortOrder002.hashCode());
    }

    @Test
    public void shouldEquals_withSameObject() {
        final SortOrder sortOrder = new SortOrder();

        assertTrue(sortOrder.equals(sortOrder));
    }

    @Test
    public void shouldNotEquals_withNull() {
        final SortOrder sortOrder = new SortOrder();

        assertFalse(sortOrder.equals(null));
    }

    @Test
    public void shouldNotEquals_withDifferentClass() {
        final SortOrder sortOrder = new SortOrder();

        assertFalse(sortOrder.equals(Randoms.randomString(20)));
    }

    @Test
    public void shouldNotEquals_withDifferentSortingOptions() {
        final SortingOption sortingOption001 = new SortingOption(Randoms.randomString(20),
                Randoms.randomEnum(Direction.class));
        final SortingOption sortingOption002 = new SortingOption(Randoms.randomString(20),
                Randoms.randomEnum(Direction.class));

        final SortOrder sortOrder001 = new SortOrder();
        final SortOrder sortOrder002 = new SortOrder();

        sortOrder001.addSortingOption(sortingOption001);
        sortOrder002.addSortingOption(sortingOption002);

        assertFalse(sortOrder001.equals(sortOrder002));
        assertFalse(sortOrder002.equals(sortOrder001));
    }

    @Test
    public void shouldEquals_withSameLogicalSortingOption() {
        final String expectedKey = Randoms.randomString(20);
        final Direction expectedDirection = Randoms.randomEnum(Direction.class);

        final SortingOption sortingOption001 = new SortingOption(expectedKey, expectedDirection);
        final SortingOption sortingOption002 = new SortingOption(expectedKey, expectedDirection);

        final SortOrder sortOrder001 = new SortOrder();
        final SortOrder sortOrder002 = new SortOrder();

        sortOrder001.addSortingOption(sortingOption001);
        sortOrder002.addSortingOption(sortingOption002);

        assertTrue(sortOrder001.equals(sortOrder002));
        assertTrue(sortOrder002.equals(sortOrder001));
    }

    @Test
    public void shouldEquals_withSameSortingOption() {
        final SortingOption sortingOption = new SortingOption(Randoms.randomString(20),
                Randoms.randomEnum(Direction.class));

        final SortOrder sortOrder001 = new SortOrder();
        final SortOrder sortOrder002 = new SortOrder();

        sortOrder001.addSortingOption(sortingOption);
        sortOrder002.addSortingOption(sortingOption);

        assertTrue(sortOrder001.equals(sortOrder002));
        assertTrue(sortOrder002.equals(sortOrder001));
    }
}
