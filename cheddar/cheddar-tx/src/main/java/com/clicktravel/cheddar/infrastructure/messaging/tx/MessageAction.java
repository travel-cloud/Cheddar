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
package com.clicktravel.cheddar.infrastructure.messaging.tx;

import com.clicktravel.cheddar.infrastructure.messaging.MessagePublisher;
import com.clicktravel.cheddar.infrastructure.messaging.MessageSender;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;

public class MessageAction {

    private final TypedMessage typedMessage;
    private final int delaySeconds;

    public MessageAction(final TypedMessage typedMessage, final int delaySeconds) {
        this.typedMessage = typedMessage;
        this.delaySeconds = delaySeconds;
    }

    public TypedMessage message() {
        return typedMessage;
    }

    public int delay() {
        return delaySeconds;
    }

    public void apply(final MessageSender<TypedMessage> messageSender) {
        if (delay() > 0) {
            messageSender.sendDelayedMessage(typedMessage, delaySeconds);
        } else {
            messageSender.send(typedMessage);
        }
    }

    public void apply(final MessagePublisher<TypedMessage> messagePublisher) {
        messagePublisher.publish(typedMessage);
    }

}
