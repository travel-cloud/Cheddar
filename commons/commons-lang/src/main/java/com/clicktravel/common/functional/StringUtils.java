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
import java.util.regex.Pattern;

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
     * @return Enum constant, or {@code null} if input string is blank
     */
    public static <E extends Enum<E>> E parseEnum(final String s, final Class<E> enumClass) {
        final String t = s.trim().toUpperCase().replace(' ', '_');
        if (t.isEmpty()) {
            return null;
        }
        final E value = E.valueOf(enumClass, t);
        return value;
    }

    /**
     * Joins a list of substrings with a join between each element
     * @param substrings List of substrings
     * @param join Join to place between each element
     * @return Joined string
     */
    public static String join(final List<String> substrings, final String join) {
        final StringBuilder sb = new StringBuilder();
        for (final String substring : substrings) {
            if (sb.length() != 0) {
                sb.append(join);
            }
            sb.append(substring);
        }
        return sb.toString();
    }

    /**
     * Joins a list of substring with commas
     * @param substrings List of substrings
     * @return Joined string
     */
    public static String join(final List<String> substrings) {
        return join(substrings, ", ");
    }

    /**
     * Return {@code true} if string contains a specified regex pattern
     * @param string String to test
     * @param pattern Regex pattern to find in string
     * @return {@code true} if string contains regex pattern
     */
    public static boolean containsPattern(final String string, final String pattern) {
        return containsPattern(string, Pattern.compile(pattern));
    }

    /**
     * Return {@code true} if string contains a specified {@link Pattern}
     * @param string String to test
     * @param pattern Pattern to find in string
     * @return {@code true} if string contains pattern
     */
    public static boolean containsPattern(final String string, final Pattern pattern) {
        return pattern.matcher(string).find();
    }

}
