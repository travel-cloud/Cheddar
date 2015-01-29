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

import com.clicktravel.cheddar.infrastructure.messaging.Message;
import com.clicktravel.cheddar.infrastructure.messaging.MessagePublisher;
import com.clicktravel.cheddar.infrastructure.messaging.MessageSender;

public class MessageAction {

    private final Message message;
    private final int delaySeconds;

    public MessageAction(final Message message, final int delaySeconds) {
        this.message = message;
        this.delaySeconds = delaySeconds;
    }

    public Message message() {
        return message;
    }

    public int delay() {
        return delaySeconds;
    }

    public void apply(final MessageSender messageSender) {
        if (delay() > 0) {
            messageSender.sendDelayedMessage(message, delaySeconds);
        } else {
            messageSender.sendMessage(message);
        }
    }

    public void apply(final MessagePublisher messagePublisher) {
        messagePublisher.publishMessage(message);
    }

}
