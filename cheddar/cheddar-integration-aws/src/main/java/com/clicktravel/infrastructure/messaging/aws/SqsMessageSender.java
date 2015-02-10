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

import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.clicktravel.cheddar.infrastructure.messaging.MessageSender;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageSendException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Deprecated
public class SqsMessageSender extends SqsMessageQueueAccessor implements MessageSender<TypedMessage> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private String queueUrl;

    public SqsMessageSender(final String queueName) {
        super(queueName);
    }

    private String queueUrl() {
        if (queueUrl == null) {
            queueUrl = amazonSqsClient().getQueueUrl(new GetQueueUrlRequest(queueName())).getQueueUrl();
        }
        return queueUrl;
    }

    @Override
    public void sendMessage(final TypedMessage typedMessage) throws MessageSendException {
        sendDelayedMessage(typedMessage, 0);
    }

    @Override
    public void sendDelayedMessage(final TypedMessage typedMessage, final int delaySeconds) throws MessageSendException {
        try {
            final String json = asJson(typedMessage);
            amazonSqsClient().sendMessage(new SendMessageRequest(queueUrl(), json).withDelaySeconds(delaySeconds));
            logger.trace("Successfully sent message: Payload=" + json + "] to SQS queue: [" + queueName() + "]");
        } catch (final Exception e) {
            throw new MessageSendException("Could not send message to SQS queue: " + queueName(), e);
        }
    }

    private String asJson(final TypedMessage message) {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("Subject", message.getType());
        rootNode.put("Message", message.getPayload());
        try {
            final String json = mapper.writeValueAsString(rootNode);
            return json;
        } catch (final Exception e) {
            throw new IllegalStateException("Could not serialize message for queue", e);
        }
    }
}
