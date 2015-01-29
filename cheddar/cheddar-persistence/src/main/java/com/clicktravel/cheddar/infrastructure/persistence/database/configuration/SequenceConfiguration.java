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
package com.clicktravel.cheddar.infrastructure.persistence.database.configuration;

/**
 * Holds the definition of a particular sequence.
 *
 * Underlying database mechanisms may use this configuration to create sequences and seed them with a starting value.
 * The sequence name is typically unique within the context of the database schema.
 */
public class SequenceConfiguration {

    private final String sequenceName;

    private final long startingValue;

    public SequenceConfiguration(final String sequenceName) {
        this(sequenceName, 1);
    }

    public SequenceConfiguration(final String sequenceName, final long startingValue) {
        if (sequenceName == null || sequenceName.isEmpty()) {
            throw new IllegalArgumentException("Sequence name must not be empty");
        }
        this.sequenceName = sequenceName;
        this.startingValue = startingValue;
    }

    public String sequenceName() {
        return sequenceName;
    }

    public long startingValue() {
        return startingValue;
    }

}
