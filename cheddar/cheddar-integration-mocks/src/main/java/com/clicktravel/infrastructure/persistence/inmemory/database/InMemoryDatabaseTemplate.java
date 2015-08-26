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
package com.clicktravel.infrastructure.persistence.inmemory.database;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.clicktravel.cheddar.infrastructure.persistence.database.*;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.DatabaseSchemaHolder;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.ItemConfiguration;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.UniqueConstraint;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.ItemConstraintViolationException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.handler.PersistenceExceptionHandler;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.*;
import com.clicktravel.infrastructure.inmemory.Resettable;
import com.clicktravel.infrastructure.persistence.inmemory.SerializedItem;

public class InMemoryDatabaseTemplate extends AbstractDatabaseTemplate implements Resettable {

    private static final AtomicLong ATOMIC_COUNTER = new AtomicLong();
    private final Map<String, Map<ItemId, SerializedItem>> items = new ConcurrentHashMap<>();
    private final Map<String, Map<String, ItemId>> uniqueConstraints = new ConcurrentHashMap<>();
    private final Map<Class<? extends Item>, ItemConfiguration> itemConfigurations = new ConcurrentHashMap<>();

    public InMemoryDatabaseTemplate(final DatabaseSchemaHolder databaseSchemaHolder) {
        for (final ItemConfiguration itemConfiguration : databaseSchemaHolder.itemConfigurations()) {
            itemConfigurations.put(itemConfiguration.itemClass(), itemConfiguration);
            final String tableName = itemConfiguration.tableName();
            items.put(tableName, new HashMap<ItemId, SerializedItem>());
            for (final UniqueConstraint uniqueConstraint : itemConfiguration.uniqueConstraints()) {
                uniqueConstraints.put(newUniqueConstraintKey(tableName, uniqueConstraint.propertyName()),
                        new HashMap<String, ItemId>());
            }
        }
    }

    @Override
    public <T extends Item> T read(final ItemId itemId, final Class<T> itemClass) throws NonExistentItemException {
        final SerializedItem serializedItem = getItemMap(getItemTableName(itemClass)).get(itemId);
        if (serializedItem == null) {
            throw new NonExistentItemException("Item with identifier [" + itemId.value() + "] did not exist");
        }
        final T item = serializedItem.getEntity(itemClass);
        if (itemClass.isAssignableFrom(item.getClass())) {
            return item;
        }
        throw new NonExistentItemException("Item with identifier [" + itemId.value() + "] did not exist");
    }

