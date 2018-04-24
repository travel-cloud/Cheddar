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

import java.io.IOException;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.clicktravel.cheddar.server.flow.control.RateLimiterConfiguration;
import com.clicktravel.common.concurrent.RateLimiter;

/**
 * Applies a rate limit function to all (matched) requests, except for status resource (/status). This filter will block
 * until the rate limiter allows the request processing to proceed.
 */
@Provider
@Priority(Priorities.USER)
public class FlowControlledRequestFilter implements ContainerRequestFilter {

    @Autowired
    @Value("${flow.control.rateLimitLogging:true}")
    private boolean rateLimitLogging;

    private final Logger logger = Logger.getLogger(FlowControlledRequestFilter.class);

    private final RateLimiter restRequestRateLimiter;

    @Inject
    public FlowControlledRequestFilter(final RateLimiterConfiguration rateLimiterConfiguration) {
        restRequestRateLimiter = rateLimiterConfiguration.restRequestRateLimiter();
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        final String path = requestContext.getUriInfo().getRequestUri().getPath();
        final boolean isStatusResource = path.matches("/status(/.*)?");
        if (!isStatusResource) {
            try {
                final long start = rateLimitLogging ? System.currentTimeMillis() : 0l;
                restRequestRateLimiter.takeToken(); // block until allowed by rate limit
                if (rateLimitLogging) {
                    logger.debug(path + " took " + ((System.currentTimeMillis() - start) / 1000)
                            + "s to obtain a token to process");
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
