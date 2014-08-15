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
package com.clicktravel.cheddar.server.runtime.config;

import org.springframework.core.env.Environment;

public class RuntimeConfiguration {

    public static final String LOCAL_PROFILE = "local";
    public static final String CI_PROFILE = "ci";
    public static final String STABLE_PROFILE = "stable";
    public static final String UAT_PROFILE = "uat";
    public static final String PLATFORM_FAILURE_PROFILE = "platform-failure";
    public static final String PRODUCTION_PROFILE = "production";

    public static boolean isLocalEnvironment(final Environment environment) {
        for (final String profile : environment.getActiveProfiles()) {
            if (profile.equalsIgnoreCase(LOCAL_PROFILE)) {
                return true;
            }
        }
        return false;
    }
}
