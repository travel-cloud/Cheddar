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

    private final ComparisonOperator comparisonOperator;
    private final Set<String> values;

    public Condition(final ComparisonOperator comparisonOperator, final Set<String> values) {
        this.comparisonOperator = comparisonOperator;
        this.values = new HashSet<>();
        setValues(values);
    }

    public Condition(final ComparisonOperator comparisonOperator, final String value) {
        this.comparisonOperator = comparisonOperator;
        values = new HashSet<>();
        setValue(value);
    }

    public Condition(final ComparisonOperator comparisonOperator) {
        this.comparisonOperator = comparisonOperator;
        values = new HashSet<>();
    }

    public ComparisonOperator getComparisonOperator() {
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