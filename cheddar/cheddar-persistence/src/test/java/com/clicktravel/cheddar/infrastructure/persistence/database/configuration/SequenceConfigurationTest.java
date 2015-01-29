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
package com.clicktravel.cheddar.infrastructure.persistence.database.configuration;

import static com.clicktravel.common.random.Randoms.randomLong;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class SequenceConfigurationTest {

    @Test
    public void shouldCreateSequenceConfiguration_withSequenceName() throws Exception {
        // Given
        final String sequenceName = randomString(10);

        // When
        final SequenceConfiguration sequenceConfiguration = new SequenceConfiguration(sequenceName);

        // Then
        assertNotNull(sequenceConfiguration);
        assertEquals(sequenceName, sequenceConfiguration.sequenceName());
    }

    @Test
    public void shouldNotCreateSequenceConfiguration_withEmptySequenceName() throws Exception {
        // Given
        final String sequenceName = "";

        // When
        IllegalArgumentException actualException = null;
        try {
            new SequenceConfiguration(sequenceName);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotCreateSequenceConfiguration_withNullSequenceName() throws Exception {
        // Given
        final String sequenceName = null;

        // When
        IllegalArgumentException actualException = null;
        try {
            new SequenceConfiguration(sequenceName);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCreateSequenceConfiguration_withSequenceNameAndStartingValue() throws Exception {
        // Given
        final String sequenceName = randomString(10);
        final long startingValue = randomLong();

        // When
        final SequenceConfiguration sequenceConfiguration = new SequenceConfiguration(sequenceName, startingValue);

        // Then
        assertNotNull(sequenceConfiguration);
        assertEquals(sequenceName, sequenceConfiguration.sequenceName());
        assertEquals(startingValue, sequenceConfiguration.startingValue());
    }

}
