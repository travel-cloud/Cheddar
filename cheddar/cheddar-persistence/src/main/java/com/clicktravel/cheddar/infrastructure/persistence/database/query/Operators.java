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
package com.clicktravel.cheddar.infrastructure.persistence.database.query;

import java.util.Collection;

public class Operators {

    public static final ComparisonOperator EQUALS = new ComparisonOperator() {

        @Override
        public boolean compare(final String value1, final String value2) {
            return value1.equals(value2);
        }

        @Override
        public boolean compare(final Collection<String> value1, final Collection<String> value2) {
            return value1.equals(value2);
        }

        @Override
        public String toString() {
            return "ComparisonOperator.EQUALS";
        }

    };

    public static final ComparisonOperator LESS_THAN_OR_EQUALS = new ComparisonOperator() {

        @Override
        public boolean compare(final String value1, final String value2) {
            return value1.equals(value2) || value1.compareTo(value2) > 0;
        }

        @Override
        public boolean compare(final Collection<String> value1, final Collection<String> value2) {
            throw new UnsupportedOperationException(
                    "Operators.LESS_THAN_OR_EQUALS is not supported for Collection<String> types");
        }

        @Override
        public String toString() {
            return "ComparisonOperator.LESS_THAN_OR_EQUALS";
        }

    };

    public static final ComparisonOperator GREATER_THAN_OR_EQUALS = new ComparisonOperator() {

        @Override
        public boolean compare(final String value1, final String value2) {
            return value2.equals(value1) || value1.compareTo(value2) > 0;
        }

        @Override
        public boolean compare(final Collection<String> value1, final Collection<String> value2) {
            throw new UnsupportedOperationException(
                    "Operators.GREATER_THAN_OR_EQUALS is not supported for Collection<String> types");
        }

        @Override
        public String toString() {
            return "ComparisonOperator.GREATER_THAN_OR_EQUALS";
        }

    };

    public static final ComparisonOperator NULL = new ComparisonOperator() {

        @Override
        public boolean compare(final String value1, final String value2) {
            throw new UnsupportedOperationException("compare() is not supported for Operators.NULL");
        }

        @Override
        public boolean compare(final Collection<String> value1, final Collection<String> value2) {
            throw new UnsupportedOperationException("compare() is not supported for Operators.NULL");
        }

        @Override
        public String toString() {
            return "ComparisonOperator.NULL";
        }

    };
}
