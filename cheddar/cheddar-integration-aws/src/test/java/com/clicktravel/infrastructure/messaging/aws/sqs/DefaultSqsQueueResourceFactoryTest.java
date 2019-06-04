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

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DefaultSqsQueueResourceFactory.class)
@PowerMockIgnore("javax.xml.*")
public class DefaultSqsQueueResourceFactoryTest {

    private DefaultSqsQueueResourceFactory factory;
    private AmazonSQS mockAmazonSqsClient;

    @Before
    public void setUp() {
        mockAmazonSqsClient = mock(AmazonSQS.class);
        factory = new DefaultSqsQueueResourceFactory(mockAmazonSqsClient);
    }

    @Test
    public void shouldCreateSqsQueueResource_withName() throws Exception {
        // Given
        final String name = randomString();
        final String queueUrl = randomString();
        final GetQueueUrlResult getQueueUrlResult = new GetQueueUrlResult().withQueueUrl(queueUrl);
        final GetQueueUrlRequest expectedGetQueueUrlRequest = new GetQueueUrlRequest(name);
        when(mockAmazonSqsClient.getQueueUrl(expectedGetQueueUrlRequest)).thenReturn(getQueueUrlResult);
        final SqsQueueResource mockSqsQueueResource = mock(SqsQueueResource.class);
        whenNew(SqsQueueResource.class).withArguments(name, queueUrl, mockAmazonSqsClient)
                .thenReturn(mockSqsQueueResource);

        // When
        final SqsQueueResource result = factory.createSqsQueueResource(name);

        // Then
        assertSame(mockSqsQueueResource, result);
    }
}
