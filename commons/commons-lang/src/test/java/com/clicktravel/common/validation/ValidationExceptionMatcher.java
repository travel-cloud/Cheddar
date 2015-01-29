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

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.clicktravel.common.functional.Equals;

public class ValidationExceptionMatcher extends TypeSafeMatcher<ValidationException> {

    private final String expectedErrorMessage;
    private final String expectedField;
    private String foundCheckErrorMessage = null;
    private String foundField = null;

    public ValidationExceptionMatcher(final String expectedErrorMessage, final String expectedField) {
        this.expectedErrorMessage = expectedErrorMessage;
        this.expectedField = expectedField;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("Expected: [errorMessage=").appendValue(expectedErrorMessage)
                .appendText(", field property=").appendValue(expectedField).appendText("], but found: [errorMessage=")
                .appendValue(foundCheckErrorMessage).appendText(", field property=").appendValue(foundField)
                .appendText("]");
    }

    @Override
    protected boolean matchesSafely(final ValidationException validationException) {
        foundCheckErrorMessage = validationException.getMessage();
        foundField = validationException.getFields()[0];
        return foundCheckErrorMessage.contains(expectedErrorMessage) && Equals.safeEquals(expectedField, foundField);
    }

}