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
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.messaging.BasicMessage;

public class SqsBasicMessageQueueTest {

    private SqsQueueResource mockSqsQueueResource;
    private SqsBasicMessageQueue sqsBasicMessageQueue;

    @Before
    public void setUp() {
        mockSqsQueueResource = mock(SqsQueueResource.class);
        sqsBasicMessageQueue = new SqsBasicMessageQueue(mockSqsQueueResource);
    }

    @Test
    public void shouldSendMessage_withMessage() throws Exception {
        // Given
        final BasicMessage mockBasicMessage = mock(BasicMessage.class);
        final String body = randomString();
        when(mockBasicMessage.getBody()).thenReturn(body);

        // When
        sqsBasicMessageQueue.send(mockBasicMessage);

        // Then
        verify(mockSqsQueueResource).sendMessage(body);
    }

    @Test
    public void shouldReturnMessages_onReceiveMessage() {
        // When
        final List<com.amazonaws.services.sqs.model.Message> mockSqsMessages = new LinkedList<>();
        final List<String> bodies = new LinkedList<>();
        final List<String> receiptHandles = new LinkedList<>();
        final List<String> messageIds = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            final String body = randomString();
            final String messageId = randomId();
            final String receiptHandle = randomId();
            final com.amazonaws.services.sqs.model.Message mockSqsMessage = mock(
                    com.amazonaws.services.sqs.model.Message.class);
            when(mockSqsMessage.getMessageId()).thenReturn(messageId);
            when(mockSqsMessage.getReceiptHandle()).thenReturn(receiptHandle);
            when(mockSqsMessage.getBody()).thenReturn(body);
            mockSqsMessages.add(mockSqsMessage);
            bodies.add(body);
            messageIds.add(messageId);
            receiptHandles.add(receiptHandle);
        }
        when(mockSqsQueueResource.receiveMessages()).thenReturn(mockSqsMessages);

        // When
        final List<BasicMessage> receivedMessages = sqsBasicMessageQueue.receive();

        // Then
        assertNotNull(receivedMessages);
        assertEquals(3, receivedMessages.size());
        for (int i = 0; i < 3; i++) {
            final BasicMessage receivedMessage = receivedMessages.get(i);
            assertEquals(bodies.get(i), receivedMessage.getBody());
            assertEquals(messageIds.get(i), receivedMessage.getMessageId());
            assertEquals(receiptHandles.get(i), receivedMessage.getReceiptHandle());
        }
    }
}
