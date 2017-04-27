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

import java.util.HashSet;
import java.util.Set;

public class Condition {

    private final Operators comparisonOperator;
    private final Set<String> values;

    public Condition(final Operators comparisonOperator, final Set<String> values) {
        this.comparisonOperator = comparisonOperator;
        this.values = new HashSet<>();
        setValues(values);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (comparisonOperator == null ? 0 : comparisonOperator.hashCode());
        result = prime * result + (values == null ? 0 : values.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Condition other = (Condition) obj;
        if (comparisonOperator == null) {
            if (other.comparisonOperator != null) {
                return false;
            }
        } else if (!comparisonOperator.equals(other.comparisonOperator)) {
            return false;
        }
        if (values == null) {
            if (other.values != null) {
                return false;
            }
        } else if (!values.equals(other.values)) {
            return false;
        }
        return true;
    }

    public Condition(final Operators comparisonOperator, final String value) {
        this.comparisonOperator = comparisonOperator;
        values = new HashSet<>();
        setValue(value);
    }

    public Condition(final Operators comparisonOperator) {
        this.comparisonOperator = comparisonOperator;
        values = new HashSet<>();
    }

    public boolean hasMissingComparisonValues() {
        if (comparisonOperator == null) {
            throw new InvalidConditionOperatorException("Comparison operator cannot be null");
        }

        switch (comparisonOperator) {
            case EQUALS:
            case LESS_THAN_OR_EQUALS:
            case GREATER_THAN_OR_EQUALS:
                return !values.stream().anyMatch(value -> value != null && !value.isEmpty());
            case NOT_NULL:
            case NULL:
            default:
                return false;
        }
    }

    public Operators getComparisonOperator() {
        return comparisonOperator;
    }

    public Set<String> getValues() {
        return values;
    }

    private void setValue(final String value) {
        values.clear();
        values.add(value);
    }

    private void setValues(final Set<String> values) {
        this.values.clear();
        this.values.addAll(values);
    }

    @Override
    public String toString() {
        return "Condition [comparisonOperator=" + comparisonOperator + ", values=" + values + "]";
    }

}