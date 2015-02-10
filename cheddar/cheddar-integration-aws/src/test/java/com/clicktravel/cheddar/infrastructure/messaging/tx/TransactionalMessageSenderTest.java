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

import static com.clicktravel.common.random.Randoms.randomInt;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.messaging.MessageSender;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;
import com.clicktravel.cheddar.infrastructure.tx.NestedTransactionException;
import com.clicktravel.cheddar.infrastructure.tx.NonExistentTransactionException;

public class TransactionalMessageSenderTest {

    final MessageSender<TypedMessage> mockMessageSender = mock(MessageSender.class);

    @Test
    public void shouldCreateTransactionalMessageSender_withMessageSender() throws Exception {
        // Given
        final MessageSender<TypedMessage> messageSender = mock(MessageSender.class);

        // When
        Exception actualException = null;
        try {
            new TransactionalMessageSender(messageSender);
        } catch (final Exception e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
    }

    @Test
    public void shouldBeginTransaction_withNoExistingTransaction() throws Exception {
        // Given
        final TransactionalMessageSender transactionalMessageSender = new TransactionalMessageSender(mockMessageSender);

        // When
        Exception actualException = null;
        try {
            transactionalMessageSender.begin();
        } catch (final Exception e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
    }

    @Test
    public void shouldNotBeginTransaction_withExistingTransaction() throws Exception {
        // Given
        final TransactionalMessageSender transactionalMessageSender = new TransactionalMessageSender(mockMessageSender);
        transactionalMessageSender.begin();

        // When
        NestedTransactionException actualException = null;
        try {
            transactionalMessageSender.begin();
        } catch (final NestedTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotCommit_withNoExistingTransaction() throws Exception {
        // Given
        final TransactionalMessageSender transactionalMessageSender = new TransactionalMessageSender(mockMessageSender);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalMessageSender.commit();
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldSendMessage_withExistingTransaction() throws Exception {
        // Given
        final TransactionalMessageSender transactionalMessageSender = new TransactionalMessageSender(mockMessageSender);
        transactionalMessageSender.begin();
        final TypedMessage typedMessage = mock(TypedMessage.class);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalMessageSender.sendMessage(typedMessage);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
        verifyZeroInteractions(mockMessageSender);
    }

    @Test
    public void shouldNotSendMessage_withNonExistingTransaction() throws Exception {
        // Given
        final TransactionalMessageSender transactionalMessageSender = new TransactionalMessageSender(mockMessageSender);
        final TypedMessage typedMessage = mock(TypedMessage.class);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalMessageSender.sendMessage(typedMessage);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCommitSendMessage_withExistingTransaction() throws Exception {
        // Given
        final TransactionalMessageSender transactionalMessageSender = new TransactionalMessageSender(mockMessageSender);
        transactionalMessageSender.begin();
        final TypedMessage typedMessage = mock(TypedMessage.class);
        transactionalMessageSender.sendMessage(typedMessage);

        // When
        transactionalMessageSender.commit();

        // Then
        verify(mockMessageSender).sendMessage(typedMessage);
    }

    @Test
    public void shouldSendDelayedMessage_withExistingTransaction() throws Exception {
        // Given
        final TransactionalMessageSender transactionalMessageSender = new TransactionalMessageSender(mockMessageSender);
        transactionalMessageSender.begin();
        final TypedMessage typedMessage = mock(TypedMessage.class);
        final int delay = 1 + randomInt(100);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalMessageSender.sendDelayedMessage(typedMessage, delay);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
        verifyZeroInteractions(mockMessageSender);
    }

    @Test
    public void shouldNotSendDelayedMessage_withNonExistingTransaction() throws Exception {
        // Given
        final TransactionalMessageSender transactionalMessageSender = new TransactionalMessageSender(mockMessageSender);
        final TypedMessage typedMessage = mock(TypedMessage.class);
        final int delay = 1 + randomInt(100);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalMessageSender.sendDelayedMessage(typedMessage, delay);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCommitSendDelayedMessage_withExistingTransaction() throws Exception {
        // Given
        final TransactionalMessageSender transactionalMessageSender = new TransactionalMessageSender(mockMessageSender);
        transactionalMessageSender.begin();
        final TypedMessage typedMessage = mock(TypedMessage.class);
        final int delay = 1 + randomInt(100);
        transactionalMessageSender.sendDelayedMessage(typedMessage, delay);

        // When
        transactionalMessageSender.commit();

        // Then
        verify(mockMessageSender).sendDelayedMessage(typedMessage, delay);
    }

}
