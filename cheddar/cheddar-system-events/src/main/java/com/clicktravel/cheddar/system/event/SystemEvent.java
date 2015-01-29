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
package com.clicktravel.cheddar.system.event;

import com.clicktravel.cheddar.event.Event;

/**
 * Interface representing a low-level system event within the Click Platform eco-system which may be targetting a
 * particular application of version of that application.
 */
public interface SystemEvent extends Event {

    /**
     * Returns the name of the application at which this System Event is targetted
     *
     * @return The name of the application. NULL means all applications
     */
    String getTargetApplicationName();

    /**
     * Returns the version of the application at which this System Event is targetted
     *
     * @return The version of the application. NULL means all versions of the targetted application
     */
    String getTargetApplicationVersion();

}
