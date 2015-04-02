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
package com.clicktravel.infrastructure.persistence.aws.dynamodb;

import java.beans.PropertyDescriptor;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDbPropertyMarshaller;
import com.amazonaws.services.dynamodbv2.model.*;
import com.clicktravel.cheddar.infrastructure.persistence.database.AbstractDatabaseTemplate;
import com.clicktravel.cheddar.infrastructure.persistence.database.GeneratedKeyHolder;
import com.clicktravel.cheddar.infrastructure.persistence.database.Item;
import com.clicktravel.cheddar.infrastructure.persistence.database.SequenceKeyGenerator;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.DatabaseSchemaHolder;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.ItemConfiguration;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.SequenceConfiguration;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.UniqueConstraint;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.ItemConstraintViolationException;
import com.clicktravel.cheddar.infrastructure.persistence.exception.PersistenceResourceFailureException;

public abstract class AbstractDynamoDbTemplate extends AbstractDatabaseTemplate {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected boolean initialized;
    protected AmazonDynamoDB amazonDynamoDbClient;
    protected final DatabaseSchemaHolder databaseSchemaHolder;
    protected final HashMap<Class<? extends Item>, ItemConfiguration> itemConfigurationMap;
    protected final Set<String> sequenceConfigurations;

    private static final String SEQUENCE_TABLE_NAME = "sequences";
    private static final String SEQUENCE_NAME_ATTRIBUTE = "name";
    private static final String SEQUENCE_CURRENT_VALUE_ATTRIBUTE = "currentValue";

    public AbstractDynamoDbTemplate(final DatabaseSchemaHolder databaseSchemaHolder) {
        this.databaseSchemaHolder = databaseSchemaHolder;
        itemConfigurationMap = new HashMap<>();
        for (final ItemConfiguration itemConfiguration : databaseSchemaHolder.itemConfigurations()) {
            itemConfigurationMap.put(itemConfiguration.itemClass(), itemConfiguration);
        }
        sequenceConfigurations = new HashSet<>();
        for (final SequenceConfiguration sequenceConfiguration : databaseSchemaHolder.sequenceConfigurations()) {
            sequenceConfigurations.add(sequenceConfiguration.sequenceName());
        }
    }

    public void initialize(final AmazonDynamoDB amazonDynamoDbClient) {
        this.amazonDynamoDbClient = amazonDynamoDbClient;
        initialized = true;
    }

    public DatabaseSchemaHolder databaseSchemaHolder() {
        return databaseSchemaHolder;
    }

    @Override
    public GeneratedKeyHolder generateKeys(final SequenceKeyGenerator sequenceKeyGenerator) {
        final String sequenceName = sequenceKeyGenerator.sequenceName();
        if (!sequenceConfigurations.contains(sequenceName)) {
            throw new IllegalStateException("Unsupported sequence: " + sequenceName);
        }
        final Map<String, AttributeValue> key = new HashMap<>();
        key.put(SEQUENCE_NAME_ATTRIBUTE, new AttributeValue(sequenceName));
        final AttributeValueUpdate attributeValueUpdate = new AttributeValueUpdate().withAction("ADD").withValue(
                new AttributeValue().withN(String.valueOf(sequenceKeyGenerator.keyCount())));
        final Map<String, AttributeValueUpdate> attributeUpdates = new HashMap<>();
        attributeUpdates.put(SEQUENCE_CURRENT_VALUE_ATTRIBUTE, attributeValueUpdate);
        final String tableName = databaseSchemaHolder.schemaName() + "-" + SEQUENCE_TABLE_NAME;
        final UpdateItemRequest updateItemRequest = new UpdateItemRequest().withTableName(tableName).withKey(key)
                .withAttributeUpdates(attributeUpdates).withReturnValues("UPDATED_NEW");
        final UpdateItemResult updateItemResult;
        try {
            updateItemResult = amazonDynamoDbClient.updateItem(updateItemRequest);
        } catch (final AmazonServiceException e) {
            throw new PersistenceResourceFailureException("Failure while attempting DynamoDb Update (generate keys)", e);
        }
        final Map<String, AttributeValue> attributes = updateItemResult.getAttributes();
        final AttributeValue currentAttributeValue = attributes.get(SEQUENCE_CURRENT_VALUE_ATTRIBUTE);
        final Long currentValue = Long.valueOf(currentAttributeValue.getN());
        final Collection<Long> keys = new ArrayList<>();
        for (long i = currentValue - sequenceKeyGenerator.keyCount(); i < currentValue; i++) {
            keys.add(i + 1);
        }
        return new GeneratedKeyHolder(keys);
    }

    protected final ItemConfiguration getItemConfiguration(final Class<? extends Item> itemClass) {
        if (!initialized) {
            throw new IllegalStateException("PersistenceTemplate not initialized.");
        }
        final ItemConfiguration itemConfiguration = itemConfigurationMap.get(itemClass);
        if (itemConfiguration == null) {
            throw new IllegalStateException("No ItemConfiguration for " + itemClass);
        }
        return itemConfiguration;
    }

    protected final Collection<PropertyDescriptor> constraintPropertyDescriptors(
            final ItemConfiguration itemConfiguration) {
        final Collection<PropertyDescriptor> contraintPropertyDescriptors = new HashSet<>();
        for (final UniqueConstraint uniqueConstraint : itemConfiguration.uniqueConstraints()) {
            contraintPropertyDescriptors.add(uniqueConstraint.propertyDescriptor());
        }
        return contraintPropertyDescriptors;
    }

