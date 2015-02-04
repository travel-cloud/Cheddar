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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonUniqueResultException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.handler.PersistenceExceptionHandler;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Query;

public class AbstractDatabaseTemplateTest {

    @Test
    public void shouldFetchUnique_withSingleResultFromQuery() throws Exception {
        // Given
        final StubItem item = new StubItem();
        final Collection<StubItem> items = Arrays.asList(item);
        final Query mockQuery = mock(Query.class);
        final AbstractDatabaseTemplate databaseTemplate = new AbstractDatabaseTemplate() {

            @Override
            public <T extends Item> T read(final ItemId key, final Class<T> itemClass) throws NonExistentItemException {
                return null;
            }

            @Override
            public <T extends Item> T create(final T item,
                    final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {
                return null;
            }

            @Override
            public <T extends Item> T update(final T item,
                    final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {
                return null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T extends Item> Collection<T> fetch(final Query query, final Class<T> itemClass) {
                return (Collection<T>) items;
            }

            @Override
            public GeneratedKeyHolder generateKeys(final SequenceKeyGenerator sequenceKeyGenerator) {
                return null;
            }

            @Override
            public void delete(final Item item, final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {
            }

        };

        // When
        final StubItem returnedItem = databaseTemplate.fetchUnique(mockQuery, StubItem.class);

        // Then
        assertEquals(item, returnedItem);
    }

    @Test
    public void shouldNotFetchUnique_withMultipleResultFromQuery() throws Exception {
        // Given
        final Collection<StubItem> items = Arrays.asList(new StubItem(), new StubItem(), new StubItem());
        final Query mockQuery = mock(Query.class);
        final AbstractDatabaseTemplate databaseTemplate = new AbstractDatabaseTemplate() {

            @Override
            public <T extends Item> T update(final T item,
                    final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {
                return null;
            }

            @Override
            public <T extends Item> T read(final ItemId key, final Class<T> itemClass) throws NonExistentItemException {
                return null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T extends Item> Collection<T> fetch(final Query query, final Class<T> itemClass) {
                return (Collection<T>) items;
            }

            @Override
            public void delete(final Item item, final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {
            }

            @Override
            public <T extends Item> T create(final T item,
                    final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {
                return null;
            }

            @Override
            public GeneratedKeyHolder generateKeys(final SequenceKeyGenerator sequenceKeyGenerator) {
                return null;
            }

        };

        // When
        NonUniqueResultException actualException = null;
        try {
            databaseTemplate.fetchUnique(mockQuery, StubItem.class);
        } catch (final NonUniqueResultException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }
}
