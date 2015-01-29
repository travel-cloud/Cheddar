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

import java.util.ArrayList;
import java.util.List;

public class StringUtils {

    /**
     * Replace a substring and return the resulting string
     * @param s Input string
     * @param startIndex Index of starting character of substring
     * @param endIndex Index of character following substring
     * @param replacement Replacement substring
     * @return The resulting string with the substring replaced
     */
    public static String replaceSubstring(final String s, final int startIndex, final int endIndex,
            final String replacement) {
        final StringBuilder sb = new StringBuilder();
        sb.append(s.substring(0, startIndex));
        sb.append(replacement);
        sb.append(s.substring(endIndex));
        return sb.toString();
    }

    /**
     * Parses a comma separated list of enum names
     * @param s The string to scan
     * @param enumClass The class of enum constants
     * @return List of found enum constants
     */
    public static <E extends Enum<E>> List<E> parseListEnums(final String s, final Class<E> enumClass) {
        final List<E> enums = new ArrayList<>();
        final String[] substrings = s.trim().split(",");
        for (final String substring : substrings) {
            final E value = parseEnum(substring, enumClass);
            if (value == null) {
                throw new IllegalArgumentException(
                        "Unable to parse list element, expected enum constant name but actual was [" + substring + "]");
            }
            enums.add(value);
        }
        return enums;
    }

    /**
     * Parses a single enum name from a string. Case is ignored, spaces are changed to '_'
     * @param s The string to scan
     * @param enumClass The class of enum constants
     * @return Enum constant, or <code>null</code> if input string is blank
     */
    public static <E extends Enum<E>> E parseEnum(final String s, final Class<E> enumClass) {
        final String t = s.trim().toUpperCase().replace(' ', '_');
        if (t.isEmpty()) {
            return null;
        }
        final E value = E.valueOf(enumClass, t);
        return value;
    }
}
