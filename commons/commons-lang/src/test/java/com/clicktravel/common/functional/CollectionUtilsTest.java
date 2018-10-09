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
package com.clicktravel.common.functional;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class CollectionUtilsTest {

    @Test
    public void shouldPartionList_withItemLengthEqualToSize() throws Exception {
        final List<Integer> items = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        final int size = 10;
        final List<List<Integer>> expected = Arrays.asList(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

        final List<List<Integer>> actual = CollectionUtils.partition(items, size);

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void shouldPartionList_withItemLengthHalfTheSize() throws Exception {
        final List<Integer> items = Arrays.asList(1, 2, 3, 4, 5);
        final int size = 10;
        final List<List<Integer>> expected = Arrays.asList(Arrays.asList(1, 2, 3, 4, 5));

        final List<List<Integer>> actual = CollectionUtils.partition(items, size);

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void shouldPartionList_withItemLengthDoubleTheSize() throws Exception {
        final List<Integer> items = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        final int size = 5;
        final List<List<Integer>> expected = Arrays.asList(Arrays.asList(1, 2, 3, 4, 5), Arrays.asList(6, 7, 8, 9, 10));

        final List<List<Integer>> actual = CollectionUtils.partition(items, size);

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void shouldPartionList_withItemLengthMoreThanDoubleTheSize() throws Exception {
        final List<Integer> items = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        final int size = 5;
        final List<List<Integer>> expected = Arrays.asList(Arrays.asList(1, 2, 3, 4, 5), Arrays.asList(6, 7, 8, 9, 10),
                Arrays.asList(11));

        final List<List<Integer>> actual = CollectionUtils.partition(items, size);

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void shouldPartionList_withItemLengthOfZero() throws Exception {
        final List<Integer> items = Arrays.asList();
        final int size = 5;
        final List<List<Integer>> expected = Arrays.asList(Arrays.asList());

        final List<List<Integer>> actual = CollectionUtils.partition(items, size);

        Assert.assertEquals(actual, expected);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotPartionList_withNegativeSize() throws Exception {
        final List<Integer> items = Arrays.asList(1, 2, 3, 4, 5);
        final int size = -1;

        CollectionUtils.partition(items, size);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotPartionList_withNullItems() throws Exception {
        final List<Integer> items = null;
        final int size = 10;

        CollectionUtils.partition(items, size);
    }

}