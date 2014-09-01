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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class SqsMessageProcessorThreadFactory implements ThreadFactory {

    private final AtomicInteger threadSequenceNumber = new AtomicInteger();
    private final String queueName;

    public SqsMessageProcessorThreadFactory(final String queueName) {
        this.queueName = queueName;
    }

    @Override
    public Thread newThread(final Runnable r) {
        final int seq = threadSequenceNumber.incrementAndGet();
        return new Thread(r, "MessageProcessor:" + queueName + ":" + seq);
    }
}