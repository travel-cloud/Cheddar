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

import java.util.Collection;

import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonUniqueResultException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.handler.PersistenceExceptionHandler;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Query;

public interface DatabaseTemplate {

    /**
     * Read a single item identified by its id from the store
     * @param itemId
     * @return Item with the specified itemId
     * @throws NonExistentItemException - this exception will be thrown if not matching item could located in the
     *             persistence store
     */
    <T extends Item> T read(final ItemId itemId, Class<T> itemClass) throws NonExistentItemException;

    <T extends Item> T create(T item, PersistenceExceptionHandler<?>... persistenceExceptionHandlers);

    <T extends Item> T update(T item, PersistenceExceptionHandler<?>... persistenceExceptionHandlers);

    void delete(Item item, PersistenceExceptionHandler<?>... persistenceExceptionHandlers);

    <T extends Item> Collection<T> fetch(final Query query, Class<T> itemClass);

    <T extends Item> T fetchUnique(final Query query, Class<T> itemClass) throws NonUniqueResultException;

    GeneratedKeyHolder generateKeys(SequenceKeyGenerator sequenceKeyGenerator);

}