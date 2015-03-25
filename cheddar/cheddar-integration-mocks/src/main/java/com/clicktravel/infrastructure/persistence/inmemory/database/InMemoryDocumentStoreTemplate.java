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

import java.util.Collection;

import com.clicktravel.cheddar.infrastructure.persistence.database.DocumentItem;
import com.clicktravel.cheddar.infrastructure.persistence.database.DocumentStoreTemplate;
import com.clicktravel.cheddar.infrastructure.persistence.database.ItemId;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.DatabaseSchemaHolder;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonUniqueResultException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.handler.PersistenceExceptionHandler;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Query;

public class InMemoryDocumentStoreTemplate extends InMemoryDatabaseTemplate implements DocumentStoreTemplate {

    public InMemoryDocumentStoreTemplate(final DatabaseSchemaHolder databaseSchemaHolder) {
        super(databaseSchemaHolder);
    }

    @Override
    public <T extends DocumentItem> T createDocument(final T item,
            final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {
        return super.create(item, persistenceExceptionHandlers);
    }

    @Override
    public <T extends DocumentItem> T readDocument(final ItemId itemId, final Class<T> itemClass)
            throws NonExistentItemException {
        return super.read(itemId, itemClass);
    }

    @Override
    public <T extends DocumentItem> T updateDocument(final T item,
            final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {
        return super.update(item, persistenceExceptionHandlers);
    }

    @Override
    public <T extends DocumentItem> void deleteDocument(final T item,
            final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {
        super.delete(item, persistenceExceptionHandlers);

    }

    @Override
    public <T extends DocumentItem> Collection<T> fetchDocuments(final Query query, final Class<T> itemClass) {
        return super.fetch(query, itemClass);
    }

    @Override
    public <T extends DocumentItem> T fetchUniqueDocument(final Query query, final Class<T> itemClass)
            throws NonUniqueResultException {
        return super.fetchUnique(query, itemClass);
    }
}
