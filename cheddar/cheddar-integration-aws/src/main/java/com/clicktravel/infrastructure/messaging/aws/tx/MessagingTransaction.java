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
package com.clicktravel.infrastructure.messaging.aws.tx;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import com.clicktravel.cheddar.infrastructure.messaging.Message;
import com.clicktravel.cheddar.infrastructure.messaging.MessagePublisher;
import com.clicktravel.cheddar.infrastructure.messaging.MessageSender;
import com.clicktravel.cheddar.infrastructure.tx.Transaction;

public class MessagingTransaction implements Transaction {

    private final Queue<MessageAction> messageActions;

    private final String transactionId;

    public MessagingTransaction() {
        messageActions = new LinkedList<>();
        transactionId = UUID.randomUUID().toString();
    }

    @Override
    public String transactionId() {
        return transactionId;
    }

    public void applyActions(final MessagePublisher messagePublisher) {
        while (!messageActions.isEmpty()) {
            final MessageAction messageAction = messageActions.remove();
            messagePublisher.publishMessage(messageAction.message());
        }
    }

    public void applyActions(final MessageSender messageSender) {
        while (!messageActions.isEmpty()) {
            final MessageAction messageAction = messageActions.remove();
            messageAction.apply(messageSender);
        }
    }

    public void addMessage(final Message message) {
        messageActions.add(new MessageAction(message, 0));
    }

    public void addDelayedMessage(final Message message, final int delay) {
        messageActions.add(new MessageAction(message, delay));
    }

}
