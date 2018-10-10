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
package com.clicktravel.cheddar.server.application.configuration;

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ApplicationConfigurationTest {

    @Test
    public void shouldCreateApplicationConfiguration_withNameAndVersionandFrameworkVersion() throws Exception {
        // Given
        final String name = randomString(10);
        final String version = randomString(10);
        final String frameworkVersion = randomString(10);

        // When
        final ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(name, version,
                frameworkVersion);

        // Then
        assertNotNull(applicationConfiguration);
        assertEquals(name, applicationConfiguration.name());
        assertEquals(version, applicationConfiguration.version());
        assertEquals(frameworkVersion, applicationConfiguration.frameworkVersion());
    }

}
