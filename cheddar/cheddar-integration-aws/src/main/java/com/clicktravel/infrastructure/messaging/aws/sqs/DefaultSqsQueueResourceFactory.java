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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;

public class DefaultSqsQueueResourceFactory implements SqsQueueResourceFactory {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AmazonSQS amazonSqsClient;

    @Autowired
    public DefaultSqsQueueResourceFactory(final AmazonSQS amazonSqsClient) {
        this.amazonSqsClient = amazonSqsClient;
    }

    @Override
    public SqsQueueResource createSqsQueueResource(final String name) {
        final String queueUrl = amazonSqsClient.getQueueUrl(new GetQueueUrlRequest(name)).getQueueUrl();
        logger.info("Using existing SQS queue: " + name);
        final SqsQueueResource sqsQueueResource = new SqsQueueResource(name, queueUrl, amazonSqsClient);
        return sqsQueueResource;
    }

}
