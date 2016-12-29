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

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import com.clicktravel.cheddar.request.context.DefaultSecurityContext;
import com.clicktravel.cheddar.request.context.SecurityContextHolder;

/**
 * Intercept each HTTP request to set the security context applicable during processing of the request
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class ContainerSecurityRequestFilter implements ContainerRequestFilter {

    private static final String CLICK_PLATFORM_SCHEME = "clickplatform";
    private static final String CLICK_PLATFORM_AGENT_AUTHORIZATION_HEADER = "Agent-Authorization";
    private static final String CLICK_PLATFORM_TEAM_ID_HEADER = "Team-Id";

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        final MultivaluedMap<String, String> headers = requestContext.getHeaders();
        final String userId = getValueForHeaderAndScheme(headers, HttpHeaders.AUTHORIZATION, CLICK_PLATFORM_SCHEME);
        final String teamId = getValueForHeaderAndScheme(headers, CLICK_PLATFORM_TEAM_ID_HEADER, CLICK_PLATFORM_SCHEME);
        final String agentUserId = getValueForHeaderAndScheme(headers, CLICK_PLATFORM_AGENT_AUTHORIZATION_HEADER,
                CLICK_PLATFORM_SCHEME);
        SecurityContextHolder.set(new DefaultSecurityContext(userId, teamId, agentUserId));
    }

    private String getValueForHeaderAndScheme(final MultivaluedMap<String, String> headers, final String header,
            final String scheme) {
        if (headers.containsKey(header)) {
            for (final String headerValue : headers.get(header)) {
                final String[] headerValueParts = headerValue.split(" ");
                if (headerValueParts.length == 2) {
                    if (scheme.equals(headerValueParts[0])) {
                        return headerValueParts[1];
                    }
                }
            }
        }
        return null;
    }

}
