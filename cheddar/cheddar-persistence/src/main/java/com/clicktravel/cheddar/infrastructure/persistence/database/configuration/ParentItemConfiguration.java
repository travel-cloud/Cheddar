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

import java.util.HashMap;
import java.util.Map;

import com.clicktravel.cheddar.infrastructure.persistence.database.Item;

public class ParentItemConfiguration extends ItemConfiguration {

    private static final String DEFAULT_DISCRIMINATOR = "discriminator";
    private final String discriminator;
    private final Map<String, Class<? extends Item>> variantItemClasses;

    public ParentItemConfiguration(final Class<? extends Item> itemClass, final String tableName) {
        this(itemClass, tableName, DEFAULT_DISCRIMINATOR);
    }

    public ParentItemConfiguration(final Class<? extends Item> itemClass, final String tableName,
            final String discriminator) {
        super(itemClass, tableName);
        this.discriminator = discriminator;
        variantItemClasses = new HashMap<>();
    }

    public String discriminator() {
        return discriminator;
    }

    void registerVariantItemClass(final Class<? extends Item> variantItemClass, final String discriminatorValue) {
        variantItemClasses.put(discriminatorValue, variantItemClass);
    }

    @SuppressWarnings("unchecked")
    public <T extends Item> Class<? extends T> getVariantItemClass(final String discriminatorValue) {
        final Class<? extends T> itemClass = (Class<? extends T>) variantItemClasses.get(discriminatorValue);
        if (itemClass == null) {
            throw new IllegalStateException("No subclass found with discriminator value:" + discriminatorValue);
        }
        return itemClass;
    }
}
