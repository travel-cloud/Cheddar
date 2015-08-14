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
package com.clicktravel.cheddar.application.pending.result;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Container for a single {@link Result} that is passed between threads. This is used as a rendezvous between a thread
 * which offers a result and another thread which polls for the result, blocking until it is offered. Instances of this
 * class are single use only, just one result may be passed.
 */
class PendingResult {

    private static final long TIMEOUT_SECONDS = 30;
    private final CountDownLatch countdownLatch = new CountDownLatch(1);
    private Result result;

    /**
     * Offers a {@link Result}, returning immediately.
     * @param result {@link Result} to offer
     */
    public void offerResult(final Result result) {
        this.result = result;
        countdownLatch.countDown();
    }

    /**
     * Polls for a {@link Result}, blocking until it is offered by some other thread. If the result has already been
     * offered, the result is returned immediately.
     * @return {@link Result} obtained, potentially after blocking
     * @throws InterruptedException, PendingResultTimeoutException
     */
    public Result pollResult() throws InterruptedException, PendingResultTimeoutException {
        if (!countdownLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            throw new PendingResultTimeoutException();
        }
        return result;
    }
}
