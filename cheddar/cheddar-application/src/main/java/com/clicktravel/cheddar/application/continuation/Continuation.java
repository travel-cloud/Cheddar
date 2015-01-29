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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Container for a single {@link MethodResult} that is passed from one thread to another. This is used as a rendezvous
 * to pass a result (either an object or thrown exception) from a {@link ContinueResult} method to a blocked
 * {@link DeferResult} method.
 */
class Continuation {

    private static final long TIMEOUT_SECONDS = 120;

    private final CountDownLatch countdownLatch = new CountDownLatch(1);
    private MethodResult methodResult;

    void setMethodResult(final MethodResult methodResult) {
        this.methodResult = methodResult;
    }

    MethodResult pollForMethodResult() {
        try {
            final boolean resultOffered = countdownLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!resultOffered) {
                throw new IllegalStateException("Timed out waiting for continuation method result");
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return methodResult;
    }

    void offerMethodResult() {
        countdownLatch.countDown();
    }

}
