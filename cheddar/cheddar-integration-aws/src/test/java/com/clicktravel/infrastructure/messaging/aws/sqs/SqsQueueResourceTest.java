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

import static com.clicktravel.common.random.Randoms.randomInt;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;

@SuppressWarnings("unchecked")
public class SqsQueueResourceTest {

    private String queueName;
    private String queueUrl;
    private AmazonSQS amazonSqsClient;
    private SqsQueueResource sqsQueueResource;

    @Before
    public void setUp() {
        queueName = randomString();
        queueUrl = randomString();
        amazonSqsClient = mock(AmazonSQS.class);
        sqsQueueResource = new SqsQueueResource(queueName, queueUrl, amazonSqsClient);
    }

    @Test
    public void shouldSendMessage_withMessageBody() {
        // Given
        final String messageBody = randomString();

        // When
        sqsQueueResource.sendMessage(messageBody);

        // Then
        final ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(amazonSqsClient).sendMessage(captor.capture());
        final SendMessageRequest sendMessageRequest = captor.getValue();
        assertEquals(queueUrl, sendMessageRequest.getQueueUrl());
        assertEquals(messageBody, sendMessageRequest.getMessageBody());
        assertNull(sendMessageRequest.getDelaySeconds());
    }

    @Test
    public void shouldThrowException_onExceptionForSendMessage() {
        // Given
        final String messageBody = randomString();
        doThrow(AmazonClientException.class).when(amazonSqsClient).sendMessage(any(SendMessageRequest.class));

        // When
        AmazonClientException thrownException = null;
        try {
            sqsQueueResource.sendMessage(messageBody);
        } catch (final AmazonClientException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldSendDelayedMessage_withMessageBodyAndDelay() {
        // Given
        final String messageBody = randomString();
        final int delaySeconds = randomInt(1000);

        // When
        sqsQueueResource.sendDelayedMessage(messageBody, delaySeconds);

        // Then
        final ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(amazonSqsClient).sendMessage(captor.capture());
        final SendMessageRequest sendMessageRequest = captor.getValue();
        assertEquals(queueUrl, sendMessageRequest.getQueueUrl());
        assertEquals(messageBody, sendMessageRequest.getMessageBody());
        assertEquals(new Integer(delaySeconds), sendMessageRequest.getDelaySeconds());
    }

    @Test
    public void shouldThrowException_onExceptionForSendDelayedMessage() {
        // Given
        final String messageBody = randomString();
        final int delaySeconds = randomInt(1000);
        doThrow(AmazonClientException.class).when(amazonSqsClient).sendMessage(any(SendMessageRequest.class));

        // When
        AmazonClientException thrownException = null;
        try {
            sqsQueueResource.sendDelayedMessage(messageBody, delaySeconds);
        } catch (final AmazonClientException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldReceiveMessages() {
        // Given
        final List<Message> mockReceivedMessages = mock(List.class);
        final ReceiveMessageResult mockReceiveMessageResponse = mock(ReceiveMessageResult.class);
        when(mockReceiveMessageResponse.getMessages()).thenReturn(mockReceivedMessages);
        final ArgumentCaptor<ReceiveMessageRequest> captor = ArgumentCaptor.forClass(ReceiveMessageRequest.class);
        when(amazonSqsClient.receiveMessage(captor.capture())).thenReturn(mockReceiveMessageResponse);

        // When
        final List<Message> actualReceivedMessages = sqsQueueResource.receiveMessages();

        // Then
        assertSame(mockReceivedMessages, actualReceivedMessages);
        final ReceiveMessageRequest receiveMessageRequest = captor.getValue();
        assertEquals(queueUrl, receiveMessageRequest.getQueueUrl());
        assertNull(receiveMessageRequest.getWaitTimeSeconds());
        assertNull(receiveMessageRequest.getMaxNumberOfMessages());
    }

    @Test
    public void shouldThrowException_onExceptionForReceiveMessages() {
        doThrow(AmazonClientException.class).when(amazonSqsClient).receiveMessage(any(ReceiveMessageRequest.class));

        // When
        AmazonClientException thrownException = null;
        try {
            sqsQueueResource.receiveMessages();
        } catch (final AmazonClientException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);

    }

    @Test
    public void shouldReceiveMessages_withWaitTimeAndMaxMessagesParameters() {
        // Given
        final int waitTimeSeconds = randomInt(20);
        final int maxMessages = 1 + randomInt(10);
        final List<Message> mockReceivedMessages = mock(List.class);
        final ReceiveMessageResult mockReceiveMessageResponse = mock(ReceiveMessageResult.class);
        when(mockReceiveMessageResponse.getMessages()).thenReturn(mockReceivedMessages);
        final ArgumentCaptor<ReceiveMessageRequest> captor = ArgumentCaptor.forClass(ReceiveMessageRequest.class);
        when(amazonSqsClient.receiveMessage(captor.capture())).thenReturn(mockReceiveMessageResponse);

        // When
        final List<Message> actualReceivedMessages = sqsQueueResource.receiveMessages(waitTimeSeconds, maxMessages);

        // Then
        assertSame(mockReceivedMessages, actualReceivedMessages);
        final ReceiveMessageRequest receiveMessageRequest = captor.getValue();
        assertEquals(queueUrl, receiveMessageRequest.getQueueUrl());
        assertEquals(new Integer(waitTimeSeconds), receiveMessageRequest.getWaitTimeSeconds());
        assertEquals(new Integer(maxMessages), receiveMessageRequest.getMaxNumberOfMessages());
    }

    @Test
    public void shouldThrowException_onExceptionForReceiveMessagesWithParameters() {
        doThrow(AmazonClientException.class).when(amazonSqsClient).receiveMessage(any(ReceiveMessageRequest.class));
        final int waitTimeSeconds = randomInt(20);
        final int maxMessages = 1 + randomInt(10);

        // When
        AmazonClientException thrownException = null;
        try {
            sqsQueueResource.receiveMessages(waitTimeSeconds, maxMessages);
        } catch (final AmazonClientException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldDeleteMessage_withReceiptHandle() {
        // Given
        final String receiptHandle = randomString();

        // When
        sqsQueueResource.deleteMessage(receiptHandle);

        // Then
        final ArgumentCaptor<DeleteMessageRequest> captor = ArgumentCaptor.forClass(DeleteMessageRequest.class);
        verify(amazonSqsClient).deleteMessage(captor.capture());
        final DeleteMessageRequest deleteMessageRequest = captor.getValue();
        assertEquals(queueUrl, deleteMessageRequest.getQueueUrl());
        assertEquals(receiptHandle, deleteMessageRequest.getReceiptHandle());
    }

    @Test
    public void shouldThrowException_onExceptionForDeleteMessage() {
        // Given
        doThrow(AmazonClientException.class).when(amazonSqsClient).deleteMessage(any(DeleteMessageRequest.class));
        final String receiptHandle = randomString();

        // When
        AmazonClientException thrownException = null;
        try {
            sqsQueueResource.deleteMessage(receiptHandle);
        } catch (final AmazonClientException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldSetQueueAttributes_withPolicy() {
        // Given
        final Policy mockPolicy = mock(Policy.class);
        final String mockPolicyJson = randomString();
        when(mockPolicy.toJson()).thenReturn(mockPolicyJson);

        // When
        sqsQueueResource.setPolicy(mockPolicy);

        // Then
        final ArgumentCaptor<SetQueueAttributesRequest> captor = ArgumentCaptor
                .forClass(SetQueueAttributesRequest.class);
        verify(amazonSqsClient).setQueueAttributes(captor.capture());
        final SetQueueAttributesRequest setQueueAttributesRequest = captor.getValue();
        assertEquals(queueUrl, setQueueAttributesRequest.getQueueUrl());
        assertEquals(mockPolicyJson, setQueueAttributesRequest.getAttributes()
                .get(QueueAttributeName.Policy.toString()));
    }

    @Test
    public void shouldReturnQueueArn() {
        // Given
        final String queueArn = randomString();
        final Map<String, String> attributes = Collections.singletonMap(QueueAttributeName.QueueArn.toString(),
                queueArn);
        final GetQueueAttributesResult mockGetQueueAttributesResult = mock(GetQueueAttributesResult.class);
        when(mockGetQueueAttributesResult.getAttributes()).thenReturn(attributes);
        final ArgumentCaptor<GetQueueAttributesRequest> captor = ArgumentCaptor
                .forClass(GetQueueAttributesRequest.class);
        when(amazonSqsClient.getQueueAttributes(captor.capture())).thenReturn(mockGetQueueAttributesResult);

        // When
        final String returnedQueueArn = sqsQueueResource.queueArn();

        // Then
        final GetQueueAttributesRequest getQueueAttributesRequest = captor.getValue();
        assertEquals(queueUrl, getQueueAttributesRequest.getQueueUrl());
        assertEquals(Arrays.asList(QueueAttributeName.QueueArn.toString()),
                getQueueAttributesRequest.getAttributeNames());
        assertEquals(queueArn, returnedQueueArn);
    }

    @Test
    public void shouldReturnQueueName() {
        // When
        final String returnedQueueName = sqsQueueResource.getQueueName();

        // Then
        assertEquals(queueName, returnedQueueName);
    }
}
