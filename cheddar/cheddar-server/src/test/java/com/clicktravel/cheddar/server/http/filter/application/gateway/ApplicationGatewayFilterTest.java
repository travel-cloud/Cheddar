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
package com.clicktravel.cheddar.server.http.filter.application.gateway;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;

import com.clicktravel.common.random.Randoms;

public class ApplicationGatewayFilterTest {

    private static final String APPLICATION_GATEWAY_TOKEN_HEADER = "x-clicktravel-application-gateway-token";

    private ApplicationGatewayFilterConfiguration mockApplicationGatewayFilterConfiguration;
    private ApplicationGatewayFilter applicationGatewayFilter;
    private ContainerRequestContext mockContainerRequestContext;

    @Before
    public void setUp() {
        mockApplicationGatewayFilterConfiguration = mock(ApplicationGatewayFilterConfiguration.class);
        applicationGatewayFilter = new ApplicationGatewayFilter(mockApplicationGatewayFilterConfiguration);
        mockContainerRequestContext = mock(ContainerRequestContext.class);
    }

    @Test
    public void shouldAllowRequest_withGatewayTokenHeader() throws Exception {
        // Given
        final String applicationToken = Randoms.randomString();
        when(mockApplicationGatewayFilterConfiguration.applicationGatewayToken()).thenReturn(applicationToken);
        setUpRequestPath("http://www.example.com/request");
        when(mockContainerRequestContext.getHeaderString(APPLICATION_GATEWAY_TOKEN_HEADER))
                .thenReturn(applicationToken);

        // When
        applicationGatewayFilter.filter(mockContainerRequestContext);

        // Then
        verify(mockContainerRequestContext, never()).abortWith(any(Response.class));
    }

    @Test
    public void shouldAbortRequest_withoutGatewayTokenHeader() throws Exception {
        // Given
        final String applicationToken = Randoms.randomString();
        when(mockApplicationGatewayFilterConfiguration.applicationGatewayToken()).thenReturn(applicationToken);
        setUpRequestPath("http://www.example.com/request");

        // When
        applicationGatewayFilter.filter(mockContainerRequestContext);

        // Then
        verify(mockContainerRequestContext).abortWith(any(Response.class));
    }

    @Test
    public void shouldAllowAnyRequest_whenConfiguredGatewayTokenIsEmpty() throws Exception {
        // Given
        when(mockApplicationGatewayFilterConfiguration.applicationGatewayToken()).thenReturn("");
        setUpRequestPath("http://www.example.com/request");

        // When
        applicationGatewayFilter.filter(mockContainerRequestContext);

        // Then
        verify(mockContainerRequestContext, never()).abortWith(any(Response.class));
    }

    @Test
    public void shouldAllowStatusRequest_withoutGatewayTokenHeader() throws Exception {
        // Given
        final String applicationToken = Randoms.randomString();
        when(mockApplicationGatewayFilterConfiguration.applicationGatewayToken()).thenReturn(applicationToken);
        setUpRequestPath("http://www.example.com/status");

        // When
        applicationGatewayFilter.filter(mockContainerRequestContext);

        // Then
        verify(mockContainerRequestContext, never()).abortWith(any(Response.class));
    }

    @Test
    public void shouldAllowStatusSubresourceRequest_withoutGatewayTokenHeader() throws Exception {
        // Given
        final String applicationToken = Randoms.randomString();
        when(mockApplicationGatewayFilterConfiguration.applicationGatewayToken()).thenReturn(applicationToken);
        setUpRequestPath("http://www.example.com/status/" + Randoms.randomString());

        // When
        applicationGatewayFilter.filter(mockContainerRequestContext);

        // Then
        verify(mockContainerRequestContext, never()).abortWith(any(Response.class));
    }

    private void setUpRequestPath(final String path) throws Exception {
        final UriInfo mockUriInfo = mock(UriInfo.class);
        when(mockContainerRequestContext.getUriInfo()).thenReturn(mockUriInfo);
        final URI uri = new URI(path);
        when(mockUriInfo.getRequestUri()).thenReturn(uri);
    }
}
