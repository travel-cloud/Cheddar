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
package com.clicktravel.cheddar.infrastructure.persistence.database;

import java.util.List;

public interface BatchDatabaseTemplate extends DatabaseTemplate {
    /**
     * Batch write a list of items (max 25) to the store. This operation is non-transactional, does not support unique
     * constraints and does not support optimistic locking. These must be taken into consideration when implementing
     * this operation.
     * @param items - a list of items to be batch written
     * @param itemClass - the class of the item being batch written. This allows us to reject any batch writes for a
     *            class that has unique constraints.
     * @return a list of the successfully written items from the batch. Any unsuccessful writes are removed before
     *         returning.
     */
    <T extends Item> List<T> batchWrite(final List<T> items, final Class<T> itemClass);

}
