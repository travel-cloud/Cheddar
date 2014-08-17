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
package com.clicktravel.infrastructure.messaging.aws.tx;

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.messaging.Message;
import com.clicktravel.cheddar.infrastructure.tx.NestedTransactionException;
import com.clicktravel.cheddar.infrastructure.tx.NonExistentTransactionException;
import com.clicktravel.infrastructure.messaging.aws.SnsMessagePublisher;

public class TransactionalSnsMessagePublisherTest {

    final SnsMessagePublisher mockSnsMessagePublisher = mock(SnsMessagePublisher.class);

    @Test
    public void shouldCreateTransactionalSnsMessagePublisher_withSnsMessagePublisher() throws Exception {
        // Given
        final SnsMessagePublisher snsMessagePublisher = mock(SnsMessagePublisher.class);

        // When
        Exception actualException = null;
        try {
            new TransactionalSnsMessagePublisher(snsMessagePublisher);
        } catch (final Exception e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
    }

    @Test
    public void shouldReturnExchangeName_withSnsMessagePublisher() throws Exception {
        // Given
        final String exchangeName = randomString(10);
        when(mockSnsMessagePublisher.exchangeName()).thenReturn(exchangeName);
        final TransactionalSnsMessagePublisher transactionalSnsMessagePublisher = new TransactionalSnsMessagePublisher(
                mockSnsMessagePublisher);

        // When
        final String returnedExchangeName = transactionalSnsMessagePublisher.exchangeName();

        // Then
        verify(mockSnsMessagePublisher).exchangeName();
        assertEquals(exchangeName, returnedExchangeName);
    }

    @Test
    public void shouldBeginTransaction_withNoExistingTransaction() throws Exception {
        // Given
        final TransactionalSnsMessagePublisher transactionalSnsMessagePublisher = new TransactionalSnsMessagePublisher(
                mockSnsMessagePublisher);

        // When
        Exception actualException = null;
        try {
            transactionalSnsMessagePublisher.begin();
        } catch (final Exception e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
    }

    @Test
    public void shouldNotBeginTransaction_withExistingTransaction() throws Exception {
        // Given
        final TransactionalSnsMessagePublisher transactionalSnsMessagePublisher = new TransactionalSnsMessagePublisher(
                mockSnsMessagePublisher);
        transactionalSnsMessagePublisher.begin();

        // When
        NestedTransactionException actualException = null;
        try {
            transactionalSnsMessagePublisher.begin();
        } catch (final NestedTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotCommit_withNoExistingTransaction() throws Exception {
        // Given
        final TransactionalSnsMessagePublisher transactionalSnsMessagePublisher = new TransactionalSnsMessagePublisher(
                mockSnsMessagePublisher);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalSnsMessagePublisher.commit();
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldPublishMessage_withExistingTransaction() throws Exception {
        // Given
        final TransactionalSnsMessagePublisher transactionalSnsMessagePublisher = new TransactionalSnsMessagePublisher(
                mockSnsMessagePublisher);
        transactionalSnsMessagePublisher.begin();
        final Message message = mock(Message.class);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalSnsMessagePublisher.publishMessage(message);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
        verifyZeroInteractions(mockSnsMessagePublisher);
    }

    @Test
    public void shouldNotPublishMessage_withNonExistingTransaction() throws Exception {
        // Given
        final TransactionalSnsMessagePublisher transactionalSnsMessagePublisher = new TransactionalSnsMessagePublisher(
                mockSnsMessagePublisher);
        final Message message = mock(Message.class);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalSnsMessagePublisher.publishMessage(message);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCommitPublishMessage_withExistingTransaction() throws Exception {
        // Given
        final TransactionalSnsMessagePublisher transactionalSnsMessagePublisher = new TransactionalSnsMessagePublisher(
                mockSnsMessagePublisher);
        transactionalSnsMessagePublisher.begin();
        final Message message = mock(Message.class);
        transactionalSnsMessagePublisher.publishMessage(message);

        // When
        transactionalSnsMessagePublisher.commit();

        // Then
        verify(mockSnsMessagePublisher).publishMessage(message);
    }

}
