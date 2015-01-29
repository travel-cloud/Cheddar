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
package com.clicktravel.cheddar.server.rest.resource.status;

import static com.clicktravel.common.random.Randoms.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;

import com.clicktravel.cheddar.server.application.configuration.ApplicationConfiguration;
import com.clicktravel.cheddar.server.application.lifecycle.LifecycleStatus;
import com.clicktravel.cheddar.server.application.lifecycle.LifecycleStatusHolder;
import com.clicktravel.cheddar.server.application.status.DeferrableProcessingStatusHolder;
import com.clicktravel.cheddar.server.application.status.RestAdapterStatusHolder;
import com.clicktravel.cheddar.server.flow.control.RateLimiterConfiguration;
import com.clicktravel.common.concurrent.RateLimiter;

public class StatusResourceTest {

    private ApplicationConfiguration mockApplicationConfiguration;
    private LifecycleStatusHolder mockLifecycleStatusHolder;
    private RestAdapterStatusHolder mockRestAdapterStatusHolder;
    private DeferrableProcessingStatusHolder mockDeferrableProcessingStatusHolder;
    private RateLimiterConfiguration mockRateLimiterConfiguration;

    @Before
    public void setUp() {
        mockApplicationConfiguration = mock(ApplicationConfiguration.class);
        mockLifecycleStatusHolder = mock(LifecycleStatusHolder.class);
        mockRestAdapterStatusHolder = mock(RestAdapterStatusHolder.class);
        mockDeferrableProcessingStatusHolder = mock(DeferrableProcessingStatusHolder.class);
        mockRateLimiterConfiguration = mock(RateLimiterConfiguration.class);
    }

