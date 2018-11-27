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

import com.clicktravel.common.concurrent.RateLimiter;
import com.clicktravel.common.random.Randoms;

public class FlowControlledRequestFilterTest {

    private FlowControlledRequestFilter flowControlledRequestFilter;
    private RateLimiter mockRateLimiter;
    private ContainerRequestContext mockContainerRequestContext;

    @Before
    public void setUp() {
        mockContainerRequestContext = mock(ContainerRequestContext.class);
        mockRateLimiter = mock(RateLimiter.class);
        flowControlledRequestFilter = new FlowControlledRequestFilter(mockRateLimiter);
    }

    @Test
    public void shouldApplyFlowControl_onNonStatusResourceRequest() throws Exception {
        // Given
        setUpRequestPath("http://www.example.com/request");

        // When
        flowControlledRequestFilter.filter(mockContainerRequestContext);

        // Then
        verify(mockRateLimiter).takeToken();
    }

    @Test
    public void shouldNotApplyFlowControl_onStatusResourceRequest() throws Exception {
        // Given
        setUpRequestPath("http://www.example.com/status");

        // When
        flowControlledRequestFilter.filter(mockContainerRequestContext);

        // Then
        verifyZeroInteractions(mockRateLimiter);
    }

    @Test
    public void shouldNotApplyFlowControl_onStatusSubresourceRequest() throws Exception {
        // Given
        setUpRequestPath("http://www.example.com/status/" + Randoms.randomString());

        // When
        flowControlledRequestFilter.filter(mockContainerRequestContext);

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
