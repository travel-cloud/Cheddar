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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LifecycleStatusHolder {

    private LifecycleStatus lifecycleStatus;

    @Autowired
    public LifecycleStatusHolder(@Value("${blue.green.mode}") final boolean blueGreenMode) {
        lifecycleStatus = blueGreenMode ? LifecycleStatus.INACTIVE : LifecycleStatus.RUNNING;
    }

    public LifecycleStatus getLifecycleStatus() {
        return lifecycleStatus;
    }

    public void setLifecycleStatus(final LifecycleStatus lifecycleStatus) {
        this.lifecycleStatus = lifecycleStatus;
    }

}
