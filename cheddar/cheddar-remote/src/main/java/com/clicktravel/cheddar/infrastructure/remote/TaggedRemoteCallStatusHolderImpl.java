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
package com.clicktravel.cheddar.infrastructure.remote;

import org.joda.time.DateTimeUtils;
import org.springframework.stereotype.Component;

import com.clicktravel.cheddar.remote.TaggedRemoteCallStatusHolder;

/**
 * Monitors execution of tagged remote calls; determines if a tagged remote call was executed in the last 30 seconds.
 * This is used for monitoring execution of remote calls due to deferrable event processing.
 */
@Component
public class TaggedRemoteCallStatusHolderImpl implements TaggedRemoteCallStatusHolder {

    private static final long RECENT_PROCESSED_CALL_THRESHOLD_MILLIS = 30 * 1000; // 30 seconds
    private int callsInProgress;
    private long lastCallCompletedTime;
    private boolean processedAnyCalls;

    @Override
    public synchronized void taggedRemoteCallStarted() {
        processedAnyCalls = true;
        callsInProgress++;
    }

    @Override
    public synchronized void taggedRemoteCallCompleted() {
        lastCallCompletedTime = DateTimeUtils.currentTimeMillis();
        if (--callsInProgress < 0) {
            throw new IllegalStateException("Error counting number of tagged remote calls in progress");
        }
    }

    @Override
    public synchronized boolean processedRecentTaggedRemoteCall() {
        if (!processedAnyCalls) {
            return false;
        }

        if (callsInProgress > 0) {
            return true;
        }

        final long elapsedSinceLastCallCompleted = DateTimeUtils.currentTimeMillis() - lastCallCompletedTime;
        return elapsedSinceLastCallCompleted < RECENT_PROCESSED_CALL_THRESHOLD_MILLIS;
    }
}
