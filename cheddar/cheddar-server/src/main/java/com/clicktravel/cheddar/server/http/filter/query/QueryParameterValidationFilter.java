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

package com.clicktravel.cheddar.server.http.filter.query;

import java.io.IOException;
import java.util.List;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter used to return a standard error in the case that an invalid query string is provided to a URL. Previously we
 * were outputting a HTML page with a stack trace on which is a security breach due to it exposing information about
 * libraries and frameworks we use.
 */
@Provider
@Priority(Priorities.USER)
public class QueryParameterValidationFilter implements ContainerRequestFilter {

    final Logger logger = LoggerFactory.getLogger(QueryParameterValidationFilter.class);

    private boolean queryStringContainsDoubleQuote(final List<String> queryString) {
        return queryString.stream().anyMatch(query -> "\"".equals(query));
    }

    private boolean anyInvalidQueryStringParameters(final MultivaluedMap<String, String> queryParameters) {
        return queryParameters.entrySet().stream()
                .anyMatch(queryString -> queryString != null && queryStringContainsDoubleQuote(queryString.getValue()));
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        final MultivaluedMap<String, String> queryParameters = requestContext.getUriInfo().getQueryParameters();

        if (queryParameters != null && anyInvalidQueryStringParameters(queryParameters)) {
            logger.debug("Invalid query string parameter provided to {}: {}", requestContext.getUriInfo().getPath(),
                    queryParameters);

            final String errorJson = "{\"error\": \"Invalid query parameter\", \"details\": \"Invalid query string parameter provided.\"}";

            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST).entity(errorJson)
                    .type(MediaType.APPLICATION_JSON).build());
        }

    }

}
