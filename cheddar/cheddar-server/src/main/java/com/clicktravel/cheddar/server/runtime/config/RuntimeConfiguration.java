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

    /**
     * TODO : Migrate profile to {@link #DEV_PROFILE}
     */
    @Deprecated
    public static final String LOCAL_PROFILE = "local";

    /**
     * TODO : Remove this profile
     */
    @Deprecated
    public static final String PLATFORM_FAILURE_PROFILE = "platform-failure";

    /**
     * Profile for using in-memory mocks of external services. No AWS infrastructure or services are accessed. This
     * profile is intended for use by service tests.
     */
    public static final String TEST_PROFILE = "test";

    /**
     * Profile for using services available in AWS "Dev" infrastructure. Environment and service properties are set to
     * enable multiple developers to execute services locally (a developer's machine, rather than an EC2 instance) using
     * this profile. For example, URLs for DynamoDB access are set to refer to a DynamoDB Local instance. Another
     * example, SQS queues are given names unique to a developer to enable exclusive use by the locally executing
     * service(s) under development.
     */
    public static final String DEV_PROFILE = "dev";

    /** Profile for using services available in AWS "CI" infrastructure */
    public static final String CI_PROFILE = "ci";

    /** Profile for using services available in AWS "Stable" infrastructure */
    public static final String STABLE_PROFILE = "stable";

    /** Profile for using services available in AWS "UAT" infrastructure */
    public static final String UAT_PROFILE = "uat";

    /** Profile for using services available in AWS "Production" infrastructure */
    public static final String PRODUCTION_PROFILE = "production";

    public static boolean isLocalEnvironment(final Environment environment) {
        for (final String profile : environment.getActiveProfiles()) {
            if (profile.equalsIgnoreCase(LOCAL_PROFILE) || profile.equalsIgnoreCase(TEST_PROFILE)
                    || profile.equalsIgnoreCase(DEV_PROFILE)) {
                return true;
            }
        }
        return false;
    }
}