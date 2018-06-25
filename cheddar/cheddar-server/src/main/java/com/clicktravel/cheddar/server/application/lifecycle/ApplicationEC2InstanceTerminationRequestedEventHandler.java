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

import com.clicktravel.cheddar.system.event.ApplicationEC2InstanceTerminationRequestedEvent;
import com.clicktravel.cheddar.system.event.SystemEvent;
import com.clicktravel.cheddar.system.event.handler.AbstractSystemEventHandler;

public class ApplicationEC2InstanceTerminationRequestedEventHandler extends AbstractSystemEventHandler {

    private final String ec2InstanceId;
    private final ApplicationLifecycleController applicationLifecycleController;

    public ApplicationEC2InstanceTerminationRequestedEventHandler(final String ec2InstanceId,
            final ApplicationLifecycleController applicationLifecycleController) {
        super(null, null);
        this.ec2InstanceId = ec2InstanceId;
        this.applicationLifecycleController = applicationLifecycleController;
    }

    @Override
    protected void handleSystemEvent(final SystemEvent event) {
        final ApplicationEC2InstanceTerminationRequestedEvent systemEvent = (ApplicationEC2InstanceTerminationRequestedEvent) event;
        if (ec2InstanceId.equals(systemEvent.getEc2InstanceId())) {
            requestApplicationShutdown();
        }
    }

    private void requestApplicationShutdown() {
        new Thread(() -> {
            logger.info(String.format(
                    "Received event that EC2 instance %s is being terminated - Commencing graceful termination of Java process",
                    ec2InstanceId));
            applicationLifecycleController.shutdownApplication();
            logger.info("Java process terminating");
        }).start();
    }

    @Override
    public Class<? extends SystemEvent> getEventClass() {
        return ApplicationEC2InstanceTerminationRequestedEvent.class;
    }

}
