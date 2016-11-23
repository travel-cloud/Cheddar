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

import com.amazonaws.services.sqs.model.Message;
import com.clicktravel.cheddar.infrastructure.messaging.BasicMessage;
import com.clicktravel.cheddar.infrastructure.messaging.SimpleBasicMessage;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageReceiveException;
import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageSendException;

public class SqsBasicMessageQueue extends SqsMessageQueue<BasicMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqsBasicMessageQueue.class);

    public SqsBasicMessageQueue(final SqsQueueResource sqsQueueResource) {
        super(sqsQueueResource);
    }

    @Override
    protected String toSqsMessageBody(final BasicMessage basicMessage) {
        return basicMessage.getBody();
    }

    @Override
    protected BasicMessage toMessage(final Message sqsMessage) {
        return new SimpleBasicMessage(sqsMessage.getBody(), sqsMessage.getMessageId(), sqsMessage.getReceiptHandle());
    }

    @Override
    public List<BasicMessage> receive() throws MessageReceiveException {
        final List<BasicMessage> receivedMessages = super.receive();
        logReceivedMessages(receivedMessages);
        return receivedMessages;
    }

    @Override
    public List<BasicMessage> receive(final int waitTimeSeconds, final int maxMessages) throws MessageReceiveException {
        final List<BasicMessage> receivedMessages = super.receive(waitTimeSeconds, maxMessages);
        logReceivedMessages(receivedMessages);
        return receivedMessages;
    }

    @Override
    public void send(final BasicMessage message) throws MessageSendException {
        logSendMessage(message);
        super.send(message);
    }

    @Override
    public void sendDelayedMessage(final BasicMessage message, final int delaySeconds) throws MessageSendException {
        logSendMessage(message);
        super.sendDelayedMessage(message, delaySeconds);
    }

    private void logReceivedMessages(final List<BasicMessage> receivedMessages) {
        for (final BasicMessage message : receivedMessages) {
            LOGGER.debug("MSG-RECV [{}] [{}]", message.getClass().getSimpleName(), message.getBody());
        }
    }

    private void logSendMessage(final BasicMessage message) {
        if (message != null) {
            LOGGER.debug("MSG-SEND [{}] [{}]", message.getClass().getSimpleName(), message.getBody());
        }
    }

}
