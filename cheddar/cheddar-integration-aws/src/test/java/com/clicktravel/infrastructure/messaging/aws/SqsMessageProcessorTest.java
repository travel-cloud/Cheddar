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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonServiceException.ErrorType;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.clicktravel.cheddar.infrastructure.messaging.MessageHandler;
import com.clicktravel.common.concurrent.RateLimiter;
import com.clicktravel.common.random.Randoms;

public class SqsMessageProcessorTest {

    private AmazonSQS mockAmazonSQSClient;
    private String queueName;
    private String queueUrl;
    private String handledMessageType;
    private String handledMessagePayload;
    private MessageHandler mockMessageHandler;
    private Map<String, MessageHandler> messageHandlers;
    private SqsMessageProcessorExecutor mockSqsMessageProcessorExecutor;
    private RateLimiter mockRateLimiter;
    private com.amazonaws.services.sqs.model.Message mockSqsMessage;
    private GetQueueUrlResult mockGetQueueUrlResult;

    @Before
    public void setup() throws Exception {
        queueName = randomString();
        queueUrl = randomString();
        mockGetQueueUrlResult = mock(GetQueueUrlResult.class);
        when(mockGetQueueUrlResult.getQueueUrl()).thenReturn(queueUrl);
        mockAmazonSQSClient = mock(AmazonSQS.class);
        handledMessageType = randomString();
        handledMessagePayload = randomString();
        mockMessageHandler = mock(MessageHandler.class);
        messageHandlers = new HashMap<>();
        messageHandlers.put(handledMessageType, mockMessageHandler);
        mockSqsMessageProcessorExecutor = mock(SqsMessageProcessorExecutor.class);
        when(mockSqsMessageProcessorExecutor.getMaximumPoolSize()).thenReturn(1 + Randoms.randomInt(5));
        mockRateLimiter = mock(RateLimiter.class);
        mockSqsMessage = mock(com.amazonaws.services.sqs.model.Message.class);
        final String messageBody = "{ \"Subject\" : \"" + handledMessageType + "\", \"Message\" : \""
                + handledMessagePayload + "\" }";
        when(mockSqsMessage.getBody()).thenReturn(messageBody);
    }

    @Test
    public void shouldCreateSqsMessageProcessor() throws Exception {
        // Given
        final ArgumentCaptor<GetQueueUrlRequest> getQueueUrlRequestCaptor = ArgumentCaptor
                .forClass(GetQueueUrlRequest.class);
        when(mockAmazonSQSClient.getQueueUrl(getQueueUrlRequestCaptor.capture())).thenReturn(mockGetQueueUrlResult);

        // When
        new SqsMessageProcessor(mockAmazonSQSClient, queueName, messageHandlers, mockRateLimiter,
                mockSqsMessageProcessorExecutor);

        // Then
        final GetQueueUrlRequest actualGetQueueUrlRequest = getQueueUrlRequestCaptor.getValue();
        assertEquals(queueName, actualGetQueueUrlRequest.getQueueName());
    }

