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
package com.clicktravel.cheddar.server.rest.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.clicktravel.cheddar.server.application.lifecycle.ApplicationLifecycleController;

public class RestApplication {

    private static final String DEFAULT_CONTEXT = "UNKNOWN";
    private static final int DEFAULT_SERVICE_PORT = 8080;
    private static final String DEFAULT_BIND_ADDRESS = "0.0.0.0";
    private static final String DEFAULT_WORKER_THREADS = "16";

    /**
     * Starts the RestServer to listen on the given port and address combination
     *
     * @param args String arguments, {@code [context [service-port [status-port [bind-address] ] ] ]} where
     *            <ul>
     *            <li>{@code context} - Name of application, defaults to {@code UNKNOWN}</li>
     *            <li>{@code service-port} - Port number for REST service endpoints, defaults to {@code 8080}</li>
     *            <li>{@code status-port} - Port number for REST status endpoints ({@code /status} and
     *            {@code /status/healthCheck}), defaults to {@code service-port + 100}</li>
     *            <li>{@code bind-address} - Local IP address to bind server to, defaults to {@code 0.0.0.0}</li>
     *            </ul>
     *
     * @throws Exception
     */
    public static void main(final String... args) {
        final String context = args.length > 0 ? args[0] : DEFAULT_CONTEXT;
        final int servicePort = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_SERVICE_PORT;
        final int statusPort = args.length > 2 ? Integer.parseInt(args[2]) : servicePort + 100;
        final String bindAddress = args.length > 3 ? args[3] : DEFAULT_BIND_ADDRESS;
        final int workerThreads = Integer.parseInt(System.getProperty("worker.threads", DEFAULT_WORKER_THREADS));
        MDC.put("context", context);
        MDC.put("hostId", System.getProperty("host.id", "UNKNOWN"));
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        final Logger logger = LoggerFactory.getLogger(RestApplication.class);
        try {
            logger.info("Java process starting");
            logger.info(String.format("java.version:[%s] java.vendor:[%s] maxMemoryMb:[%d]",
                    System.getProperty("java.version"), System.getProperty("java.vendor"),
                    Runtime.getRuntime().maxMemory() / (1024 * 1024)));
            @SuppressWarnings("resource")
            final ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                    "applicationContext.xml");
            logger.debug("Finished getting ApplicationContext");
            final ApplicationLifecycleController applicationLifecycleController = applicationContext
                    .getBean(ApplicationLifecycleController.class);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown hook invoked - Commencing graceful termination of Java process");
                applicationLifecycleController.shutdownApplication();
                logger.info("Java process terminating");
            }));
            applicationLifecycleController.startApplication(servicePort, statusPort, bindAddress, workerThreads);
            Thread.currentThread().join();
        } catch (final InterruptedException e) {
            logger.info("Java process interrupted");
            System.exit(1);
        } catch (final Exception e) {
            logger.error("Error starting Java process", e);
            System.exit(1);
        }
    }
}