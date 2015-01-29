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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.clicktravel.cheddar.infrastructure.tx.TransactionException;
import com.clicktravel.infrastructure.messaging.aws.tx.TransactionalSnsMessagePublisher;
import com.clicktravel.infrastructure.messaging.aws.tx.TransactionalSqsMessageSender;
import com.clicktravel.infrastructure.persistence.aws.dynamodb.tx.TransactionalDynamoDbTemplate;
import com.clicktravel.infrastructure.persistence.aws.s3.tx.TransactionalS3FileStore;

public class AWSTransactionalResourceManagerTest {

    private TransactionalS3FileStore mockTransactionalS3Filestore;
    private TransactionalDynamoDbTemplate mockTransactionalDynamoDbTemplate;
    private TransactionalSnsMessagePublisher mockTransactionalSnsMessagePublisher;
    private TransactionalSqsMessageSender mockTransactionalSqsMessageSender;
    private AwsTransactionalResourceManager awsTransactionalResourceManager;
    private InOrder inOrder;
    private TransactionException mockTransactionException;

    @Before
    public void setUp() {
        mockTransactionalS3Filestore = mock(TransactionalS3FileStore.class);
        mockTransactionalDynamoDbTemplate = mock(TransactionalDynamoDbTemplate.class);
        mockTransactionalSnsMessagePublisher = mock(TransactionalSnsMessagePublisher.class);
        mockTransactionalSqsMessageSender = mock(TransactionalSqsMessageSender.class);
        awsTransactionalResourceManager = new AwsTransactionalResourceManager(mockTransactionalDynamoDbTemplate,
                mockTransactionalS3Filestore, mockTransactionalSqsMessageSender, mockTransactionalSnsMessagePublisher);
        inOrder = Mockito.inOrder(mockTransactionalDynamoDbTemplate, mockTransactionalS3Filestore,
                mockTransactionalSqsMessageSender, mockTransactionalSnsMessagePublisher);
        mockTransactionException = mock(TransactionException.class);
    }

    @Test
    public void shouldBeginAllTransactionalResourcesInCorrectOrder() {
        // When
        awsTransactionalResourceManager.begin();

        // Then
        inOrder.verify(mockTransactionalSnsMessagePublisher).begin();
        inOrder.verify(mockTransactionalSqsMessageSender).begin();
        inOrder.verify(mockTransactionalS3Filestore).begin();
        inOrder.verify(mockTransactionalDynamoDbTemplate).begin();
        verifyNoMoreInteractions(mockTransactionalDynamoDbTemplate, mockTransactionalS3Filestore,
                mockTransactionalSqsMessageSender, mockTransactionalSnsMessagePublisher);
    }

    @Test
    public void shouldCommitTransactionalResourceInCorrectOrder() {
        // When
        awsTransactionalResourceManager.commit();

        // Then
        inOrder.verify(mockTransactionalDynamoDbTemplate).commit();
        inOrder.verify(mockTransactionalS3Filestore).commit();
        inOrder.verify(mockTransactionalSqsMessageSender).commit();
        inOrder.verify(mockTransactionalSnsMessagePublisher).commit();
        verifyNoMoreInteractions(mockTransactionalDynamoDbTemplate, mockTransactionalS3Filestore,
                mockTransactionalSqsMessageSender, mockTransactionalSnsMessagePublisher);
    }

    @Test
    public void shouldAbortTransactionalResourceInCorrectOrder() {
        // When
        awsTransactionalResourceManager.abort();

        // Then
        inOrder.verify(mockTransactionalDynamoDbTemplate).abort();
        inOrder.verify(mockTransactionalS3Filestore).abort();
        inOrder.verify(mockTransactionalSqsMessageSender).abort();
        inOrder.verify(mockTransactionalSnsMessagePublisher).abort();
        verifyNoMoreInteractions(mockTransactionalDynamoDbTemplate, mockTransactionalS3Filestore,
                mockTransactionalSqsMessageSender, mockTransactionalSnsMessagePublisher);
    }

    @Test
    public void shouldAbortAllTransactionalResources_whenExceptionsThrown() {
        // Given
        doThrow(mockTransactionException).when(mockTransactionalDynamoDbTemplate).abort();
        doThrow(mockTransactionException).when(mockTransactionalS3Filestore).abort();
        doThrow(mockTransactionException).when(mockTransactionalSnsMessagePublisher).abort();
        doThrow(mockTransactionException).when(mockTransactionalSqsMessageSender).abort();

        // When
        awsTransactionalResourceManager.abort();

        // Then
        inOrder.verify(mockTransactionalDynamoDbTemplate).abort();
        inOrder.verify(mockTransactionalS3Filestore).abort();
        inOrder.verify(mockTransactionalSqsMessageSender).abort();
        inOrder.verify(mockTransactionalSnsMessagePublisher).abort();
        verifyNoMoreInteractions(mockTransactionalDynamoDbTemplate, mockTransactionalS3Filestore,
                mockTransactionalSqsMessageSender, mockTransactionalSnsMessagePublisher);
    }
}