    @Test(timeout = 2000)
    public void shouldExecuteMessageHandlingWorkerThenShutdown_withMessageAndShutdownRequest() throws Exception {
        // Given
        when(mockAmazonSQSClient.getQueueUrl(any(GetQueueUrlRequest.class))).thenReturn(mockGetQueueUrlResult);
        final List<com.amazonaws.services.sqs.model.Message> receivedSqsMessages = new ArrayList<>();
        receivedSqsMessages.add(mockSqsMessage);
        final ReceiveMessageResult mockReceiveMessageResultWithMessage = mock(ReceiveMessageResult.class);
        when(mockReceiveMessageResultWithMessage.getMessages()).thenReturn(receivedSqsMessages);
        final ReceiveMessageResult mockReceiveMessageResultWithoutMessage = mock(ReceiveMessageResult.class);
        when(mockReceiveMessageResultWithoutMessage.getMessages()).thenReturn(
                new ArrayList<com.amazonaws.services.sqs.model.Message>());
        final ArgumentCaptor<ReceiveMessageRequest> receiveMessageRequestCaptor = ArgumentCaptor
                .forClass(ReceiveMessageRequest.class);
        when(mockAmazonSQSClient.receiveMessage(receiveMessageRequestCaptor.capture())).then(
                new Answer<ReceiveMessageResult>() {
                    private boolean firstInvocation = true;

                    @Override
                    public ReceiveMessageResult answer(final InvocationOnMock invocation) throws Throwable {
                        if (firstInvocation) {
                            firstInvocation = false;
                            return mockReceiveMessageResultWithMessage;
                        } else {
                            Thread.sleep(400);
                            return mockReceiveMessageResultWithoutMessage;
                        }
                    }
                });
        final SqsMessageProcessor sqsMessageProcessor = new SqsMessageProcessor(mockAmazonSQSClient, queueName,
                messageHandlers, mockRateLimiter, mockSqsMessageProcessorExecutor);

        // When
        final Thread sqsMessageProcessorThread = new Thread(sqsMessageProcessor);
        sqsMessageProcessorThread.start();
        Thread.sleep(200);
        sqsMessageProcessor.shutdown();
        Thread.sleep(600);

        // Then
        verify(mockRateLimiter).takeToken();
        final ArgumentCaptor<MessageHandlingWorker> messageHandlingWorkerCaptor = ArgumentCaptor
                .forClass(MessageHandlingWorker.class);
        verify(mockSqsMessageProcessorExecutor).execute(messageHandlingWorkerCaptor.capture());
        final MessageHandlingWorker messageHandlingWorker = messageHandlingWorkerCaptor.getValue();
        assertEquals(handledMessageType, messageHandlingWorker.message().getType());
        assertEquals(handledMessagePayload, messageHandlingWorker.message().getPayload());
        assertSame(mockMessageHandler, messageHandlingWorker.messageHandler());
        assertTrue(sqsMessageProcessorThread.getState().equals(State.TERMINATED));
    }

    @Test(timeout = 2000)
    public void shouldContinueProcessingMessages_withServiceException() throws Exception {
        // Given
        when(mockAmazonSQSClient.getQueueUrl(any(GetQueueUrlRequest.class))).thenReturn(mockGetQueueUrlResult);
        final AmazonServiceException mockAmazonServiceException = mock(AmazonServiceException.class);
        when(mockAmazonServiceException.getErrorType()).thenReturn(ErrorType.Service);
        final ReceiveMessageResult mockReceiveMessageResultWithoutMessage = mock(ReceiveMessageResult.class);
        when(mockReceiveMessageResultWithoutMessage.getMessages()).thenReturn(
                new ArrayList<com.amazonaws.services.sqs.model.Message>());
        final boolean[] secondInvocation = { false };
        when(mockAmazonSQSClient.receiveMessage(any(ReceiveMessageRequest.class))).then(
                new Answer<ReceiveMessageResult>() {
                    private boolean firstInvocation = true;

                    @Override
                    public ReceiveMessageResult answer(final InvocationOnMock invocation) throws Throwable {
                        if (firstInvocation) {
                            firstInvocation = false;
                            throw mockAmazonServiceException;
                        } else {
                            secondInvocation[0] = true;
                            Thread.sleep(100);
                            return mockReceiveMessageResultWithoutMessage;
                        }
                    }
                });
        final SqsMessageProcessor sqsMessageProcessor = new SqsMessageProcessor(mockAmazonSQSClient, queueName,
                messageHandlers, mockRateLimiter, mockSqsMessageProcessorExecutor);

        // When
        final Thread sqsMessageProcessorThread = new Thread(sqsMessageProcessor);
        sqsMessageProcessorThread.start();
        Thread.sleep(700);
        sqsMessageProcessor.shutdown();
        Thread.sleep(300);

        // Then
        assertTrue(secondInvocation[0]);
    }
}
