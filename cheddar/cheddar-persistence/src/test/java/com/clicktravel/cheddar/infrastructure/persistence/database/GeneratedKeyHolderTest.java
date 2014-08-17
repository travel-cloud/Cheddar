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

import static com.clicktravel.common.random.Randoms.randomLong;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

public class GeneratedKeyHolderTest {

    @Test
    public void shouldCreateGeneratedKeyHolder_withKeys() {
        // Given
        final Collection<Long> keys = Arrays.asList(randomLong(), randomLong(), randomLong());

        // When
        final GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder(keys);

        // Then
        assertEquals(keys, generatedKeyHolder.keys());
    }

}
