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
package com.clicktravel.infrastructure.persistence.aws.dynamodb.tx;

import static com.clicktravel.common.random.Randoms.randomBoolean;
import static com.clicktravel.common.random.Randoms.randomId;
import static com.clicktravel.common.random.Randoms.randomLong;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Set;

import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import com.clicktravel.cheddar.infrastructure.persistence.database.GeneratedKeyHolder;
import com.clicktravel.cheddar.infrastructure.persistence.database.ItemId;
import com.clicktravel.cheddar.infrastructure.persistence.database.SequenceKeyGenerator;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Query;
import com.clicktravel.cheddar.infrastructure.tx.NestedTransactionException;
import com.clicktravel.cheddar.infrastructure.tx.NonExistentTransactionException;
import com.clicktravel.infrastructure.persistence.aws.dynamodb.DynamoDbTemplate;
import com.clicktravel.infrastructure.persistence.aws.dynamodb.StubItem;

@SuppressWarnings("unchecked")
public class TransactionalDynamoDbTemplateTest {

    private final DynamoDbTemplate dynamoDbTemplate = mock(DynamoDbTemplate.class);

    @Test
    public void shouldCreateTransactionalDynamoDbTemplate_withDynamoDbTemplate() {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = mock(DynamoDbTemplate.class);

        // When
        final TransactionalDynamoDbTemplate transactionalDynamoDbTemplate = new TransactionalDynamoDbTemplate(
                dynamoDbTemplate);

        // Then
        assertNotNull(transactionalDynamoDbTemplate);
    }

    @Test
    public void shouldRead_withItemIdAndItemClass() throws Exception {
        // Given
        final ItemId itemId = mock(ItemId.class);
        final StubItem mockItem = mock(StubItem.class);
        when(dynamoDbTemplate.read(any(ItemId.class), any(Class.class))).thenReturn(mockItem);
        final TransactionalDynamoDbTemplate transactionalDynamoDbTemplate = new TransactionalDynamoDbTemplate(
                dynamoDbTemplate);

        // When
        final StubItem item = transactionalDynamoDbTemplate.read(itemId, StubItem.class);

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
        final TransactionalDynamoDbTemplate transactionalDynamoDbTemplate = new TransactionalDynamoDbTemplate(
                dynamoDbTemplate);

        // When
        final Collection<StubItem> returnedItems = transactionalDynamoDbTemplate.fetch(query, StubItem.class);

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
        final TransactionalDynamoDbTemplate transactionalDynamoDbTemplate = new TransactionalDynamoDbTemplate(
                dynamoDbTemplate);

        // When
        final StubItem item = transactionalDynamoDbTemplate.fetchUnique(query, StubItem.class);

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
        final TransactionalDynamoDbTemplate transactionalDynamoDbTemplate = new TransactionalDynamoDbTemplate(
                dynamoDbTemplate);

        // When
        final GeneratedKeyHolder returnedGeneratedKeyHolder = transactionalDynamoDbTemplate
                .generateKeys(sequenceKeyGenerator);

        // Then
        verify(dynamoDbTemplate).generateKeys(sequenceKeyGenerator);
        assertEquals(mockGeneratedKeyHolder, returnedGeneratedKeyHolder);
    }

