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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.infrastructure.messaging.InvalidTypedMessage;
import com.clicktravel.cheddar.infrastructure.messaging.SimpleMessage;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageParseException;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageReceiveException;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageSendException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * AWS SQS implementation for a {@code MessageQueue<TypedMessage>}
 */
public class SqsTypedMessageQueue extends SqsMessageQueue<TypedMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqsBasicMessageQueue.class);

    public SqsTypedMessageQueue(final SqsQueueResource sqsQueueResource) {
        super(sqsQueueResource);
    }

    public SqsTypedMessageQueue(final SqsQueueResource sqsQueueResource, final boolean logReceivedMessages) {
        super(sqsQueueResource, logReceivedMessages);
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

    @Override
    public List<TypedMessage> receive() throws MessageReceiveException {
        final List<TypedMessage> receivedMessages = super.receive();
        logReceivedMessages(receivedMessages);
        return receivedMessages;
    }

    @Override
    public List<TypedMessage> receive(final int waitTimeSeconds, final int maxMessages) throws MessageReceiveException {
        final List<TypedMessage> receivedMessages = super.receive(waitTimeSeconds, maxMessages);
        logReceivedMessages(receivedMessages);
        return receivedMessages;
    }

    @Override
    public void send(final TypedMessage message) throws MessageSendException {
        logSendMessage(message);
        super.send(message);
    }

    @Override
    public void sendDelayedMessage(final TypedMessage message, final int delaySeconds) throws MessageSendException {
        logSendMessage(message);
        super.sendDelayedMessage(message, delaySeconds);
    }

    private void logReceivedMessages(final List<TypedMessage> receivedMessages) {
        if (isLogReceivedMessages()) {
            for (final TypedMessage message : receivedMessages) {
                try {
                    LOGGER.debug("MSG-RECV [{}] [{}]", message.getType(), message.getPayload());
                } catch (final MessageParseException e) {
                    LOGGER.debug("MSG-RECV [{}]", e.getMessage());
                }
            }
        }
    }

    private void logSendMessage(final TypedMessage message) {
        if (message != null && isLogReceivedMessages()) {
            LOGGER.debug("MSG-SEND [{}] [{}]", message.getType(), message.getPayload());
        }
    }

}
