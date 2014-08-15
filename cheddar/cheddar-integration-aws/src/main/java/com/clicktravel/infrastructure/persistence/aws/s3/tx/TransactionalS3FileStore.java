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
package com.clicktravel.infrastructure.persistence.aws.s3.tx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FileItem;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FilePath;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FileStore;
import com.clicktravel.cheddar.infrastructure.tx.NestedTransactionException;
import com.clicktravel.cheddar.infrastructure.tx.NonExistentTransactionException;
import com.clicktravel.cheddar.infrastructure.tx.TransactionException;
import com.clicktravel.cheddar.infrastructure.tx.TransactionalResource;
import com.clicktravel.infrastructure.persistence.aws.s3.S3FileStore;

public class TransactionalS3FileStore implements FileStore, TransactionalResource {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final S3FileStore s3FileStore;

    private final ThreadLocal<FileStoreTransaction> currentTransaction = new ThreadLocal<FileStoreTransaction>();

    public TransactionalS3FileStore(final S3FileStore s3FileStore) {
        this.s3FileStore = s3FileStore;
    }

    private FileStoreTransaction getCurrentTransaction() {
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
        currentTransaction.set(new FileStoreTransaction());
        logger.trace("Beginning transaction: " + currentTransaction.get().transactionId());
    }

    @Override
    public void commit() throws TransactionException {
        final FileStoreTransaction transaction = getCurrentTransaction();
        logger.trace("Committing transaction: " + transaction.transactionId());
        transaction.applyActions(s3FileStore);
        currentTransaction.remove();
        logger.trace("Transaction successfully commit: " + transaction.transactionId());
    }

    @Override
    public FileItem read(final FilePath filePath) throws NonExistentItemException {
        return s3FileStore.read(filePath);
    }

    @Override
    public void write(final FilePath filePath, final FileItem fileItem) {
        final FileStoreTransaction transaction = getCurrentTransaction();
        transaction.addWriteAction(filePath, fileItem);
    }

    @Override
    public void delete(final FilePath filePath) throws NonExistentItemException {
        final FileStoreTransaction transaction = getCurrentTransaction();
        transaction.addDeleteAction(filePath);
    }

    @Override
    public void abort() throws TransactionException {
        currentTransaction.remove();
    }
}
