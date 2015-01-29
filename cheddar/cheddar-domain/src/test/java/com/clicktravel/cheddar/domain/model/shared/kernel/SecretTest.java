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
package com.clicktravel.cheddar.domain.model.shared.kernel;

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.clicktravel.common.random.Randoms;

public class SecretTest {

    @Test
    public void shouldCreateSecret_withValue() {
        // Given
        final String secretValue = randomString(128);

        // When
        final Secret secret = new Secret(secretValue);

        // Then
        assertNotNull(secret);
        assertEquals(secretValue, secret.value());
    }

    @Test
    public void shouldBeEqual_withSameSecretValue() {

        // Given
        final String secretValue = Randoms.randomString(5);

        final Secret secret1 = new Secret(secretValue);
        final Secret secret2 = new Secret(secretValue);

        // When
        final boolean equals1 = secret1.equals(secret2);
        final boolean equals2 = secret2.equals(secret1);

        // Then
        assertTrue(equals1);
        assertTrue(equals2);
    }

    @Test
    public void shouldReturnSameHashcode_withSameSecretValue() {

        // Given
        final String secretValue = Randoms.randomString(5);

        final Secret secret1 = new Secret(secretValue);
        final Secret secret2 = new Secret(secretValue);

        // When
        final int hashcode1 = secret1.hashCode();
        final int hashcode2 = secret2.hashCode();

        // Then
        assertEquals(hashcode1, hashcode2);

    }

}
