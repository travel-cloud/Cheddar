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
package com.clicktravel.cheddar.server.application.lifecycle;

/**
 * Status of application with respect to blue-green deployment lifecycle
 */
public enum LifecycleStatus {
    /**
     * Application does not process any command, response, event or application work messages. REST requests are
     * accepted but not processed. ELB health checks report instance as unhealthy i.e. not ready for traffic.<br>
     * An application starts in this state if the server is started in blue-green mode.
     */
    INACTIVE,

    /**
     * Application does not process any command, response, event or application work messages. REST requests are
     * accepted but not processed. ELB health checks report instance as healthy i.e. ready for traffic.
     */
    PAUSED,

    /**
     * Application processes command, response, event and application work messages and REST requests as normal. ELB
     * health checks report instance as healthy i.e. ready for traffic.<br>
     * An application starts in this state if the server is not started in blue-green mode.
     */
    RUNNING,

    /**
     * Application processes command, response, event messages for high priority domain event handlers and REST requests
     * as normal. Event and application work messages for low priority event messages are not taken from the queue;
     * shutdown of thread pool for low priority domain event handlers and application work message handlers is started
     * when entering this state. ELB health checks report instance as healthy i.e. ready for traffic.
     */
    HALTING_LOW_PRIORITY_EVENTS,

    /**
     * Application processes command, response, event messages for high priority domain event handlers and REST requests
     * as normal. Event messages for low priority domain event handlers or application work are not taken from the
     * queues. ELB health checks report instance as unhealthy i.e. not ready for traffic. It is expected that few, if
     * any, REST requests are received.
     */
    DRAINING_REQUESTS,

    /**
     * Application processes command and response messages as normal. No messages are taken from event or application
     * work queues. Shutdown of thread pool for high priority domain event handlers is started when entering this state.
     * ELB health checks report instance as unhealthy i.e. not ready for traffic. No REST requests should be received in
     * this state. When processing of event messages held in MessageListener completes, the lifecycle status is advanced
     * to TERMINATING
     */
    HALTING_HIGH_PRIORITY_EVENTS,

    /**
     * Application processes command and response messages as normal. No messages are taken from event or application
     * work queues. ELB health checks report instance as unhealthy i.e. not ready for traffic. No REST requests should
     * be received in this state. When processing of commands managed by the RemotingGateway completes, the lifecycle
     * status is advanced to TERMINATED
     */
    TERMINATING,

    /**
     * Application does not process any queue (command, response, event or application work). ELB health checks report
     * instance as unhealthy i.e. not ready for traffic. No REST requests should be received in this state.
     */
    TERMINATED

}
