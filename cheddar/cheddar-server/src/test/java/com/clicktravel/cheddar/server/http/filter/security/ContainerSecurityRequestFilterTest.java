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

import static com.clicktravel.common.random.Randoms.randomId;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Optional;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.clicktravel.cheddar.request.context.DefaultSecurityContext;
import com.clicktravel.cheddar.request.context.SecurityContextHolder;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SecurityContextHolder.class })
public class ContainerSecurityRequestFilterTest {

    private static final String CLICK_PLATFORM_SCHEME = "clickplatform";
    private static final String CLICK_PLATFORM_AGENT_AUTHORIZATION_HEADER = "Agent-Authorization";
    private static final String CLICK_PLATFORM_TEAM_ID_HEADER = "Team-Id";

    private ContainerRequestContext mockContainerRequestContext;
    private MultivaluedMap<String, String> headers;

    @Before
    public void setUp() {
        mockContainerRequestContext = mock(ContainerRequestContext.class);
        headers = new MultivaluedHashMap<String, String>();
        when(mockContainerRequestContext.getHeaders()).thenReturn(headers);
    }

    @Test
    public void shouldSetSecurityContextProperties_withHeaders() throws Exception {
        // Given
        mockStatic(SecurityContextHolder.class);
        final String userId = randomId();
        final String teamId = randomId();
        final String agentUserId = randomId();
        headers.add(HttpHeaders.AUTHORIZATION, CLICK_PLATFORM_SCHEME + " " + userId);
        headers.add(CLICK_PLATFORM_TEAM_ID_HEADER, CLICK_PLATFORM_SCHEME + " " + teamId);
        headers.add(CLICK_PLATFORM_AGENT_AUTHORIZATION_HEADER, CLICK_PLATFORM_SCHEME + " " + agentUserId);
        final ContainerSecurityRequestFilter containerSecurityRequestFilter = new ContainerSecurityRequestFilter();

        // When
        containerSecurityRequestFilter.filter(mockContainerRequestContext);

        // Then
        final ArgumentCaptor<DefaultSecurityContext> securityContextCaptor = ArgumentCaptor
                .forClass(DefaultSecurityContext.class);
        verifyStatic();
        SecurityContextHolder.set(securityContextCaptor.capture());
        assertEquals(Optional.of(userId), securityContextCaptor.getValue().userId());
        assertEquals(Optional.of(teamId), securityContextCaptor.getValue().teamId());
        assertEquals(Optional.of(agentUserId), securityContextCaptor.getValue().agentUserId());
    }

    @Test
    public void shouldSetEmptySecurityContextProperties_withNoHeaders() throws Exception {
        // Given
        mockStatic(SecurityContextHolder.class);
        final ContainerSecurityRequestFilter containerSecurityRequestFilter = new ContainerSecurityRequestFilter();

        // When
        containerSecurityRequestFilter.filter(mockContainerRequestContext);

        // Then
        final ArgumentCaptor<DefaultSecurityContext> securityContextCaptor = ArgumentCaptor
                .forClass(DefaultSecurityContext.class);
        verifyStatic();
        SecurityContextHolder.set(securityContextCaptor.capture());
        assertEquals(Optional.empty(), securityContextCaptor.getValue().userId());
        assertEquals(Optional.empty(), securityContextCaptor.getValue().teamId());
        assertEquals(Optional.empty(), securityContextCaptor.getValue().agentUserId());
    }
}
