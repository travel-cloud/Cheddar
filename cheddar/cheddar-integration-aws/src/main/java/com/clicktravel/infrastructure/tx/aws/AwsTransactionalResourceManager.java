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
package com.clicktravel.infrastructure.tx.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.infrastructure.tx.TransactionException;
import com.clicktravel.cheddar.infrastructure.tx.TransactionalResource;
import com.clicktravel.cheddar.infrastructure.tx.TransactionalResourceManager;
import com.clicktravel.infrastructure.messaging.aws.tx.TransactionalSnsMessagePublisher;
import com.clicktravel.infrastructure.messaging.aws.tx.TransactionalSqsMessageSender;
import com.clicktravel.infrastructure.persistence.aws.dynamodb.tx.TransactionalDynamoDbTemplate;
import com.clicktravel.infrastructure.persistence.aws.s3.tx.TransactionalS3FileStore;

public class AwsTransactionalResourceManager implements TransactionalResourceManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final TransactionalResource transactionalDynamoDbTemplate;
    private final TransactionalResource transactionalS3FileStore;
    private final TransactionalResource transactionalSqsMessageSender;
    private final TransactionalResource transactionalSnsMessagePublisher;

    public AwsTransactionalResourceManager(final TransactionalDynamoDbTemplate transactionalDynamoDbTemplate,
            final TransactionalS3FileStore transactionalS3FileStore,
            final TransactionalSqsMessageSender transactionalSqsMessageSender,
            final TransactionalSnsMessagePublisher transactionalSnsMessagePublisher) {
        this.transactionalDynamoDbTemplate = transactionalDynamoDbTemplate;
        this.transactionalS3FileStore = transactionalS3FileStore;
        this.transactionalSqsMessageSender = transactionalSqsMessageSender;
        this.transactionalSnsMessagePublisher = transactionalSnsMessagePublisher;
    }

    @Override
    public void begin() throws TransactionException {
        logger.trace("Starting global transaction");
        beginSnsTransaction();
        beginSqsTransaction();
        beginS3Transaction();
        beginDynamoDbTransaction();
    }

    @Override
    public void commit() throws TransactionException {
        logger.trace("Committing global transaction");

        // Order of commits is important

        // Some constraints in the domain are enforced on 'save' actions e.g. uniqueness constraint
        // It is unsafe for the remaining commits to proceed if this commit is not completed first
        commitDynamoDbTransaction();

        commitS3Transaction();

        commitSqsTransaction();

        // By doing this commit last, we ensure domain events are published only if all other actions in this
        // transaction committed OK
        commitSnsTransaction();
    }

    @Override
    public void abort() {
        logger.trace("Aborting global transaction");
        abortDynamoDbTransaction();
        abortS3Transaction();
        abortSqsTransaction();
        abortSnsTransaction();
    }

    private void beginDynamoDbTransaction() throws TransactionException {
        if (transactionalDynamoDbTemplate != null) {
            transactionalDynamoDbTemplate.begin();
        }
    }

    private void beginS3Transaction() throws TransactionException {
        if (transactionalS3FileStore != null) {
            transactionalS3FileStore.begin();
        }
    }

    private void beginSqsTransaction() throws TransactionException {
        if (transactionalSqsMessageSender != null) {
            transactionalSqsMessageSender.begin();
        }
    }

    private void beginSnsTransaction() throws TransactionException {
        if (transactionalSnsMessagePublisher != null) {
            transactionalSnsMessagePublisher.begin();
        }
    }

    private void commitDynamoDbTransaction() throws TransactionException {
        if (transactionalDynamoDbTemplate != null) {
            transactionalDynamoDbTemplate.commit();
        }
    }

    private void commitS3Transaction() throws TransactionException {
        if (transactionalS3FileStore != null) {
            transactionalS3FileStore.commit();
        }
    }

    private void commitSqsTransaction() throws TransactionException {
        if (transactionalSqsMessageSender != null) {
            transactionalSqsMessageSender.commit();
        }
    }

    private void commitSnsTransaction() throws TransactionException {
        if (transactionalSnsMessagePublisher != null) {
            transactionalSnsMessagePublisher.commit();
        }
    }

    private void abortDynamoDbTransaction() {
        if (transactionalDynamoDbTemplate != null) {
            try {
                transactionalDynamoDbTemplate.abort();
            } catch (final TransactionException e) {
                logger.error("Problem aborting DynamoDbTemplate transaction : " + e.getMessage());
            }
        }
    }

    private void abortS3Transaction() {
        if (transactionalS3FileStore != null) {
            try {
                transactionalS3FileStore.abort();
            } catch (final TransactionException e) {
                logger.error("Problem aborting S3FileStore transaction : " + e.getMessage());
            }
        }
    }

    private void abortSqsTransaction() {
        if (transactionalSqsMessageSender != null) {
            try {
                transactionalSqsMessageSender.abort();
            } catch (final TransactionException e) {
                logger.error("Problem aborting SqsMessageSender transaction : " + e.getMessage());
            }
        }
    }

    private void abortSnsTransaction() {
        if (transactionalSnsMessagePublisher != null) {
            try {
                transactionalSnsMessagePublisher.abort();
            } catch (final TransactionException e) {
                logger.error("Problem aborting SnsMessagePublisher transaction : " + e.getMessage());
            }
        }
    }

}
