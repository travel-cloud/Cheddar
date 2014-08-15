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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.clicktravel.common.functional.Equals;
import com.clicktravel.common.random.Randoms;

public class EqualsTest {

    @Test
    public void shouldBeSafeEquals_withObjectsThatAreEqual() {
        final String obj1 = Randoms.randomString(10);
        final String obj2 = new String(obj1);
        assertTrue(Equals.safeEquals(obj1, obj2));
    }

    @Test
    public void shouldBeSafeEquals_withNullObjects() {
        assertTrue(Equals.safeEquals(null, null));
    }

    @Test
    public void shouldNotBeSafeEquals_withOneNullObject() {
        final String obj1 = Randoms.randomString(10);
        assertFalse(Equals.safeEquals(obj1, null));
        assertFalse(Equals.safeEquals(null, obj1));
    }

    @Test
    public void shouldNotBeSafeEquals_withObjectsThatAreNotEqual() {
        final String string1 = Randoms.randomString(5);
        final String string2 = Randoms.randomString(10);
        final Integer int1 = Randoms.randomInt(10);
        assertFalse(Equals.safeEquals(string1, string2));
        assertFalse(Equals.safeEquals(int1, string1));
    }

    @Test
    public void shouldBeBlank_withNullOrWhitespaceString() {
        assertTrue(Equals.isNullOrBlank(null));
        assertTrue(Equals.isNullOrBlank("  "));
        assertTrue(Equals.isNullOrBlank("\t"));
    }

    @Test
    public void shouldNotBeBlank_withString() {
        assertFalse(Equals.isNullOrBlank(Randoms.randomString(5)));
    }

    @Test
    public void shouldBeSameString_withWhitespaceAdded() {
        final String s = Randoms.randomString(10);
        final String s1 = "  " + s;
        final String s2 = s + "\t\t";
        assertTrue(Equals.safeTrimmedStringEquals(s1, s2));
    }

    @Test
    public void shouldBeSameString_withNullOrBlankStrings() {
        assertTrue(Equals.safeTrimmedStringEquals(null, null));
        assertTrue(Equals.safeTrimmedStringEquals(" ", null));
        assertTrue(Equals.safeTrimmedStringEquals(null, "\t"));
        assertTrue(Equals.safeTrimmedStringEquals("  ", "\n\n"));
    }

    @Test
    public void shouldNotBeSameString_withDifferentStrings() {
        final String s1 = Randoms.randomString(5);
        final String s2 = Randoms.randomString(8);
        assertFalse(Equals.safeTrimmedStringEquals(s1, s2));
    }
}
