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

import com.amazonaws.services.sqs.AmazonSQS;
import com.clicktravel.cheddar.infrastructure.messaging.MessageQueueAccessor;

public class SqsMessageQueueAccessor implements MessageQueueAccessor {

    private AmazonSQS amazonSqsClient;
    private final String queueName;
    private boolean configured;

    public SqsMessageQueueAccessor(final String queueName) {
        this.queueName = queueName;
        configured = false;
    }

    @Override
    public String queueName() {
        return queueName;
    }

    public AmazonSQS amazonSqsClient() {
        if (!configured) {
            throw new IllegalStateException("Message queue accessor has not been configured");
        }
        return amazonSqsClient;
    }

    public void configure(final AmazonSQS amazonSqsClient) {
        this.amazonSqsClient = amazonSqsClient;
        configured = true;
    }

}
