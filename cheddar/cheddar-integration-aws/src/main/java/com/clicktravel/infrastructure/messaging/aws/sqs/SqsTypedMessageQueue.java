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

import com.clicktravel.cheddar.infrastructure.messaging.InvalidTypedMessage;
import com.clicktravel.cheddar.infrastructure.messaging.SimpleMessage;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * AWS SQS implementation for a {@code MessageQueue<TypedMessage>}
 */
public class SqsTypedMessageQueue extends SqsMessageQueue<TypedMessage> {

    public SqsTypedMessageQueue(final SqsQueueResource sqsQueueResource) {
        super(sqsQueueResource);
    }

    @Override
    protected String toSqsMessageBody(final TypedMessage typedMessage) {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("Subject", typedMessage.getType());
        rootNode.put("Message", typedMessage.getPayload());
        try {
            final String json = mapper.writeValueAsString(rootNode);
            return json;
        } catch (final Exception e) {
            throw new IllegalStateException("Could not serialize message for queue", e);
        }
    }

    @Override
    protected TypedMessage toMessage(final com.amazonaws.services.sqs.model.Message sqsMessage) {
        final String receiptHandle = sqsMessage.getReceiptHandle();
        final String messageId = sqsMessage.getMessageId();
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode jsonNode = mapper.readTree(sqsMessage.getBody());
            final String messageType = jsonNode.get("Subject").textValue();
            final String messagePayload = jsonNode.get("Message").textValue();
            return new SimpleMessage(messageType, messagePayload, messageId, receiptHandle);
        } catch (final Exception e) {
            return new InvalidTypedMessage(messageId, receiptHandle,
                    new MessageParseException("Could not parse message from SQS message: " + sqsMessage.getBody()));
        }
    }

}
