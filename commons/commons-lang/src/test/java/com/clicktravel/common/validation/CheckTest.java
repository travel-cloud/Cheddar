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

import static com.clicktravel.common.random.Randoms.randomInt;
import static com.clicktravel.common.random.Randoms.randomPhoneNumber;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import com.clicktravel.common.random.Randoms;

public class CheckTest {

    private String field;

    @Before
    public void setUp() {
        field = Randoms.randomString(10);
    }

    @Test
    public void shouldThrowValidationException_givenNullValue() {
        // Given
        final String value = null;

        // When
        ValidationException actualException = null;
        try {
            Check.isNotEmptyOrNull(field, value);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertThat(actualException.getFields()[0], is(field));
    }

    @Test
    public void shouldThrowValidationException_givenEmptyValue() {
        // Given
        final String value = " \t ";

        // When
        ValidationException actualException = null;
        try {
            Check.isNotEmptyOrNull(field, value);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertThat(actualException.getFields()[0], is(field));
    }

    @Test
    public void shouldNotThrowValidationException_givenNonEmptyValue() {
        // Given
        final String value = Randoms.randomString(10);

        ValidationException actualException = null;
        try {
            Check.isNotEmptyOrNull(field, value);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
    }

    @Test
    public void shouldThrowValidationException_givenNullCollection() {
        // Given
        final Collection<Object> value = null;

        // When
        ValidationException actualException = null;
        try {
            Check.isNotEmptyOrNull(field, value);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertThat(actualException.getFields()[0], is(field));
    }

    @Test
    public void shouldThrowValidationException_givenEmptyCollection() {
        // Given
        final Collection<Object> value = Collections.emptyList();

        // When
        ValidationException actualException = null;
        try {
            Check.isNotEmptyOrNull(field, value);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertThat(actualException.getFields()[0], is(field));
    }

    @Test
    public void shouldNotThrowValidationException_givenNonEmptyCollection() {
        // Given
        final Collection<Object> value = Collections.singletonList(new Object());

        ValidationException actualException = null;
        try {
            Check.isNotEmptyOrNull(field, value);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
    }

    @Test
    public void shouldThrowValidationException_givenNullObject() {
        // Given
        final Object value = null;

        // When
        ValidationException actualException = null;
        try {
            Check.isNotNull(field, value);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertThat(actualException.getFields()[0], is(field));
    }

    @Test
    public void shouldNotThrowValidationException_givenObject() {
        // Given
        final Object value = new Object();

        // When
        ValidationException actualException = null;
        try {
            Check.isNotNull(field, value);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
    }

    @Test
    public void shouldThrowValidationException_givenNullEmail() {
        // Given
        final String email = null;

        // When
        ValidationException actualException = null;
        try {
            Check.isValidEmail(field, email);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertThat(actualException.getFields()[0], is(field));
    }

    @Test
    public void shouldThrowValidationException_givenEmptyEmail() {
        // Given
        final String email = "";

        // When
        ValidationException actualException = null;
        try {
            Check.isValidEmail(field, email);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertThat(actualException.getFields()[0], is(field));
    }

    @Test
    public void shouldValidateEmailAddress_withValidEmailAddresses() {
        // Given
        final List<String> validEmails = new ArrayList<>();
        validEmails.add("prettyandsimple@example.com");
        validEmails.add("very.common@example.com");
        validEmails.add("disposable.style.email.with+symbol@example.com");
        validEmails.add("other.email-with-dash@example.com");
        validEmails.add("x@example.com");
        validEmails.add("example-indeed@strange-example.com");
        validEmails.add("#!$%&'*+-/=?^_`{}|~@example.org");
        validEmails.add("example@s.solutions");
        validEmails.add("Example@s.solutions");

        // When
        final List<String> invalidatedEmails = new ArrayList<>();
        for (final String email : validEmails) {
            try {
                Check.isValidEmail(field, email);
            } catch (final ValidationException e) {
                invalidatedEmails.add(email);
            }
        }

        // Then
        if (!invalidatedEmails.isEmpty()) {
            fail("Expected to validate the following email addresses, but were actually invalidated: "
                    + invalidatedEmails);
        }
    }

    @Test
    public void shouldNotValidateEmailAddress_withInvalidEmailAddresses() {
        // Given
        final List<String> invalidEmails = new ArrayList<>();
        invalidEmails.add("Abc.example.com");
        invalidEmails.add("A@b@c@example.com");
        invalidEmails.add("a\"b(c)d,e:f;g<h>i[j\\k]l@example.com");
        invalidEmails.add("just\"not\"right@example.com");
        invalidEmails.add("this is\"not\\allowed@example.com");
        invalidEmails.add("this\\ still\\\"not\\allowed@example.com");
        invalidEmails.add("john.doe@example..com");
        invalidEmails.add("Modapat@1");

        // When
        final List<String> validatedEmails = new ArrayList<>();
        for (final String email : invalidEmails) {
            try {
                Check.isValidEmail(field, email);
                validatedEmails.add(email);
            } catch (final ValidationException e) {
                // expected to throw an exception for all emails
            }
        }

        // Then
        if (!validatedEmails.isEmpty()) {
            fail("Expected to invalidate the following email addresses, but were actually validated: "
                    + validatedEmails);
        }
    }

    @Test
    public void shouldThrowValidationException_givenNullContainingString() {
        // Given
        final String string = null;
        final CharSequence charSequence = Randoms.randomString(15);

        // When
        ValidationException actualException = null;
        try {
            Check.contains(field, string, charSequence);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertThat(actualException.getFields()[0], is(field));
    }

    @Test
    public void shouldThrowValidationException_givenNullCharSequence() {
        // Given
        final String string = Randoms.randomString(15);
        final CharSequence charSequence = null;

        // When
        ValidationException actualException = null;
        try {
            Check.contains(field, string, charSequence);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertThat(actualException.getFields()[0], is(field));
    }

    @Test
    public void shouldNotThrowValidationException_givenEmptyCharSequence() {
        // Given
        final String string = Randoms.randomString(15);
        final CharSequence charSequence = "";

        // When
        ValidationException actualException = null;
        try {
            Check.contains(field, string, charSequence);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
    }

    @Test
    public void shouldThrowValidationException_givenCharSequenceNotInString() {
        // Given
        final String string = Randoms.randomString(50);
        final CharSequence charSequence = "------------------------------";

        // When
        ValidationException actualException = null;
        try {
            Check.contains(field, string, charSequence);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertThat(actualException.getFields()[0], is(field));
    }

    @Test
    public void shouldNotThrowValidationException_givenCharSequenceInString() {
        // Given
        final String string = Randoms.randomString(50);
        final int beginIndex = Randoms.randomInt(15);
        final int endIndex = 50 - Randoms.randomInt(15);
        final CharSequence charSequence = string.subSequence(beginIndex, endIndex);

        // When
        ValidationException actualException = null;
        try {
            Check.contains(field, string, charSequence);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
    }

    @Test
    public void shouldThrowValidationException_withInvalidLengthArgument() {
        // Given
        final String value = Randoms.randomString(10);
        final int length = -1 - Randoms.randomInt(10);

        // When
        ValidationException actualException = null;
        try {
            Check.isLength(field, value, length);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertThat(actualException.getFields()[0], is(field));
    }

    @Test
    public void shouldThrowValidationException_withIncorrectLength() {
        // Given
        final String value = Randoms.randomString(10);
        final int length = Randoms.randomInt(8);

        // When
        ValidationException actualException = null;
        try {
            Check.isLength(field, value, length);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertThat(actualException.getFields()[0], is(field));
    }

    @Test
    public void shouldThrowValidationException_withNullStringForLengthValidation() {
        // Given
        final String value = null;
        final int length = Randoms.randomInt(20);

        // When
        ValidationException actualException = null;
        try {
            Check.isLength(field, value, length);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertThat(actualException.getFields()[0], is(field));
    }

    @Test
    public void shouldNotThrowValidationException_withCorrectStringLength() {
        // Given
        final int length = Randoms.randomInt(20);
        final String value = Randoms.randomString(length);

        // When
        ValidationException actualException = null;
        try {
            Check.isLength(field, value, length);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
    }

    @Test
    public void shouldNotThrowValidationException_withStringValueThatMatchesPattern() {
        // Given
        final String value = Randoms.randomString();
        final Pattern pattern = Pattern.compile(Pattern.quote(value));

        // When
        ValidationException actualException = null;
        try {
            Check.matchesPattern(field, value, pattern);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
    }

    @Test
    public void shouldThrowValidationException_withNullStringValueForPatternMatch() {
        // Given
        final Pattern pattern = Pattern.compile(Pattern.quote(Randoms.randomString()));
        final String value = null;

        // When
        ValidationException actualException = null;
        try {
            Check.matchesPattern(field, value, pattern);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertThat(actualException.getFields()[0], is(field));
    }

    @Test
    public void shouldThrowValidationException_withStringValueThatDoesNotMatchPattern() {
        // Given
        final String value = Randoms.randomString();
        final Pattern pattern = Pattern.compile("EXTRA" + Pattern.quote(value));

        // When
        ValidationException actualException = null;
        try {
            Check.matchesPattern(field, value, pattern);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertThat(actualException.getFields()[0], is(field));
    }

    @Test
    public void shouldCheckPhoneNumber_withValidPhoneNumber() throws Exception {
        // Given
        final String phoneNumber = randomPhoneNumber();

        // When
        ValidationException actualException = null;
        try {
            Check.isValidPhoneNumber("phoneNumber", phoneNumber);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
    }

    @Test
    public void shouldCheckPhoneNumber_withPhoneNumberWithoutLeadingPlusSign() throws Exception {
        // Given
        final String phoneNumber = "020 02020202";

        // When
        ValidationException actualException = null;
        try {
            Check.isValidPhoneNumber("phoneNumber", phoneNumber);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
    }

    @Test
    public void shouldCheckPhoneNumber_withNullPhoneNumber() throws Exception {
        // Given
        final String phoneNumber = null;

        // When
        ValidationException actualException = null;
        try {
            Check.isValidPhoneNumber("phoneNumber", phoneNumber);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertEquals(1, actualException.getFields().length);
        assertEquals("phoneNumber", actualException.getFields()[0]);
    }

    @Test
    public void shouldCheckPhoneNumber_withAlphabeticalPhoneNumber() throws Exception {
        // Given
        final String phoneNumber = "a" + randomString(10);

        // When
        ValidationException actualException = null;
        try {
            Check.isValidPhoneNumber("phoneNumber", phoneNumber);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertEquals(1, actualException.getFields().length);
        assertEquals("phoneNumber", actualException.getFields()[0]);
    }

    @Test
    public void shouldCheckPhoneNumber_withLongPhoneNumber() throws Exception {
        // Given
        final StringBuilder sb = new StringBuilder();
        while (sb.length() < 20) {
            sb.append(randomInt(Integer.MAX_VALUE));
        }
        final String phoneNumber = "+" + sb.toString();

        // When
        ValidationException actualException = null;
        try {
            Check.isValidPhoneNumber("phoneNumber", phoneNumber);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertEquals(1, actualException.getFields().length);
        assertEquals("phoneNumber", actualException.getFields()[0]);
    }

}
