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
package com.clicktravel.cheddar.infrastructure.persistence.database;

import static com.clicktravel.common.random.Randoms.randomInt;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class SequenceKeyGeneratorTest {

    @Test
    public void shouldCreateSequenceKeyGenerator_withSequenceName() throws Exception {
        // Given
        final String sequenceName = randomString(10);

        // When
        final SequenceKeyGenerator sequenceKeyGenerator = new SequenceKeyGenerator(sequenceName);

        // Then
        assertEquals(sequenceName, sequenceKeyGenerator.sequenceName());
        assertEquals(1, sequenceKeyGenerator.keyCount());
    }

    @Test
    public void shouldNotCreateSequenceKeyGenerator_withEmptySequenceName() throws Exception {
        // Given
        final String sequenceName = "";

        // When
        IllegalArgumentException actualException = null;
        try {
            new SequenceKeyGenerator(sequenceName);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotCreateSequenceKeyGenerator_withNullSequenceName() throws Exception {
        // Given
        final String sequenceName = null;

        // When
        IllegalArgumentException actualException = null;
        try {
            new SequenceKeyGenerator(sequenceName);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCreateSequenceKeyGenerator_withSequenceNameAndKeyCount() throws Exception {
        // Given
        final String sequenceName = randomString(10);
        final int keyCount = randomInt(Integer.MAX_VALUE);

        // When
        final SequenceKeyGenerator sequenceKeyGenerator = new SequenceKeyGenerator(sequenceName, keyCount);

        // Then
        assertEquals(sequenceName, sequenceKeyGenerator.sequenceName());
        assertEquals(keyCount, sequenceKeyGenerator.keyCount());
    }

    @Test
    public void shouldCreateSequenceKeyGenerator_withSequenceNameAndInvalidKeyCount() throws Exception {
        // Given
        final String sequenceName = randomString(10);
        final int keyCount = randomInt(Integer.MAX_VALUE) * -1;

        // When
        IllegalArgumentException actualException = null;
        try {
            new SequenceKeyGenerator(sequenceName, keyCount);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }
        // Then
        assertNotNull(actualException);
    }

}
