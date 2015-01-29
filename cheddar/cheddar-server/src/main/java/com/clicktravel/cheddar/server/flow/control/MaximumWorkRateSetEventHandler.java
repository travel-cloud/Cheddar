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
package com.clicktravel.cheddar.server.flow.control;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.clicktravel.cheddar.system.event.MaximumWorkRateSetEvent;
import com.clicktravel.cheddar.system.event.SystemEvent;
import com.clicktravel.cheddar.system.event.handler.AbstractSystemEventHandler;
import com.clicktravel.common.concurrent.RateLimiter;

@Component
public class MaximumWorkRateSetEventHandler extends AbstractSystemEventHandler {

    private final RateLimiter restRequestRateLimiter;
    private final RateLimiter highPriorityDomainEventHandlerRateLimiter;
    private final RateLimiter lowPriorityDomainEventHandlerRateLimiter;

    @Autowired
    public MaximumWorkRateSetEventHandler(@Value("${server.application.name}") final String applicationName,
            @Value("${server.application.version}") final String applicationVersion,
            final RateLimiter restRequestRateLimiter, final RateLimiter highPriorityDomainEventHandlerRateLimiter,
            final RateLimiter lowPriorityDomainEventHandlerRateLimiter) {
        super(applicationName, applicationVersion);
        this.restRequestRateLimiter = restRequestRateLimiter;
        this.highPriorityDomainEventHandlerRateLimiter = highPriorityDomainEventHandlerRateLimiter;
        this.lowPriorityDomainEventHandlerRateLimiter = lowPriorityDomainEventHandlerRateLimiter;
    }

    @Override
    protected void handleSystemEvent(final SystemEvent event) {
        logger.info("Setting new maximum work rate: " + event);
        final MaximumWorkRateSetEvent systemEvent = (MaximumWorkRateSetEvent) event;
        restRequestRateLimiter.setParameters(systemEvent.getRestRequestBucketCapacity(),
                systemEvent.getRestRequestTokenReplacementDelay());
        highPriorityDomainEventHandlerRateLimiter.setParameters(
                systemEvent.getHighPriorityEventHandlerBucketCapacity(),
                systemEvent.getHighPriorityEventHandlerTokenReplacementDelay());
        lowPriorityDomainEventHandlerRateLimiter.setParameters(systemEvent.getLowPriorityEventHandlerBucketCapacity(),
                systemEvent.getLowPriorityEventHandlerTokenReplacementDelay());
    }

    @Override
    public Class<? extends SystemEvent> getEventClass() {
        return MaximumWorkRateSetEvent.class;
    }

}
