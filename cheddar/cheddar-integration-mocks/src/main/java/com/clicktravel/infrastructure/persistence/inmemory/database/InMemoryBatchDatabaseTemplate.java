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
package com.clicktravel.infrastructure.persistence.inmemory.database;

import java.util.ArrayList;
import java.util.List;

import com.clicktravel.cheddar.infrastructure.persistence.database.BatchDatabaseTemplate;
import com.clicktravel.cheddar.infrastructure.persistence.database.Item;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.DatabaseSchemaHolder;

public class InMemoryBatchDatabaseTemplate extends InMemoryDatabaseTemplate implements BatchDatabaseTemplate {

    public InMemoryBatchDatabaseTemplate(final DatabaseSchemaHolder databaseSchemaHolder) {
        super(databaseSchemaHolder);
    }

    @Override
    public <T extends Item> List<T> batchWrite(final List<T> items, final Class<T> itemClass) {
        final List<T> updatedItems = new ArrayList<>();
        for (final T item : items) {
            final T updatedItem = update(item);
            updatedItems.add(updatedItem);
        }
        return updatedItems;
    }

}
