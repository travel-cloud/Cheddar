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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.ThreadPoolExecutor;

import org.joda.time.DateTime;
import org.junit.Test;

public class MessageHandlingWorkerRejectedExecutionHandlerTest {

    @Test
    public void shouldCreateMessageHandlingWorkerRejectedExecutionHandler_withSqsMessageProcessor() throws Exception {
        // Given
        final SqsMessageProcessor sqsMessageProcessor = mock(SqsMessageProcessor.class);

        // When
        final MessageHandlingWorkerRejectedExecutionHandler handler = new MessageHandlingWorkerRejectedExecutionHandler(
                sqsMessageProcessor);

        // Then
        assertNotNull(handler);
    }

    @Test
    public void shouldNotCreateMessageHandlingWorkerRejectedExecutionHandler_withNullSqsMessageProcessor()
            throws Exception {
        // Given
        final SqsMessageProcessor sqsMessageProcessor = null;

        // When
        IllegalArgumentException actualException = null;
        try {
            new MessageHandlingWorkerRejectedExecutionHandler(sqsMessageProcessor);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test(timeout = 2000)
    public void shouldPerformRejectedException_withCommandAndExecutor() throws Exception {
        // Given
        final Runnable command = mock(Runnable.class);
        final ThreadPoolExecutor executor = mock(ThreadPoolExecutor.class);
        final SqsMessageProcessor sqsMessageProcessor = mock(SqsMessageProcessor.class);
        when(sqsMessageProcessor.isProcessing()).thenReturn(true);
        final MessageHandlingWorkerRejectedExecutionHandler handler = new MessageHandlingWorkerRejectedExecutionHandler(
                sqsMessageProcessor);
        final long startTime = DateTime.now().getMillis();

        // When
        handler.rejectedExecution(command, executor);

        // Then
        final long duration = DateTime.now().getMillis() - startTime;
        verify(executor).execute(command);
        assertTrue("Duration must be more than 1000ms. Actual duration: " + duration, duration >= 1000);
    }

    @Test(timeout = 1000)
    public void shouldNotPerformRejectedException_withSqsMessagerProcessorNotProcessing() throws Exception {
        // Given
        final Runnable command = mock(Runnable.class);
        final ThreadPoolExecutor executor = mock(ThreadPoolExecutor.class);
        final SqsMessageProcessor sqsMessageProcessor = mock(SqsMessageProcessor.class);
        when(sqsMessageProcessor.isProcessing()).thenReturn(false);
        final MessageHandlingWorkerRejectedExecutionHandler handler = new MessageHandlingWorkerRejectedExecutionHandler(
                sqsMessageProcessor);
        final long startTime = DateTime.now().getMillis();

        // When
        handler.rejectedExecution(command, executor);

        // Then
        final long duration = DateTime.now().getMillis() - startTime;
        verifyZeroInteractions(executor);
        assertTrue("Duration must be less than 1000ms. Actual duration: " + duration, duration <= 1000);
    }

}
