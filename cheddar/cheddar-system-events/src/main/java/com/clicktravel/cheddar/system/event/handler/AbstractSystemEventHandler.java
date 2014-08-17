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
package com.clicktravel.cheddar.system.event.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.system.event.SystemEvent;

public abstract class AbstractSystemEventHandler implements SystemEventHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final String applicationName;
    private final String applicationVersion;

    public AbstractSystemEventHandler(final String applicationName, final String applicationVersion) {
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
    }

    @Override
    public void handle(final SystemEvent event) {
        if (isSystemEventSupported(event)) {
            handleSystemEvent(event);
        } else {
            if (logger.isDebugEnabled()) {
                final String applicationNameSignature = event.getTargetApplicationName() == null ? "*" : event
                        .getTargetApplicationName();
                final String applicationVersionSignature = event.getTargetApplicationVersion() == null ? "*" : event
                        .getTargetApplicationVersion();
                logger.debug("Ignoring message for '" + applicationNameSignature + "-" + applicationVersionSignature
                        + "': " + event.type());
            }
        }
    }

    private boolean isSystemEventSupported(final SystemEvent event) {
        final String targetApplicationName = event.getTargetApplicationName();
        final String targetApplicationVersion = event.getTargetApplicationVersion();
        return (targetApplicationName == null || targetApplicationName.equals(applicationName))
                && (targetApplicationVersion == null || targetApplicationVersion.equals(applicationVersion));
    }

    protected abstract void handleSystemEvent(SystemEvent event);

}
