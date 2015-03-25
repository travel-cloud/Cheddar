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
import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDbPropertyMarshaller;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.*;
import com.clicktravel.cheddar.infrastructure.persistence.database.DocumentItem;
import com.clicktravel.cheddar.infrastructure.persistence.database.DocumentStoreTemplate;
import com.clicktravel.cheddar.infrastructure.persistence.database.Item;
import com.clicktravel.cheddar.infrastructure.persistence.database.ItemId;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.*;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.ItemConstraintViolationException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonUniqueResultException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.handler.PersistenceExceptionHandler;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.AttributeQuery;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Operators;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Query;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.UnsupportedQueryException;
import com.clicktravel.cheddar.infrastructure.persistence.exception.PersistenceResourceFailureException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

//also BatchDocumentWrite??
public class DynamoDocumentStoreTemplate implements DocumentStoreTemplate {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private boolean initialized;
    private AmazonDynamoDB amazonDynamoDbClient;
    private DynamoDB dynamoDBClient;
    private final DatabaseSchemaHolder databaseSchemaHolder;
    private final HashMap<Class<? extends Item>, ItemConfiguration> itemConfigurationMap;
    private final Set<String> sequenceConfigurations;

    public DynamoDocumentStoreTemplate(final DatabaseSchemaHolder databaseSchemaHolder) {
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
        dynamoDBClient = new DynamoDB(amazonDynamoDbClient);
        initialized = true;
    }

    public DatabaseSchemaHolder databaseSchemaHolder() {
        return databaseSchemaHolder;
    }