    @Override
    public <T extends Item> T create(final T item, final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {
        final ItemId itemId = getItemId(item);
        final String tableName = getItemTableName(item.getClass());
        final SerializedItem oldSerializedItem = getItemMap(tableName).get(itemId);
        if (oldSerializedItem != null) {
            throw new IllegalAccessError("Item already exist with identifier [" + itemId.value() + "] in ["
                    + item.getClass() + "] repository");
        }
        createUniqueConstraints(item);
        item.setVersion(1L);
        getItemMap(tableName).put(itemId, getSerializedItem(itemId.value(), item));
        return item;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Item> T update(final T item, final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {
        final ItemId itemId = getItemId(item);
        final Class<? extends Item> itemType = item.getClass();
        final String tableName = getItemTableName(itemType);
        final SerializedItem oldSerializedItem = getItemMap(tableName).get(itemId);
        if (oldSerializedItem == null) {
            return create(item);
        }
        final T oldItem = (T) oldSerializedItem.getEntity(item.getClass());
        if (!item.getVersion().equals(oldItem.getVersion())) {
            throw new IllegalAccessError("Expected version [" + item.getVersion() + "] but was ["
                    + oldItem.getVersion() + "]");
        }
        deleteUniqueConstraints(oldItem);
        try {
            createUniqueConstraints(item);
        } catch (final ItemConstraintViolationException e) {
            createUniqueConstraints(oldItem);
            throw e;
        }
        item.setVersion(item.getVersion() + 1);
        getItemMap(tableName).put(itemId, getSerializedItem(itemId.value(), item));
        return item;
    }

    @Override
    public void delete(final Item item, final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {
        if (item != null) {
            getItemMap(getItemTableName(item.getClass())).remove(getItemId(item));
        }
        deleteUniqueConstraints(item);
    }

    private String uniqueConstraintPropertyValue(final Object propertyValue) {
        return String.valueOf(propertyValue).toUpperCase();
    }

    private void createUniqueConstraints(final Item item) {
        final Class<? extends Item> itemClass = item.getClass();
        final String tableName = getItemTableName(itemClass);
        final Collection<PropertyDescriptor> uniqueConstraintProperties = getUniqueConstraintProperties(itemClass);
        final Map<String, String> newConstraints = new HashMap<>();
        final ItemId itemId = getItemId(item);
        for (final PropertyDescriptor propertyDescriptor : uniqueConstraintProperties) {
            final String propertyName = propertyDescriptor.getName();
            final String uniqueConstraintKey = newUniqueConstraintKey(tableName, propertyName);
            final Map<String, ItemId> uniqueValues = uniqueConstraints.get(uniqueConstraintKey);
            Object propertyValue = null;
            try {
                propertyValue = propertyDescriptor.getReadMethod().invoke(item);
            } catch (final Exception e) {
                throw new IllegalStateException("Could not invoke read method", e);
            }
            if (propertyValue != null) {
                final String uniqueConstraintPropertyValue = uniqueConstraintPropertyValue(propertyValue);
                final ItemId existingItemId = uniqueValues.get(uniqueConstraintPropertyValue);
                if (existingItemId != null) {
                    throw new ItemConstraintViolationException(propertyName, "Already in use");
                }
                newConstraints.put(uniqueConstraintKey, uniqueConstraintPropertyValue);
            }
        }
        for (final Entry<String, String> entry : newConstraints.entrySet()) {
            final String uniqueConstraintKey = entry.getKey();
            final String uniqueConstraintPropertyValue = entry.getValue();
            uniqueConstraints.get(uniqueConstraintKey).put(uniqueConstraintPropertyValue, itemId);
        }
    }

    private void deleteUniqueConstraints(final Item item) {
        final Class<? extends Item> itemClass = item.getClass();
        final String tableName = getItemTableName(itemClass);
        final Collection<PropertyDescriptor> uniqueConstraintProperties = getUniqueConstraintProperties(itemClass);
        for (final PropertyDescriptor propertyDescriptor : uniqueConstraintProperties) {
            final String uniqueConstraintKey = newUniqueConstraintKey(tableName, propertyDescriptor.getName());
            final Map<String, ItemId> uniqueValues = uniqueConstraints.get(uniqueConstraintKey);
            Object propertyValue = null;
            try {
                propertyValue = propertyDescriptor.getReadMethod().invoke(item);
            } catch (final Exception e) {
                throw new IllegalStateException("Could not invoke read method", e);
            }
            if (propertyValue != null) {
                final String uniqueConstraintPropertyValue = uniqueConstraintPropertyValue(propertyValue);
                final ItemId itemId = uniqueValues.get(uniqueConstraintPropertyValue);
                if (itemId.equals(getItemId(item))) {
                    uniqueConstraints.get(uniqueConstraintKey).remove(uniqueConstraintPropertyValue);
                }
            }
        }
    }

    /**
     * Checks the presence of a matching unique constraint for the given item property
     * 
     * @param item The Item for which to check unique constraints
     * @param propertyName The name of the property for which the unique constraint should be checked
     * @param propertyValue The value of the property which needs to be checked for presence of unique constraint
     * @return
     */
    boolean hasMatchingUniqueConstraint(final Item item, final String propertyName, final String propertyValue) {
        final Class<? extends Item> itemClass = item.getClass();
        final String tableName = getItemTableName(itemClass);
        final Map<String, ItemId> uniqueConstraintsForProperty = uniqueConstraints.get(newUniqueConstraintKey(
                tableName, propertyName));
        final ItemId itemId = getItemId(item);
        for (final Entry<String, ItemId> entry : uniqueConstraintsForProperty.entrySet()) {
            if (entry.getValue().equals(itemId)) {
                return entry.getKey().equals(propertyValue.toUpperCase());
            }
        }
        return false;
    }

    @Override
    public <T extends Item> Collection<T> fetch(final Query query, final Class<T> itemClass) {
        if (query instanceof AttributeQuery) {
            return executeQuery((AttributeQuery) query, itemClass);
        } else if (query instanceof KeySetQuery) {
            return executeQuery((KeySetQuery) query, itemClass);
        } else {
            throw new UnsupportedQueryException(query.getClass());
        }
    }

    @Override
    public GeneratedKeyHolder generateKeys(final SequenceKeyGenerator sequenceKeyGenerator) {
        final Collection<Long> keys = new ArrayList<>();
        final int keyCount = sequenceKeyGenerator.keyCount();
        final long startingKey = ATOMIC_COUNTER.getAndAdd(keyCount) + 1;
        for (int i = 0; i < keyCount; i++) {
            keys.add(startingKey + i);
        }
        return new GeneratedKeyHolder(keys);
    }

    private static String newUniqueConstraintKey(final String tableName, final String propertyName) {
        return tableName + ":" + propertyName;
    }

    private ItemId getItemId(final Item item) {
        final ItemConfiguration itemConfiguration = getItemConfiguration(item.getClass());
        return itemConfiguration.getItemId(item);
    }

    private ItemConfiguration getItemConfiguration(final Class<? extends Item> itemClass) {
        final ItemConfiguration itemConfiguration = itemConfigurations.get(itemClass);
        if (itemConfiguration == null) {
            throw new IllegalStateException("No ItemConfiguration for " + itemClass);
        }
        return itemConfiguration;
    }

    private String getItemTableName(final Class<? extends Item> itemClass) {
        final ItemConfiguration itemConfiguration = itemConfigurations.get(itemClass);
        if (itemConfiguration == null) {
            throw new IllegalStateException("No ItemConfiguration for " + itemClass);
        }
        return itemConfiguration.tableName();
    }

    private <T extends Item> Collection<PropertyDescriptor> getUniqueConstraintProperties(
            final Class<? extends Item> itemClass) {
        final Collection<PropertyDescriptor> contraintPropertyDescriptors = new HashSet<>();
        for (final UniqueConstraint uniqueConstraint : getItemConfiguration(itemClass).uniqueConstraints()) {
            contraintPropertyDescriptors.add(uniqueConstraint.propertyDescriptor());
        }
        return contraintPropertyDescriptors;
    }

    private SerializedItem getSerializedItem(final String itemId, final Object repositoryItem) {
        return new SerializedItem(repositoryItem);
    }

    private <T extends Item> Collection<T> executeQuery(final KeySetQuery query, final Class<T> itemClass) {
        final Map<ItemId, T> allItems = getAllItems(itemClass);
        final Collection<T> matches = new ArrayList<>();
        for (final Entry<ItemId, T> entry : allItems.entrySet()) {
            if (query.itemIds().contains(entry.getKey())) {
                matches.add(entry.getValue());
            }
        }
        return matches;
    }

    private <T extends Item> Collection<T> executeQuery(final AttributeQuery query, final Class<T> itemClass) {
        final Map<ItemId, T> allItems = getAllItems(itemClass);
        final Collection<T> matches = new ArrayList<>();
        for (final T item : allItems.values()) {
            final String attribute = query.getAttributeName();
            try {
                final Method getter = new PropertyDescriptor(attribute, item.getClass()).getReadMethod();
                final Object itemPropertyValue = getter.invoke(item);
                final Class<?> itemPropertyType = getter.getReturnType();
                final Condition condition = query.getCondition();
                final Set<String> values = condition.getValues();
                String singleValue = null;
                if (values != null && !values.isEmpty()) {
                    singleValue = values.iterator().next();
                }
                final boolean isSingleItemProperty = !Collection.class.isAssignableFrom(itemPropertyType);
                String singleItemPropertyValue = null;
                if (isSingleItemProperty) {
                    singleItemPropertyValue = String.valueOf(itemPropertyValue);
                }
                switch (query.getCondition().getComparisonOperator()) {
                    case NULL:
                        if (itemPropertyValue == null) {
                            matches.add(item);
                        }
                        break;
                    case NOT_NULL:
                        if (itemPropertyValue != null) {
                            matches.add(item);
                        }
                        break;
                    case LESS_THAN_OR_EQUALS:
                        if (isSingleItemProperty && singleItemPropertyValue.compareTo(singleValue) <= 0) {
                            matches.add(item);
                        }
                        break;
                    case GREATER_THAN_OR_EQUALS:
                        if (isSingleItemProperty && singleItemPropertyValue.compareTo(singleValue) >= 0) {
                            matches.add(item);
                        }
                        break;
                    case EQUALS:
                        if (isSingleItemProperty && singleItemPropertyValue.equals(singleValue)) {
                            matches.add(item);
                        } else if (values.equals(itemPropertyValue)) {
                            matches.add(item);
                        }
                        break;
                    default:
                        break;
                }
            } catch (final Exception e) {
                throw new IllegalStateException("No getter for property [" + attribute + "] on class: ["
                        + item.getClass() + "]");
            }

        }
        return matches;
    }

    private <T extends Item> Map<ItemId, T> getAllItems(final Class<T> itemClass) {
        final Map<ItemId, T> allItems = new HashMap<>();
        final String tableName = getItemTableName(itemClass);
        for (final SerializedItem serializedItem : getItemMap(tableName).values()) {
            final T item = serializedItem.getEntity(itemClass);
            if (itemClass.isAssignableFrom(item.getClass())) {
                allItems.put(getItemId(item), item);
            }
        }
        return allItems;
    }

    private Map<ItemId, SerializedItem> getItemMap(final String tableName) {
        final Map<ItemId, SerializedItem> itemMap = items.get(tableName);
        if (itemMap == null) {
            throw new IllegalStateException("Unknown table: " + tableName);
        }
        return itemMap;
    }

    @Override
    public void reset() {
        for (final Entry<String, Map<ItemId, SerializedItem>> entry : items.entrySet()) {
            entry.setValue(new HashMap<ItemId, SerializedItem>());
        }
        for (final Entry<String, Map<String, ItemId>> entry : uniqueConstraints.entrySet()) {
            entry.setValue(new HashMap<String, ItemId>());
        }
    }

}