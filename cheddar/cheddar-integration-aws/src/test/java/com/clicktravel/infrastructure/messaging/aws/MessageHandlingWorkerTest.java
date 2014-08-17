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
package com.clicktravel.infrastructure.messaging.aws;

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.clicktravel.cheddar.infrastructure.messaging.Message;
import com.clicktravel.cheddar.infrastructure.messaging.MessageHandler;

public class MessageHandlingWorkerTest {

    @Test
    public void shouldCreateMessageHandlingWorker_with() throws Exception {
        // Given
        final Message message = mock(Message.class);
        final MessageHandler messageHandler = mock(MessageHandler.class);
        final AmazonSQS amazonSqsClient = mock(AmazonSQS.class);
        final String queueUrl = randomString();
        final String messageReceiptHandle = randomString();

        // When
        final MessageHandlingWorker messageHandlingWorker = new MessageHandlingWorker(message, messageHandler,
                amazonSqsClient, queueUrl, messageReceiptHandle);

        // Then
        assertNotNull(messageHandlingWorker);
        assertEquals(message, messageHandlingWorker.message());
        assertEquals(messageHandler, messageHandlingWorker.messageHandler());
    }

    @Test
    public void shouldRun_withMessageAndMessageHandler() throws Exception {
        // Given
        final Message message = mock(Message.class);
        final MessageHandler messageHandler = mock(MessageHandler.class);
        final AmazonSQS amazonSqsClient = mock(AmazonSQS.class);
        final String queueUrl = randomString();
        final String messageReceiptHandle = randomString();
        final MessageHandlingWorker messageHandlingWorker = new MessageHandlingWorker(message, messageHandler,
                amazonSqsClient, queueUrl, messageReceiptHandle);

        // When
        messageHandlingWorker.run();

        // Then
        final ArgumentCaptor<DeleteMessageRequest> deleteMessageRequestArgumentCaptor = ArgumentCaptor
                .forClass(DeleteMessageRequest.class);
        verify(messageHandler).handle(message);
        verify(amazonSqsClient).deleteMessage(deleteMessageRequestArgumentCaptor.capture());
        assertEquals(queueUrl, deleteMessageRequestArgumentCaptor.getValue().getQueueUrl());
        assertEquals(messageReceiptHandle, deleteMessageRequestArgumentCaptor.getValue().getReceiptHandle());
    }

    @Test
    public void shouldRun_withMessageAndMessageHandlerAndException() throws Exception {
        // Given
        final Message message = mock(Message.class);
        final MessageHandler messageHandler = mock(MessageHandler.class);
        doThrow(Exception.class).when(messageHandler).handle(any(Message.class));
        final AmazonSQS amazonSqsClient = mock(AmazonSQS.class);
        final String queueUrl = randomString();
        final String messageReceiptHandle = randomString();
        final MessageHandlingWorker messageHandlingWorker = new MessageHandlingWorker(message, messageHandler,
                amazonSqsClient, queueUrl, messageReceiptHandle);

        // When
        messageHandlingWorker.run();

        // Then
        final ArgumentCaptor<DeleteMessageRequest> deleteMessageRequestArgumentCaptor = ArgumentCaptor
                .forClass(DeleteMessageRequest.class);
        verify(messageHandler).handle(message);
        verify(amazonSqsClient).deleteMessage(deleteMessageRequestArgumentCaptor.capture());
        assertEquals(queueUrl, deleteMessageRequestArgumentCaptor.getValue().getQueueUrl());
        assertEquals(messageReceiptHandle, deleteMessageRequestArgumentCaptor.getValue().getReceiptHandle());
    }

    @Test
    public void shouldRun_withNullMessageHandler() throws Exception {
        // Given
        final Message message = mock(Message.class);
        final MessageHandler messageHandler = null;
        final AmazonSQS amazonSqsClient = mock(AmazonSQS.class);
        final String queueUrl = randomString();
        final String messageReceiptHandle = randomString();
        final MessageHandlingWorker messageHandlingWorker = new MessageHandlingWorker(message, messageHandler,
                amazonSqsClient, queueUrl, messageReceiptHandle);

        // When
        messageHandlingWorker.run();

        // Then
        final ArgumentCaptor<DeleteMessageRequest> deleteMessageRequestArgumentCaptor = ArgumentCaptor
                .forClass(DeleteMessageRequest.class);
        verify(amazonSqsClient).deleteMessage(deleteMessageRequestArgumentCaptor.capture());
        assertEquals(queueUrl, deleteMessageRequestArgumentCaptor.getValue().getQueueUrl());
        assertEquals(messageReceiptHandle, deleteMessageRequestArgumentCaptor.getValue().getReceiptHandle());
    }

}
