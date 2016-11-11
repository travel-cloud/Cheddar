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

import com.clicktravel.cheddar.infrastructure.messaging.exception.MessageParseException;

public class InvalidTypedMessage extends InvalidMessage implements TypedMessage {

    public InvalidTypedMessage(final String messageId, final String receiptHandle,
            final MessageParseException messageParseException) {
        super(messageId, receiptHandle, messageParseException);
    }

    @Override
    public String getType() {
        throw getMessageParseException();
    }

    @Override
    public String getPayload() {
        throw getMessageParseException();
    }

}
