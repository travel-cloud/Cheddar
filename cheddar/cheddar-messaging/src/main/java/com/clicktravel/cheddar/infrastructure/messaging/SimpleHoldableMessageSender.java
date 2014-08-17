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
package com.clicktravel.cheddar.infrastructure.messaging;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageSendException;

/**
 * Decorates a {@link MessageSender} to hold messages to be sent later. Uses an in-memory queue for holding pending send
 * message requests. This class is threadsafe.
 */
public class SimpleHoldableMessageSender implements HoldableMessageSender {

    private final MessageSender messageSender;
    private final Queue<SendMessageRequest> sendMessageRequests = new ConcurrentLinkedQueue<>();
    private volatile boolean holdMessages;

    public SimpleHoldableMessageSender(final MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @Override
    public void sendMessage(final Message message) throws MessageSendException {
        sendDelayedMessage(message, 0);
    }

    @Override
    public void sendDelayedMessage(final Message message, final int delaySeconds) throws MessageSendException {
        sendMessageRequests.offer(new SendMessageRequest(message, delaySeconds));
        if (!holdMessages) {
            processSendMessageRequests();
        }
    }

    @Override
    public void forwardMessages() {
        holdMessages = false;
        processSendMessageRequests();
    }

    @Override
    public void holdMessages() {
        holdMessages = true;
    }

    private void processSendMessageRequests() {
        SendMessageRequest sendMessageRequest;
        while ((sendMessageRequest = sendMessageRequests.poll()) != null) {
            sendMessageRequest.execute();
        }
    }

    private class SendMessageRequest {
        private final Message message;
        private final int delaySeconds;

        SendMessageRequest(final Message message, final int delaySeconds) {
            this.message = message;
            this.delaySeconds = delaySeconds;
        }

        void execute() {
            messageSender.sendDelayedMessage(message, delaySeconds);
        }
    }
}
