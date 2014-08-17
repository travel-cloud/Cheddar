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
package com.clicktravel.cheddar.server.http.filter.security;

import static org.mockito.Mockito.never;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.clicktravel.cheddar.request.context.SecurityContextHolder;
import com.clicktravel.common.random.Randoms;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SecurityContextHolder.class })
public class ContainerSecurityRequestFilterTest {

    private static final String AUTHORIZATION_HEADER_VALUE_PREFIX = "clickplatform ";

    @Test
    public void shouldSetPrincipal_withClickPlatformAuthorizationHeader() throws Exception {
        // Given
        mockStatic(SecurityContextHolder.class);
        final String principal = Randoms.randomString();
        final ContainerRequestContext mockContainerRequestContext = mock(ContainerRequestContext.class);
        final MultivaluedMap<String, String> headersMap = new MultivaluedHashMap<>();
        headersMap.add(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER_VALUE_PREFIX + principal);
        when(mockContainerRequestContext.getHeaders()).thenReturn(headersMap);
        final ContainerSecurityRequestFilter containerSecurityRequestFilter = new ContainerSecurityRequestFilter();

        // When
        containerSecurityRequestFilter.filter(mockContainerRequestContext);

        // Then
        verifyStatic();
        SecurityContextHolder.setPrincipal(principal);
    }

    @Test
    public void shouldNotSetPrincipal_withGeneralAuthorizationHeader() throws Exception {
        // Given
        mockStatic(SecurityContextHolder.class);
        final String principal = Randoms.randomString();
        final ContainerRequestContext mockContainerRequestContext = mock(ContainerRequestContext.class);
        final MultivaluedMap<String, String> headersMap = new MultivaluedHashMap<>();
        headersMap.add(HttpHeaders.AUTHORIZATION, principal);
        when(mockContainerRequestContext.getHeaders()).thenReturn(headersMap);
        final ContainerSecurityRequestFilter containerSecurityRequestFilter = new ContainerSecurityRequestFilter();

        // When
        containerSecurityRequestFilter.filter(mockContainerRequestContext);

        // Then
        verifyStatic(never());
        SecurityContextHolder.setPrincipal(principal);
    }
}
