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
package com.clicktravel.cheddar.server.http.filter.charset;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

/**
 * Response filter that sets default value for {@code charset} parameter for {@code Content-Type} header if not already
 * set
 */
@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class CharsetResponseFilter implements ContainerResponseFilter {

    private static final String DEFAULT_CHARSET = "utf-8";

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext)
            throws IOException {
        final MediaType type = responseContext.getMediaType();
        if (type != null && !type.getParameters().containsKey(MediaType.CHARSET_PARAMETER)) {
            responseContext.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, type.withCharset(DEFAULT_CHARSET));
        }
    }
}
