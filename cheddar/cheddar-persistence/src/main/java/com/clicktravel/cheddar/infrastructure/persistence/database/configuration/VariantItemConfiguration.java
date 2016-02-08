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
import java.util.Collections;
import java.util.HashSet;

import com.clicktravel.cheddar.infrastructure.persistence.database.Item;

public class VariantItemConfiguration extends ItemConfiguration {

    private final ParentItemConfiguration parentItemConfiguration;
    private final String discriminatorValue;

    public VariantItemConfiguration(final ParentItemConfiguration parentItemConfiguration,
            final Class<? extends Item> itemClass, final String discriminatorValue) {
        super(itemClass, parentItemConfiguration.tableName());
        this.parentItemConfiguration = parentItemConfiguration;
        this.discriminatorValue = discriminatorValue;
        this.parentItemConfiguration.registerVariantItemClass(itemClass, discriminatorValue);
    }

    public ParentItemConfiguration parentItemConfiguration() {
        return parentItemConfiguration;
    }

    public String discriminatorValue() {
        return discriminatorValue;
    }

    @Override
    public Collection<IndexDefinition> indexDefinitions() {
        final Collection<IndexDefinition> allIndexDefinitions = new HashSet<>(super.indexDefinitions());
        allIndexDefinitions.addAll(parentItemConfiguration.indexDefinitions());
        return Collections.unmodifiableCollection(allIndexDefinitions);
    }

    @Override
    public void registerUniqueConstraints(final Collection<UniqueConstraint> uniqueConstraints) {
        throw new UnsupportedOperationException("VariantItemConfiguration cannot register new unique constraints");
    }

    @Override
    public Collection<UniqueConstraint> uniqueConstraints() {
        return parentItemConfiguration.uniqueConstraints();
    }

}
