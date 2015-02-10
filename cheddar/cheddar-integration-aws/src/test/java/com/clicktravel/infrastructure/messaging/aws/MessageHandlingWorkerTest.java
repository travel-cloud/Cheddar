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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Semaphore;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.clicktravel.cheddar.infrastructure.messaging.MessageHandler;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;

public class MessageHandlingWorkerTest {

    private TypedMessage typedMessage;
    private MessageHandler<TypedMessage> messageHandler;
    private AmazonSQS amazonSqsClient;
    private DeleteMessageRequest deleteMessageRequest;
    private Semaphore semaphore;

    @Before
    public void setUp() {
        typedMessage = mock(TypedMessage.class);
        messageHandler = mock(MessageHandler.class);
        amazonSqsClient = mock(AmazonSQS.class);
        deleteMessageRequest = mock(DeleteMessageRequest.class);
        semaphore = mock(Semaphore.class);
    }

    @Test
    public void shouldRun_withMessageAndMessageHandler() throws Exception {
        // Given
        final MessageHandlingWorker messageHandlingWorker = new MessageHandlingWorker(typedMessage, messageHandler,
                amazonSqsClient, deleteMessageRequest, semaphore);

        // When
        messageHandlingWorker.run();

        // Then
        verify(messageHandler).handle(typedMessage);
        verify(amazonSqsClient).deleteMessage(deleteMessageRequest);
        verify(semaphore).release();
    }

    @Test
    public void shouldRun_withMessageAndMessageHandlerAndException() throws Exception {
        // Given
        doThrow(Exception.class).when(messageHandler).handle(any(TypedMessage.class));
        final MessageHandlingWorker messageHandlingWorker = new MessageHandlingWorker(typedMessage, messageHandler,
                amazonSqsClient, deleteMessageRequest, semaphore);

        // When
        messageHandlingWorker.run();

        // Then
        verify(messageHandler).handle(typedMessage);
        verify(amazonSqsClient).deleteMessage(deleteMessageRequest);
        verify(semaphore).release();
    }

}
