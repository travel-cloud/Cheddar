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

public class SimpleMessage extends AbstractMessage implements TypedMessage {

    private final String type;
    private final String payload;

    public SimpleMessage(final String type, final String payload, final String receiptHandle) {
        super(receiptHandle);
        this.type = type;
        this.payload = payload;
    }

    public SimpleMessage(final String type, final String payload) {
        this(type, payload, null);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "SimpleMessage; type:[" + type + "] payload:[" + payload + "]";
    }

}
