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

public class Equals {

    /**
     * For two objects, return <code>true</code> if they are equal. Two null objects are considered equal
     * @param obj1 First object (may be null)
     * @param obj2 Second object (may be null)
     * @return <code>true</true> if objects are equal
     */
    public static boolean safeEquals(final Object obj1, final Object obj2) {
        return obj1 == null ? obj2 == null : obj1.equals(obj2);
    }

    /**
     * Return <code>true</code> if a string is null, empty or contains only whitespace characters
     * @param s String for testing
     * @return <code>true</code> if string is null or blank
     */
    public static boolean isNullOrBlank(final String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * For two strings, return <code>true</code> if they are the same after trimming. All <code>null</code> and blank
     * strings are considered equal
     * @param s1 First string
     * @param s2 Second string
     * @return <code>true</code> if both strings are considered the same
     */
    public static boolean safeTrimmedStringEquals(final String s1, final String s2) {
        final String s1Trimmed = s1 == null ? "" : s1.trim();
        final String s2Trimmed = s2 == null ? "" : s2.trim();
        return s1Trimmed.equals(s2Trimmed);
    }
}
