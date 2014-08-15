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
package com.clicktravel.cheddar.infrastructure.persistence.database.exception;

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class NonExistentItemExceptionTest {

    @Test
    public void shouldConstructExceptionAndMessage_withMessage() {
        // Given
        final String message = randomString();

        // When
        final NonExistentItemException nonExistentItemException = new NonExistentItemException(message);

        // Then
        assertNotNull(nonExistentItemException);
        assertNotNull(nonExistentItemException.getMessage());
        assertEquals(message, nonExistentItemException.getMessage());
    }

}
