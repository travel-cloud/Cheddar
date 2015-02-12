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

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.messaging.MessagePublisher;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;
import com.clicktravel.cheddar.infrastructure.tx.NestedTransactionException;
import com.clicktravel.cheddar.infrastructure.tx.NonExistentTransactionException;

@SuppressWarnings("unchecked")
public class TransactionalMessagePublisherTest {

    final MessagePublisher<TypedMessage> mockMessagePublisher = mock(MessagePublisher.class);

    @Test
    public void shouldCreateTransactionalMessagePublisher_withMessagePublisher() throws Exception {
        // Given
        final MessagePublisher<TypedMessage> messagePublisher = mock(MessagePublisher.class);

        // When
        Exception actualException = null;
        try {
            new TransactionalMessagePublisher(messagePublisher);
        } catch (final Exception e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
    }

    @Test
    public void shouldReturnExchangeName_withMessagePublisher() throws Exception {
        // Given
        final String exchangeName = randomString(10);
        when(mockMessagePublisher.exchangeName()).thenReturn(exchangeName);
        final TransactionalMessagePublisher transactionalMessagePublisher = new TransactionalMessagePublisher(
                mockMessagePublisher);

        // When
        final String returnedExchangeName = transactionalMessagePublisher.exchangeName();

        // Then
        verify(mockMessagePublisher).exchangeName();
        assertEquals(exchangeName, returnedExchangeName);
    }

    @Test
    public void shouldBeginTransaction_withNoExistingTransaction() throws Exception {
        // Given
        final TransactionalMessagePublisher transactionalMessagePublisher = new TransactionalMessagePublisher(
                mockMessagePublisher);

        // When
        Exception actualException = null;
        try {
            transactionalMessagePublisher.begin();
        } catch (final Exception e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
    }

    @Test
    public void shouldNotBeginTransaction_withExistingTransaction() throws Exception {
        // Given
        final TransactionalMessagePublisher transactionalMessagePublisher = new TransactionalMessagePublisher(
                mockMessagePublisher);
        transactionalMessagePublisher.begin();

        // When
        NestedTransactionException actualException = null;
        try {
            transactionalMessagePublisher.begin();
        } catch (final NestedTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotCommit_withNoExistingTransaction() throws Exception {
        // Given
        final TransactionalMessagePublisher transactionalMessagePublisher = new TransactionalMessagePublisher(
                mockMessagePublisher);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalMessagePublisher.commit();
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldPublishMessage_withExistingTransaction() throws Exception {
        // Given
        final TransactionalMessagePublisher transactionalMessagePublisher = new TransactionalMessagePublisher(
                mockMessagePublisher);
        transactionalMessagePublisher.begin();
        final TypedMessage typedMessage = mock(TypedMessage.class);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalMessagePublisher.publishMessage(typedMessage);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
        verifyZeroInteractions(mockMessagePublisher);
    }

    @Test
    public void shouldNotPublishMessage_withNonExistingTransaction() throws Exception {
        // Given
        final TransactionalMessagePublisher transactionalMessagePublisher = new TransactionalMessagePublisher(
                mockMessagePublisher);
        final TypedMessage typedMessage = mock(TypedMessage.class);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalMessagePublisher.publishMessage(typedMessage);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCommitPublishMessage_withExistingTransaction() throws Exception {
        // Given
        final TransactionalMessagePublisher transactionalMessagePublisher = new TransactionalMessagePublisher(
                mockMessagePublisher);
        transactionalMessagePublisher.begin();
        final TypedMessage typedMessage = mock(TypedMessage.class);
        transactionalMessagePublisher.publishMessage(typedMessage);

        // When
        transactionalMessagePublisher.commit();

        // Then
        verify(mockMessagePublisher).publishMessage(typedMessage);
    }

}
