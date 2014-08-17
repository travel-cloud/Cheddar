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

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.clicktravel.cheddar.infrastructure.messaging.Message;
import com.clicktravel.cheddar.infrastructure.messaging.MessageHandler;

public class MessageHandlingWorker implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Message message;
    private final MessageHandler messageHandler;
    private final AmazonSQS amazonSqsClient;
    private final String queueUrl;
    private final String messageReceiptHandle;

    public MessageHandlingWorker(final Message message, final MessageHandler messageHandler,
            final AmazonSQS amazonSqsClient, final String queueUrl, final String messageReceiptHandle) {
        this.message = message;
        this.messageHandler = messageHandler;
        this.amazonSqsClient = amazonSqsClient;
        this.queueUrl = queueUrl;
        this.messageReceiptHandle = messageReceiptHandle;
    }

    @Override
    public void run() {
        if (messageHandler != null) {
            try {
                messageHandler.handle(message);
            } catch (final Exception e) {
                logger.warn(e.getMessage(), e);
            }
        } else {
            logger.debug("Unsupported message type: " + message.getType());
        }
        // TODO Batch the delete message requests
        amazonSqsClient.deleteMessage(new DeleteMessageRequest().withQueueUrl(queueUrl).withReceiptHandle(
                messageReceiptHandle));
    }

    public Message message() {
        return message;
    }

    public MessageHandler messageHandler() {
        return messageHandler;
    }

}
