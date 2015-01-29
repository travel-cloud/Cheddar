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

public class ExchangeQueueBinding {

    private final String exchangeName;
    private final String queueName;

    public ExchangeQueueBinding(final String exchangeName, final String queueName) {
        if (exchangeName == null || queueName == null) {
            throw new IllegalStateException("Exchange or queue name must not be null. Exchange: " + exchangeName
                    + ", queue: " + queueName);
        }
        this.exchangeName = exchangeName;
        this.queueName = queueName;
    }

    public String exchangeName() {
        return exchangeName;
    }

    public String queueName() {
        return queueName;
    }

}
