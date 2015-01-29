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

import static com.clicktravel.common.random.Randoms.randomInt;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.messaging.Message;
import com.clicktravel.cheddar.infrastructure.tx.NestedTransactionException;
import com.clicktravel.cheddar.infrastructure.tx.NonExistentTransactionException;
import com.clicktravel.infrastructure.messaging.aws.SqsMessageSender;

public class TransactionalSqsMessageSenderTest {

    final SqsMessageSender mockSqsMessageSender = mock(SqsMessageSender.class);

    @Test
    public void shouldCreateTransactionalSqsMessageSender_withSqsMessageSender() throws Exception {
        // Given
        final SqsMessageSender snsMessageSender = mock(SqsMessageSender.class);

        // When
        Exception actualException = null;
        try {
            new TransactionalSqsMessageSender(snsMessageSender);
        } catch (final Exception e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
    }

    @Test
    public void shouldBeginTransaction_withNoExistingTransaction() throws Exception {
        // Given
        final TransactionalSqsMessageSender transactionalSqsMessageSender = new TransactionalSqsMessageSender(
                mockSqsMessageSender);

        // When
        Exception actualException = null;
        try {
            transactionalSqsMessageSender.begin();
        } catch (final Exception e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
    }

    @Test
    public void shouldNotBeginTransaction_withExistingTransaction() throws Exception {
        // Given
        final TransactionalSqsMessageSender transactionalSqsMessageSender = new TransactionalSqsMessageSender(
                mockSqsMessageSender);
        transactionalSqsMessageSender.begin();

        // When
        NestedTransactionException actualException = null;
        try {
            transactionalSqsMessageSender.begin();
        } catch (final NestedTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotCommit_withNoExistingTransaction() throws Exception {
        // Given
        final TransactionalSqsMessageSender transactionalSqsMessageSender = new TransactionalSqsMessageSender(
                mockSqsMessageSender);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalSqsMessageSender.commit();
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldSendMessage_withExistingTransaction() throws Exception {
        // Given
        final TransactionalSqsMessageSender transactionalSqsMessageSender = new TransactionalSqsMessageSender(
                mockSqsMessageSender);
        transactionalSqsMessageSender.begin();
        final Message message = mock(Message.class);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalSqsMessageSender.sendMessage(message);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
        verifyZeroInteractions(mockSqsMessageSender);
    }

    @Test
    public void shouldNotSendMessage_withNonExistingTransaction() throws Exception {
        // Given
        final TransactionalSqsMessageSender transactionalSqsMessageSender = new TransactionalSqsMessageSender(
                mockSqsMessageSender);
        final Message message = mock(Message.class);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalSqsMessageSender.sendMessage(message);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCommitSendMessage_withExistingTransaction() throws Exception {
        // Given
        final TransactionalSqsMessageSender transactionalSqsMessageSender = new TransactionalSqsMessageSender(
                mockSqsMessageSender);
        transactionalSqsMessageSender.begin();
        final Message message = mock(Message.class);
        transactionalSqsMessageSender.sendMessage(message);

        // When
        transactionalSqsMessageSender.commit();

        // Then
        verify(mockSqsMessageSender).sendMessage(message);
    }

    @Test
    public void shouldSendDelayedMessage_withExistingTransaction() throws Exception {
        // Given
        final TransactionalSqsMessageSender transactionalSqsMessageSender = new TransactionalSqsMessageSender(
                mockSqsMessageSender);
        transactionalSqsMessageSender.begin();
        final Message message = mock(Message.class);
        final int delay = 1 + randomInt(100);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalSqsMessageSender.sendDelayedMessage(message, delay);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
        verifyZeroInteractions(mockSqsMessageSender);
    }

    @Test
    public void shouldNotSendDelayedMessage_withNonExistingTransaction() throws Exception {
        // Given
        final TransactionalSqsMessageSender transactionalSqsMessageSender = new TransactionalSqsMessageSender(
                mockSqsMessageSender);
        final Message message = mock(Message.class);
        final int delay = 1 + randomInt(100);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalSqsMessageSender.sendDelayedMessage(message, delay);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCommitSendDelayedMessage_withExistingTransaction() throws Exception {
        // Given
        final TransactionalSqsMessageSender transactionalSqsMessageSender = new TransactionalSqsMessageSender(
                mockSqsMessageSender);
        transactionalSqsMessageSender.begin();
        final Message message = mock(Message.class);
        final int delay = 1 + randomInt(100);
        transactionalSqsMessageSender.sendDelayedMessage(message, delay);

        // When
        transactionalSqsMessageSender.commit();

        // Then
        verify(mockSqsMessageSender).sendDelayedMessage(message, delay);
    }

}
