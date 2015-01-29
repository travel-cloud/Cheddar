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

import static com.clicktravel.common.random.Randoms.randomInt;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import com.clicktravel.cheddar.event.Event;
import com.clicktravel.cheddar.system.event.MaximumWorkRateSetEvent;
import com.clicktravel.common.concurrent.RateLimiter;

public class MaximumWorkRateSetEventHandlerTest {

    private final RateLimiter mockRestRequestRateLimiter = mock(RateLimiter.class);
    private final RateLimiter mockHighPriorityDomainEventHandlerRateLimiter = mock(RateLimiter.class);
    private final RateLimiter mockLowPriorityDomainEventHandlerRateLimiter = mock(RateLimiter.class);

    @Test
    public void shouldCreateMaximumWorkRateSetEventHandler_withApplicationNameAndApplicationVersionAndRateLimiterConfiguration()
            throws Exception {
        // Given
        final String applicationName = randomString(10);
        final String applicationVersion = randomString(10);

        // When
        final MaximumWorkRateSetEventHandler maximumWorkRateSetEventHandler = new MaximumWorkRateSetEventHandler(
                applicationName, applicationVersion, mockRestRequestRateLimiter,
                mockHighPriorityDomainEventHandlerRateLimiter, mockLowPriorityDomainEventHandlerRateLimiter);

        // Then
        assertNotNull(maximumWorkRateSetEventHandler);
    }

    @Test
    public void shouldGetEventClass() throws Exception {
        // Given
        final Class<? extends Event> maximumWorkRateSetEventClass = MaximumWorkRateSetEvent.class;

        final String applicationName = randomString(10);
        final String applicationVersion = randomString(10);

        final MaximumWorkRateSetEventHandler maximumWorkRateSetEventHandler = new MaximumWorkRateSetEventHandler(
                applicationName, applicationVersion, mockRestRequestRateLimiter,
                mockHighPriorityDomainEventHandlerRateLimiter, mockLowPriorityDomainEventHandlerRateLimiter);

        // When
        maximumWorkRateSetEventHandler.getEventClass();

        // Then
        assertEquals(maximumWorkRateSetEventClass, maximumWorkRateSetEventHandler.getEventClass());
    }

    @Test
    public void shouldHandleSystemEvent_withMaximumWorkRateSetEvent() throws Exception {
        // Given
        final MaximumWorkRateSetEvent event = randomMaximumWorkRateSetEvent();

        final String applicationName = randomString(10);
        final String applicationVersion = randomString(10);

        final MaximumWorkRateSetEventHandler maximumWorkRateSetEventHandler = new MaximumWorkRateSetEventHandler(
                applicationName, applicationVersion, mockRestRequestRateLimiter,
                mockHighPriorityDomainEventHandlerRateLimiter, mockLowPriorityDomainEventHandlerRateLimiter);

        // When
        maximumWorkRateSetEventHandler.handle(event);

        // Then
        verify(mockRestRequestRateLimiter).setParameters(event.getRestRequestBucketCapacity(),
                event.getRestRequestTokenReplacementDelay());
        verify(mockHighPriorityDomainEventHandlerRateLimiter).setParameters(
                event.getHighPriorityEventHandlerBucketCapacity(),
                event.getHighPriorityEventHandlerTokenReplacementDelay());
        verify(mockLowPriorityDomainEventHandlerRateLimiter).setParameters(
                event.getLowPriorityEventHandlerBucketCapacity(),
                event.getLowPriorityEventHandlerTokenReplacementDelay());
    }

    private MaximumWorkRateSetEvent randomMaximumWorkRateSetEvent() {
        final MaximumWorkRateSetEvent event = new MaximumWorkRateSetEvent();
        event.setRestRequestBucketCapacity(randomInt(Integer.MAX_VALUE - 1) + 1);
        event.setRestRequestTokenReplacementDelay(randomInt(Integer.MAX_VALUE));
        event.setHighPriorityEventHandlerBucketCapacity(randomInt(Integer.MAX_VALUE - 1) + 1);
        event.setHighPriorityEventHandlerTokenReplacementDelay(randomInt(Integer.MAX_VALUE));
        event.setLowPriorityEventHandlerBucketCapacity(randomInt(Integer.MAX_VALUE - 1) + 1);
        event.setLowPriorityEventHandlerTokenReplacementDelay(randomInt(Integer.MAX_VALUE));
        return event;
    }

}
