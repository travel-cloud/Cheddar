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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DatabaseSchemaHolder {

    private final String schemaName;
    private final Set<ItemConfiguration> itemConfigurations;
    private final Set<SequenceConfiguration> sequenceConfigurations;

    public DatabaseSchemaHolder(final String schemaName, final Collection<ItemConfiguration> itemConfigurations) {
        this(schemaName, itemConfigurations, new HashSet<SequenceConfiguration>());
    }

    public DatabaseSchemaHolder(final String schemaName, final Collection<ItemConfiguration> itemConfigurations,
            final Collection<SequenceConfiguration> sequenceConfigurations) {
        if (schemaName == null || schemaName.isEmpty()) {
            throw new IllegalArgumentException("Schema name must not be empty");
        }
        if (itemConfigurations == null) {
            throw new IllegalArgumentException("Item configurations name must not be null");
        }
        this.schemaName = schemaName;
        this.itemConfigurations = new HashSet<>(itemConfigurations);
        this.sequenceConfigurations = new HashSet<>(sequenceConfigurations);
    }

    public Collection<ItemConfiguration> itemConfigurations() {
        return itemConfigurations;
    }

    public Collection<SequenceConfiguration> sequenceConfigurations() {
        return sequenceConfigurations;
    }

    public String schemaName() {
        return schemaName;
    }

}