    @Test
    public void shouldReturnResource_onGetStatus() throws Exception {
        // Given
        final LifecycleStatus expectedLifecycleStatus = randomEnum(LifecycleStatus.class);
        final boolean expectedProcessingRestRequest = randomBoolean();
        final boolean expectedDeferrableProcessing = randomBoolean();

        when(mockLifecycleStatusHolder.getLifecycleStatus()).thenReturn(expectedLifecycleStatus);
        when(mockRestAdapterStatusHolder.restRequestsInProgress()).thenReturn(expectedProcessingRestRequest ? 2 : 1);
        when(mockDeferrableProcessingStatusHolder.isDeferrableProcessing()).thenReturn(expectedDeferrableProcessing);
        final String applicationName = randomString(10);
        final String applicationVersion = randomString(10);
        final String frameworkVersion = randomString(10);
        when(mockApplicationConfiguration.name()).thenReturn(applicationName);
        when(mockApplicationConfiguration.version()).thenReturn(applicationVersion);
        when(mockApplicationConfiguration.frameworkVersion()).thenReturn(frameworkVersion);
        final RateLimiter mockRestRequestRateLimiter = mock(RateLimiter.class);
        final RateLimiter mockHighPriorityEventHandlerRateLimiter = mock(RateLimiter.class);
        final RateLimiter mockLowPriorityEventHandlerRateLimiter = mock(RateLimiter.class);
        when(mockRestRequestRateLimiter.getBucketCapacity()).thenReturn(randomInt(1000) + 1);
        when(mockRestRequestRateLimiter.getTokenReplacementDelayMillis()).thenReturn(Math.abs(randomLong()));
        when(mockHighPriorityEventHandlerRateLimiter.getBucketCapacity()).thenReturn(randomInt(1000) + 1);
        when(mockHighPriorityEventHandlerRateLimiter.getTokenReplacementDelayMillis()).thenReturn(
                Math.abs(randomLong()));
        when(mockLowPriorityEventHandlerRateLimiter.getBucketCapacity()).thenReturn(randomInt(1000) + 1);
        when(mockLowPriorityEventHandlerRateLimiter.getTokenReplacementDelayMillis())
                .thenReturn(Math.abs(randomLong()));
        when(mockRateLimiterConfiguration.restRequestRateLimiter()).thenReturn(mockRestRequestRateLimiter);
        when(mockRateLimiterConfiguration.highPriorityDomainEventHandlerRateLimiter()).thenReturn(
                mockHighPriorityEventHandlerRateLimiter);
        when(mockRateLimiterConfiguration.lowPriorityDomainEventHandlerRateLimiter()).thenReturn(
                mockLowPriorityEventHandlerRateLimiter);
        final StatusResource statusResource = new StatusResource(mockApplicationConfiguration,
                mockLifecycleStatusHolder, mockRestAdapterStatusHolder, mockDeferrableProcessingStatusHolder,
                mockRateLimiterConfiguration);

        // When
        final Response status = statusResource.getStatus();

        // Then
        assertNotNull(status);
        final Object entity = status.getEntity();
        assertTrue(entity instanceof StatusResult);
        final StatusResult statusResult = (StatusResult) entity;
        assertEquals(applicationName, statusResult.getName());
        assertEquals(applicationVersion, statusResult.getVersion());
        assertEquals(frameworkVersion, statusResult.getFrameworkVersion());
        assertEquals(expectedLifecycleStatus.name(), statusResult.getStatus());
        assertEquals(expectedProcessingRestRequest, statusResult.isProcessingRestRequest());
        assertEquals(expectedDeferrableProcessing, statusResult.isDeferrableProcessing());
        assertEquals(expectedDeferrableProcessing, statusResult.isProcessedRecentDeferrableEvent());
        assertEquals(mockRestRequestRateLimiter.getBucketCapacity(), statusResult.getMaximumWorkRates()
                .getRestRequest().getBucketCapacity());
        assertEquals(mockRestRequestRateLimiter.getTokenReplacementDelayMillis(), statusResult.getMaximumWorkRates()
                .getRestRequest().getTokenReplacementDelay());
        assertEquals(mockHighPriorityEventHandlerRateLimiter.getBucketCapacity(), statusResult.getMaximumWorkRates()
                .getHighPriorityDomainEventHandler().getBucketCapacity());
        assertEquals(mockHighPriorityEventHandlerRateLimiter.getTokenReplacementDelayMillis(), statusResult
                .getMaximumWorkRates().getHighPriorityDomainEventHandler().getTokenReplacementDelay());
        assertEquals(mockLowPriorityEventHandlerRateLimiter.getBucketCapacity(), statusResult.getMaximumWorkRates()
                .getLowPriorityDomainEventHandler().getBucketCapacity());
        assertEquals(mockLowPriorityEventHandlerRateLimiter.getTokenReplacementDelayMillis(), statusResult
                .getMaximumWorkRates().getLowPriorityDomainEventHandler().getTokenReplacementDelay());
    }

    @Test
    public void shouldReturnServiceUnavailable_onNotAcceptingRestRequests() throws Exception {
        // Given
        when(mockRestAdapterStatusHolder.isAcceptingRequests()).thenReturn(false);
        final StatusResource statusResource = new StatusResource(mockApplicationConfiguration,
                mockLifecycleStatusHolder, mockRestAdapterStatusHolder, mockDeferrableProcessingStatusHolder,
                mockRateLimiterConfiguration);

        // When
        final Response response = statusResource.getHealthCheck();

        // Then
        assertNotNull(response);
        assertEquals(Status.SERVICE_UNAVAILABLE.getStatusCode(), response.getStatus());
    }

    @Test
    public void shouldReturnReady_onAcceptingRestRequests() throws Exception {
        // Given
        when(mockRestAdapterStatusHolder.isAcceptingRequests()).thenReturn(true);
        final StatusResource statusResource = new StatusResource(mockApplicationConfiguration,
                mockLifecycleStatusHolder, mockRestAdapterStatusHolder, mockDeferrableProcessingStatusHolder,
                mockRateLimiterConfiguration);

        // When
        final Response response = statusResource.getHealthCheck();

        // Then
        assertNotNull(response);
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        final Object entity = response.getEntity();
        assertTrue(entity instanceof String);
        assertEquals("Ready", entity);
    }
}
