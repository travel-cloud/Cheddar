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
package com.clicktravel.cheddar.infrastructure.messaging.pooled.listener;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.messaging.MessageHandler;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;

@SuppressWarnings("unchecked")
public class MessageHandlerWorkerTest {

    private TypedMessage typedMessage;
    private MessageHandler<TypedMessage> messageHandler;
    private PooledMessageListener<TypedMessage> pooledMessageListener;

    @Before
    public void setUp() {
        typedMessage = mock(TypedMessage.class);
        messageHandler = mock(MessageHandler.class);
        pooledMessageListener = mock(PooledMessageListener.class);
    }

    @Test
    public void shouldRun_withMessageAndMessageHandler() throws Exception {
        // Given
        final MessageHandlerWorker<TypedMessage> messageHandlingWorker = new MessageHandlerWorker<TypedMessage>(
                pooledMessageListener, typedMessage, messageHandler);

        // When
        messageHandlingWorker.run();

        // Then
        verify(messageHandler).handle(typedMessage);
        verify(pooledMessageListener).completeMessageProcessing(typedMessage);
    }

    @Test
    public void shouldRun_withMessageAndMessageHandlerAndException() throws Exception {
        // Given
        doThrow(Exception.class).when(messageHandler).handle(any(TypedMessage.class));
        final MessageHandlerWorker<TypedMessage> messageHandlingWorker = new MessageHandlerWorker<TypedMessage>(
                pooledMessageListener, typedMessage, messageHandler);

        // When
        messageHandlingWorker.run();

        // Then
        verify(messageHandler).handle(typedMessage);
        verify(pooledMessageListener).completeMessageProcessing(typedMessage);
    }

}
