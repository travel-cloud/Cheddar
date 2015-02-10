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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.clicktravel.cheddar.infrastructure.messaging.MessagePublisher;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessagePublishException;

@Deprecated
public class SnsMessagePublisher implements MessagePublisher<TypedMessage> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String exchangeName;
    private AmazonSNS amazonSnsClient;
    private String topicArn;
    private boolean configured;

    public SnsMessagePublisher(final String exchangeName) {
        this.exchangeName = exchangeName;
        configured = false;
    }

    @Override
    public void publishMessage(final TypedMessage typedMessage) throws MessagePublishException {
        if (!configured) {
            throw new MessagePublishException("Publisher has not been configured. Exchange: [" + exchangeName + "]");
        }
        try {
            amazonSnsClient.publish(new PublishRequest().withTopicArn(topicArn).withSubject(typedMessage.getType())
                    .withMessage(typedMessage.getPayload()));
            logger.debug("Successfully published message: [Subject=" + typedMessage.getType() + ", Message="
                    + typedMessage.getPayload() + "] to SNS: [" + exchangeName + "]");
        } catch (final Exception e) {
            throw new MessagePublishException("Could not publish to SNS: [" + exchangeName + "]", e);
        }
    }

    public void configure(final AmazonSNS amazonSnsClient, final String topicArn) {
        this.amazonSnsClient = amazonSnsClient;
        this.topicArn = topicArn;
        configured = true;
    }

    @Override
    public String exchangeName() {
        return exchangeName;
    }

    public AmazonSNS amazonSnsClient() {
        return amazonSnsClient;
    }

}