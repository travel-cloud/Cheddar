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

/**
 * HTTP server which exposes JAX-RS resources.
 *
 * A Grizzly HTTP server is used with the provided {@link ResourceConfig} configuration
 */
public class RestServer {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ResourceConfig resourceConfig;
    private HttpServer httpServer;

    public RestServer(final ResourceConfig resourceConfig) {
        this.resourceConfig = resourceConfig;
    }

    public void start(final int servicePort, final String bindAddress, final int workerThreads) throws IOException {
        final URI baseUri = UriBuilder.fromUri("http://" + bindAddress).port(servicePort).build();
        logger.info("Configuring REST server on: " + baseUri.toString());
        httpServer = GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig, false);

        httpServer.getServerConfiguration()
                .setDefaultErrorPageGenerator((request, status, reasonPhrase, description, exception) -> {
                    logger.debug("Grizzly error thrown.  Status: {}; Reason: {}; Description: {}; Exception: {}",
                            status, reasonPhrase, description, exception.getStackTrace());

                    return "<html><body><h1>Error processing request</h1><p>Apologies, there was an error processing your request. Please try again.</p></body></html>";
                });

        enableAutoGenerationOfSwaggerSpecification();
        configureWorkerThreadPool(httpServer.getListener("grizzly"), workerThreads);
        logger.info("Starting REST server; servicePort:[" + servicePort + "]");
        httpServer.start();
    }

    private void enableAutoGenerationOfSwaggerSpecification() {
        // The main scanner class used to scan the classes for swagger + jax-rs annoatations
        final BeanConfig beanConfig = new BeanConfig();
        beanConfig.setResourcePackage("com.clicktravel.services,com.clicktravel.services.*");
        beanConfig.setSchemes(new String[] { "https" });
        beanConfig.setBasePath("/");
        final Info info = new Info();
        info.setVersion("2.0.0");
        beanConfig.setInfo(info);
        beanConfig.setTitle("Swagger Specification");
        beanConfig.setVersion("0.0.0");
        beanConfig.setScan(true);
    }

    private void configureWorkerThreadPool(final NetworkListener networkListener, final int workerThreads) {
        final TCPNIOTransport transport = networkListener.getTransport();

        if (transport.getWorkerThreadPoolConfig() == null) {
            transport.setWorkerThreadPoolConfig(ThreadPoolConfig.defaultConfig());
        }
        transport.getWorkerThreadPoolConfig().setMaxPoolSize(workerThreads).setCorePoolSize(workerThreads);
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