    protected final <T extends Item> Collection<PropertyDescriptor> createUniqueConstraintIndexes(final T item,
            final ItemConfiguration itemConfiguration) {
        return createUniqueConstraintIndexes(item, itemConfiguration, constraintPropertyDescriptors(itemConfiguration));
    }

    protected final <T extends Item> Collection<PropertyDescriptor> createUniqueConstraintIndexes(final T item,
            final ItemConfiguration itemConfiguration,
            final Collection<PropertyDescriptor> constraintPropertyDescriptors) {
        final Set<PropertyDescriptor> createdConstraintPropertyDescriptors = new HashSet<>();
        ItemConstraintViolationException itemConstraintViolationException = null;
        for (final UniqueConstraint uniqueConstraint : itemConfiguration.uniqueConstraints()) {
            final String uniqueConstraintPropertyName = uniqueConstraint.propertyName();
            final PropertyDescriptor uniqueConstraintPropertyDescriptor = uniqueConstraint.propertyDescriptor();
            if (constraintPropertyDescriptors.contains(uniqueConstraintPropertyDescriptor)) {
                final AttributeValue uniqueConstraintAttributeValue = DynamoDbPropertyMarshaller.getValue(item,
                        uniqueConstraintPropertyDescriptor);
                if (uniqueConstraintAttributeValue == null) {
                    continue;
                }
                if (uniqueConstraintAttributeValue.getS() != null) {
                    uniqueConstraintAttributeValue.setS(uniqueConstraintAttributeValue.getS().toUpperCase());
                }
                final Map<String, AttributeValue> attributeMap = new HashMap<>();
                attributeMap.put("property", new AttributeValue(uniqueConstraintPropertyName));
                attributeMap.put("value", uniqueConstraintAttributeValue);
                final Map<String, ExpectedAttributeValue> expectedResults = new HashMap<>();
                expectedResults.put("value", new ExpectedAttributeValue(false));
                final String indexTableName = databaseSchemaHolder.schemaName() + "-indexes."
                        + itemConfiguration.tableName();
                final PutItemRequest itemRequest = new PutItemRequest().withTableName(indexTableName)
                        .withItem(attributeMap).withExpected(expectedResults);
                try {
                    amazonDynamoDbClient.putItem(itemRequest);
                    createdConstraintPropertyDescriptors.add(uniqueConstraintPropertyDescriptor);
                } catch (final ConditionalCheckFailedException e) {
                    itemConstraintViolationException = new ItemConstraintViolationException(
                            uniqueConstraintPropertyName, "Unique constraint violation on property '"
                                    + uniqueConstraintPropertyName + "' ('" + uniqueConstraintAttributeValue
                                    + "') of item " + item.getClass());
                    break;
                } catch (final AmazonServiceException e) {
                    throw new PersistenceResourceFailureException(
                            "Failure while attempting DynamoDb put (creating unique constraint index entry)", e);
                }
            }
        }
        if (itemConstraintViolationException != null) {
            try {
                deleteUniqueConstraintIndexes(item, itemConfiguration, createdConstraintPropertyDescriptors);
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }
            throw itemConstraintViolationException;
        }
        return createdConstraintPropertyDescriptors;
    }

    protected final <T extends Item> void deleteUniqueConstraintIndexes(final T item,
            final ItemConfiguration itemConfiguration) {
        deleteUniqueConstraintIndexes(item, itemConfiguration, constraintPropertyDescriptors(itemConfiguration));

    }

    protected final <T extends Item> void deleteUniqueConstraintIndexes(final T item,
            final ItemConfiguration itemConfiguration,
            final Collection<PropertyDescriptor> constraintPropertyDescriptors) {
        if (constraintPropertyDescriptors.isEmpty()) {
            return;
        }
        for (final UniqueConstraint uniqueConstraint : itemConfiguration.uniqueConstraints()) {
            final String uniqueConstraintPropertyName = uniqueConstraint.propertyName();
            final PropertyDescriptor uniqueConstraintPropertyDescriptor = uniqueConstraint.propertyDescriptor();
            if (constraintPropertyDescriptors.contains(uniqueConstraintPropertyDescriptor)) {
                final AttributeValue uniqueConstraintAttributeValue = DynamoDbPropertyMarshaller.getValue(item,
                        uniqueConstraintPropertyDescriptor);
                if (uniqueConstraintAttributeValue.getS() != null) {
                    uniqueConstraintAttributeValue.setS(uniqueConstraintAttributeValue.getS().toUpperCase());
                }
                final Map<String, AttributeValue> key = new HashMap<>();
                key.put("property", new AttributeValue(uniqueConstraintPropertyName));
                key.put("value", uniqueConstraintAttributeValue);
                final String indexTableName = databaseSchemaHolder.schemaName() + "-indexes."
                        + itemConfiguration.tableName();
                final DeleteItemRequest itemRequest = new DeleteItemRequest().withTableName(indexTableName)
                        .withKey(key);
                try {
                    amazonDynamoDbClient.deleteItem(itemRequest);
                } catch (final AmazonServiceException e) {
                    throw new PersistenceResourceFailureException(
                            "Failed while attempting to perform DynamoDb Delete (for unique constraints)", e);
                }
            }
        }
    }
}
