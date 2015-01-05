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
package com.clicktravel.common.security;

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.*;

import org.junit.Test;

public class ConfidentialStringTest {

    @Test
    public void shouldReturnNull_onNoArgConstructor() {
        // When
        final ConfidentialString s = new ConfidentialString();

        // Then
        assertNotNull(s);
        assertNull(s.getString());
    }

    @Test
    public void shouldReturnNull_onConstructorWithNullStringArg() {
        // Given
        final String string = null;

        // When
        final ConfidentialString s = new ConfidentialString(string);

        // Then
        assertNotNull(s);
        assertNull(s.getString());
    }

    @Test
    public void shouldReturnString_onConstructorWithStringArg() {
        // Given
        final String string = randomString();

        // When
        final ConfidentialString s = new ConfidentialString(string);

        // Then
        assertNotNull(s);
        assertEquals(string, s.getString());
    }

    @Test
    public void shouldNotContainStringContents_onToString() {
        // Given
        final String string = randomString();
        final ConfidentialString s = new ConfidentialString(string);

        // When
        final String toString = s.toString();

        // Then
        assertFalse(toString.contains(string));
    }

    @Test
    public void shouldBeEqual_withSameStringContent() {
        // Given
        final String string1 = randomString();
        final String string2 = new String(string1); // new string with same content
        final ConfidentialString s1 = new ConfidentialString(string1);
        final ConfidentialString s2 = new ConfidentialString(string2);

        // When
        final boolean isEquals = s1.equals(s2);

        // Then
        assertTrue("ConfidentialStrings with same content should be equal", isEquals);
    }

    @Test
    public void shouldHaveEqualHash_withSameStringContent() {
        // Given
        final String string1 = randomString();
        final String string2 = new String(string1); // new string with same content
        final int s1HashCode = new ConfidentialString(string1).hashCode();
        final int s2HashCode = new ConfidentialString(string2).hashCode();

        // When
        final boolean isHashEquals = s1HashCode == s2HashCode;

        // Then
        assertTrue("ConfidentialStrings with same content should have equal hashCode", isHashEquals);
    }

    @Test
    public void shouldNotBeEqual_withDifferentStringContent() {
        // Given
        final String string1 = randomString();
        final String string2 = randomString(1 + string1.length()); // different length to string1
        final ConfidentialString s1 = new ConfidentialString(string1);
        final ConfidentialString s2 = new ConfidentialString(string2);

        // When
        final boolean isEquals = s1.equals(s2);

        // Then
        assertFalse("ConfidentialStrings with different content should not be equal", isEquals);
    }

}
