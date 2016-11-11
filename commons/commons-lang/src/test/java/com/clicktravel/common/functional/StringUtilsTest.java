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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void shouldReplaceSubstring_withStringStartIndexEndIndexAndReplacment() {
        // Given
        final String s = "leftMIDDLEright";
        final int startIndex = 4;
        final int endIndex = 10;
        final String replacement = "CENTRE";

        // When
        final String result = StringUtils.replaceSubstring(s, startIndex, endIndex, replacement);

        // Then
        assertEquals("leftCENTREright", result);
    }

    @Test
    public void shouldParseListEnums_withStringAndEnumClass() {
        // Given
        final String s = "Value One,  value TWO ";
        final Class<TestEnum> enumClass = TestEnum.class;

        // When
        final List<TestEnum> result = StringUtils.parseListEnums(s, enumClass);

        // Then
        assertEquals(Arrays.asList(TestEnum.VALUE_ONE, TestEnum.VALUE_TWO), result);
    }

    @Test
    public void shouldNotParseListEnums_withStringContainingBlankElement() {
        // Given
        final String s = "Value One, ,Value One";
        final Class<TestEnum> enumClass = TestEnum.class;

        // When
        IllegalArgumentException thrownException = null;
        try {
            StringUtils.parseListEnums(s, enumClass);
        } catch (final IllegalArgumentException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldParseEnum_withStringAndEnumClass() {
        // Given
        final String s = "value TWO";
        final Class<TestEnum> enumClass = TestEnum.class;

        // When
        final TestEnum result = StringUtils.parseEnum(s, enumClass);

        // Then
        assertEquals(TestEnum.VALUE_TWO, result);
    }

    @Test
    public void shouldParseEnumAsNull_withBlankString() {
        // Given
        final String s = "   ";
        final Class<TestEnum> enumClass = TestEnum.class;

        // When
        final TestEnum result = StringUtils.parseEnum(s, enumClass);

        // Then
        assertNull(result);
    }

    @Test
    public void shouldReturnEnumConstantFromValue_withStringValue() {
        // Given
        final String s = "Second";
        final Class<TestEnum> enumClass = TestEnum.class;

        // When
        final TestEnum result = StringUtils.fromValue(enumClass, s);

        // Then
        assertEquals(TestEnum.VALUE_TWO, result);
    }

    @Test
    public void shouldNotReturnEnumConstantFromValue_withUnknownStringValue() {
        // Given
        final String s = "unknown";
        final Class<TestEnum> enumClass = TestEnum.class;

        // When
        IllegalArgumentException thrownException = null;
        try {
            StringUtils.fromValue(enumClass, s);
        } catch (final IllegalArgumentException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

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

    private enum TestEnum {

        VALUE_ONE("number one"),
        VALUE_TWO("Second");

        private final String value;

        TestEnum(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
