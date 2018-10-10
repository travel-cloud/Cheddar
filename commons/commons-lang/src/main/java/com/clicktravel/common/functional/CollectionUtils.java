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

import java.util.ArrayList;
import java.util.List;

public class CollectionUtils {

    public static <T> List<List<T>> partition(final List<T> items, final int size) {
        if (items == null) {
            throw new IllegalArgumentException("Items must not be null");
        }
        if (size < 0) {
            throw new IllegalArgumentException("Size must be more than 0");
        }
        final int maxIndex = items.size() < size ? items.size() : size;
        final List<List<T>> lists = new ArrayList<>();
        lists.add(items.subList(0, maxIndex));
        final List<T> remainingItems = items.subList(maxIndex, items.size());
        if (remainingItems.size() > 0) {
            final List<List<T>> remainingLists = partition(remainingItems, size);
            lists.addAll(remainingLists);
        }
        return lists;
    }

}