    @Override
    public <T extends DocumentItem> T createDocument(final T item,
            final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {
        final ItemConfiguration itemConfiguration = getItemConfiguration(item.getClass());
        final Collection<PropertyDescriptor> createdConstraintPropertyDescriptors = createUniqueConstraintIndexes(item,
                itemConfiguration);
        final String tableName = databaseSchemaHolder.schemaName() + "." + itemConfiguration.tableName();
        final com.amazonaws.services.dynamodbv2.document.Item awsItem = com.amazonaws.services.dynamodbv2.document.Item
                .fromJSON(itemToString(item));
        final PutItemSpec putItemSpec = new PutItemSpec().withItem(awsItem);

        final Table table = dynamoDBClient.getTable(tableName);
        boolean itemRequestSucceeded = false;
        try {
            table.putItem(putItemSpec);
            itemRequestSucceeded = true;
        } finally {
            if (!itemRequestSucceeded) {
                try {
                    deleteUniqueConstraintIndexes(item, itemConfiguration, createdConstraintPropertyDescriptors);
                } catch (final Exception deleteUniqueConstraintIndexesException) {
                    logger.error(deleteUniqueConstraintIndexesException.getMessage(),
                            deleteUniqueConstraintIndexesException);
                }
            }
        }
        item.setVersion(1l);
        return item;
    }

    @Override
    public <T extends DocumentItem> T readDocument(final ItemId itemId, final Class<T> itemClass)
            throws NonExistentItemException {
        final ItemConfiguration itemConfiguration = getItemConfiguration(itemClass);
        final String tableName = databaseSchemaHolder.schemaName() + "." + itemConfiguration.tableName();

        final GetItemSpec itemSpec = new GetItemSpec().withPrimaryKey(getPrimaryKey(itemId, itemConfiguration));

        T item = null;

        final Table table = dynamoDBClient.getTable(tableName);

        final com.amazonaws.services.dynamodbv2.document.Item tableItem = table.getItem(itemSpec);
        if (tableItem != null) {
            final String tableText = tableItem.toJSON();
            if (tableText.isEmpty()) {
                throw new NonExistentItemException(String.format(
                        "The document of type [%s] with id [%s] does not exist", itemClass.getName(), itemId));
            }
            item = stringToItem(tableText, itemClass);
        } else {
            throw new NonExistentItemException(String.format("The document of type [%s] with id [%s] does not exist",
                    itemClass.getName(), itemId));
        }
        return item;
    }

    @Override
    public <T extends DocumentItem> T updateDocument(final T item,
            final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {

        final ItemConfiguration itemConfiguration = getItemConfiguration(item.getClass());
        final DocumentItem tmpItem = readDocument(itemConfiguration.getItemId(item), item.getClass());
        if (tmpItem == null) {
            throw new NonExistentItemException(String.format("The document of type [%s] with id [%s] does not exist",
                    item.getClass().getName(), itemConfiguration.getItemId(item)));
        }
        Long version = tmpItem.getVersion();
        item.setVersion(version++);

        final String tableName = databaseSchemaHolder.schemaName() + "." + itemConfiguration.tableName();
        final com.amazonaws.services.dynamodbv2.document.Item awsItem = com.amazonaws.services.dynamodbv2.document.Item
                .fromJSON(itemToString(item));
        final PutItemSpec putItemSpec = new PutItemSpec().withItem(awsItem);

        dynamoDBClient.getTable(tableName).putItem(putItemSpec);
        return item;
    }

    @Override
    public <T extends DocumentItem> void deleteDocument(final T item,
            final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {
        final ItemConfiguration itemConfiguration = getItemConfiguration(item.getClass());
        final String tableName = databaseSchemaHolder.schemaName() + "." + itemConfiguration.tableName();
        final DeleteItemSpec deleteItemSpec = new DeleteItemSpec().withPrimaryKey(getPrimaryKey(
                itemConfiguration.getItemId(item), itemConfiguration));

        final Table table = dynamoDBClient.getTable(tableName);
        table.deleteItem(deleteItemSpec);

        deleteUniqueConstraintIndexes(item, itemConfiguration);

    }

    @Override
    public <T extends DocumentItem> Collection<T> fetchDocuments(final Query query, final Class<T> itemClass) {
        final long startTimeMillis = System.currentTimeMillis();
        Collection<T> result;
        if (query instanceof AttributeQuery) {
            result = executeQuery((AttributeQuery) query, itemClass);
        } else {
            throw new UnsupportedQueryException(query.getClass());
        }
        final long elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
        logger.debug("Database fetch executed in " + elapsedTimeMillis + "ms. Query:[" + query + "]");
        return result;
    }

    private <T extends DocumentItem> Collection<T> executeQuery(final AttributeQuery query, final Class<T> itemClass) {
        final ItemConfiguration itemConfiguration = getItemConfiguration(itemClass);
        final String tableName = databaseSchemaHolder.schemaName() + "." + itemConfiguration.tableName();

        final Table table = dynamoDBClient.getTable(tableName);

        final List<T> totalItems = new ArrayList<>();

        logger.warn("Performing table scan");

        final ScanSpec scanSpec = generateScanSpec(query);
        final ItemCollection<ScanOutcome> scanOutcome = table.scan(scanSpec);

        final Iterator<com.amazonaws.services.dynamodbv2.document.Item> iterator = scanOutcome.iterator();
        while (iterator.hasNext()) {
            final com.amazonaws.services.dynamodbv2.document.Item item = iterator.next();
            totalItems.add(stringToItem(item.toJSON(), itemClass));
        }
        return totalItems;
    }

    private ScanSpec generateScanSpec(final AttributeQuery query) {
        ScanSpec scanSpec = new ScanSpec();

        final StringBuilder filterExpression = new StringBuilder();
        final ValueMap valueMap = new ValueMap();
        int valueMapCount = 0;

        if (query.getCondition().getComparisonOperator().equals(Operators.NULL)) {
            filterExpression.append("attribute_not_exists(").append(query.getAttributeName()).append(")");
        } else if (query.getCondition().getComparisonOperator().equals(Operators.NOT_NULL)) {
            filterExpression.append("attribute_exists(").append(query.getAttributeName()).append(")");
        } else {
            if (query.getCondition().getComparisonOperator().equals(Operators.EQUALS)) {
                filterExpression.append(query.getAttributeName()).append(" IN (");

                final Iterator<String> valueIterator = query.getCondition().getValues().iterator();
                while (valueIterator.hasNext()) {
                    filterExpression.append(":").append(valueMapCount);
                    valueMap.withString(":" + valueMapCount, valueIterator.next());
                    valueMapCount++;
                    if (valueIterator.hasNext()) {
                        filterExpression.append(",");
                    }
                }
                filterExpression.append(")");
            } else if (query.getCondition().getComparisonOperator().equals(Operators.LESS_THAN_OR_EQUALS)) {
                if (query.getCondition().getValues().size() == 1) {
                    filterExpression.append(query.getAttributeName()).append(" <= ").append(":").append(valueMapCount);
                    valueMap.withString(":" + valueMapCount, query.getCondition().getValues().iterator().next());
                    valueMapCount++;
                } else {
                    // throw exeption??
                }
            } else if (query.getCondition().getComparisonOperator().equals(Operators.GREATER_THAN_OR_EQUALS)) {
                if (query.getCondition().getValues().size() == 1) {
                    filterExpression.append(query.getAttributeName()).append(" >= ").append(":").append(valueMapCount);
                    valueMap.withString(":" + valueMapCount, query.getCondition().getValues().iterator().next());
                    valueMapCount++;
                } else {
                    // throw exeption??
                }
            }
        }

        if (filterExpression.length() > 0) {
            scanSpec = scanSpec.withFilterExpression(filterExpression.toString());
        }
        if (valueMap.size() > 0) {
            scanSpec = scanSpec.withValueMap(valueMap);
        }
        return scanSpec;
    }

    @Override
    public <T extends DocumentItem> T fetchUniqueDocument(final Query query, final Class<T> itemClass)
            throws NonUniqueResultException {
        final Collection<T> items = this.fetchDocuments(query, itemClass);
        if (items.size() != 1) {
            throw new NonUniqueResultException(itemClass, items);
        }
        return items.iterator().next();
    }

    private PrimaryKey getPrimaryKey(final ItemId itemId, final ItemConfiguration itemConfiguration) {
        final PrimaryKeyDefinition primaryKeyDefinition = itemConfiguration.primaryKeyDefinition();
        final PrimaryKey key = new PrimaryKey(primaryKeyDefinition.propertyName(), itemId.value());
        if (CompoundPrimaryKeyDefinition.class.isAssignableFrom(primaryKeyDefinition.getClass())) {
            final CompoundPrimaryKeyDefinition compoundPrimaryKeyDefinition = (CompoundPrimaryKeyDefinition) primaryKeyDefinition;
            key.addComponent(compoundPrimaryKeyDefinition.supportingPropertyName(), itemId.supportingValue());
        }
        return key;
    }

    private ItemConfiguration getItemConfiguration(final Class<? extends Item> itemClass) {
        if (!initialized) {
            throw new IllegalStateException("PersistenceTemplate not initialized.");
        }
        final ItemConfiguration itemConfiguration = itemConfigurationMap.get(itemClass);
        if (itemConfiguration == null) {
            throw new IllegalStateException("No ItemConfiguration for " + itemClass);
        }
        return itemConfiguration;
    }

    private <T extends DocumentItem> String itemToString(final T item) {
        final ObjectMapper mapper = new ObjectMapper();
        final StringBuilder value = new StringBuilder();
        try {
            value.append(mapper.writeValueAsString(item));
        } catch (final JsonProcessingException e) {
            throw new PersistenceResourceFailureException("Failure converting item to String", e);
        }
        return value.toString();
    }

    private <T extends DocumentItem> T stringToItem(final String item, final Class<T> valueType) {
        final ObjectMapper mapper = new ObjectMapper();
        T value = null;
        try {
            value = mapper.readValue(item, valueType);
        } catch (final IOException e) {
            throw new PersistenceResourceFailureException("Failure converting String to item", e);
        }
        return value;
    }

    private Collection<PropertyDescriptor> constraintPropertyDescriptors(final ItemConfiguration itemConfiguration) {
        final Collection<PropertyDescriptor> contraintPropertyDescriptors = new HashSet<>();
        for (final UniqueConstraint uniqueConstraint : itemConfiguration.uniqueConstraints()) {
            contraintPropertyDescriptors.add(uniqueConstraint.propertyDescriptor());
        }
        return contraintPropertyDescriptors;
    }

    private <T extends DocumentItem> Collection<PropertyDescriptor> createUniqueConstraintIndexes(final T item,
            final ItemConfiguration itemConfiguration) {
        return createUniqueConstraintIndexes(item, itemConfiguration, constraintPropertyDescriptors(itemConfiguration));
    }

    private <T extends DocumentItem> void deleteUniqueConstraintIndexes(final T item,
            final ItemConfiguration itemConfiguration) {
        deleteUniqueConstraintIndexes(item, itemConfiguration, constraintPropertyDescriptors(itemConfiguration));
    }

    private <T extends DocumentItem> Collection<PropertyDescriptor> createUniqueConstraintIndexes(final T item,
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

    private <T extends DocumentItem> void deleteUniqueConstraintIndexes(final T item,
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
