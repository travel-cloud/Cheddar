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
package com.clicktravel.cheddar.server.application.status;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

/**
 * Monitors the processing of REST requests; determines if any REST requests are currently in progress.
 */
@Component
public class RestAdapterStatusHolder {

    private final AtomicInteger numRestRequestsInProgress = new AtomicInteger();

    public void requestProcessingStarted() {
        numRestRequestsInProgress.incrementAndGet();
    }

    public void requestProcessingFinished() {
        final int count = numRestRequestsInProgress.decrementAndGet();
        if (count < 0) {
            throw new IllegalStateException("Error counting number of REST requests in progress");
        }
    }

    public int restRequestsInProgress() {
        return numRestRequestsInProgress.get();
    }
}
