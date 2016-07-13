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
package com.clicktravel.infrastructure.messaging.aws.sqs;

import static com.clicktravel.common.random.Randoms.randomId;
import static com.clicktravel.common.random.Randoms.randomInt;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.AmazonClientException;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageDeleteException;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageReceiveException;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageSendException;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessagingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SuppressWarnings("unchecked")
public class SqsTypedMessageQueueTest {

    private SqsQueueResource mockSqsQueueResource;
    private SqsTypedMessageQueue sqsTypedMessageQueue;

    @Before
    public void setUp() {
        mockSqsQueueResource = mock(SqsQueueResource.class);
        sqsTypedMessageQueue = new SqsTypedMessageQueue(mockSqsQueueResource);
    }

    @Test
    public void shouldReturnQueueName() {
        // Given
        final String queueName = randomString();
        when(mockSqsQueueResource.getQueueName()).thenReturn(queueName);

        // When
        final String name = sqsTypedMessageQueue.getName();

        // Then
        assertEquals(queueName, name);
    }

    @Test
    public void shouldSendMessage_withMessage() throws Exception {
        // Given
        final TypedMessage mockTypedMessage = mock(TypedMessage.class);
        final String messageType = randomString(10);
        final String messagePayload = randomString();
        when(mockTypedMessage.getType()).thenReturn(messageType);
        when(mockTypedMessage.getPayload()).thenReturn(messagePayload);

        // When
        sqsTypedMessageQueue.send(mockTypedMessage);

        // Then
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockSqsQueueResource).sendMessage(captor.capture());
        final String sqsMessageBody = captor.getValue();
        final JsonNode root = new ObjectMapper().readTree(sqsMessageBody);
        assertEquals(messageType, root.get("Subject").textValue());
        assertEquals(messagePayload, root.get("Message").textValue());
    }

    @Test
    public void shouldThrowMessageSendException_onAmazonClientExceptionForSend() {
        final TypedMessage mockTypedMessage = mock(TypedMessage.class);
        final String messageType = randomString(10);
        final String messagePayload = randomString();
        when(mockTypedMessage.getType()).thenReturn(messageType);
        when(mockTypedMessage.getPayload()).thenReturn(messagePayload);
        doThrow(AmazonClientException.class).when(mockSqsQueueResource).sendMessage(anyString());

        // When
        MessageSendException thrownException = null;
        try {
            sqsTypedMessageQueue.send(mockTypedMessage);
        } catch (final MessageSendException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldSendDelayedMessage_withMessageAndDelay() throws Exception {
        // Given
        final TypedMessage mockTypedMessage = mock(TypedMessage.class);
        final String messageType = randomString(10);
        final String messagePayload = randomString();
        when(mockTypedMessage.getType()).thenReturn(messageType);
        when(mockTypedMessage.getPayload()).thenReturn(messagePayload);
        final int delaySeconds = randomInt(100);

        // When
        sqsTypedMessageQueue.sendDelayedMessage(mockTypedMessage, delaySeconds);

        // Then
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockSqsQueueResource).sendDelayedMessage(captor.capture(), eq(delaySeconds));
        final String sqsMessageBody = captor.getValue();
        final JsonNode root = new ObjectMapper().readTree(sqsMessageBody);
        assertEquals(messageType, root.get("Subject").textValue());
        assertEquals(messagePayload, root.get("Message").textValue());
    }

    @Test
    public void shouldThrowMessageSendException_onAmazonClientExceptionForSendDelayedMessage() {
        final TypedMessage mockTypedMessage = mock(TypedMessage.class);
        final String messageType = randomString(10);
        final String messagePayload = randomString();
        when(mockTypedMessage.getType()).thenReturn(messageType);
        when(mockTypedMessage.getPayload()).thenReturn(messagePayload);
        final int delaySeconds = randomInt(100);
        doThrow(AmazonClientException.class).when(mockSqsQueueResource).sendDelayedMessage(anyString(), anyInt());

        // When
        MessageSendException thrownException = null;
        try {
            sqsTypedMessageQueue.sendDelayedMessage(mockTypedMessage, delaySeconds);
        } catch (final MessageSendException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldReturnMessages_onReceiveValidMessages() throws Exception {
        // When
        final List<com.amazonaws.services.sqs.model.Message> mockSqsMessages = new LinkedList<>();
        final List<String> messageTypes = new LinkedList<>();
        final List<String> payloads = new LinkedList<>();
        final List<String> messageIds = new LinkedList<>();
        final List<String> receiptHandles = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            final String messageType = randomString();
            final String payload = randomString();
            final String messageId = randomId();
            final String receiptHandle = randomId();
            mockSqsMessages.add(mockSqsMessage(messageType, payload, messageId, receiptHandle));
            messageTypes.add(messageType);
            payloads.add(payload);
            messageIds.add(messageId);
            receiptHandles.add(receiptHandle);
        }
        when(mockSqsQueueResource.receiveMessages()).thenReturn(mockSqsMessages);

        // When
        final List<TypedMessage> receivedMessages = sqsTypedMessageQueue.receive();

        // Then
        assertNotNull(receivedMessages);
        assertEquals(3, receivedMessages.size());
        for (int i = 0; i < 3; i++) {
            final TypedMessage receivedMessage = receivedMessages.get(i);
            assertEquals(messageTypes.get(i), receivedMessage.getType());
            assertEquals(payloads.get(i), receivedMessage.getPayload());
            assertEquals(messageIds.get(i), receivedMessage.getMessageId());
            assertEquals(receiptHandles.get(i), receivedMessage.getReceiptHandle());
        }
    }

    @Test
    public void shouldReturnMessages_onReceiveInvalidMessages() {
        // When
        final List<com.amazonaws.services.sqs.model.Message> mockSqsMessages = new LinkedList<>();
        final List<String> receiptHandles = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            final String receiptHandle = randomString();
            mockSqsMessages.add(mockSqsMessageWithMalformedBody(receiptHandle));
            receiptHandles.add(receiptHandle);
        }
        when(mockSqsQueueResource.receiveMessages()).thenReturn(mockSqsMessages);

        // When
        final List<TypedMessage> receivedMessages = sqsTypedMessageQueue.receive();

        // Then
        assertNotNull(receivedMessages);
        assertEquals(3, receivedMessages.size());

        for (int i = 0; i < 3; i++) {
            final TypedMessage receivedMessage = receivedMessages.get(i);
            assertEquals(receiptHandles.get(i), receivedMessage.getReceiptHandle());

            MessagingException thrownException = null;
            try {
                receivedMessage.getType();
            } catch (final MessagingException e) {
                thrownException = e;
            }
            assertNotNull(thrownException);

            thrownException = null;
            try {
                receivedMessage.getPayload();
            } catch (final MessagingException e) {
                thrownException = e;
            }
            assertNotNull(thrownException);
        }
    }

    private com.amazonaws.services.sqs.model.Message mockSqsMessage(final String type, final String payload,
            final String messageId, final String receiptHandle) throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("Subject", type);
        rootNode.put("Message", payload);
        final String body = mapper.writeValueAsString(rootNode);
        final com.amazonaws.services.sqs.model.Message mockSqsMessage = mock(
                com.amazonaws.services.sqs.model.Message.class);
        when(mockSqsMessage.getMessageId()).thenReturn(messageId);
        when(mockSqsMessage.getReceiptHandle()).thenReturn(receiptHandle);
        when(mockSqsMessage.getBody()).thenReturn(body);
        return mockSqsMessage;
    }

    private com.amazonaws.services.sqs.model.Message mockSqsMessageWithMalformedBody(final String receiptHandle) {
        final com.amazonaws.services.sqs.model.Message mockSqsMessage = mock(
                com.amazonaws.services.sqs.model.Message.class);
        when(mockSqsMessage.getReceiptHandle()).thenReturn(receiptHandle);
        when(mockSqsMessage.getBody()).thenReturn(randomString());
        return mockSqsMessage;
    }

    @Test
    public void shouldThrowMessageReceiveException_onAmazonClientExceptionForReceive() {
        // Given
        when(mockSqsQueueResource.receiveMessages()).thenThrow(AmazonClientException.class);

        // When
        MessageReceiveException thrownException = null;
        try {
            sqsTypedMessageQueue.receive();
        } catch (final MessageReceiveException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldReceiveMessagesWithWaitTimeAndMaxMessagesAttributes_withParameters() throws Exception {
        // Given
        final int waitTimeSeconds = randomInt(10);
        final int maxMessages = 1 + randomInt(10);
        final List<com.amazonaws.services.sqs.model.Message> mockSqsMessages = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            mockSqsMessages.add(mockSqsMessage(randomString(), randomString(), randomId(), randomId()));
        }
        when(mockSqsQueueResource.receiveMessages(waitTimeSeconds, maxMessages)).thenReturn(mockSqsMessages);

        // When
        final List<TypedMessage> receivedMessages = sqsTypedMessageQueue.receive(waitTimeSeconds, maxMessages);

        // Then
        assertNotNull(receivedMessages);
        assertEquals(3, receivedMessages.size());
        verify(mockSqsQueueResource).receiveMessages(waitTimeSeconds, maxMessages);
    }

    @Test
    public void shouldThrowMessageReceiveException_onAmazonClientExceptionForReceiveWithParameters() {
        // Given
        final int waitTimeSeconds = randomInt(10);
        final int maxMessages = 1 + randomInt(10);
        when(mockSqsQueueResource.receiveMessages(anyInt(), anyInt())).thenThrow(AmazonClientException.class);

        // When
        MessageReceiveException thrownException = null;
        try {
            sqsTypedMessageQueue.receive(waitTimeSeconds, maxMessages);
        } catch (final MessageReceiveException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldDeleteMessage_withMessage() {
        // Given
        final String receiptHandle = randomString();
        final TypedMessage mockTypedMessage = mock(TypedMessage.class);
        when(mockTypedMessage.getReceiptHandle()).thenReturn(receiptHandle);

        // When
        sqsTypedMessageQueue.delete(mockTypedMessage);

        // Then
        verify(mockSqsQueueResource).deleteMessage(receiptHandle);
    }

    @Test
    public void shouldThrowMessageDeleteException_onAmazonClientExceptionForDelete() {
        // Given
        final String receiptHandle = randomString();
        final TypedMessage mockTypedMessage = mock(TypedMessage.class);
        when(mockTypedMessage.getReceiptHandle()).thenReturn(receiptHandle);
        doThrow(AmazonClientException.class).when(mockSqsQueueResource).deleteMessage(anyString());

        // When
        MessageDeleteException thrownException = null;
        try {
            sqsTypedMessageQueue.delete(mockTypedMessage);
        } catch (final MessageDeleteException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldReturnSqsQueueResource() {
        // When
        final SqsQueueResource returnedSqsQueueResource = sqsTypedMessageQueue.getSqsQueue();

        // Then
        assertSame(mockSqsQueueResource, returnedSqsQueueResource);
    }
}
