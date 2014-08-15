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
package com.clicktravel.cheddar.server.http.filter.flow.control;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;

import com.clicktravel.cheddar.server.flow.control.RateLimiterConfiguration;
import com.clicktravel.common.concurrent.RateLimiter;
import com.clicktravel.common.random.Randoms;

public class RateLimitedRequestFilterTest {

    private RateLimitedRequestFilter rateLimitedRequestFilter;
    private RateLimiter mockRateLimiter;
    private ContainerRequestContext mockContainerRequestContext;

    @Before
    public void setUp() {
        mockContainerRequestContext = mock(ContainerRequestContext.class);
        final RateLimiterConfiguration mockRateLimiterConfiguration = mock(RateLimiterConfiguration.class);
        mockRateLimiter = mock(RateLimiter.class);
        when(mockRateLimiterConfiguration.restRequestRateLimiter()).thenReturn(mockRateLimiter);
        rateLimitedRequestFilter = new RateLimitedRequestFilter(mockRateLimiterConfiguration);
    }

    @Test
    public void shouldRateLimit_NonStatusResourceRequest() throws Exception {
        // Given
        setUpRequestPath("http://www.example.com/request");

        // When
        rateLimitedRequestFilter.filter(mockContainerRequestContext);

        // Then
        verify(mockRateLimiter).takeToken();
    }

    @Test
    public void shouldNotRateLimit_StatusResourceRequest() throws Exception {
        // Given
        setUpRequestPath("http://www.example.com/status");

        // When
        rateLimitedRequestFilter.filter(mockContainerRequestContext);

        // Then
        verifyZeroInteractions(mockRateLimiter);
    }

    @Test
    public void shouldNotRateLimit_StatusSubresourceRequest() throws Exception {
        // Given
        setUpRequestPath("http://www.example.com/status/" + Randoms.randomString());

        // When
        rateLimitedRequestFilter.filter(mockContainerRequestContext);

        // Then
        verifyZeroInteractions(mockRateLimiter);
    }

    private void setUpRequestPath(final String path) throws Exception {
        final UriInfo mockUriInfo = mock(UriInfo.class);
        when(mockContainerRequestContext.getUriInfo()).thenReturn(mockUriInfo);
        final URI uri = new URI(path);
        when(mockUriInfo.getRequestUri()).thenReturn(uri);
    }
}
