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

import java.io.IOException;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

/**
 * Class to reject requests made to end-points which do not have the required HTTP header which is injected by the
 * application gateway
 *
 * The exception to this rule is for the status page which can be accessed without checking this header
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
public class ApplicationGatewayFilter implements ContainerRequestFilter {

    private static final String APPLICATION_GATEWAY_TOKEN_HEADER = "x-clicktravel-application-gateway-token";

    private final ApplicationGatewayFilterConfiguration applicationGatewayFilterConfiguration;

    @Inject
    public ApplicationGatewayFilter(final ApplicationGatewayFilterConfiguration applicationGatewayFilterConfiguration) {
        this.applicationGatewayFilterConfiguration = applicationGatewayFilterConfiguration;
    }

    /**
     * Any request which does not supply the correct Application Gateway header will be rejected with a HTTP 401 status.
     * Exceptions to this rule is made for the status resource
     */
    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        final String path = requestContext.getUriInfo().getRequestUri().getPath();
        final boolean isStatusResource = path.matches("/status(/.*)?");
        if (!isStatusResource) {
            final String applicationGatewayToken = applicationGatewayFilterConfiguration.applicationGatewayToken();
            if (!applicationGatewayToken.isEmpty()) {
                final String receivedApplicationGatewayToken = requestContext
                        .getHeaderString(APPLICATION_GATEWAY_TOKEN_HEADER);
                final boolean hasApplicationGatewayTokenMatch = applicationGatewayToken
                        .equals(receivedApplicationGatewayToken);
                if (!hasApplicationGatewayTokenMatch) {
                    requestContext.abortWith(Response.status(Status.UNAUTHORIZED).build());
                }
            }
        }
    }

}
