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
package com.clicktravel.common.validation;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Simple value validation methods, intended for basic checks on field values of requests on a service API.
 *
 * Each check method will throw a ValidationException if the check fails.
 */
public class Check {

    private final static String NULL_OR_EMPTY = "Value is NULL or empty";
    private final static String INVALID_EMAIL_ADDRESS = "Value is an invalid email";
    private final static String INVALID_PHONE_NUMBER = "Value is an invalid phone number";
    private final static String DOES_NOT_CONTAIN = "Value does not contain the CharSequence ";
    private final static String INVALID_LENGTH = "Value does not have the correct length of ";
    private final static String INVALID_LENGTH_ARG = "Length argument is invalid ";
    private final static String IS_NOT_BETWEEN = "Value is not between the allowed minimum & maximum values";
    private final static String DOES_NOT_MATCH_PATTERN = "Value does not match pattern";
    private final static Pattern PHONE_NUMBER_PATTERN = Pattern.compile("\\+[\\d -]+");
    private final static Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9.!#$%&'*+\\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");

    /**
     * Check that a string value is not empty (after trimming) or <code>null</code>
     * @param field Name of field to report if check fails
     * @param value String value to check
     * @throws ValidationException if check fails
     */
    public static void isNotEmptyOrNull(final String field, final String value) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(NULL_OR_EMPTY, field);
        }
    }

    /**
     * Check that a collection is not empty (contains no elements) or <code>null</code>
     * @param field Name of field to report if check fails
     * @param value Collection to check
     * @throws ValidationException if check fails
     */
    public static void isNotEmptyOrNull(final String field, final Collection<?> value) throws ValidationException {
        if (value == null || value.isEmpty()) {
            final String valueStr = value == null ? null : value.toString();
            final String errorMessage = String.format(NULL_OR_EMPTY + " : value -> [%s]", valueStr);
            throw new ValidationException(errorMessage, field);
        }
    }

    /**
     * Check that a value object is not <code>null</code>
     * @param field Name of field to report if check fails
     * @param value Object to check
     * @throws ValidationException if check fails
     */
    public static void isNotNull(final String field, final Object value) throws ValidationException {
        if (value == null) {
            throw new ValidationException(NULL_OR_EMPTY, field);
        }
    }

    /**
     * Check that an e-mail address has correct syntax according to RFC 2822. The regular expression used to specify a
     * valid e-mail address could be found at
     * <a href="http://w3c.github.io/html/sec-forms.html#email-state-typeemail">w3c.github.io</a>
     * @param field Name of field to report if check fails
     * @param email E-mail address to check
     * @throws ValidationException if check fails
     */
    public static void isValidEmail(final String field, final String email) throws ValidationException {
        if (email == null || !EMAIL_ADDRESS_PATTERN.matcher(email).matches()) {
            final String errorMessage = String.format(INVALID_EMAIL_ADDRESS + " : value -> [%s]", email);
            throw new ValidationException(errorMessage, field);
        }
    }

    /**
     * Check that a phone number has correct syntax
     * @param field Name of field to report if check fails
     * @param phoneNumber Phone number to check
     * @throws ValidationException if check fails
     */
    public static void isValidPhoneNumber(final String field, final String phoneNumber) throws ValidationException {
        if (phoneNumber == null || !PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches()
                || phoneNumber.replaceAll("[^0-9]", "").length() > 15) {
            final String errorMessage = String.format(INVALID_PHONE_NUMBER + " : value -> [%s]", phoneNumber);
            throw new ValidationException(errorMessage, field);
        }
    }

    /**
     * Check that a string contains a subsequence of characters
     * @param field Name of field to report if check fails
     * @param value Containing string to check
     * @param expectedSubsequence Expected subsequence of characters
     * @throws ValidationException if check fails
     */
    public static void contains(final String field, final String value, final CharSequence expectedSubsequence)
            throws ValidationException {
        if (value == null || expectedSubsequence == null || !value.contains(expectedSubsequence)) {
            final String errorMessage = String.format(DOES_NOT_CONTAIN + "'%s', value -> [%s]", expectedSubsequence,
                    value);
            throw new ValidationException(errorMessage, field);
        }
    }

    /**
     * Check that a string value has an expected length
     * @param field Name of field to report if check fails
     * @param string String value to check
     * @param expectedLength Expected length of string value
     * @throws ValidationException if check fails
     */
    public static void isLength(final String field, final String string, final int expectedLength)
            throws ValidationException {
        if (expectedLength > -1) {
            if (string == null || string.length() != expectedLength) {
                final String errorMessage = String.format(INVALID_LENGTH + "[%s], string -> [%s]", expectedLength,
                        string);
                throw new ValidationException(errorMessage, field);
            }
        } else {
            final String errorMessage = String.format(INVALID_LENGTH_ARG + "[%s], string -> [%s]", expectedLength,
                    string);
            throw new ValidationException(errorMessage, field);
        }
    }

    public static void isBetween(final String field, final int value, final int minValue, final int maxValue)
            throws ValidationException {
        if (value < minValue || value > maxValue) {
            final String errorMessage = String.format(
                    IS_NOT_BETWEEN + ", allowed minValue -> [%s], allowed maxValue -> [%s], actual value -> [%s]",
                    minValue, maxValue, value);
            throw new ValidationException(errorMessage, field);
        }
    }

    /**
     * Check that a string matches a regular expression
     * @param field Name of field to report if check fails
     * @param value String value to check. If <code>null</code> the check fails
     * @param pattern Regular expression pattern that entire string is expected to match
     * @throws ValidationException if check fails
     */
    public static void matchesPattern(final String field, final String value, final Pattern pattern)
            throws ValidationException {
        if (value == null || !pattern.matcher(value).matches()) {
            final String errorMessage = String.format(
                    DOES_NOT_MATCH_PATTERN + ", value -> [%s], expected pattern -> [%s]", value, pattern.toString());
            throw new ValidationException(errorMessage, field);
        }
    }

}
