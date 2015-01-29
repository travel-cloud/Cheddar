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
package com.clicktravel.infrastructure.persistence.aws.cloudsearch.client;

import static com.clicktravel.common.random.Randoms.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.junit.Test;

public class FieldTest {

    @Test
    public void shouldCreateField_withNameAndValue() throws Exception {
        // Given
        final String name = randomString(10);
        final Object value = Arrays.asList(randomString(), randomLocalDate(), randomLong(), randomBoolean(),
                randomBigDecimal(randomInt(1000), 3));

        // When
        final Field field = new Field(name, value);

        // Then
        assertNotNull(field);
        assertEquals(name, field.getName());
        assertEquals(value, field.getValue());
    }

}
