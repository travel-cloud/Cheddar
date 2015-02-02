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

import static com.clicktravel.common.random.Randoms.randomId;
import static com.clicktravel.common.random.Randoms.randomLong;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.Set;

import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import com.clicktravel.cheddar.infrastructure.persistence.database.DatabaseTemplate;
import com.clicktravel.cheddar.infrastructure.persistence.database.GeneratedKeyHolder;
import com.clicktravel.cheddar.infrastructure.persistence.database.ItemId;
import com.clicktravel.cheddar.infrastructure.persistence.database.SequenceKeyGenerator;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.handler.PersistenceExceptionHandler;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Query;
import com.clicktravel.cheddar.infrastructure.persistence.exception.PersistenceException;
import com.clicktravel.cheddar.infrastructure.tx.NestedTransactionException;
import com.clicktravel.cheddar.infrastructure.tx.NonExistentTransactionException;
import com.clicktravel.cheddar.infrastructure.tx.TransactionalResourceException;

@SuppressWarnings("unchecked")
public class TransactionalDatabaseTemplateTest {

    private final DatabaseTemplate dynamoDbTemplate = mock(DatabaseTemplate.class);

    @Test
    public void shouldCreateTransactionalDatabaseTemplate_withDatabaseTemplate() {
        // Given
        final DatabaseTemplate dynamoDbTemplate = mock(DatabaseTemplate.class);

        // When
        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);

