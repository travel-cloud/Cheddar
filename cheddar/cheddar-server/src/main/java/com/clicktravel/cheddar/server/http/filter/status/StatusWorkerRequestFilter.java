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
package com.clicktravel.cheddar.server.http.filter.status;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import com.clicktravel.cheddar.server.rest.RestServer;

/**
 * REST request filter that ensures status worker threads only respond to requests for status (/status) resources.
 * Requests for other resources are rejected with a 404 Not Found response.
 */
@Provider
public class StatusWorkerRequestFilter implements ContainerRequestFilter {

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        final boolean isStatusWorkerThread = Thread.currentThread().getName()
                .startsWith(RestServer.STATUS_POOL_NAME_PREFIX);
        final String path = requestContext.getUriInfo().getRequestUri().getPath();
        final boolean isStatusResource = path.matches("/status(/.*)?");
        if (isStatusWorkerThread && !isStatusResource) {
            requestContext.abortWith(Response.status(Status.NOT_FOUND).build());
        }
    }
}
