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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.amazonaws.services.sns.AmazonSNS;
import com.clicktravel.cheddar.infrastructure.messaging.Message;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessagePublishException;
import com.clicktravel.common.random.Randoms;

public class SnsMessagePublisherTest {

    @Test
    public void shouldPublishMessage_withMessageAndTopicArnSet() {
        // Given
        final AmazonSNS mockAmazonSnsClient = mock(AmazonSNS.class);
        final String exchange = Randoms.randomString(15);
        final String subject = Randoms.randomString(10);
        final String messagePayload = Randoms.randomString(20);
        final SnsMessagePublisher snsMessagePublisher = new SnsMessagePublisher(exchange);
        final String topicArn = randomString(10);
        snsMessagePublisher.configure(mockAmazonSnsClient, topicArn);
        final Message message = mock(Message.class);
        when(message.getType()).thenReturn(subject);
        when(message.getPayload()).thenReturn(messagePayload);

        // When
        snsMessagePublisher.publishMessage(message);

        // Then
        verify(mockAmazonSnsClient).publish(
                argThat(new PublishRequestArgumentMatcher(topicArn, subject, messagePayload)));
    }

    @Test
    public void shouldNotPublishMessage_withMessageAndNoTopicArnSet() {
        // Given
        final String exchange = Randoms.randomString(15);
        final String subject = Randoms.randomString(10);
        final String messagePayload = Randoms.randomString(20);
        final SnsMessagePublisher snsMessagePublisher = new SnsMessagePublisher(exchange);
        final Message message = mock(Message.class);
        when(message.getType()).thenReturn(subject);
        when(message.getPayload()).thenReturn(messagePayload);

        // When
        MessagePublishException expectedException = null;
        try {
            snsMessagePublisher.publishMessage(message);
        } catch (final MessagePublishException e) {
            expectedException = e;
        }

        // Then
        assertNotNull(expectedException);
    }
}
