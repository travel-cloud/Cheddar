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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.clicktravel.cheddar.infrastructure.messaging.MessageHandler;
import com.clicktravel.common.concurrent.RateLimiter;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SqsMessageProcessor.class, LinkedBlockingQueue.class, ThreadPoolExecutor.class })
@SuppressWarnings("unchecked")
public class SqsMessageProcessorTest {

    private static final long THREAD_WAKE_UP_TIME = 1000;
    private LinkedBlockingQueue<?> mockLinkedBlockingQueue;
    private ThreadPoolExecutor mockThreadPoolExecutor;
    private RateLimiter mockRateLimiter;

    @Before
    public void setup() throws Exception {
        mockLinkedBlockingQueue = mock(LinkedBlockingQueue.class);
        mockThreadPoolExecutor = mock(ThreadPoolExecutor.class);
        mockRateLimiter = mock(RateLimiter.class);
        whenNew(LinkedBlockingQueue.class).withAnyArguments().thenReturn(mockLinkedBlockingQueue);
        whenNew(ThreadPoolExecutor.class).withAnyArguments().thenReturn(mockThreadPoolExecutor);
    }

    @Test
    public void shouldCreateSqsMessageProcessor() throws Exception {
        // Given
        final AmazonSQS amazonSqsClient = mock(AmazonSQS.class);
        final String queueName = randomString(10);
        final Map<String, MessageHandler> messageHandlers = mock(Map.class);
        final GetQueueUrlResult getQueueUrlResult = mock(GetQueueUrlResult.class);
        final String queueUrl = randomString(10);
        when(getQueueUrlResult.getQueueUrl()).thenReturn(queueUrl);
        when(amazonSqsClient.getQueueUrl(any(GetQueueUrlRequest.class))).thenReturn(getQueueUrlResult);

        // When
        final SqsMessageProcessor sqsMessageProcessor = new SqsMessageProcessor(amazonSqsClient, queueName,
                messageHandlers, mockRateLimiter);

        // Then
        assertNotNull(sqsMessageProcessor);
        assertTrue(sqsMessageProcessor.isProcessing());
        final ArgumentCaptor<GetQueueUrlRequest> getQueueUrlRequestArgumentCaptor = ArgumentCaptor
                .forClass(GetQueueUrlRequest.class);
        verify(amazonSqsClient).getQueueUrl(getQueueUrlRequestArgumentCaptor.capture());
        assertEquals(queueName, getQueueUrlRequestArgumentCaptor.getValue().getQueueName());
        verifyNew(LinkedBlockingQueue.class).withNoArguments();
        verifyNew(ThreadPoolExecutor.class).withArguments(10, 10, 0L, TimeUnit.SECONDS, mockLinkedBlockingQueue);
    }

    @Test
    public void shouldStartProcessing_withMessages() throws Exception {
        // Given
        final AmazonSQS amazonSqsClient = mock(AmazonSQS.class);
        final String queueName = randomString(10);
        final Map<String, MessageHandler> messageHandlers = new HashMap<>();
        final String messageType = randomString(10);
        final MessageHandler messageHandler = mock(MessageHandler.class);
        messageHandlers.put(messageType, messageHandler);
        final GetQueueUrlResult getQueueUrlResult = mock(GetQueueUrlResult.class);
        final String queueUrl = randomString(10);
        when(getQueueUrlResult.getQueueUrl()).thenReturn(queueUrl);
        when(amazonSqsClient.getQueueUrl(any(GetQueueUrlRequest.class))).thenReturn(getQueueUrlResult);
        final ReceiveMessageResult receiveMessageResult = mock(ReceiveMessageResult.class);
        final com.amazonaws.services.sqs.model.Message mockSqsMessage = mock(com.amazonaws.services.sqs.model.Message.class);
        when(mockSqsMessage.getBody()).thenReturn("{\"Subject\":\"" + messageType + "\",\"Message\":null}");
        final String messageReceiptHandle = randomString(10);
        when(mockSqsMessage.getReceiptHandle()).thenReturn(messageReceiptHandle);
        final List<com.amazonaws.services.sqs.model.Message> messages = Arrays.asList(mockSqsMessage);
        when(receiveMessageResult.getMessages()).thenReturn(messages);
        when(amazonSqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(receiveMessageResult);
        final SqsMessageProcessor sqsMessageProcessor = new SqsMessageProcessor(amazonSqsClient, queueName,
                messageHandlers, mockRateLimiter);

        // When
        final Thread sqsMessageProcessorThread = new Thread(sqsMessageProcessor);
        sqsMessageProcessorThread.start();
        Thread.sleep(THREAD_WAKE_UP_TIME);

        // Then
        assertTrue(sqsMessageProcessor.isProcessing());
        assertTrue(sqsMessageProcessorThread.isAlive());
        final ArgumentCaptor<ReceiveMessageRequest> receiveMessageRequestArgumentCaptor = ArgumentCaptor
                .forClass(ReceiveMessageRequest.class);
        verify(amazonSqsClient, atLeast(1)).receiveMessage(receiveMessageRequestArgumentCaptor.capture());
        assertEquals(queueUrl, receiveMessageRequestArgumentCaptor.getValue().getQueueUrl());
        final ArgumentCaptor<MessageHandlingWorker> messageHandlingWorkerArgumentCaptor = ArgumentCaptor
                .forClass(MessageHandlingWorker.class);
        verify(mockThreadPoolExecutor, atLeast(1)).execute(messageHandlingWorkerArgumentCaptor.capture());
        assertEquals(messageType, messageHandlingWorkerArgumentCaptor.getValue().message().getType());
        assertEquals(messageHandler, messageHandlingWorkerArgumentCaptor.getValue().messageHandler());

        // When
        sqsMessageProcessor.shutdown();

        // Then
        assertFalse(sqsMessageProcessor.isProcessing());
    }

    @Test
    public void shouldStopProcessing_withRunningProcess() throws Exception {
        // Given
        final AmazonSQS amazonSqsClient = mock(AmazonSQS.class);
        final String queueName = randomString(10);
        final Map<String, MessageHandler> messageHandlers = new HashMap<>();
        final GetQueueUrlResult getQueueUrlResult = mock(GetQueueUrlResult.class);
        final String queueUrl = randomString(10);
        when(getQueueUrlResult.getQueueUrl()).thenReturn(queueUrl);
        when(amazonSqsClient.getQueueUrl(any(GetQueueUrlRequest.class))).thenReturn(getQueueUrlResult);
        final ReceiveMessageResult receiveMessageResult = mock(ReceiveMessageResult.class);
        final List<com.amazonaws.services.sqs.model.Message> messages = Arrays.asList();
        when(receiveMessageResult.getMessages()).thenReturn(messages);
        when(amazonSqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(receiveMessageResult);
        final SqsMessageProcessor sqsMessageProcessor = new SqsMessageProcessor(amazonSqsClient, queueName,
                messageHandlers, mockRateLimiter);

        // When
        final Thread sqsMessageProcessorThread = new Thread(sqsMessageProcessor);
        sqsMessageProcessorThread.start();
        Thread.sleep(THREAD_WAKE_UP_TIME);

        // Then
        assertTrue(sqsMessageProcessor.isProcessing());
        assertTrue(sqsMessageProcessorThread.isAlive());

        // When
        sqsMessageProcessor.shutdown();

        // Then
        assertFalse(sqsMessageProcessor.isProcessing());
    }

}
