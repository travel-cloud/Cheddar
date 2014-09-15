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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.clicktravel.cheddar.server.rest.RestServer;

public class RestApplication {

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
    public static void main(final String... args) throws Exception {
        final String context = args.length > 0 ? args[0] : "UNKNOWN";
        final int servicePort = args.length > 1 ? Integer.parseInt(args[1]) : 8080;
        final int statusPort = args.length > 2 ? Integer.parseInt(args[2]) : servicePort + 100;
        final String bindAddress = args.length > 3 ? args[3] : "0.0.0.0";
        MDC.put("context", context);
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        final Logger logger = LoggerFactory.getLogger(RestApplication.class);
        final RestServer restServer = new RestServer(servicePort, statusPort, bindAddress);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                restServer.stop();
            }
        }));
        try {
            restServer.start();
            Thread.currentThread().join();
        } catch (final IOException e) {
            logger.error("Error running REST server", e);
        }
    }
}