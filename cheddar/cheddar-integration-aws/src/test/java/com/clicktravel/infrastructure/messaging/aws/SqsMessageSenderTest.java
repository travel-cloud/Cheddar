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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;
import com.clicktravel.common.random.Randoms;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SqsMessageSenderTest {

    private AmazonSQS mockAmazonSqsClient;
    private String queueName;
    private String queueUrl;
    private String subject;
    private String messageBody;
    private SqsMessageSender sqsMessageSender;
    private TypedMessage typedMessage;
    private GetQueueUrlResult mockGetQueueUrlResult;

    @Before
    public void setUp() {
        mockAmazonSqsClient = mock(AmazonSQS.class);
        queueName = Randoms.randomString(15);
        queueUrl = Randoms.randomString(15);
        subject = Randoms.randomString(10);
        messageBody = Randoms.randomString(20);
        sqsMessageSender = new SqsMessageSender(queueName);
        sqsMessageSender.configure(mockAmazonSqsClient);
        typedMessage = mock(TypedMessage.class);
        mockGetQueueUrlResult = mock(GetQueueUrlResult.class);
        when(mockGetQueueUrlResult.getQueueUrl()).thenReturn(queueUrl);
        when(mockAmazonSqsClient.getQueueUrl(any(GetQueueUrlRequest.class))).thenReturn(mockGetQueueUrlResult);
        when(typedMessage.getType()).thenReturn(subject);
        when(typedMessage.getPayload()).thenReturn(messageBody);
    }

    @Test
    public void shouldSendDelayedMessage_withMessage() throws Exception {
        // Given
        final int delaySeconds = 1 + Randoms.randomInt(10);

        // When
        sqsMessageSender.sendDelayedMessage(typedMessage, delaySeconds);

        // Then
        checkSendMessageRequest(delaySeconds);
    }

    @Test
    public void shouldSendMessage_withMessage() throws Exception {
        // Given

        // When
        sqsMessageSender.sendMessage(typedMessage);

        // Then
        checkSendMessageRequest(0);
    }

    private void checkSendMessageRequest(final int delaySeconds) throws Exception {
        final ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(mockAmazonSqsClient).sendMessage(captor.capture());
        final SendMessageRequest actualSendMessageRequest = captor.getValue();
        assertEquals(queueUrl, actualSendMessageRequest.getQueueUrl());
        assertEquals(new Integer(delaySeconds), actualSendMessageRequest.getDelaySeconds());
        final String actualMessageBody = actualSendMessageRequest.getMessageBody();
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode rootNode = mapper.readTree(actualMessageBody);
        final String actualSentSubject = rootNode.get("Subject").textValue();
        final String actualSentMessageBody = rootNode.get("Message").textValue();
        assertEquals(subject, actualSentSubject);
        assertEquals(messageBody, actualSentMessageBody);
    }
}
