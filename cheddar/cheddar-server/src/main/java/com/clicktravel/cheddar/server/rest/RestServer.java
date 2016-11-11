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

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.models.Info;
import io.swagger.models.Swagger;
import io.swagger.models.auth.OAuth2Definition;

/**
 * HTTP server which exposes JAX-RS resources.
 *
 * A Grizzly HTTP server is used with the provided {@link ResourceConfig} configuration
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
        enableAutoGenerationOfSwaggerSpecification();
        configureThreadPools(httpServer.getListener("grizzly"), SERVICE_POOL_NAME_PREFIX, SERVICE_WORKER_THREADS,
                SERVICE_KERNEL_THREADS);
        final NetworkListener statusPortListener = new NetworkListener("status", baseUri.getHost(), statusPort);
        configureThreadPools(statusPortListener, STATUS_POOL_NAME_PREFIX, STATUS_WORKER_THREADS, STATUS_KERNEL_THREADS);
        httpServer.addListener(statusPortListener);
        logger.info("Starting REST server; servicePort:[" + servicePort + "] statusPort:[" + statusPort + "]");
        httpServer.start();
    }

    private void enableAutoGenerationOfSwaggerSpecification() {
        // The main scanner class used to scan the classes for swagger + jax-rs annoatations
        final io.swagger.jaxrs.config.BeanConfig beanConfig = new BeanConfig();
        // Could not get the try base path to work with swagger 2.0 as it is matched by contains in
        // 'com.wordnik.swagger.jaxrs.config.BeanConfig.classes')'
        beanConfig.setResourcePackage("com.clicktravel.services,com.clicktravel.services.*");
        beanConfig.setSchemes(new String[] { "https" });
        beanConfig.setBasePath("/");
        final Info info = new Info();
        info.setVersion("1.0.0");
        beanConfig.setInfo(info);
        beanConfig.setTitle("Swagger Specification");
        beanConfig.setVersion("1.0.0");

        // The follow sets up the security schemes so they can be referenced later (doesn't affect codegen methods!)
        final Swagger swaggerConfiguration = beanConfig.getSwagger();
        final OAuth2Definition oauth2ImplicitDefinition = new OAuth2Definition();
        final String applicationGatewayEndpiont = "https://auth.travel.cloud";
        final String authorizationUrl = String.format("%s/authorize", applicationGatewayEndpiont);

        oauth2ImplicitDefinition.implicit(authorizationUrl);
        oauth2ImplicitDefinition.addScope("Default", "The default scope for Oauth authentication");
        swaggerConfiguration.addSecurityDefinition("implicit", oauth2ImplicitDefinition);

        /*
         * Issues with 3scale active docs so temporarily commenting out This adds a global reference-able parameter to
         * the spec (doesn't affect codegen methods!) This also cuased final HeaderParameter headerParameter = new
         * HeaderParameter(); headerParameter.name("Authorization"); headerParameter.setRequired(true);
         * swaggerConfiguration.addParameter("authorization", headerParameter);
         */

        beanConfig.configure(swaggerConfiguration);
        // This method sets the vales on the scanner and goes and scans the classes that fit the resource package
        beanConfig.setScan(true);
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