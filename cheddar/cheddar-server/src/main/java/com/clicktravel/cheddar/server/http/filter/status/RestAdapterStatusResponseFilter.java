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

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import com.clicktravel.cheddar.server.application.status.RestAdapterStatusHolder;

@Provider
@Priority(500)
public class RestAdapterStatusResponseFilter implements ContainerResponseFilter {

    private final RestAdapterStatusHolder restAdapterStatusHolder;

    @Inject
    public RestAdapterStatusResponseFilter(final RestAdapterStatusHolder restAdapterStatusHolder) {
        this.restAdapterStatusHolder = restAdapterStatusHolder;
    }

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext)
            throws IOException {
        restAdapterStatusHolder.requestProcessingFinished();
    }

}
