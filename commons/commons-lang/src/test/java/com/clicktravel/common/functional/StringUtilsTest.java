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
package com.clicktravel.common.functional;

import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void shouldContainPattern_withStringAndRegex() {
        // Given
        final String string = "abcXYZdef";
        final String regex = "X?Z";

        // When
        final boolean containsPattern = StringUtils.containsPattern(string, regex);

        // Then
        assertTrue(containsPattern);
    }

    @Test
    public void shouldContainPattern_withStringAndPattern() {
        // Given
        final String string = "abcXYZdef";
        final Pattern pattern = Pattern.compile("[A-Z]{3}");

        // When
        final boolean containsPattern = StringUtils.containsPattern(string, pattern);

        // Then
        assertTrue(containsPattern);
    }
}
