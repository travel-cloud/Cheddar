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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.clicktravel.cheddar.infrastructure.messaging.tx.TransactionalMessagePublisher;
import com.clicktravel.cheddar.infrastructure.messaging.tx.TransactionalMessageSender;
import com.clicktravel.cheddar.infrastructure.persistence.database.tx.TransactionalDatabaseTemplate;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.tx.TransactionalFileStore;
import com.clicktravel.cheddar.infrastructure.tx.TransactionException;
import com.clicktravel.cheddar.infrastructure.tx.TransactionalResourceManager;

public class TransactionalResourceManagerTest {

    private TransactionalFileStore mockTransactionalFilestore;
    private TransactionalDatabaseTemplate mockTransactionalDatabaseTemplate;
    private TransactionalMessagePublisher mockTransactionalMessagePublisher;
    private TransactionalMessageSender mockTransactionalMessageSender;
    private TransactionalResourceManager transactionalResourceManager;
    private InOrder inOrder;
    private TransactionException mockTransactionException;

    @Before
    public void setUp() {
        mockTransactionalFilestore = mock(TransactionalFileStore.class);
        mockTransactionalDatabaseTemplate = mock(TransactionalDatabaseTemplate.class);
        mockTransactionalMessagePublisher = mock(TransactionalMessagePublisher.class);
        mockTransactionalMessageSender = mock(TransactionalMessageSender.class);
        transactionalResourceManager = new SimpleTransactionalResourceManager();
        transactionalResourceManager.setTransactionalFileStore(mockTransactionalFilestore);
        transactionalResourceManager.setTransactionalDatabaseTemplate(mockTransactionalDatabaseTemplate);
        transactionalResourceManager.setTransactionalMessagePublisher(mockTransactionalMessagePublisher);
        transactionalResourceManager.setTransactionalMessageSender(mockTransactionalMessageSender);
        inOrder = Mockito.inOrder(mockTransactionalDatabaseTemplate, mockTransactionalFilestore,
                mockTransactionalMessageSender, mockTransactionalMessagePublisher);
        mockTransactionException = mock(TransactionException.class);
    }

    @Test
    public void shouldThrowExceptionIfAlreadyInTransaction_onBegin() {
        // Given
        transactionalResourceManager.begin();

        // When
        TransactionException thrownException = null;
        try {
            transactionalResourceManager.begin();
        } catch (final TransactionException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldThrowExceptionIfNotInTransaction_onCommit() {
        // When
        TransactionException thrownException = null;
        try {
            transactionalResourceManager.commit();
        } catch (final TransactionException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldReturnFalseWhenNotInTransaction_onInTransaction() {
        // When
        final boolean inTransaction = transactionalResourceManager.inTransaction();

        // Then
        assertFalse(inTransaction);
    }

    @Test
    public void shouldReturnTrueWhenInTransaction_onInTransaction() {
        // Given
        transactionalResourceManager.begin();

        // When
        final boolean inTransaction = transactionalResourceManager.inTransaction();

        // Then
        assertTrue(inTransaction);
    }

    @Test
    public void shouldBeginAllTransactionalResourcesInCorrectOrder() {
        // When
        transactionalResourceManager.begin();

        // Then
        inOrder.verify(mockTransactionalMessagePublisher).begin();
        inOrder.verify(mockTransactionalMessageSender).begin();
        inOrder.verify(mockTransactionalFilestore).begin();
        inOrder.verify(mockTransactionalDatabaseTemplate).begin();
        verifyNoMoreInteractions(mockTransactionalDatabaseTemplate, mockTransactionalFilestore,
                mockTransactionalMessageSender, mockTransactionalMessagePublisher);
    }

    @Test
    public void shouldCommitTransactionalResourceInCorrectOrder() {
        // Given
        transactionalResourceManager.begin();

        // When
        transactionalResourceManager.commit();

        // Then
        inOrder.verify(mockTransactionalMessagePublisher).begin();
        inOrder.verify(mockTransactionalMessageSender).begin();
        inOrder.verify(mockTransactionalFilestore).begin();
        inOrder.verify(mockTransactionalDatabaseTemplate).begin();
        inOrder.verify(mockTransactionalDatabaseTemplate).commit();
        inOrder.verify(mockTransactionalFilestore).commit();
        inOrder.verify(mockTransactionalMessageSender).commit();
        inOrder.verify(mockTransactionalMessagePublisher).commit();
        verifyNoMoreInteractions(mockTransactionalDatabaseTemplate, mockTransactionalFilestore,
                mockTransactionalMessageSender, mockTransactionalMessagePublisher);
    }

    @Test
    public void shouldAbortTransactionalResourceInCorrectOrder() {
        // Given
        transactionalResourceManager.begin();

        // When
        transactionalResourceManager.abort();

        // Then
        inOrder.verify(mockTransactionalMessagePublisher).begin();
        inOrder.verify(mockTransactionalMessageSender).begin();
        inOrder.verify(mockTransactionalFilestore).begin();
        inOrder.verify(mockTransactionalDatabaseTemplate).begin();
        inOrder.verify(mockTransactionalDatabaseTemplate).abort();
        inOrder.verify(mockTransactionalFilestore).abort();
        inOrder.verify(mockTransactionalMessageSender).abort();
        inOrder.verify(mockTransactionalMessagePublisher).abort();
        verifyNoMoreInteractions(mockTransactionalDatabaseTemplate, mockTransactionalFilestore,
                mockTransactionalMessageSender, mockTransactionalMessagePublisher);
    }

    @Test
    public void shouldAbortAllTransactionalResources_whenExceptionsThrown() {
        // Given
        doThrow(mockTransactionException).when(mockTransactionalDatabaseTemplate).abort();
        doThrow(mockTransactionException).when(mockTransactionalFilestore).abort();
        doThrow(mockTransactionException).when(mockTransactionalMessagePublisher).abort();
        doThrow(mockTransactionException).when(mockTransactionalMessageSender).abort();
        transactionalResourceManager.begin();

        // When
        transactionalResourceManager.abort();

        // Then
        inOrder.verify(mockTransactionalMessagePublisher).begin();
        inOrder.verify(mockTransactionalMessageSender).begin();
        inOrder.verify(mockTransactionalFilestore).begin();
        inOrder.verify(mockTransactionalDatabaseTemplate).begin();
        inOrder.verify(mockTransactionalDatabaseTemplate).abort();
        inOrder.verify(mockTransactionalFilestore).abort();
        inOrder.verify(mockTransactionalMessageSender).abort();
        inOrder.verify(mockTransactionalMessagePublisher).abort();
        verifyNoMoreInteractions(mockTransactionalDatabaseTemplate, mockTransactionalFilestore,
                mockTransactionalMessageSender, mockTransactionalMessagePublisher);
    }
}