        // Then
        assertNotNull(transactionalDatabaseTemplate);
    }

    @Test
    public void shouldRead_withItemIdAndItemClass() throws Exception {
        // Given
        final ItemId itemId = mock(ItemId.class);
        final StubItem mockItem = mock(StubItem.class);
        when(dynamoDbTemplate.read(any(ItemId.class), any(Class.class))).thenReturn(mockItem);
        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);

        // When
        final StubItem item = transactionalDatabaseTemplate.read(itemId, StubItem.class);

        // Then
        verify(dynamoDbTemplate).read(itemId, StubItem.class);
        assertEquals(mockItem, item);
    }

    @Test
    public void shouldFetch_withQueryAndItemClass() throws Exception {
        // Given
        final Query query = mock(Query.class);
        final Set<StubItem> items = Sets.newSet(randomStubItem(), randomStubItem(), randomStubItem());
        when(dynamoDbTemplate.fetch(any(Query.class), any(Class.class))).thenReturn(items);
        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);

        // When
        final Collection<StubItem> returnedItems = transactionalDatabaseTemplate.fetch(query, StubItem.class);

        // Then
        verify(dynamoDbTemplate).fetch(query, StubItem.class);
        assertEquals(items, returnedItems);
    }

    @Test
    public void shouldFetchUnique_withQueryAndItemClass() throws Exception {
        // Given
        final Query query = mock(Query.class);
        final StubItem mockItem = mock(StubItem.class);
        when(dynamoDbTemplate.fetchUnique(any(Query.class), any(Class.class))).thenReturn(mockItem);
        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);

        // When
        final StubItem item = transactionalDatabaseTemplate.fetchUnique(query, StubItem.class);

        // Then
        verify(dynamoDbTemplate).fetchUnique(query, StubItem.class);
        assertEquals(mockItem, item);
    }

    @Test
    public void shouldGenerateKeys_withSequenceKeyGenerator() throws Exception {
        // Given
        final SequenceKeyGenerator sequenceKeyGenerator = mock(SequenceKeyGenerator.class);
        final GeneratedKeyHolder mockGeneratedKeyHolder = mock(GeneratedKeyHolder.class);
        when(dynamoDbTemplate.generateKeys(any(SequenceKeyGenerator.class))).thenReturn(mockGeneratedKeyHolder);
        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);

        // When
        final GeneratedKeyHolder returnedGeneratedKeyHolder = transactionalDatabaseTemplate
                .generateKeys(sequenceKeyGenerator);

        // Then
        verify(dynamoDbTemplate).generateKeys(sequenceKeyGenerator);
        assertEquals(mockGeneratedKeyHolder, returnedGeneratedKeyHolder);
    }

    @Test
    public void shouldBeginTransaction_withNoExistingTransaction() throws Exception {
        // Given
        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);

        // When
        Exception actualException = null;
        try {
            transactionalDatabaseTemplate.begin();
        } catch (final Exception e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
    }

    @Test
    public void shouldNotBeginTransaction_withExistingTransaction() throws Exception {
        // Given
        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);
        transactionalDatabaseTemplate.begin();

        // When
        NestedTransactionException actualException = null;
        try {
            transactionalDatabaseTemplate.begin();
        } catch (final NestedTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotCommit_withNoExistingTransaction() throws Exception {
        // Given
        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalDatabaseTemplate.commit();
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCreate_withPersistenceExceptionHandlers() throws Exception {
        // Given
        final PersistenceExceptionHandler<PersistenceException> persistenceExceptionHandler = mock(PersistenceExceptionHandler.class);

        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);
        transactionalDatabaseTemplate.begin();
        final StubItem item = randomStubItem();

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalDatabaseTemplate.create(item, persistenceExceptionHandler);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
        verifyZeroInteractions(dynamoDbTemplate);
    }

    @Test
    public void shouldNotCreate_withNonExistingTransaction() throws Exception {
        // Given
        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);
        final StubItem item = randomStubItem();

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalDatabaseTemplate.create(item);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCommitCreate_withExistingTransaction() throws Exception {
        // Given
        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);
        transactionalDatabaseTemplate.begin();
        final StubItem item = randomStubItem();
        transactionalDatabaseTemplate.create(item);

        // When
        transactionalDatabaseTemplate.commit();

        // Then
        verify(dynamoDbTemplate).create(item);
    }

    @Test
    public void shouldCommitCreate_withPersistenceExceptionHandler() throws Exception {
        // Given
        final PersistenceExceptionHandler<PersistenceException> persistenceExceptionHandler = mock(PersistenceExceptionHandler.class);
        final StubItem item = randomStubItem();
        final PersistenceException persistenceException = mock(PersistenceException.class);
        when(dynamoDbTemplate.create(item)).thenThrow(persistenceException);
        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);
        transactionalDatabaseTemplate.begin();
        transactionalDatabaseTemplate.create(item, persistenceExceptionHandler);

        // When
        transactionalDatabaseTemplate.commit();

        // Then
        verify(persistenceExceptionHandler).handle(persistenceException);
    }

    @Test
    public void shouldNotCommitCreate_withPersistenceExceptionHandlerThrowingException() throws Exception {
        // Given
        final PersistenceExceptionHandler<PersistenceException> persistenceExceptionHandler = mock(PersistenceExceptionHandler.class);
        final StubItem item = randomStubItem();
        final PersistenceException persistenceException = mock(PersistenceException.class);
        when(dynamoDbTemplate.create(item)).thenThrow(persistenceException);
        final RuntimeException runtimeException = mock(RuntimeException.class);
        doThrow(runtimeException).when(persistenceExceptionHandler).handle(persistenceException);

        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);
        transactionalDatabaseTemplate.begin();
        transactionalDatabaseTemplate.create(item, persistenceExceptionHandler);

        // When
        TransactionalResourceException actualException = null;
        try {
            transactionalDatabaseTemplate.commit();
        } catch (final TransactionalResourceException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertEquals(runtimeException, actualException.getCause());
    }

    @Test
    public void shouldUpdate_withExistingTransaction() throws Exception {
        // Given
        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);
        transactionalDatabaseTemplate.begin();
        final StubItem item = randomStubItem();

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalDatabaseTemplate.update(item);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
        verifyZeroInteractions(dynamoDbTemplate);
    }

    @Test
    public void shouldUpdate_withPersistenceExceptionHandler() throws Exception {
        // Given
        final PersistenceExceptionHandler<PersistenceException> persistenceExceptionHandler = mock(PersistenceExceptionHandler.class);

        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);
        transactionalDatabaseTemplate.begin();
        final StubItem item = randomStubItem();

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalDatabaseTemplate.update(item, persistenceExceptionHandler);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
        verifyZeroInteractions(dynamoDbTemplate);
    }

    @Test
    public void shouldNotUpdate_withNonExistingTransaction() throws Exception {
        // Given
        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);
        final StubItem item = randomStubItem();

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalDatabaseTemplate.update(item);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCommitUpdate_withExistingTransaction() throws Exception {
        // Given
        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);
        transactionalDatabaseTemplate.begin();
        final StubItem item = randomStubItem();
        transactionalDatabaseTemplate.update(item);

        // When
        transactionalDatabaseTemplate.commit();

        // Then
        verify(dynamoDbTemplate).update(item);
    }

    @Test
    public void shouldCommitUpdate_withPersistenceExceptionHandler() throws Exception {
        // Given
        final PersistenceExceptionHandler<PersistenceException> persistenceExceptionHandler = mock(PersistenceExceptionHandler.class);
        final StubItem item = randomStubItem();
        final PersistenceException persistenceException = mock(PersistenceException.class);
        when(dynamoDbTemplate.update(item)).thenThrow(persistenceException);
        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);
        transactionalDatabaseTemplate.begin();
        transactionalDatabaseTemplate.update(item, persistenceExceptionHandler);

        // When
        transactionalDatabaseTemplate.commit();

        // Then
        verify(persistenceExceptionHandler).handle(persistenceException);
    }

    @Test
    public void shouldNotCommitUpdate_withPersistenceExceptionHandlerThrowingException() throws Exception {
        // Given
        final PersistenceExceptionHandler<PersistenceException> persistenceExceptionHandler = mock(PersistenceExceptionHandler.class);
        final StubItem item = randomStubItem();
        final PersistenceException persistenceException = mock(PersistenceException.class);
        when(dynamoDbTemplate.update(item)).thenThrow(persistenceException);
        final RuntimeException runtimeException = mock(RuntimeException.class);
        doThrow(runtimeException).when(persistenceExceptionHandler).handle(persistenceException);

        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);
        transactionalDatabaseTemplate.begin();
        transactionalDatabaseTemplate.update(item, persistenceExceptionHandler);

        // When
        TransactionalResourceException actualException = null;
        try {
            transactionalDatabaseTemplate.commit();
        } catch (final TransactionalResourceException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertEquals(runtimeException, actualException.getCause());
    }

    @Test
    public void shouldDelete_withExistingTransaction() throws Exception {
        // Given
        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);
        transactionalDatabaseTemplate.begin();
        final StubItem item = randomStubItem();

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalDatabaseTemplate.delete(item);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
        verifyZeroInteractions(dynamoDbTemplate);
    }

    @Test
    public void shouldDelete_withPersistenceExceptionHandler() throws Exception {
        // Given
        final PersistenceExceptionHandler<?> persistenceExceptionHandler = mock(PersistenceExceptionHandler.class);
        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);
        transactionalDatabaseTemplate.begin();
        final StubItem item = randomStubItem();

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalDatabaseTemplate.delete(item, persistenceExceptionHandler);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
        verifyZeroInteractions(dynamoDbTemplate);
    }

    @Test
    public void shouldNotDelete_withNonExistingTransaction() throws Exception {
        // Given
        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);
        final StubItem item = randomStubItem();

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalDatabaseTemplate.delete(item);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCommitDelete_withExistingTransaction() throws Exception {
        // Given
        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);
        transactionalDatabaseTemplate.begin();
        final StubItem item = randomStubItem();
        transactionalDatabaseTemplate.delete(item);

        // When
        transactionalDatabaseTemplate.commit();

        // Then
        verify(dynamoDbTemplate).delete(item);
    }

    @Test
    public void shouldCommitDelete_withPersistenceExceptionHandler() throws Exception {
        // Given
        final PersistenceExceptionHandler<PersistenceException> persistenceExceptionHandler = mock(PersistenceExceptionHandler.class);
        final StubItem item = randomStubItem();
        final PersistenceException persistenceException = mock(PersistenceException.class);
        doThrow(persistenceException).when(dynamoDbTemplate).delete(item);
        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);
        transactionalDatabaseTemplate.begin();
        transactionalDatabaseTemplate.delete(item, persistenceExceptionHandler);

        // When
        transactionalDatabaseTemplate.commit();

        // Then
        verify(persistenceExceptionHandler).handle(persistenceException);
    }

    @Test
    public void shouldNotCommitDelete_withPersistenceExceptionHandlerThrowingException() throws Exception {
        // Given
        final PersistenceExceptionHandler<PersistenceException> persistenceExceptionHandler = mock(PersistenceExceptionHandler.class);
        final StubItem item = randomStubItem();
        final PersistenceException persistenceException = mock(PersistenceException.class);
        doThrow(persistenceException).when(dynamoDbTemplate).delete(item);
        final RuntimeException runtimeException = mock(RuntimeException.class);
        doThrow(runtimeException).when(persistenceExceptionHandler).handle(persistenceException);

        final TransactionalDatabaseTemplate transactionalDatabaseTemplate = new TransactionalDatabaseTemplate(
                dynamoDbTemplate);
        transactionalDatabaseTemplate.begin();
        transactionalDatabaseTemplate.delete(item, persistenceExceptionHandler);

        // When
        TransactionalResourceException actualException = null;
        try {
            transactionalDatabaseTemplate.commit();
        } catch (final TransactionalResourceException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertEquals(runtimeException, actualException.getCause());
    }

    private StubItem randomStubItem() {
        final StubItem item = new StubItem();
        item.setId(randomId());
        item.setStringProperty(randomString(10));
        item.setVersion(randomLong());
        return item;
    }

}
