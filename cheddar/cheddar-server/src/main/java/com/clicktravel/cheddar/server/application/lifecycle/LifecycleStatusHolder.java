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

import static com.clicktravel.cheddar.server.application.lifecycle.LifecycleStatus.INACTIVE;
import static com.clicktravel.cheddar.server.application.lifecycle.LifecycleStatus.RUNNING;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LifecycleStatusHolder {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private volatile LifecycleStatus lifecycleStatus;

    @Autowired
    public LifecycleStatusHolder(@Value("${blue.green.startup:false}") final boolean blueGreenStartup) {
        logger.debug("Blue-green startup: " + blueGreenStartup);
        lifecycleStatus = blueGreenStartup ? INACTIVE : RUNNING;
    }

    public LifecycleStatus getLifecycleStatus() {
        return lifecycleStatus;
    }

    public void setLifecycleStatus(final LifecycleStatus lifecycleStatus) {
        this.lifecycleStatus = lifecycleStatus;
    }

}
