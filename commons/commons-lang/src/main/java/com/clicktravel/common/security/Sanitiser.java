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

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public class Sanitiser {

    private static final String[] SUSPECT_START_CHARACTERS = { "=", "+", "-", "@", "0x09", "0x0D" };
    // Allows any English characters (case-insensitive) and also commas, full stops, apostrophes and hyphens
    // to support names like, "Test Tester, Jr", "Test Tester Jr.", "Test O'Tester" and "Test-Bob Tester".
    private static final Pattern FULL_NAME_REGEX = Pattern.compile("^[a-z ,.'-]+$", Pattern.CASE_INSENSITIVE);

    public static String sanitiseValue(final String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        String sanitisedString = value.trim();

        // Remove starting characters as per https://owasp.org/www-community/attacks/CSV_Injection
        while (StringUtils.startsWithAny(sanitisedString, SUSPECT_START_CHARACTERS)) {
            sanitisedString = sanitisedString.substring(1);
        }

        if (!FULL_NAME_REGEX.matcher(sanitisedString).matches()) {
            // Use OWASP Java HTML Sanitiser to sanitise HTML/JS input. This policy
            // allows no exceptions.
            final PolicyFactory policyFactory = new HtmlPolicyBuilder().toFactory();
            sanitisedString = policyFactory.sanitize(sanitisedString);
        }

        return sanitisedString;
    }
}
