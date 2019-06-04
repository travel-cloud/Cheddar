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
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.ListTopicsResult;
import com.amazonaws.services.sns.model.Topic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DefaultSnsTopicResourceFactory.class)
@PowerMockIgnore("javax.xml.*")
public class DefaultSnsTopicResourceFactoryTest {

    private DefaultSnsTopicResourceFactory factory;
    private AmazonSNS mockAmazonSnsClient;
    private String region;
    private String accountId;

    @Before
    public void setUp() {
        mockAmazonSnsClient = mock(AmazonSNS.class);
        factory = new DefaultSnsTopicResourceFactory(mockAmazonSnsClient);
        region = randomString(10);
        accountId = randomString(20);
    }

    @Test
    public void shouldCreateSnsTopicResource_withName() throws Exception {
        // Given
        final String name = randomString();
        final String topicArn = topicArnForName(name);
        final List<Topic> topics = Arrays.asList(randomTopic(), randomTopic(), topicForArn(topicArn), randomTopic());
        final ListTopicsResult mockListTopicsResult = mock(ListTopicsResult.class);
        when(mockListTopicsResult.getTopics()).thenReturn(topics);
        when(mockListTopicsResult.getNextToken()).thenReturn(null);
        final String nextToken = null;
        when(mockAmazonSnsClient.listTopics(nextToken)).thenReturn(mockListTopicsResult);
        final SnsTopicResource mockSnsTopicResource = mock(SnsTopicResource.class);
        whenNew(SnsTopicResource.class).withArguments(name, topicArn, mockAmazonSnsClient)
                .thenReturn(mockSnsTopicResource);

        // When
        final SnsTopicResource result = factory.createSnsTopicResource(name);

        // Then
        assertSame(mockSnsTopicResource, result);
    }

    private String topicArnForName(final String topicName) {
        return String.format("arn:aws:sns:%s:%s:%s", region, accountId, topicName);
    }

    private Topic topicForArn(final String topicArn) {
        return new Topic().withTopicArn(topicArn);
    }

    private Topic randomTopic() {
        return topicForArn(topicArnForName(randomString(20)));
    }

}
