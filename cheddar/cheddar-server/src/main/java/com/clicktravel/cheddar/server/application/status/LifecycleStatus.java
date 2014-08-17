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
package com.clicktravel.cheddar.server.application.status;

/**
 * Status of application with respect to blue-green deployment lifecycle
 */
public enum LifecycleStatus {
    /**
     * Application does not receive any command, response or event messages. Adapter (REST) requests are accepted but
     * not processed. ELB health checks report instance as unhealthy i.e. not ready for traffic.<br>
     * An application starts in this state if the server is started in blue-green mode.
     */
    INACTIVE,

    /**
     * Application does not receive any command, response or event messages. Adapter (REST) requests are accepted but
     * not processed. ELB health checks report instance as healthy i.e. ready for traffic.<br>
     */
    PAUSED,

    /**
     * Application receives command, response, event messages and REST requests as normal.<br>
     * An application starts in this state if the server is not started in blue-green mode.
     */
    RUNNING,

    /**
     * Application receives command and response messages, as well as REST requests. Event messages are received, but
     * event handlers defer processing if possible by re-queueing the messages.
     */
    HALTING_EVENTS,

    /**
     * Application receives command and response messages as normal. Application may receive REST requests, but not many
     * (if any at all) will be received. Event messages are not received, but any currently event processing in progress
     * is allowed to complete. Command and response messages are received until the queues drain. Event, command and
     * response message processing loops terminate when all processing is complete. When all message processing loops
     * have terminated, the application state is advanced to TERMINATED state.
     */
    TERMINATING,

    /**
     * Application only responds to GET /status request. No command, response or event messages are received.
     */
    TERMINATED

}
