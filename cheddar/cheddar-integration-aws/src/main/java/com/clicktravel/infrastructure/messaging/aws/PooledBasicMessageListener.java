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

import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

import com.clicktravel.cheddar.infrastructure.messaging.BasicMessage;
import com.clicktravel.cheddar.infrastructure.messaging.MessageHandler;
import com.clicktravel.cheddar.infrastructure.messaging.MessageQueue;
import com.clicktravel.common.concurrent.RateLimiter;

public class PooledBasicMessageListener extends PooledMessageListener<BasicMessage> {

    private final MessageHandler<BasicMessage> messageHandler;

    public PooledBasicMessageListener(final MessageQueue<BasicMessage> basicMessageQueue,
            final RateLimiter rateLimiter, final ThreadPoolExecutor threadPoolExecutor, final Semaphore semaphore,
            final MessageHandler<BasicMessage> messageHandler, final int maxReceivedMessages) {
        super(basicMessageQueue, rateLimiter, threadPoolExecutor, semaphore, maxReceivedMessages);
        this.messageHandler = messageHandler;
    }

    @Override
    protected MessageHandler<BasicMessage> getHandlerForMessage(final BasicMessage message) {
        return messageHandler;
    }

}
