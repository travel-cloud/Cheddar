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

import com.clicktravel.cheddar.request.context.AgentSecurityContext;
import com.clicktravel.cheddar.request.context.BasicSecurityContext;
import com.clicktravel.cheddar.request.context.SecurityContextHolder;

/**
 * Intercept each HTTP request to extract the security header and set its value as the principal within
 * {@link com.clicktravel.cheddar.application.security.SecurityContextHolder}
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class ContainerSecurityRequestFilter implements ContainerRequestFilter {

    private static final String CLICK_PLATFORM_AUTHORIZATION_SCHEME = "clickplatform";
    private static final String CLICK_PLATFORM_AGENT_AUTHORIZATION_SCHEME = "clickplatform-agent";

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        final MultivaluedMap<String, String> headersMap = requestContext.getHeaders();
        String principal = null;
        String agent = null;
        if (headersMap.containsKey(HttpHeaders.AUTHORIZATION)) {
            for (final String headerValue : headersMap.get(HttpHeaders.AUTHORIZATION)) {
                final String[] headerValueParts = headerValue.split(" ");
                if (headerValueParts.length == 2) {
                    if (CLICK_PLATFORM_AUTHORIZATION_SCHEME.equals(headerValueParts[0])) {
                        principal = headerValueParts[1];
                    } else if (CLICK_PLATFORM_AGENT_AUTHORIZATION_SCHEME.equals(headerValueParts[0])) {
                        agent = headerValueParts[1];
                    }
                }
            }
        }
        if (principal != null && agent != null) {
            SecurityContextHolder.set(new AgentSecurityContext(principal, agent));
        } else if (principal != null) {
            SecurityContextHolder.set(new BasicSecurityContext(principal));
        } else {
            SecurityContextHolder.clear();
        }
    }

}
