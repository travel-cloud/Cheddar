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
package com.clicktravel.cheddar.infrastructure.persistence.database;

public class SequenceKeyGenerator {

    private final String sequenceName;
    private final int keyCount;

    public SequenceKeyGenerator(final String sequenceName) {
        this(sequenceName, 1);
    }

    public SequenceKeyGenerator(final String sequenceName, final int keyCount) {
        if (sequenceName == null || sequenceName.isEmpty()) {
            throw new IllegalArgumentException("Sequence name must not be empty");
        }
        if (keyCount < 1) {
            throw new IllegalArgumentException("Key count must be more than zero");
        }
        this.sequenceName = sequenceName;
        this.keyCount = keyCount;
    }

    public String sequenceName() {
        return sequenceName;
    }

    public int keyCount() {
        return keyCount;
    }

}
