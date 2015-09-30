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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.*;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.clicktravel.cheddar.infrastructure.persistence.database.Item;
import com.clicktravel.cheddar.infrastructure.persistence.database.ItemId;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.CompoundPrimaryKeyDefinition;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.DatabaseSchemaHolder;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.ItemConfiguration;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.PrimaryKeyDefinition;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.OptimisticLockException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.handler.PersistenceExceptionHandler;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.*;
import com.clicktravel.cheddar.infrastructure.persistence.exception.PersistenceResourceFailureException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Deprecated
public class DynamoDocumentStoreTemplate extends AbstractDynamoDbTemplate {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private DynamoDB dynamoDBClient = null;
    private final ObjectMapper mapper;

    public DynamoDocumentStoreTemplate(final DatabaseSchemaHolder databaseSchemaHolder) {
        super(databaseSchemaHolder);
        mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void initialize(final AmazonDynamoDB amazonDynamoDbClient) {
        super.initialize(amazonDynamoDbClient);
        dynamoDBClient = new DynamoDB(amazonDynamoDbClient);
    }

    /**
     * Simple method for splitting a list into a list of smaller lists of the supplied length
     *
     * @param list
     * @param length
     * @return
     */
    private <T> List<List<T>> split(final List<T> list, final int length) {
        final List<List<T>> parts = new ArrayList<List<T>>();
        final int size = list.size();
        for (int i = 0; i < size; i += length) {
            parts.add(new ArrayList<T>(list.subList(i, Math.min(size, i + length))));
        }
        return parts;
    }

    private <T extends Item> List<T> executeQuery(final KeySetQuery query, final Class<T> itemClass) {
        final ItemConfiguration itemConfiguration = getItemConfiguration(itemClass);
        final String tableName = databaseSchemaHolder.schemaName() + "." + itemConfiguration.tableName();
        // max 100 keys per fetch
        final List<List<ItemId>> split_ids = split(new ArrayList<ItemId>(query.itemIds()), 100);
        final List<T> fetchedItems = new ArrayList<T>();
        for (final List<ItemId> ids : split_ids) {
            final TableKeysAndAttributes keys = new TableKeysAndAttributes(tableName);
            for (final ItemId id : ids) {
                keys.addPrimaryKey(getPrimaryKey(id, itemConfiguration));
            }
            processBatchRead(dynamoDBClient.batchGetItem(keys), fetchedItems, tableName, itemClass);
        }
        return fetchedItems;
    }

    private <T extends Item> void processBatchRead(final BatchGetItemOutcome outcome, final List<T> fetchedItems,
            final String tableName, final Class<T> itemClass) {
        final List<com.amazonaws.services.dynamodbv2.document.Item> items = outcome.getTableItems().get(tableName);
        for (final com.amazonaws.services.dynamodbv2.document.Item item : items) {
            fetchedItems.add(stringToItem(item.toJSON(), itemClass));
        }
        if (outcome.getUnprocessedKeys().size() == 0) {
            logger.debug("All items fetched");
        } else {
            logger.debug("Still " + outcome.getUnprocessedKeys().size() + " to fetch");
            processBatchRead(dynamoDBClient.batchGetItemUnprocessed(outcome.getUnprocessedKeys()), fetchedItems,
                    tableName, itemClass);
        }
    }

    @Override
    public <T extends Item> T create(final T item, final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {
        item.setVersion(1l);
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
        return item;
    }

    @Override
    public <T extends Item> T read(final ItemId itemId, final Class<T> itemClass) throws NonExistentItemException {
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
    public <T extends Item> T update(final T item, final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {
        final ItemConfiguration itemConfiguration = getItemConfiguration(item.getClass());
        if (item.getVersion() == null) {
            return create(item);
        }

        final Expected expectedCondition = new Expected(VERSION_ATTRIBUTE).eq(item.getVersion());
        final Long newVersion = item.getVersion() + 1l;
        item.setVersion(newVersion);

        final String tableName = databaseSchemaHolder.schemaName() + "." + itemConfiguration.tableName();
        final String itemJson = itemToString(item);
        final PrimaryKey primaryKey = new PrimaryKey();
        final ItemId itemId = itemConfiguration.getItemId(item);
        final PrimaryKeyDefinition primaryKeyDefinition = itemConfiguration.primaryKeyDefinition();
        primaryKey.addComponent(primaryKeyDefinition.propertyName(), itemId.value());
        if (primaryKeyDefinition instanceof CompoundPrimaryKeyDefinition) {
            primaryKey.addComponent(((CompoundPrimaryKeyDefinition) primaryKeyDefinition).supportingPropertyName(),
                    itemId.supportingValue());
        }
        final Table table = dynamoDBClient.getTable(tableName);
        final com.amazonaws.services.dynamodbv2.document.Item previousAwsItem = table.getItem(primaryKey);
        final String previousItemJson = previousAwsItem.toJSON();

        final String mergedJson = mergeJSONObjects(itemJson, previousItemJson);
        final com.amazonaws.services.dynamodbv2.document.Item awsItem = com.amazonaws.services.dynamodbv2.document.Item
                .fromJSON(mergedJson);
        final PutItemSpec putItemSpec = new PutItemSpec().withItem(awsItem).withExpected(expectedCondition);
        try {
            table.putItem(putItemSpec);
        } catch (final ConditionalCheckFailedException e) {
            throw new OptimisticLockException("Conflicting write detected while updating item");
        }
        return item;
    }

    @Override
    public void delete(final Item item, final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {
        final ItemConfiguration itemConfiguration = getItemConfiguration(item.getClass());
        final String tableName = databaseSchemaHolder.schemaName() + "." + itemConfiguration.tableName();
        final DeleteItemSpec deleteItemSpec = new DeleteItemSpec().withPrimaryKey(getPrimaryKey(
                itemConfiguration.getItemId(item), itemConfiguration));

        final Table table = dynamoDBClient.getTable(tableName);
        table.deleteItem(deleteItemSpec);

        deleteUniqueConstraintIndexes(item, itemConfiguration);

    }

    @Override
    public <T extends Item> Collection<T> fetch(final Query query, final Class<T> itemClass) {
        final long startTimeMillis = System.currentTimeMillis();
        Collection<T> result;
        if (query instanceof AttributeQuery) {
            result = executeQuery((AttributeQuery) query, itemClass);
        } else if (query instanceof KeySetQuery) {
            result = executeQuery((KeySetQuery) query, itemClass);
        } else {
            throw new UnsupportedQueryException(query.getClass());
        }
        final long elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
        logger.debug("Database fetch executed in " + elapsedTimeMillis + "ms. Query:[" + query + "]");
        return result;
    }

    private <T extends Item> Collection<T> executeQuery(final AttributeQuery query, final Class<T> itemClass) {
        final ItemConfiguration itemConfiguration = getItemConfiguration(itemClass);
        final String tableName = databaseSchemaHolder.schemaName() + "." + itemConfiguration.tableName();

        final Table table = dynamoDBClient.getTable(tableName);

        final List<T> totalItems = new ArrayList<>();

        if (itemConfiguration.hasIndexOn(query.getAttributeName())
                && query.getCondition().getComparisonOperator() == Operators.EQUALS) {

            final QuerySpec querySpec = generateQuerySpec(query);
            final ItemCollection<QueryOutcome> queryOutcome;

            if (itemConfiguration.primaryKeyDefinition().propertyName().equals(query.getAttributeName())) {
                // if the query is for the has then call query on table
                queryOutcome = table.query(querySpec);
            } else {
                final Index index = table.getIndex(query.getAttributeName() + "_idx");
                queryOutcome = index.query(querySpec);
            }

            final Iterator<com.amazonaws.services.dynamodbv2.document.Item> iterator = queryOutcome.iterator();
            while (iterator != null && iterator.hasNext()) {
                final com.amazonaws.services.dynamodbv2.document.Item item = iterator.next();
                totalItems.add(stringToItem(item.toJSON(), itemClass));
            }
        } else {
            logger.debug("Performing table scan with query: " + query);
            ScanSpec scanSpec = null;
            try {
                scanSpec = generateScanSpec(query, itemClass);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                throw new PersistenceResourceFailureException("Could not create ScanSpec for query: " + query, e);
            }
            final ItemCollection<ScanOutcome> scanOutcome = table.scan(scanSpec);

            final Iterator<com.amazonaws.services.dynamodbv2.document.Item> iterator = scanOutcome.iterator();
            while (iterator.hasNext()) {
                final com.amazonaws.services.dynamodbv2.document.Item item = iterator.next();
                totalItems.add(stringToItem(item.toJSON(), itemClass));
            }
        }

        return totalItems;
    }

    private QuerySpec generateQuerySpec(final AttributeQuery query) {
        final QuerySpec querySpec = new QuerySpec().withHashKey(query.getAttributeName(), query.getCondition()
                .getValues().iterator().next());
        return querySpec;
    }

    private <T extends Item> ScanSpec generateScanSpec(final AttributeQuery query, final Class<T> tableItemType)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
        final Class<?> clazz = getScanSpecOperandType(query.getAttributeName(), tableItemType);

        ScanSpec scanSpec = new ScanSpec();

        final StringBuilder filterExpression = new StringBuilder();
        final ValueMap valueMap = new ValueMap();
        int valueMapCount = 0;

        if (query.getCondition().getComparisonOperator() == Operators.NULL) {
            filterExpression.append("attribute_not_exists(").append(query.getAttributeName()).append(")");
        } else if (query.getCondition().getComparisonOperator() == Operators.NOT_NULL) {
            filterExpression.append("attribute_exists(").append(query.getAttributeName()).append(")");
        } else {
            if (query.getCondition().getComparisonOperator() == Operators.EQUALS) {
                filterExpression.append(query.getAttributeName()).append(" IN (");

                final Iterator<String> valueIterator = query.getCondition().getValues().iterator();
                while (valueIterator.hasNext()) {
                    filterExpression.append(":").append(valueMapCount);
                    valueMap.with(":" + valueMapCount, valueIterator.next());
                    valueMapCount++;
                    if (valueIterator.hasNext()) {
                        filterExpression.append(",");
                    }
                }
                filterExpression.append(")");
            } else if (query.getCondition().getComparisonOperator() == Operators.LESS_THAN_OR_EQUALS) {
                if (query.getCondition().getValues().size() == 1) {
                    filterExpression.append(query.getAttributeName()).append(" <= ").append(":").append(valueMapCount);
                    final Object valueInstance = clazz.getConstructor(String.class).newInstance(
                            query.getCondition().getValues().iterator().next());
                    valueMap.with(":" + valueMapCount, valueInstance);
                    valueMapCount++;
                } else {
                    // throw exeption??
                }
            } else if (query.getCondition().getComparisonOperator() == Operators.GREATER_THAN_OR_EQUALS) {
                if (query.getCondition().getValues().size() == 1) {
                    filterExpression.append(query.getAttributeName()).append(" >= ").append(":").append(valueMapCount);
                    final Object valueInstance = clazz.getConstructor(String.class).newInstance(
                            query.getCondition().getValues().iterator().next());
                    valueMap.with(":" + valueMapCount, valueInstance);
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

    private <T extends Item> Class<?> getScanSpecOperandType(final String fieldName, final Class<T> itemClass) {
        Class<?> returnType = null;
        final Field[] fieldArray = itemClass.getDeclaredFields();
        for (final Field field : fieldArray) {
            if (fieldName.equals(field.getName())) {
                returnType = field.getType();
            }
        }
        return returnType;
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

    <T extends Item> String itemToString(final T item) {
        final StringBuilder value = new StringBuilder();
        try {
            value.append(mapper.writeValueAsString(item));
        } catch (final JsonProcessingException e) {
            throw new PersistenceResourceFailureException("Failure converting item to String", e);
        }
        return value.toString();
    }

    private String mergeJSONObjects(final String itemJsonString, final String previousJsonString) {
        try {
            final JsonNode itemJson = mapper.readTree(itemJsonString);
            final JsonNode previousItemJson = mapper.readTree(previousJsonString);
            final JsonNode mergedItemJson = merge(itemJson, previousItemJson);
            return mapper.writeValueAsString(mergedItemJson);
        } catch (final IOException e) {
            throw new RuntimeException("JSON Exception" + e);
        }
    }

    public JsonNode merge(final JsonNode newNode, final JsonNode oldNode) {
        final Set<String> allFieldNames = new HashSet<>();
        final Iterator<String> newNodeFieldNames = oldNode.fieldNames();
        while (newNodeFieldNames.hasNext()) {
            allFieldNames.add(newNodeFieldNames.next());
        }
        final Iterator<String> oldNodeFieldNames = oldNode.fieldNames();
        while (oldNodeFieldNames.hasNext()) {
            allFieldNames.add(oldNodeFieldNames.next());
        }
        final JsonNode mergedNode = newNode.deepCopy();
        for (final String fieldName : allFieldNames) {
            final JsonNode newNodeValue = newNode.get(fieldName);
            final JsonNode oldNodeValue = oldNode.get(fieldName);
            if (newNodeValue == null && oldNodeValue == null) {
                logger.trace("Skipping (both null): " + fieldName);
            } else if (newNodeValue == null && oldNodeValue != null) {
                logger.trace("Using old (new is null): " + fieldName);
                ((ObjectNode) mergedNode).set(fieldName, oldNodeValue);
            } else if (newNodeValue != null && oldNodeValue == null) {
                logger.trace("Using new (old is null): " + fieldName);
                ((ObjectNode) mergedNode).set(fieldName, newNodeValue);
            } else {
                logger.trace("Using new: " + fieldName);
                if (oldNodeValue.isObject()) {
                    ((ObjectNode) mergedNode).set(fieldName, merge(newNodeValue, oldNodeValue));
                } else {
                    ((ObjectNode) mergedNode).set(fieldName, newNodeValue);
                }
            }
        }
        return mergedNode;
    }

    private <T extends Item> T stringToItem(final String item, final Class<T> valueType) {
        T value = null;
        try {
            value = mapper.readValue(item, valueType);
        } catch (final IOException e) {
            throw new PersistenceResourceFailureException("Failure converting String to item", e);
        }
        return value;
    }
}
