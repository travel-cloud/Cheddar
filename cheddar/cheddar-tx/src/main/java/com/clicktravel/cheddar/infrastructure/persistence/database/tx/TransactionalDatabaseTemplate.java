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
package com.clicktravel.cheddar.infrastructure.persistence.database.tx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.infrastructure.persistence.database.*;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonUniqueResultException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.handler.PersistenceExceptionHandler;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Query;
import com.clicktravel.cheddar.infrastructure.tx.*;

public class TransactionalDatabaseTemplate implements DatabaseTemplate, TransactionalResource {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DatabaseTemplate databaseTemplate;

    private final ThreadLocal<DatabaseTransaction> currentTransaction = new ThreadLocal<DatabaseTransaction>();

    public TransactionalDatabaseTemplate(final DatabaseTemplate databaseTemplate) {
        this.databaseTemplate = databaseTemplate;
    }

    private DatabaseTransaction getCurrentTransaction() {
        if (currentTransaction.get() == null) {
            throw new NonExistentTransactionException();
        }
        return currentTransaction.get();
    }

    @Override
    public void begin() throws TransactionException {
        if (currentTransaction.get() != null) {
            throw new NestedTransactionException(currentTransaction.get());
        }
        currentTransaction.set(new DatabaseTransaction());
        logger.trace("Beginning transaction: " + currentTransaction.get().transactionId());
    }

    @Override
    public void commit() throws TransactionException {
        final DatabaseTransaction transaction = getCurrentTransaction();
        logger.trace("Committing transaction: " + transaction.transactionId());
        try {
            transaction.applyActions(databaseTemplate);
            currentTransaction.remove();
            logger.trace("Transaction successfully committed: " + transaction.transactionId());
        } catch (final Throwable e) {
            throw new TransactionalResourceException("Failed to commit DatabaseTemplate transaction: "
                    + transaction.transactionId(), e);
        }
    }

    @Override
    public <T extends Item> T create(final T item, final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {
        final DatabaseTransaction transaction = getCurrentTransaction();
        final List<PersistenceExceptionHandler<?>> persistenceExceptionHandlerList = new ArrayList<PersistenceExceptionHandler<?>>();
        Collections.addAll(persistenceExceptionHandlerList, persistenceExceptionHandlers);
        final T createdItem = transaction.addCreateAction(item, persistenceExceptionHandlerList);
        return createdItem;
    }

    @Override
    public <T extends Item> T update(final T item, final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {
        final DatabaseTransaction transaction = getCurrentTransaction();
        final List<PersistenceExceptionHandler<?>> persistenceExceptionHandlerList = new ArrayList<PersistenceExceptionHandler<?>>();
        Collections.addAll(persistenceExceptionHandlerList, persistenceExceptionHandlers);
        final T createdItem = transaction.addUpdateAction(item, persistenceExceptionHandlerList);
        return createdItem;
    }

    @Override
    public void delete(final Item item, final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {
        final DatabaseTransaction transaction = getCurrentTransaction();
        final List<PersistenceExceptionHandler<?>> persistenceExceptionHandlerList = new ArrayList<PersistenceExceptionHandler<?>>();
        Collections.addAll(persistenceExceptionHandlerList, persistenceExceptionHandlers);
        transaction.addDeleteAction(item, persistenceExceptionHandlerList);
    }

    @Override
    public <T extends Item> T read(final ItemId itemId, final Class<T> itemClass) throws NonExistentItemException {
        return databaseTemplate.read(itemId, itemClass);
    }

    @Override
    public <T extends Item> Collection<T> fetch(final Query query, final Class<T> itemClass) {
        return databaseTemplate.fetch(query, itemClass);
    }

    @Override
    public <T extends Item> T fetchUnique(final Query query, final Class<T> itemClass) throws NonUniqueResultException {
        return databaseTemplate.fetchUnique(query, itemClass);
    }

    @Override
    public GeneratedKeyHolder generateKeys(final SequenceKeyGenerator sequenceKeyGenerator) {
        return databaseTemplate.generateKeys(sequenceKeyGenerator);
    }

    @Override
    public void abort() throws TransactionException {
        currentTransaction.remove();
    }

}
