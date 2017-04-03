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
package com.clicktravel.common.logging;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class LoggingFilter {

    private final Set<String> matchers;
    private final Optional<Function<String, String>> matchingFunction;
    private final String mask;

    private LoggingFilter(final Set<String> matchers, final Optional<Function<String, String>> matchingFunction,
            final String mask) {
        this.matchers = matchers;
        this.matchingFunction = matchingFunction;
        this.mask = mask;
    }

    public Set<String> matchers() {
        return Collections.unmodifiableSet(matchers);
    }

    public Optional<Function<String, String>> matchingFunction() {
        return matchingFunction;
    }

    public String mask() {
        return mask;
    }

    public class Builder {
        private static final String DEFAULT_MASK = "****";

        private final Set<String> matchers = new HashSet<>();
        private Function<String, String> matchingFunction;
        private String mask = DEFAULT_MASK;

        public Builder addMatcher(final String matcher) {
            if (matcher == null || matcher.isEmpty()) {
                throw new IllegalArgumentException("The matcher value should not be null or empty.");
            }
            matchers.add(matcher);
            return this;
        }

        public Builder withMatchingFunction(final Function<String, String> matchingFunction) {
            if (matchingFunction == null) {
                throw new IllegalArgumentException(
                        "The function used for filtering sensitive information should not e null.");
            }
            this.matchingFunction = matchingFunction;
            return this;
        }

        public Builder withMask(final String mask) {
            if (mask == null || mask.isEmpty()) {
                throw new IllegalArgumentException("The mask value should not be null or empty.");
            }
            this.mask = mask;
            return this;
        }

        public LoggingFilter build() {
            return new LoggingFilter(matchers, Optional.ofNullable(matchingFunction), mask);
        }
    }

}
