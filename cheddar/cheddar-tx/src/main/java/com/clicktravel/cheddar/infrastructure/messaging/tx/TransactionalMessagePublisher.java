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
package com.clicktravel.cheddar.infrastructure.messaging.tx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.infrastructure.messaging.MessagePublisher;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessagePublishException;
import com.clicktravel.cheddar.infrastructure.tx.NestedTransactionException;
import com.clicktravel.cheddar.infrastructure.tx.NonExistentTransactionException;
import com.clicktravel.cheddar.infrastructure.tx.TransactionException;
import com.clicktravel.cheddar.infrastructure.tx.TransactionalResource;

public class TransactionalMessagePublisher implements MessagePublisher<TypedMessage>, TransactionalResource {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final MessagePublisher<TypedMessage> messagePublisher;
    private final ThreadLocal<MessagingTransaction> currentTransaction = new ThreadLocal<MessagingTransaction>();

    public TransactionalMessagePublisher(final MessagePublisher<TypedMessage> messagePublisher) {
        this.messagePublisher = messagePublisher;
    }

    private MessagingTransaction getCurrentTransaction() {
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
        currentTransaction.set(new MessagingTransaction());
        logger.trace("Beginning transaction: " + currentTransaction.get().transactionId());
    }

    @Override
    public void commit() throws TransactionException {
        final MessagingTransaction transaction = getCurrentTransaction();
        logger.trace("Committing transaction: " + transaction.transactionId());
        transaction.applyActions(messagePublisher);
        currentTransaction.remove();
        logger.trace("Transaction successfully committed: " + transaction.transactionId());
    }

    @Override
    public void publish(final TypedMessage typedMessage) throws MessagePublishException {
        final MessagingTransaction transaction = getCurrentTransaction();
        transaction.addMessage(typedMessage);
    }

    @Override
    public void abort() throws TransactionException {
        currentTransaction.remove();
    }
}
