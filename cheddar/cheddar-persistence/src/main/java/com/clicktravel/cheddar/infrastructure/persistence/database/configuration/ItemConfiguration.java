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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.clicktravel.cheddar.infrastructure.persistence.database.Item;
import com.clicktravel.cheddar.infrastructure.persistence.database.ItemId;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.AttributeQuery;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.CompoundAttributeQuery;

public class ItemConfiguration {

    private final Class<? extends Item> itemClass;
    private final String tableName;
    private final PrimaryKeyDefinition primaryKeyDefinition;
    private final Map<String, PropertyDescriptor> properties = new HashMap<>();
    private final Map<String, IndexDefinition> indexDefinitions;
    private final Map<String, UniqueConstraint> uniqueConstraints;

    public ItemConfiguration(final Class<? extends Item> itemClass, final String tableName) {
        this(itemClass, tableName, new PrimaryKeyDefinition("id"));
    }

    public ItemConfiguration(final Class<? extends Item> itemClass, final String tableName,
            final PrimaryKeyDefinition primaryKeyDefinition) {
        this.itemClass = itemClass;
        this.tableName = tableName;
        indexDefinitions = new HashMap<>();
        uniqueConstraints = new HashMap<>();
        try {
            final BeanInfo beanInfo = Introspector.getBeanInfo(itemClass);
            final PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

            for (final PropertyDescriptor prop : propertyDescriptors) {
                properties.put(prop.getName(), prop);
            }
        } catch (final IntrospectionException e) {
            throw new IllegalStateException(e);
        }
        registerPrimaryKey(primaryKeyDefinition);
        this.primaryKeyDefinition = primaryKeyDefinition;
    }

    private void registerPrimaryKey(final PrimaryKeyDefinition primaryKeyDefinition) {
        final String primaryKeyPropertyName = primaryKeyDefinition.propertyName();
        final PropertyDescriptor propertyDescriptor = properties.get(primaryKeyPropertyName);
        if (propertyDescriptor == null) {
            throw new IllegalStateException(
                    "No property found '" + primaryKeyPropertyName + "' for item :" + itemClass);
        }
        primaryKeyDefinition.setPropertyType(propertyDescriptor.getPropertyType());

        if (CompoundPrimaryKeyDefinition.class.isAssignableFrom(primaryKeyDefinition.getClass())) {
            final CompoundPrimaryKeyDefinition compoundPrimaryKeyDefinition = (CompoundPrimaryKeyDefinition) primaryKeyDefinition;
            final String primaryKeySupportingPropertyName = compoundPrimaryKeyDefinition.supportingPropertyName();
            final PropertyDescriptor supportingPropertyDescriptor = properties.get(primaryKeySupportingPropertyName);
            if (supportingPropertyDescriptor == null) {
                throw new IllegalStateException(
                        "No property found '" + primaryKeySupportingPropertyName + "' for item :" + itemClass);
            }
            compoundPrimaryKeyDefinition.setSupportingPropertyType(supportingPropertyDescriptor.getPropertyType());
        }
    }

    public void registerIndexes(final Collection<IndexDefinition> indexDefinitions) {
        for (final IndexDefinition indexDefinition : indexDefinitions) {
            final String propertyName = indexDefinition.propertyName();
            final Class<?> propertyType = getPropertyType(propertyName);
            indexDefinition.setPropertyType(propertyType);
            String indexName;

            if (CompoundIndexDefinition.class.isAssignableFrom(indexDefinition.getClass())) {
                final CompoundIndexDefinition compoundIndexDefinition = (CompoundIndexDefinition) indexDefinition;
                final String supportingPropertyName = compoundIndexDefinition.supportingPropertyName();

                final Class<?> supportingPropertyType = getPropertyType(supportingPropertyName);
                compoundIndexDefinition.setSupportingPropertyType(supportingPropertyType);
                indexName = compoundName(propertyName, supportingPropertyName);
            } else {
                indexName = indexDefinition.propertyName();
            }

            this.indexDefinitions.put(indexName, indexDefinition);
        }
    }

