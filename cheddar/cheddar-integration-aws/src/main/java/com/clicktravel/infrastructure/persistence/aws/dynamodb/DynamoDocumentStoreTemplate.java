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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.clicktravel.cheddar.infrastructure.persistence.database.Item;
import com.clicktravel.cheddar.infrastructure.persistence.database.ItemId;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.CompoundPrimaryKeyDefinition;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.DatabaseSchemaHolder;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.ItemConfiguration;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.PrimaryKeyDefinition;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.handler.PersistenceExceptionHandler;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.*;
import com.clicktravel.cheddar.infrastructure.persistence.exception.PersistenceResourceFailureException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Deprecated
public class DynamoDocumentStoreTemplate extends AbstractDynamoDbTemplate {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private DynamoDB dynamoDBClient = null;

    public DynamoDocumentStoreTemplate(final DatabaseSchemaHolder databaseSchemaHolder) {
        super(databaseSchemaHolder);
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
        final Item tmpItem = read(itemConfiguration.getItemId(item), item.getClass());
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

    private PrimaryKey getPrimaryKey(final ItemId itemId, final ItemConfiguration itemConfiguration) {
        final PrimaryKeyDefinition primaryKeyDefinition = itemConfiguration.primaryKeyDefinition();
        final PrimaryKey key = new PrimaryKey(primaryKeyDefinition.propertyName(), itemId.value());
        if (CompoundPrimaryKeyDefinition.class.isAssignableFrom(primaryKeyDefinition.getClass())) {
            final CompoundPrimaryKeyDefinition compoundPrimaryKeyDefinition = (CompoundPrimaryKeyDefinition) primaryKeyDefinition;
            key.addComponent(compoundPrimaryKeyDefinition.supportingPropertyName(), itemId.supportingValue());
        }
        return key;
    }

    private <T extends Item> String itemToString(final T item) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
        final StringBuilder value = new StringBuilder();
        try {
            value.append(mapper.writeValueAsString(item));
        } catch (final JsonProcessingException e) {
            throw new PersistenceResourceFailureException("Failure converting item to String", e);
        }
        return value.toString();
    }

    private <T extends Item> T stringToItem(final String item, final Class<T> valueType) {
        final ObjectMapper mapper = new ObjectMapper();
        T value = null;
        try {
            value = mapper.readValue(item, valueType);
        } catch (final IOException e) {
            throw new PersistenceResourceFailureException("Failure converting String to item", e);
        }
        return value;
    }
}
