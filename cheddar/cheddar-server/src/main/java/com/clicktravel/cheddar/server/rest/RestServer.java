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
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.GrizzlyFuture;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP server which exposes JAX-RS resources
 *
 * Deployment is done via GrizzlyHttpServer and scans various packages for candidates for JAX-RS Resources and
 * Providers. The Spring container is also initialised.
 */
public class RestServer {

    private static final int SERVICE_WORKER_THREADS = 16;
    private static final int SERVICE_KERNEL_THREADS = 8;
    private static final int STATUS_WORKER_THREADS = 2;
    private static final int STATUS_KERNEL_THREADS = 2;
    public static final String SERVICE_POOL_NAME_PREFIX = "Grizzly-Service";
    public static final String STATUS_POOL_NAME_PREFIX = "Grizzly-Status";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ResourceConfig resourceConfig;
    private HttpServer httpServer;

    public RestServer(final ResourceConfig resourceConfig) {
        this.resourceConfig = resourceConfig;
    }

    public void start(final int servicePort, final int statusPort, final String bindAddress) throws IOException {
        final URI baseUri = UriBuilder.fromUri("http://" + bindAddress).port(servicePort).build();
        logger.info("Configuring REST server on: " + baseUri.toString());
        httpServer = GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig, false);
        configureThreadPools(httpServer.getListener("grizzly"), SERVICE_POOL_NAME_PREFIX, SERVICE_WORKER_THREADS,
                SERVICE_KERNEL_THREADS);
        final NetworkListener statusPortListener = new NetworkListener("status", baseUri.getHost(), statusPort);
        configureThreadPools(statusPortListener, STATUS_POOL_NAME_PREFIX, STATUS_WORKER_THREADS, STATUS_KERNEL_THREADS);
        httpServer.addListener(statusPortListener);
        logger.info("Starting REST server; servicePort:[" + servicePort + "] statusPort:[" + statusPort + "]");
        httpServer.start();
    }

    private void configureThreadPools(final NetworkListener networkListener, final String poolNamePrefix,
            final int workerThreads, final int kernelThreads) {
        final TCPNIOTransport transport = networkListener.getTransport();

        if (transport.getKernelThreadPoolConfig() == null) {
            transport.setKernelThreadPoolConfig(ThreadPoolConfig.defaultConfig());
        }
        transport.getKernelThreadPoolConfig().setPoolName(poolNamePrefix + "-Kernel").setMaxPoolSize(kernelThreads)
                .setCorePoolSize(kernelThreads);
        transport.setSelectorRunnersCount(kernelThreads);

        if (transport.getWorkerThreadPoolConfig() == null) {
            transport.setWorkerThreadPoolConfig(ThreadPoolConfig.defaultConfig());
        }
        transport.getWorkerThreadPoolConfig().setPoolName(poolNamePrefix + "-Worker").setMaxPoolSize(workerThreads)
                .setCorePoolSize(workerThreads);
    }

    public void shutdownAndAwait(final long timeoutMillis) {
        try {
            logger.info("Shutting down REST server");
            if (httpServer != null) {
                final GrizzlyFuture<HttpServer> future = httpServer.shutdown(timeoutMillis, TimeUnit.MILLISECONDS);
                future.get();
            }
            logger.info("Shutdown of REST server complete");
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

}