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

import static com.clicktravel.common.random.Randoms.randomInt;
import static com.clicktravel.common.random.Randoms.randomLong;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;

import com.clicktravel.cheddar.server.application.configuration.ApplicationConfiguration;
import com.clicktravel.common.concurrent.RateLimiter;

public class StatusResourceTest {

    private ApplicationConfiguration mockApplicationConfiguration;
    private RateLimiter mockRestRequestRateLimiter;
    private RateLimiter mockDomainEventHandlerRateLimiter;

    @Before
    public void setUp() {
        mockApplicationConfiguration = mock(ApplicationConfiguration.class);
        mockRestRequestRateLimiter = mock(RateLimiter.class);
        mockDomainEventHandlerRateLimiter = mock(RateLimiter.class);
    }

    @Test
    public void shouldReturnResource_onGetStatus() throws Exception {
        // Given
        final String applicationName = randomString(10);
        final String applicationVersion = randomString(10);
        final String frameworkVersion = randomString(10);
        when(mockApplicationConfiguration.name()).thenReturn(applicationName);
        when(mockApplicationConfiguration.version()).thenReturn(applicationVersion);
        when(mockApplicationConfiguration.frameworkVersion()).thenReturn(frameworkVersion);
        final RateLimiter mockRestRequestRateLimiter = mock(RateLimiter.class);
        final RateLimiter mockDomainEventHandlerRateLimiter = mock(RateLimiter.class);
        when(mockRestRequestRateLimiter.getBucketCapacity()).thenReturn(randomInt(1000) + 1);
        when(mockRestRequestRateLimiter.getTokenReplacementDelayMillis()).thenReturn(Math.abs(randomLong()));
        when(mockDomainEventHandlerRateLimiter.getBucketCapacity()).thenReturn(randomInt(1000) + 1);
        when(mockDomainEventHandlerRateLimiter.getTokenReplacementDelayMillis()).thenReturn(Math.abs(randomLong()));
        final StatusResource statusResource = new StatusResource(mockApplicationConfiguration,
                mockRestRequestRateLimiter, mockDomainEventHandlerRateLimiter);

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
        assertEquals(mockRestRequestRateLimiter.getBucketCapacity(),
                statusResult.getMaximumWorkRates().getRestRequest().getBucketCapacity());
        assertEquals(mockRestRequestRateLimiter.getTokenReplacementDelayMillis(),
                statusResult.getMaximumWorkRates().getRestRequest().getTokenReplacementDelay());
        assertEquals(mockDomainEventHandlerRateLimiter.getBucketCapacity(),
                statusResult.getMaximumWorkRates().getDomainEventHandler().getBucketCapacity());
        assertEquals(mockDomainEventHandlerRateLimiter.getTokenReplacementDelayMillis(),
                statusResult.getMaximumWorkRates().getDomainEventHandler().getTokenReplacementDelay());
    }

    public void shouldReturnReady() throws Exception {
        // Given
        final StatusResource statusResource = new StatusResource(mockApplicationConfiguration,
                mockRestRequestRateLimiter, mockDomainEventHandlerRateLimiter);

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
