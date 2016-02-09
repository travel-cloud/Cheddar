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

import com.clicktravel.cheddar.infrastructure.persistence.database.Item;

public class VariantItemConfiguration extends ItemConfiguration {

    private final ParentItemConfiguration parentItemConfiguration;
    private final String discriminatorValue;

    /**
     * Creates a new VariantItemConfiguration based upon an existing ParentItemConfiguration
     *
     * NOTE: The ParentItemConfiguration MUST have its IndexDefinitions and UniqueConstraints already registered in
     * order for the newly created VariantItemConfiguration to inherit these.
     *
     * @param parentItemConfiguration The item configuration on which this VariantItemConfiguration is based
     * @param itemClass The Class of the Item which this configuration is for
     * @param discriminatorValue A unique string value associated with each of the variant items. Discriminates between
     *            multiple variant types
     */
    public VariantItemConfiguration(final ParentItemConfiguration parentItemConfiguration,
            final Class<? extends Item> itemClass, final String discriminatorValue) {
        super(itemClass, parentItemConfiguration.tableName());
        this.parentItemConfiguration = parentItemConfiguration;
        this.discriminatorValue = discriminatorValue;
        this.parentItemConfiguration.registerVariantItemClass(itemClass, discriminatorValue);
        registerIndexes(parentItemConfiguration.indexDefinitions());
        registerUniqueConstraints(parentItemConfiguration.uniqueConstraints());
    }

    public ParentItemConfiguration parentItemConfiguration() {
        return parentItemConfiguration;
    }

    public String discriminatorValue() {
        return discriminatorValue;
    }

}
