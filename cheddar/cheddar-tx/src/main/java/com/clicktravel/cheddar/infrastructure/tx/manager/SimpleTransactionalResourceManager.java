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
package com.clicktravel.cheddar.infrastructure.tx.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.infrastructure.tx.*;

public class SimpleTransactionalResourceManager implements TransactionalResourceManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ThreadLocal<ResourceManagerTransaction> currentTransaction = new ThreadLocal<>();
    private TransactionalResource transactionalDatabaseTemplate;
    private TransactionalResource transactionalFileStore;
    private TransactionalResource transactionalMessageSender;
    private TransactionalResource transactionalMessagePublisher;

    @Override
    public boolean inTransaction() {
        return currentTransaction.get() != null;
    }

    @Override
    public void begin() throws TransactionException {
        if (inTransaction()) {
            throw new NestedTransactionException(currentTransaction.get());
        }
        currentTransaction.set(new ResourceManagerTransaction());
        logger.trace("Beginning global transaction: " + currentTransaction.get().transactionId());
        beginMessagePublisherTransaction();
        beginMessageSenderTransaction();
        beginFilestoreTransaction();
        beginDatabaseTemplateTransaction();
    }

    @Override
    public void commit() throws TransactionException {
        if (!inTransaction()) {
            throw new NonExistentTransactionException();
        }
        logger.trace("Committing global transaction");

        // Order of commits is important

        // Some constraints in the domain are enforced on 'save' actions e.g. uniqueness constraint
        // It is unsafe for the remaining commits to proceed if this commit is not completed first
        commitDatabaseTemplateTransaction();

        commitFileStoreTransaction();

        commitMessageSenderTransaction();

        // By doing this commit last, we ensure events are published only if all other actions in this
        // transaction committed OK
        commitMessagePublisherTransaction();

        currentTransaction.remove();
    }

    @Override
    public void abort() {
        logger.trace("Aborting global transaction");
        try {
            abortDatabaseTemplateTransaction();
            abortFileStoreTransaction();
            abortMessageSenderTransaction();
            abortMessagePublisherTransaction();
        } finally {
            currentTransaction.remove();
        }
    }

    private void beginDatabaseTemplateTransaction() throws TransactionException {
        if (transactionalDatabaseTemplate != null) {
            transactionalDatabaseTemplate.begin();
        }
    }

    private void beginFilestoreTransaction() throws TransactionException {
        if (transactionalFileStore != null) {
            transactionalFileStore.begin();
        }
    }

    private void beginMessageSenderTransaction() throws TransactionException {
        if (transactionalMessageSender != null) {
            transactionalMessageSender.begin();
        }
    }

    private void beginMessagePublisherTransaction() throws TransactionException {
        if (transactionalMessagePublisher != null) {
            transactionalMessagePublisher.begin();
        }
    }

    private void commitDatabaseTemplateTransaction() throws TransactionException {
        if (transactionalDatabaseTemplate != null) {
            transactionalDatabaseTemplate.commit();
        }
    }

    private void commitFileStoreTransaction() throws TransactionException {
        if (transactionalFileStore != null) {
            transactionalFileStore.commit();
        }
    }

    private void commitMessageSenderTransaction() throws TransactionException {
        if (transactionalMessageSender != null) {
            transactionalMessageSender.commit();
        }
    }

    private void commitMessagePublisherTransaction() throws TransactionException {
        if (transactionalMessagePublisher != null) {
            transactionalMessagePublisher.commit();
        }
    }

    private void abortDatabaseTemplateTransaction() {
        if (transactionalDatabaseTemplate != null) {
            try {
                transactionalDatabaseTemplate.abort();
            } catch (final TransactionException e) {
                logger.error("Problem aborting DatabaseTemplate transaction : " + e.getMessage());
            }
        }
    }

    private void abortFileStoreTransaction() {
        if (transactionalFileStore != null) {
            try {
                transactionalFileStore.abort();
            } catch (final TransactionException e) {
                logger.error("Problem aborting FileStore transaction : " + e.getMessage());
            }
        }
    }

    private void abortMessageSenderTransaction() {
        if (transactionalMessageSender != null) {
            try {
                transactionalMessageSender.abort();
            } catch (final TransactionException e) {
                logger.error("Problem aborting MessageSender transaction : " + e.getMessage());
            }
        }
    }

    private void abortMessagePublisherTransaction() {
        if (transactionalMessagePublisher != null) {
            try {
                transactionalMessagePublisher.abort();
            } catch (final TransactionException e) {
                logger.error("Problem aborting MessagePublisher transaction : " + e.getMessage());
            }
        }
    }

    @Override
    public void setTransactionalDatabaseTemplate(final TransactionalResource transactionalDatabaseTemplate) {
        this.transactionalDatabaseTemplate = transactionalDatabaseTemplate;
    }

    @Override
    public void setTransactionalFileStore(final TransactionalResource transactionalFileStore) {
        this.transactionalFileStore = transactionalFileStore;
    }

    @Override
    public void setTransactionalMessageSender(final TransactionalResource transactionalMessageSender) {
        this.transactionalMessageSender = transactionalMessageSender;
    }

    @Override
    public void setTransactionalMessagePublisher(final TransactionalResource transactionalMessagePublisher) {
        this.transactionalMessagePublisher = transactionalMessagePublisher;
    }

}
