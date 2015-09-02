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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.persistence.document.search.sort.SortingOption.Direction;
import com.clicktravel.common.random.Randoms;

public class SortingOptionTest {

    @Test
    public void shouldConstruct_withKey() {

        final String expectedKey = Randoms.randomString(20);

        final SortingOption sortingOption = new SortingOption(expectedKey);

        assertEquals(expectedKey, sortingOption.key());
        assertEquals(Direction.ASCENDING, sortingOption.direction());
    }

    @Test
    public void shouldConstruct_withKeyAndDirection() {

        final String expectedKey = Randoms.randomString(20);
        final Direction expectedDirection = Randoms.randomEnum(Direction.class);

        final SortingOption sortingOption = new SortingOption(expectedKey, expectedDirection);

        assertEquals(expectedKey, sortingOption.key());
        assertEquals(expectedDirection, sortingOption.direction());
    }

    @Test
    public void shouldGetSameHashCode_withDifferentObjects() {

        final String expectedKey = Randoms.randomString(20);
        final Direction expectedDirection = Randoms.randomEnum(Direction.class);

        final SortingOption sortingOption001 = new SortingOption(expectedKey, expectedDirection);
        final SortingOption sortingOption002 = new SortingOption(expectedKey, expectedDirection);

        assertEquals(sortingOption001.hashCode(), sortingOption002.hashCode());
    }

    @Test
    public void shouldGetHashCode_withNullKeyAndNullDirection() {

        final SortingOption sortingOption = new SortingOption(null, null);

        assertEquals(961, sortingOption.hashCode());
    }

    @Test
    public void shouldGetHashCode_withNullDirection() {

        final String expectedKey = Randoms.randomString(20);

        final SortingOption sortingOption = new SortingOption(expectedKey, null);

        assertTrue(String.format("%d should be different to the default: %d", sortingOption.hashCode(), 961),
                sortingOption.hashCode() != 961);
    }

    @Test
    public void shouldGetHashCode_withNullKey() {

        final Direction expectedDirection = Randoms.randomEnum(Direction.class);

        final SortingOption sortingOption = new SortingOption(null, expectedDirection);

        assertTrue(String.format("%d should be different to the default: %d", sortingOption.hashCode(), 961),
                sortingOption.hashCode() != 961);
    }

    @Test
    public void shouldEquals_withSameObject() {

        final String expectedKey = Randoms.randomString(20);
        final Direction expectedDirection = Randoms.randomEnum(Direction.class);

        final SortingOption sortingOption = new SortingOption(expectedKey, expectedDirection);

        assertTrue(sortingOption.equals(sortingOption));
    }

    @Test
    public void shouldNotEquals_withNull() {

        final String expectedKey = Randoms.randomString(20);
        final Direction expectedDirection = Randoms.randomEnum(Direction.class);

        final SortingOption sortingOption = new SortingOption(expectedKey, expectedDirection);

        assertFalse(sortingOption.equals(null));
    }

    @Test
    public void shouldNotEquals_withDifferentClass() {

        final String expectedKey = Randoms.randomString(20);
        final Direction expectedDirection = Randoms.randomEnum(Direction.class);

        final SortingOption sortingOption = new SortingOption(expectedKey, expectedDirection);

        assertFalse(sortingOption.equals(Randoms.randomString()));
    }

    @Test
    public void shouldNotEquals_withDifferentDirections() {

        final String expectedKey = Randoms.randomString(20);
        final Direction ascendingDirection = Direction.ASCENDING;
        final Direction descendingDirection = Direction.DESCENDING;

        final SortingOption sortingOptionAscendingDirection = new SortingOption(expectedKey, ascendingDirection);
        final SortingOption sortingOptionDescendingDirection = new SortingOption(expectedKey, descendingDirection);

        assertFalse(sortingOptionAscendingDirection.equals(sortingOptionDescendingDirection));
        assertFalse(sortingOptionDescendingDirection.equals(sortingOptionAscendingDirection));
    }

    @Test
    public void shouldNotEquals_withOneNullKey() {

        final String expectedKey = Randoms.randomString(20);
        final Direction expectedDirection = Randoms.randomEnum(Direction.class);

        final SortingOption sortingOptionNullKey = new SortingOption(null, expectedDirection);
        final SortingOption sortingOption = new SortingOption(expectedKey, expectedDirection);

        assertFalse(sortingOptionNullKey.equals(sortingOption));
        assertFalse(sortingOption.equals(sortingOptionNullKey));
    }

    @Test
    public void shouldEquals_withTwoNullKey() {

        final Direction expectedDirection = Randoms.randomEnum(Direction.class);

        final SortingOption sortingOptionNullKey001 = new SortingOption(null, expectedDirection);
        final SortingOption sortingOptionNullKey002 = new SortingOption(null, expectedDirection);

        assertTrue(sortingOptionNullKey001.equals(sortingOptionNullKey002));
        assertTrue(sortingOptionNullKey002.equals(sortingOptionNullKey001));
    }

    @Test
    public void shouldNotEquals_withDifferentKeys() {

        final String expectedKey001 = Randoms.randomString(20);
        final String expectedKey002 = Randoms.randomString(20);
        final Direction expectedDirection = Randoms.randomEnum(Direction.class);

        final SortingOption sortingOption001 = new SortingOption(expectedKey001, expectedDirection);
        final SortingOption sortingOption002 = new SortingOption(expectedKey002, expectedDirection);

        assertFalse(sortingOption001.equals(sortingOption002));
        assertFalse(sortingOption002.equals(sortingOption001));
    }

    @Test
    public void shouldEquals() {

        final String expectedKey = Randoms.randomString(20);
        final Direction expectedDirection = Randoms.randomEnum(Direction.class);

        final SortingOption sortingOption001 = new SortingOption(expectedKey, expectedDirection);
        final SortingOption sortingOption002 = new SortingOption(expectedKey, expectedDirection);

        assertTrue(sortingOption001.equals(sortingOption002));
        assertTrue(sortingOption002.equals(sortingOption001));
    }
}
