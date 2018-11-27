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
package com.clicktravel.cheddar.server.rest.resource.status;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.clicktravel.cheddar.server.application.configuration.ApplicationConfiguration;
import com.clicktravel.common.concurrent.RateLimiter;

@Path("/status")
public class StatusResource {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ApplicationConfiguration applicationConfiguration;
    private final RateLimiter restRequestRateLimiter;
    private final RateLimiter domainEventHandlerRateLimiter;

    @Autowired
    public StatusResource(final ApplicationConfiguration applicationConfiguration,
            final RateLimiter restRequestRateLimiter, final RateLimiter domainEventHandlerRateLimiter)
            throws IOException {
        this.applicationConfiguration = applicationConfiguration;
        this.restRequestRateLimiter = restRequestRateLimiter;
        this.domainEventHandlerRateLimiter = domainEventHandlerRateLimiter;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus() {
        logger.trace("Application version:[" + applicationConfiguration.version() + "]");
        final StatusResult status = new StatusResult();
        status.setName(applicationConfiguration.name());
        status.setVersion(applicationConfiguration.version());
        status.setFrameworkVersion(applicationConfiguration.frameworkVersion());
        status.setMaximumWorkRates(getMaximumWorkRates());
        final Response response = Response.status(javax.ws.rs.core.Response.Status.OK).entity(status).build();
        return response;
    }

    private MaximumWorkRates getMaximumWorkRates() {
        final MaximumWorkRate restResultMaximumWorkRate = new MaximumWorkRate();
        restResultMaximumWorkRate.setBucketCapacity(restRequestRateLimiter.getBucketCapacity());
        restResultMaximumWorkRate.setTokenReplacementDelay(restRequestRateLimiter.getTokenReplacementDelayMillis());
        final MaximumWorkRate domainEventHandlerMaximumWorkRate = new MaximumWorkRate();
        domainEventHandlerMaximumWorkRate.setBucketCapacity(domainEventHandlerRateLimiter.getBucketCapacity());
        domainEventHandlerMaximumWorkRate
                .setTokenReplacementDelay(domainEventHandlerRateLimiter.getTokenReplacementDelayMillis());
        final MaximumWorkRates maximumWorkRates = new MaximumWorkRates();
        maximumWorkRates.setRestRequest(restResultMaximumWorkRate);
        maximumWorkRates.setDomainEventHandler(domainEventHandlerMaximumWorkRate);
        return maximumWorkRates;
    }

    @GET
    @Path("/healthCheck")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getHealthCheck() {
        return Response.status(Status.OK).entity("Ready").build();
    }
}
