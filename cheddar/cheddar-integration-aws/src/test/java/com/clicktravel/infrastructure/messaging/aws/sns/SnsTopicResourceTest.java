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
package com.clicktravel.infrastructure.messaging.aws.sns;

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.SetTopicAttributesRequest;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.clicktravel.infrastructure.messaging.aws.sqs.SqsQueueResource;

@SuppressWarnings("unchecked")
public class SnsTopicResourceTest {

    private SnsTopicResource snsTopicResource;
    private String topicName;
    private String topicArn;
    private AmazonSNS mockAmazonSnsClient;

    @Before
    public void setUp() {
        topicName = randomString();
        topicArn = randomString();
        mockAmazonSnsClient = mock(AmazonSNS.class);
        snsTopicResource = new SnsTopicResource(topicName, topicArn, mockAmazonSnsClient);
    }

    @Test
    public void shouldPublish_withSubjectAndMessage() {
        // Given
        final String subject = randomString();
        final String message = randomString();

        // When
        snsTopicResource.publish(subject, message);

        // Then
        final ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(mockAmazonSnsClient).publish(captor.capture());
        final PublishRequest publishRequest = captor.getValue();
        assertEquals(topicArn, publishRequest.getTopicArn());
        assertEquals(subject, publishRequest.getSubject());
        assertEquals(message, publishRequest.getMessage());
        assertEquals(subject, publishRequest.getMessageAttributes().get("subject").getStringValue());
        assertEquals("String", publishRequest.getMessageAttributes().get("subject").getDataType());
    }

    @Test
    public void shouldThrowException_onAmazonClientExceptionFromPublish() {
        // Given
        final String subject = randomString();
        final String message = randomString();
        when(mockAmazonSnsClient.publish(any(PublishRequest.class))).thenThrow(AmazonClientException.class);

        // When
        AmazonClientException thrownException = null;
        try {
            snsTopicResource.publish(subject, message);
        } catch (final AmazonClientException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldSubscribe_withSqsQueueResource() {
        // Given
        final SqsQueueResource mockSqsQueueResource = mock(SqsQueueResource.class);
        final String queueArn = randomString();
        when(mockSqsQueueResource.queueArn()).thenReturn(queueArn);

        // When
        snsTopicResource.subscribe(mockSqsQueueResource);

        // Then
        final ArgumentCaptor<SubscribeRequest> captor = ArgumentCaptor.forClass(SubscribeRequest.class);
        verify(mockAmazonSnsClient).subscribe(captor.capture());
        final SubscribeRequest subscribeRequest = captor.getValue();
        assertEquals(topicArn, subscribeRequest.getTopicArn());
        assertEquals("sqs", subscribeRequest.getProtocol());
        assertEquals(queueArn, subscribeRequest.getEndpoint());
    }

    @Test
    public void shouldThrowException_onAmazonClientExceptionFromSubscribe() {
        // Given
        final SqsQueueResource mockSqsQueueResource = mock(SqsQueueResource.class);
        final String queueArn = randomString();
        when(mockSqsQueueResource.queueArn()).thenReturn(queueArn);
        when(mockAmazonSnsClient.subscribe(any(SubscribeRequest.class))).thenThrow(AmazonClientException.class);

        // When
        AmazonClientException thrownException = null;
        try {
            snsTopicResource.subscribe(mockSqsQueueResource);
        } catch (final AmazonClientException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldSetPolicy_withPolicy() {
        // Given
        final Policy mockPolicy = mock(Policy.class);
        final String mockPolicyJson = randomString();
        when(mockPolicy.toJson()).thenReturn(mockPolicyJson);

        // When
        snsTopicResource.setPolicy(mockPolicy);

        // Then
        final ArgumentCaptor<SetTopicAttributesRequest> captor = ArgumentCaptor
                .forClass(SetTopicAttributesRequest.class);
        verify(mockAmazonSnsClient).setTopicAttributes(captor.capture());
        final SetTopicAttributesRequest setTopicAttributesRequest = captor.getValue();
        assertEquals(topicArn, setTopicAttributesRequest.getTopicArn());
        assertEquals("Policy", setTopicAttributesRequest.getAttributeName());
        assertEquals(mockPolicyJson, setTopicAttributesRequest.getAttributeValue());
    }

    @Test
    public void shouldThrowException_onAmazonClientExceptionFromSetPolicy() {
        // Given
        final Policy mockPolicy = mock(Policy.class);
        final String mockPolicyJson = randomString();
        when(mockPolicy.toJson()).thenReturn(mockPolicyJson);
        doThrow(AmazonClientException.class).when(mockAmazonSnsClient)
                .setTopicAttributes(any(SetTopicAttributesRequest.class));

        // When
        AmazonClientException thrownException = null;
        try {
            snsTopicResource.setPolicy(mockPolicy);
        } catch (final AmazonClientException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldReturnTopicName() {
        // When
        final String returnedTopicName = snsTopicResource.getTopicName();

        // Then
        assertEquals(topicName, returnedTopicName);
    }

    @Test
    public void shouldReturnTopicArn() {
        // When
        final String returnedTopicArn = snsTopicResource.getTopicArn();

        // Then
        assertEquals(topicArn, returnedTopicArn);
    }
}
