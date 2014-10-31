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
package com.clicktravel.cheddar.application.continuation;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class ContinuationHandlerTest {

    @Test(timeout = 1000)
    public void shouldReturnMethodResult_whenWaitAndPut() throws Exception {
        // Given
        final Object simpleResult = new Object();
        final ContinuationHandler continuationHandler = new ContinuationHandler();
        continuationHandler.createContinuation();
        final String continuationId = continuationHandler.getContinuationId();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                continuationHandler.setSimpleMethodResult(continuationId, simpleResult);
                continuationHandler.offerMethodResult();
            }
        };
        Executors.newSingleThreadScheduledExecutor().schedule(runnable, 200, TimeUnit.MILLISECONDS);

        // When
        final Object actualResult = continuationHandler.pollForMethodReturnValue();

        // Then
        assertEquals(simpleResult, actualResult);
    }

    @Test(timeout = 1000)
    public void shouldReturnException_whenWaitAndPut() throws Exception {
        // Given
        final Exception exceptionResult = new Exception();
        final ContinuationHandler continuationHandler = new ContinuationHandler();
        continuationHandler.createContinuation();
        final String continuationId = continuationHandler.getContinuationId();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                continuationHandler.setExceptionMethodResult(continuationId, exceptionResult);
                continuationHandler.offerMethodResult();
            }
        };
        Executors.newSingleThreadScheduledExecutor().schedule(runnable, 200, TimeUnit.MILLISECONDS);

        // When
        Exception thrownException = null;
        try {
            continuationHandler.pollForMethodReturnValue();
        } catch (final Exception e) {
            thrownException = e;
        }

        // Then
        assertEquals(exceptionResult, thrownException);
    }
}
