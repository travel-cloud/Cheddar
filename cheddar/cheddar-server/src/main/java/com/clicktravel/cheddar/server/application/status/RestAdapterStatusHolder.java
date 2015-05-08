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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.clicktravel.cheddar.server.application.lifecycle.LifecycleStatus;
import com.clicktravel.cheddar.server.application.lifecycle.LifecycleStatusHolder;
import com.clicktravel.cheddar.server.http.filter.flow.control.FlowControlledRequestFilter;

/**
 * Monitors the processing of REST requests; determines if any REST requests are currently in progress. Also controls if
 * REST requests for application services should be accepted by the server.
 */
@Component
public class RestAdapterStatusHolder {

    private static final Set<LifecycleStatus> LIFECYCLE_STATES_FOR_ACCEPTING_REQUESTS = new HashSet<>(Arrays.asList(
            LifecycleStatus.PAUSED, LifecycleStatus.RUNNING, LifecycleStatus.HALTING_LOW_PRIORITY_EVENTS));

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AtomicInteger numRestRequestsInProgress = new AtomicInteger();
    private final LifecycleStatusHolder lifecycleStatusHolder;
    private boolean loggedCountError;

    @Autowired
    public RestAdapterStatusHolder(final LifecycleStatusHolder lifecycleStatusHolder) {
        this.lifecycleStatusHolder = lifecycleStatusHolder;
    }

    public void requestProcessingStarted() {
        numRestRequestsInProgress.incrementAndGet();
    }

    public void requestProcessingFinished() {
        final int count = numRestRequestsInProgress.decrementAndGet();
        if (count < 0 && !loggedCountError) {
            loggedCountError = true;
            logger.warn("Error counting number of REST requests in progress");
        }
    }

    public int restRequestsInProgress() {
        return numRestRequestsInProgress.get();
    }

    /**
     * @return {@code true} if the server should indicate to its environment it is ready to accept REST requests. In an
     *         AWS environment, this indication is a positive response to an ELB health check. Note that readiness to
     *         accept requests does not imply received requests will be processed; in {@link LifecycleStatus#PAUSED}
     *         state the worker threads handling the received requests are blocked by
     *         {@link FlowControlledRequestFilter}.
     */
    public boolean isAcceptingRequests() {
        return LIFECYCLE_STATES_FOR_ACCEPTING_REQUESTS.contains(lifecycleStatusHolder.getLifecycleStatus());
    }
}
