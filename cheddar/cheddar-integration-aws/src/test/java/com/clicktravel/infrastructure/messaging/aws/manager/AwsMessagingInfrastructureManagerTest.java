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
package com.clicktravel.infrastructure.messaging.aws.manager;

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.ListTopicsResult;
import com.amazonaws.services.sns.model.Topic;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.clicktravel.infrastructure.messaging.aws.SnsMessagePublisher;
import com.clicktravel.infrastructure.messaging.aws.SqsMessageQueueAccessor;

public class AwsMessagingInfrastructureManagerTest {

    private AmazonSQS mockAmazonSqsClient;
    private AmazonSNS mockAmazonSnsClient;

    @Before
    public void setup() {
        mockAmazonSqsClient = mock(AmazonSQS.class);
        mockAmazonSnsClient = mock(AmazonSNS.class);
    }

    @Test
    public void shouldReturnTrueIfQueueExists_withExistingQueue() {
        // Given
        final String queueName = randomString(10);
        final SqsMessageQueueAccessor mockSqsMessageQueueAccessor = mock(SqsMessageQueueAccessor.class);
        final Collection<SqsMessageQueueAccessor> sqsMessageQueueAccessors = new ArrayList<>(
                Arrays.asList(mockSqsMessageQueueAccessor));
        when(mockSqsMessageQueueAccessor.amazonSqsClient()).thenReturn(mockAmazonSqsClient);
        when(mockSqsMessageQueueAccessor.queueName()).thenReturn(queueName);
        final GetQueueUrlResult mockGetQueueUrlResult = mock(GetQueueUrlResult.class);
        when(mockAmazonSqsClient.getQueueUrl(any(GetQueueUrlRequest.class))).thenReturn(mockGetQueueUrlResult);
        final AwsMessagingInfrastructureManager messagingInfrastructureManager = new AwsMessagingInfrastructureManager(
                mockAmazonSqsClient, mockAmazonSnsClient, sqsMessageQueueAccessors);

        // When
        final boolean result = messagingInfrastructureManager.queueExists(queueName);

        // Then
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseIfQueueDoesNotExist_withNonExistingQueue() {
        // Given
        final String queueName = randomString(10);
        doThrow(new QueueDoesNotExistException(null)).when(mockAmazonSqsClient).getQueueUrl(
                any(GetQueueUrlRequest.class));
        final SqsMessageQueueAccessor mockSqsMessageQueueAccessor = mock(SqsMessageQueueAccessor.class);
        final Collection<SqsMessageQueueAccessor> sqsMessageQueueAccessors = new ArrayList<>(
                Arrays.asList(mockSqsMessageQueueAccessor));
        when(mockSqsMessageQueueAccessor.amazonSqsClient()).thenReturn(mockAmazonSqsClient);
        when(mockSqsMessageQueueAccessor.queueName()).thenReturn(queueName);
        final AwsMessagingInfrastructureManager messagingInfrastructureManager = new AwsMessagingInfrastructureManager(
                mockAmazonSqsClient, mockAmazonSnsClient, sqsMessageQueueAccessors);

        // When
        final boolean result = messagingInfrastructureManager.queueExists(queueName);

        // Then
        assertFalse(result);
        final ArgumentCaptor<GetQueueUrlRequest> getQueueUrlRequestArgumentCaptor = ArgumentCaptor
                .forClass(GetQueueUrlRequest.class);
        verify(mockAmazonSqsClient).getQueueUrl(getQueueUrlRequestArgumentCaptor.capture());
        assertEquals(queueName, getQueueUrlRequestArgumentCaptor.getValue().getQueueName());
    }

    @Test
    public void shouldCreateQueue_withQueueName() {
        // Given
        final String queueName = randomString(10);
        final SqsMessageQueueAccessor mockSqsMessageQueueAccessor = mock(SqsMessageQueueAccessor.class);
        final Collection<SqsMessageQueueAccessor> sqsMessageQueueAccessors = new ArrayList<>(
                Arrays.asList(mockSqsMessageQueueAccessor));
        when(mockSqsMessageQueueAccessor.amazonSqsClient()).thenReturn(mockAmazonSqsClient);
        when(mockSqsMessageQueueAccessor.queueName()).thenReturn(queueName);
        final AwsMessagingInfrastructureManager messagingInfrastructureManager = new AwsMessagingInfrastructureManager(
                mockAmazonSqsClient, mockAmazonSnsClient, sqsMessageQueueAccessors);

        // When
        messagingInfrastructureManager.createQueue(queueName);

        // Then
        final ArgumentCaptor<CreateQueueRequest> createQueueRequestArgumentCaptor = ArgumentCaptor
                .forClass(CreateQueueRequest.class);
        verify(mockAmazonSqsClient).createQueue(createQueueRequestArgumentCaptor.capture());
        final CreateQueueRequest createQueueRequest = createQueueRequestArgumentCaptor.getValue();
        assertEquals(queueName, createQueueRequest.getQueueName());
        final Map<String, String> queueAttributes = createQueueRequest.getAttributes();
        assertNotNull(queueAttributes);
        final String visibility = queueAttributes.get("VisibilityTimeout");
        assertEquals("300", visibility);
    }

    @Test
    public void shouldReturnTopicArnForExchange_withExistingExchange() {
        // Given
        final String exchangeName = randomString(10);
        final String topicArn = randomString(10) + ":" + exchangeName;
        final SnsMessagePublisher snsMessagePublisher = mock(SnsMessagePublisher.class);
        final Collection<SnsMessagePublisher> snsMessagePublishers = new ArrayList<>(Arrays.asList(snsMessagePublisher));
        final ListTopicsResult mockListTopicsResult = mock(ListTopicsResult.class);
        final Topic topic = new Topic();
        topic.setTopicArn(topicArn);
        when(snsMessagePublisher.amazonSnsClient()).thenReturn(mockAmazonSnsClient);
        when(snsMessagePublisher.exchangeName()).thenReturn(exchangeName);
        when(mockListTopicsResult.getTopics()).thenReturn(Arrays.asList(topic));
        when(mockAmazonSnsClient.listTopics(anyString())).thenReturn(mockListTopicsResult);
        final AwsMessagingInfrastructureManager messagingInfrastructureManager = new AwsMessagingInfrastructureManager(
                mockAmazonSqsClient, mockAmazonSnsClient, null);
        messagingInfrastructureManager.setMessagePublishers(snsMessagePublishers);

        // When
        final String result = messagingInfrastructureManager.topicArnForExchangeName(exchangeName);

        // Then
        assertEquals(topicArn, result);
    }

    @Test
    public void shouldReturnFalseIfExchangeDoesNotExist_withNonExistingExchange() {
        // Given
        final String exchangeName = randomString(10);
        final String topicArn = randomString(10) + ":" + randomString(10);
        final SnsMessagePublisher snsMessagePublisher = mock(SnsMessagePublisher.class);
        final Collection<SnsMessagePublisher> snsMessagePublishers = new ArrayList<>(Arrays.asList(snsMessagePublisher));
        final ListTopicsResult mockListTopicsResult = mock(ListTopicsResult.class);
        final Topic topic = new Topic();
        topic.setTopicArn(topicArn);
        when(snsMessagePublisher.amazonSnsClient()).thenReturn(mockAmazonSnsClient);
        when(snsMessagePublisher.exchangeName()).thenReturn(exchangeName);
        when(mockListTopicsResult.getTopics()).thenReturn(Arrays.asList(topic));
        when(mockAmazonSnsClient.listTopics(anyString())).thenReturn(mockListTopicsResult);
        final AwsMessagingInfrastructureManager messagingInfrastructureManager = new AwsMessagingInfrastructureManager(
                mockAmazonSqsClient, mockAmazonSnsClient, null);
        messagingInfrastructureManager.setMessagePublishers(snsMessagePublishers);

        // When
        final String result = messagingInfrastructureManager.topicArnForExchangeName(exchangeName);

        // Then
        assertNull(result);
    }

    @Test
    public void shouldCreateExchange_withExchangeName() {
        // Given
        final String exchangeName = randomString(10);
        final SnsMessagePublisher snsMessagePublisher = mock(SnsMessagePublisher.class);
        final Collection<SnsMessagePublisher> snsMessagePublishers = new ArrayList<>(Arrays.asList(snsMessagePublisher));
        final CreateTopicResult mockCreateTopicResult = mock(CreateTopicResult.class);
        final String topicArn = randomString(5) + ":" + randomString(5) + ":" + exchangeName;
        when(snsMessagePublisher.amazonSnsClient()).thenReturn(mockAmazonSnsClient);
        when(snsMessagePublisher.exchangeName()).thenReturn(exchangeName);
        when(mockCreateTopicResult.getTopicArn()).thenReturn(topicArn);
        when(mockAmazonSnsClient.createTopic(any(CreateTopicRequest.class))).thenReturn(mockCreateTopicResult);
        final AwsMessagingInfrastructureManager messagingInfrastructureManager = new AwsMessagingInfrastructureManager(
                mockAmazonSqsClient, mockAmazonSnsClient, null);
        messagingInfrastructureManager.setMessagePublishers(snsMessagePublishers);

        // When
        final String result = messagingInfrastructureManager.createExchange(exchangeName);

        // Then
        assertNotNull(result);
        final ArgumentCaptor<CreateTopicRequest> createTopicRequestArgumentCaptor = ArgumentCaptor
                .forClass(CreateTopicRequest.class);
        verify(mockAmazonSnsClient).createTopic(createTopicRequestArgumentCaptor.capture());
        assertEquals(exchangeName, createTopicRequestArgumentCaptor.getValue().getName());

    }
}
