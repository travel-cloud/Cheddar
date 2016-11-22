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
package com.clicktravel.cheddar.server.http.filter.logging;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Priority(Priorities.USER)
public class LoggingRequestFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingRequestFilter.class);

    @Override
    public void filter(final ContainerRequestContext requestContext) {
        if (LOGGER.isDebugEnabled()) {
            try {
                LOGGER.debug("REST-REQ [{}] [{}]", requestContext.getUriInfo().getPath(),
                        readRequestBody(requestContext));
            } catch (final Exception e) {
                LOGGER.warn("Error during logging request: ", e);
            }
        }
    }

    private String readRequestBody(final ContainerRequestContext requestContext) throws IOException {
        final byte[] buffer = new byte[256];
        final InputStream entityStream = requestContext.getEntityStream();
        final InputStream inputStream = entityStream.markSupported() ? entityStream
                : new BufferedInputStream(entityStream);
        inputStream.mark(Integer.MAX_VALUE);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        while (inputStream.available() > 0) {
            final int length = inputStream.read(buffer);
            outputStream.write(buffer, 0, length);
        }
        inputStream.reset();
        requestContext.setEntityStream(inputStream);
        return outputStream.toString(StandardCharsets.UTF_8.name());
    }

}
