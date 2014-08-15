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
package com.clicktravel.cheddar.server.rest;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.server.rest.resource.config.RestResourceConfig;

/**
 * HTTP server which exposes JAX-RS resources
 * 
 * Deployment is done via GrizzlyHttpServer and scans various packages for candidates for JAX-RS Resources and
 * Providers. The Spring container is also initialised.
 */
public class RestServer {

    private static final int SERVICE_WORKER_THREADS = 16;
    private static final int STATUS_WORKER_THREADS = 2;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final HttpServer httpServer;

    public static final String SERVICE_POOL_NAME_PREFIX = "Grizzly-Service";
    public static final String STATUS_POOL_NAME_PREFIX = "Grizzly-Status";

    public RestServer(final int servicePort, final int statusPort, final String bindAddress) {
        final ResourceConfig resourceConfig = new RestResourceConfig();
        logger.info("Registering resources has finished");
        final URI baseUri = UriBuilder.fromUri("http://" + bindAddress).port(servicePort).build();
        logger.info("Configuring REST server on: " + baseUri.toString());
        httpServer = GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig, false);
        configureThreadPools(httpServer.getListener("grizzly").getTransport(), SERVICE_POOL_NAME_PREFIX,
                SERVICE_WORKER_THREADS);
        logger.info("Configuring REST status resources on port " + statusPort);
        final NetworkListener statusPortListener = new NetworkListener("status", baseUri.getHost(), statusPort);
        configureThreadPools(statusPortListener.getTransport(), STATUS_POOL_NAME_PREFIX, STATUS_WORKER_THREADS);
        httpServer.addListener(statusPortListener);
    }

    private void configureThreadPools(final TCPNIOTransport transport, final String poolNamePrefix, final int threads) {
        transport.getKernelThreadPoolConfig().setPoolName(poolNamePrefix + "-Kernel");
        transport.getWorkerThreadPoolConfig().setPoolName(poolNamePrefix + "-Worker").setMaxPoolSize(threads)
                .setCorePoolSize(threads);
    }

    public void start() throws IOException {
        logger.info("Starting REST server");
        httpServer.start();
    }

    public void stop() {
        try {
            logger.info("Stopping REST server");
            httpServer.stop();
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

}