    public void registerUniqueConstraints(final Collection<UniqueConstraint> uniqueConstraints) {
        for (final UniqueConstraint uniqueConstraint : uniqueConstraints) {
            final String uniqueConstraintPropertyName = uniqueConstraint.propertyName();
            final PropertyDescriptor propertyDescriptor = properties.get(uniqueConstraintPropertyName);
            if (propertyDescriptor == null) {
                throw new IllegalStateException(
                        "No property found '" + uniqueConstraintPropertyName + "' for item :" + itemClass);
            }
            uniqueConstraint.setPropertyDescriptor(propertyDescriptor);
            this.uniqueConstraints.put(uniqueConstraintPropertyName, uniqueConstraint);
        }
    }

    public boolean hasIndexOn(final String propertyName) {
        return primaryKeyDefinition.propertyName().equals(propertyName) || indexDefinitions.containsKey(propertyName);
    }

    public ItemId getItemId(final Item item) {
        final Method readMethod = properties.get(primaryKeyDefinition.propertyName()).getReadMethod();
        try {
            final Object itemIdValueObj = readMethod.invoke(item);
            final String itemIdValue = itemIdValueObj == null ? null : String.valueOf(itemIdValueObj);
            if (CompoundPrimaryKeyDefinition.class.isAssignableFrom(primaryKeyDefinition.getClass())) {
                final CompoundPrimaryKeyDefinition compoundPrimaryKeyDefinition = (CompoundPrimaryKeyDefinition) primaryKeyDefinition;
                final Method supportingReadMethod = properties
                        .get(compoundPrimaryKeyDefinition.supportingPropertyName()).getReadMethod();
                final Object itemIdSupportingValueObj = supportingReadMethod.invoke(item);
                final String itemIdSupportingValue = itemIdSupportingValueObj == null ? null
                        : String.valueOf(itemIdSupportingValueObj);
                return new ItemId(itemIdValue, itemIdSupportingValue);
            }
            return new ItemId(itemIdValue);
        } catch (final InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            throw new IllegalStateException(e);
        }
    }

    public Class<? extends Item> itemClass() {
        return itemClass;
    }

    public String tableName() {
        return tableName;
    }

    public PrimaryKeyDefinition primaryKeyDefinition() {
        return primaryKeyDefinition;
    }

    public Collection<PropertyDescriptor> propertyDescriptors() {
        return Collections.unmodifiableCollection(properties.values());
    }

    public Collection<IndexDefinition> indexDefinitions() {
        return Collections.unmodifiableCollection(indexDefinitions.values());
    }

    public Collection<UniqueConstraint> uniqueConstraints() {
        return Collections.unmodifiableCollection(uniqueConstraints.values());
    }

    public boolean hasIndexForQuery(final AttributeQuery attributeQuery) {
        String indexName;

        if (CompoundAttributeQuery.class.isAssignableFrom(attributeQuery.getClass())) {
            final CompoundAttributeQuery compoundAttributeQuery = (CompoundAttributeQuery) attributeQuery;
            indexName = compoundName(attributeQuery.getAttributeName(),
                    compoundAttributeQuery.getSupportingAttributeName());
        } else {
            indexName = attributeQuery.getAttributeName();
        }

        return hasIndexOn(indexName);
    }

    private Class<?> getPropertyType(final String propertyName) {
        final PropertyDescriptor propertyDescriptor = properties.get(propertyName);
        if (propertyDescriptor == null) {
            throw new IllegalStateException("No property found '" + propertyName + "' for item :" + itemClass);
        }

        return propertyDescriptor.getPropertyType();
    }

    private String compoundName(final String propertyName, final String supportingPropertyName) {
        return propertyName + "_" + supportingPropertyName;
    }
}
