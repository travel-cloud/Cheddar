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
package com.clicktravel.cheddar.server.application.lifecycle.event.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.server.application.configuration.ApplicationConfiguration;
import com.clicktravel.cheddar.server.application.lifecycle.LifecycleStatus;
import com.clicktravel.cheddar.server.application.lifecycle.LifecycleStatusHolder;
import com.clicktravel.cheddar.system.event.SystemEvent;
import com.clicktravel.cheddar.system.event.handler.AbstractSystemEventHandler;

public abstract class AbstractApplicationLifecycleEventHandler extends AbstractSystemEventHandler {

    private final LifecycleStatusHolder lifecycleStatusHolder;
    private final LifecycleStatus expectedLifecycleStatus;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected AbstractApplicationLifecycleEventHandler(final ApplicationConfiguration applicationConfiguration,
            final LifecycleStatusHolder lifecycleStatusHolder, final LifecycleStatus expectedLifecycleStatus) {
        super(applicationConfiguration.name(), applicationConfiguration.version());
        this.lifecycleStatusHolder = lifecycleStatusHolder;
        this.expectedLifecycleStatus = expectedLifecycleStatus;
    }

    @Override
    protected void handleSystemEvent(final SystemEvent event) {
        final LifecycleStatus lifecycleStatus = lifecycleStatusHolder.getLifecycleStatus();
        if (lifecycleStatus.equals(expectedLifecycleStatus)) {
            handleApplicationLifecycleEvent(event);
        } else {
            logger.warn("Received unexpected system event [" + event.type() + "] while in lifecycle status ["
                    + lifecycleStatus + "]");
        }
    }

    protected abstract void handleApplicationLifecycleEvent(SystemEvent event);
}