    @Test
    public void shouldBeginTransaction_withNoExistingTransaction() throws Exception {
        // Given
        final TransactionalDynamoDbTemplate transactionalDynamoDbTemplate = new TransactionalDynamoDbTemplate(
                dynamoDbTemplate);

        // When
        Exception actualException = null;
        try {
            transactionalDynamoDbTemplate.begin();
        } catch (final Exception e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
    }

    @Test
    public void shouldNotBeginTransaction_withExistingTransaction() throws Exception {
        // Given
        final TransactionalDynamoDbTemplate transactionalDynamoDbTemplate = new TransactionalDynamoDbTemplate(
                dynamoDbTemplate);
        transactionalDynamoDbTemplate.begin();

        // When
        NestedTransactionException actualException = null;
        try {
            transactionalDynamoDbTemplate.begin();
        } catch (final NestedTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotCommit_withNoExistingTransaction() throws Exception {
        // Given
        final TransactionalDynamoDbTemplate transactionalDynamoDbTemplate = new TransactionalDynamoDbTemplate(
                dynamoDbTemplate);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalDynamoDbTemplate.commit();
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCreate_withExistingTransaction() throws Exception {
        // Given
        final TransactionalDynamoDbTemplate transactionalDynamoDbTemplate = new TransactionalDynamoDbTemplate(
                dynamoDbTemplate);
        transactionalDynamoDbTemplate.begin();
        final StubItem item = randomStubItem();

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalDynamoDbTemplate.create(item);
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
        final TransactionalDynamoDbTemplate transactionalDynamoDbTemplate = new TransactionalDynamoDbTemplate(
                dynamoDbTemplate);
        final StubItem item = randomStubItem();

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalDynamoDbTemplate.create(item);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCommitCreate_withExistingTransaction() throws Exception {
        // Given
        final TransactionalDynamoDbTemplate transactionalDynamoDbTemplate = new TransactionalDynamoDbTemplate(
                dynamoDbTemplate);
        transactionalDynamoDbTemplate.begin();
        final StubItem item = randomStubItem();
        transactionalDynamoDbTemplate.create(item);

        // When
        transactionalDynamoDbTemplate.commit();

        // Then
        verify(dynamoDbTemplate).create(item);
    }

    @Test
    public void shouldUpdate_withExistingTransaction() throws Exception {
        // Given
        final TransactionalDynamoDbTemplate transactionalDynamoDbTemplate = new TransactionalDynamoDbTemplate(
                dynamoDbTemplate);
        transactionalDynamoDbTemplate.begin();
        final StubItem item = randomStubItem();

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalDynamoDbTemplate.update(item);
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
        final TransactionalDynamoDbTemplate transactionalDynamoDbTemplate = new TransactionalDynamoDbTemplate(
                dynamoDbTemplate);
        final StubItem item = randomStubItem();

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalDynamoDbTemplate.update(item);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCommitUpdate_withExistingTransaction() throws Exception {
        // Given
        final TransactionalDynamoDbTemplate transactionalDynamoDbTemplate = new TransactionalDynamoDbTemplate(
                dynamoDbTemplate);
        transactionalDynamoDbTemplate.begin();
        final StubItem item = randomStubItem();
        transactionalDynamoDbTemplate.update(item);

        // When
        transactionalDynamoDbTemplate.commit();

        // Then
        verify(dynamoDbTemplate).update(item);
    }

    @Test
    public void shouldDelete_withExistingTransaction() throws Exception {
        // Given
        final TransactionalDynamoDbTemplate transactionalDynamoDbTemplate = new TransactionalDynamoDbTemplate(
                dynamoDbTemplate);
        transactionalDynamoDbTemplate.begin();
        final StubItem item = randomStubItem();

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalDynamoDbTemplate.delete(item);
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
        final TransactionalDynamoDbTemplate transactionalDynamoDbTemplate = new TransactionalDynamoDbTemplate(
                dynamoDbTemplate);
        final StubItem item = randomStubItem();

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalDynamoDbTemplate.delete(item);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCommitDelete_withExistingTransaction() throws Exception {
        // Given
        final TransactionalDynamoDbTemplate transactionalDynamoDbTemplate = new TransactionalDynamoDbTemplate(
                dynamoDbTemplate);
        transactionalDynamoDbTemplate.begin();
        final StubItem item = randomStubItem();
        transactionalDynamoDbTemplate.delete(item);

        // When
        transactionalDynamoDbTemplate.commit();

        // Then
        verify(dynamoDbTemplate).delete(item);
    }

    private StubItem randomStubItem() {
        final StubItem item = new StubItem();
        item.setId(randomId());
        item.setStringProperty(randomString(10));
        item.setStringProperty2(randomString(10));
        item.setBooleanProperty(randomBoolean());
        item.setVersion(randomLong());
        item.setStringSetProperty(Sets.newSet(randomString(10), randomString(10), randomString(10)));
        return item;
    }

}
