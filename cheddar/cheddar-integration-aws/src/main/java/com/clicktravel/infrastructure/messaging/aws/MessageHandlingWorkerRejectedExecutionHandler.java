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

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageHandlingWorkerRejectedExecutionHandler implements RejectedExecutionHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final SqsMessageProcessor sqsMessageProcessor;

    public MessageHandlingWorkerRejectedExecutionHandler(final SqsMessageProcessor sqsMessageProcessor) {
        if (sqsMessageProcessor == null) {
            throw new IllegalArgumentException("SqsMessageProcessor must not be null");
        }
        this.sqsMessageProcessor = sqsMessageProcessor;
    }

    @Override
    public void rejectedExecution(final Runnable command, final ThreadPoolExecutor executor) {
        if (sqsMessageProcessor.isProcessing()) {
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                logger.warn(e.getMessage(), e);
            }
            executor.execute(command);
        }
    }

}
