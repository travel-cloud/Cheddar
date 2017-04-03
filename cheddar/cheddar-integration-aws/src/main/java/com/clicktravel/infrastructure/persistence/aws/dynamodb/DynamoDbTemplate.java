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

import static com.amazonaws.services.dynamodbv2.datamodeling.DynamoDbPropertyMarshaller.getAttributeType;

import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDbPropertyMarshaller;
import com.amazonaws.services.dynamodbv2.model.*;
import com.clicktravel.cheddar.infrastructure.persistence.database.BatchDatabaseTemplate;
import com.clicktravel.cheddar.infrastructure.persistence.database.Item;
import com.clicktravel.cheddar.infrastructure.persistence.database.ItemId;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.*;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.ItemConstraintViolationException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.OptimisticLockException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.handler.PersistenceExceptionHandler;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.*;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Condition;
import com.clicktravel.cheddar.infrastructure.persistence.exception.PersistenceResourceFailureException;

@Deprecated
public class DynamoDbTemplate extends AbstractDynamoDbTemplate implements BatchDatabaseTemplate {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DynamoDbTemplate(final DatabaseSchemaHolder databaseSchemaHolder) {
        super(databaseSchemaHolder);
    }

    @Override
    public <T extends Item> T read(final ItemId itemId, final Class<T> itemClass) throws NonExistentItemException {
        final Map<String, AttributeValue> attributeMap = readRaw(itemId, itemClass);
        try {
            final T item = marshallIntoObject(itemClass, attributeMap);
            final String tableName = databaseSchemaHolder.schemaName() + "."
                    + getItemConfiguration(itemClass).tableName();
            LoggingUtils.logReadItemFromDatabase(tableName, item);
            return item;
        } catch (final ItemClassDiscriminatorMismatchException e) {
            throw new NonExistentItemException(
                    String.format("The item of type [%s] with id [%s] does not exist", itemClass.getName(), itemId));
        }
    }

    private Map<String, AttributeValue> readRaw(final ItemId itemId, final Class<? extends Item> itemClass) {
        return readRaw(itemId, itemClass, Collections.<String> emptyList());
    }

    private Map<String, AttributeValue> readRaw(final ItemId itemId, final Class<? extends Item> itemClass,
            final Collection<String> attributes) {
        final ItemConfiguration itemConfiguration = getItemConfiguration(itemClass);
        final String tableName = databaseSchemaHolder.schemaName() + "." + itemConfiguration.tableName();
        final GetItemRequest getItemRequest = new GetItemRequest(tableName, generateKey(itemId, itemConfiguration));
        if (attributes.size() > 0) {
            getItemRequest.withAttributesToGet(attributes);

        }

        final GetItemResult getItemResult;
        try {
            getItemResult = amazonDynamoDbClient.getItem(getItemRequest);
        } catch (final AmazonServiceException e) {
            throw new PersistenceResourceFailureException(
                    "Failure while attempting to read from DynamoDB table (" + tableName + ")", e);
        }

        if (getItemResult == null || getItemResult.getItem() == null) {
            throw new NonExistentItemException(
                    String.format("The item of type [%s] with id [%s] does not exist", itemClass.getName(), itemId));
        } else {
            return getItemResult.getItem();
        }
    }

    private Map<String, AttributeValue> generateKey(final ItemId itemId, final ItemConfiguration itemConfiguration) {
        final PrimaryKeyDefinition primaryKeyDefinition = itemConfiguration.primaryKeyDefinition();
        final AttributeValue keyValue = new AttributeValue();
        final ScalarAttributeType keyAttributeType = getAttributeType(primaryKeyDefinition.propertyType());
        switch (keyAttributeType) {
            case N:
                keyValue.withN(itemId.value());
                break;
            default:
                keyValue.withS(itemId.value());
                break;
        }
        final Map<String, AttributeValue> key = new HashMap<>();
        key.put(primaryKeyDefinition.propertyName(), keyValue);

        final AttributeValue supportingKeyValue = new AttributeValue();
        if (CompoundPrimaryKeyDefinition.class.isAssignableFrom(primaryKeyDefinition.getClass())) {
            final CompoundPrimaryKeyDefinition compoundPrimaryKeyDefinition = (CompoundPrimaryKeyDefinition) primaryKeyDefinition;
            final ScalarAttributeType supportingKeyAttributeType = getAttributeType(
                    compoundPrimaryKeyDefinition.propertyType());
            switch (supportingKeyAttributeType) {
                case N:
                    supportingKeyValue.withN(itemId.supportingValue());
                    break;
                default:
                    supportingKeyValue.withS(itemId.supportingValue());
                    break;
            }
            key.put(compoundPrimaryKeyDefinition.supportingPropertyName(), supportingKeyValue);
        }
        return key;
    }

    private <T extends Item> T marshallIntoObject(final Class<T> itemClass,
            final Map<String, AttributeValue> itemAttributeMap) throws ItemClassDiscriminatorMismatchException {
        ItemConfiguration itemConfiguration = getItemConfiguration(itemClass);
        Class<? extends T> actualItemClass = itemClass;
        if (ParentItemConfiguration.class.isAssignableFrom(itemConfiguration.getClass())) {
            final ParentItemConfiguration parentItemConfiguration = (ParentItemConfiguration) itemConfiguration;
            final AttributeValue discriminatorAttribute = itemAttributeMap.get(parentItemConfiguration.discriminator());
            if (discriminatorAttribute != null) {
                actualItemClass = parentItemConfiguration.getVariantItemClass(discriminatorAttribute.getS());
                itemConfiguration = getItemConfiguration(actualItemClass);
            }
        } else if (VariantItemConfiguration.class.isAssignableFrom(itemConfiguration.getClass())) {
            final VariantItemConfiguration variantItemConfiguration = (VariantItemConfiguration) itemConfiguration;
            final AttributeValue discriminatorAttribute = itemAttributeMap
                    .get(variantItemConfiguration.parentItemConfiguration().discriminator());
            if (discriminatorAttribute == null || !((VariantItemConfiguration) itemConfiguration).discriminatorValue()
                    .equals(discriminatorAttribute.getS())) {
                throw new ItemClassDiscriminatorMismatchException();
            }
        }
        try {
            final T item = actualItemClass.newInstance();
            for (final PropertyDescriptor propertyDescriptor : itemConfiguration.propertyDescriptors()) {
                final AttributeValue attributeValue = itemAttributeMap.get(propertyDescriptor.getName());
                DynamoDbPropertyMarshaller.setValue(item, propertyDescriptor, attributeValue);
            }
            return item;
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private <T extends Item> Collection<T> marshallIntoObjects(final Class<T> itemClass,
            final Collection<Map<String, AttributeValue>> itemAttributeMaps) {
        final Collection<T> items = new ArrayList<>();
        for (final Map<String, AttributeValue> itemAttributeMap : itemAttributeMaps) {
            try {
                final T item = marshallIntoObject(itemClass, itemAttributeMap);
                items.add(item);
            } catch (final ItemClassDiscriminatorMismatchException e) {
                logger.debug("Rejecting item due to incorrect child class type");
            }
        }
        return items;
    }

    @Override
    public <T extends Item> T create(final T item,
            final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {
        final ItemConfiguration itemConfiguration = getItemConfiguration(item.getClass());
        final Collection<PropertyDescriptor> createdConstraintPropertyDescriptors = createUniqueConstraintIndexes(item,
                itemConfiguration);
        final Map<String, ExpectedAttributeValue> expectedResults = new HashMap<>();
        expectedResults.put(itemConfiguration.primaryKeyDefinition().propertyName(), new ExpectedAttributeValue(false));
        final Map<String, AttributeValue> attributeMap = getAttributeMap(item, itemConfiguration, 1l);
        final String tableName = databaseSchemaHolder.schemaName() + "." + itemConfiguration.tableName();
        final PutItemRequest itemRequest = new PutItemRequest().withTableName(tableName).withItem(attributeMap)
                .withExpected(expectedResults);
        boolean itemRequestSucceeded = false;
        try {
            LoggingUtils.logWriteItemToDatabase(tableName, item);
            amazonDynamoDbClient.putItem(itemRequest);
            itemRequestSucceeded = true;
        } catch (final ConditionalCheckFailedException conditionalCheckFailedException) {
            throw new ItemConstraintViolationException(itemConfiguration.primaryKeyDefinition().propertyName(),
                    "Failure to create item as store already contains item with matching primary key");
        } catch (final AmazonServiceException amazonServiceException) {
            throw new PersistenceResourceFailureException(
                    "Failure while attempting DynamoDb put (create: " + tableName + ")", amazonServiceException);
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

    private Map<String, AttributeValue> getAttributeMap(final Item item, final ItemConfiguration itemConfiguration,
            final Long version) {
        final Map<String, AttributeValue> attributeMap = new HashMap<>();
        for (final PropertyDescriptor propertyDescriptor : itemConfiguration.propertyDescriptors()) {
            final String propertyName = propertyDescriptor.getName();
            if (propertyName.equals(VERSION_ATTRIBUTE)) {
                attributeMap.put(propertyName, new AttributeValue().withN(String.valueOf(version)));
            } else if (propertyDescriptor.getWriteMethod() != null) {
                final AttributeValue attributeValue = DynamoDbPropertyMarshaller.getValue(item, propertyDescriptor);
                if (attributeMap != null) {
                    attributeMap.put(propertyName, attributeValue);
                }
            }
        }
        if (VariantItemConfiguration.class.isAssignableFrom(itemConfiguration.getClass())) {
            final VariantItemConfiguration variantItemConfiguration = (VariantItemConfiguration) itemConfiguration;
            attributeMap.put(variantItemConfiguration.parentItemConfiguration().discriminator(),
                    new AttributeValue(variantItemConfiguration.discriminatorValue()));
        }
        return attributeMap;
    }

    private Map<String, AttributeValueUpdate> getAttributeUpdateMap(final Item item,
            final ItemConfiguration itemConfiguration, final Long version) {
        final Map<String, AttributeValueUpdate> attributeMap = new HashMap<>();
        for (final PropertyDescriptor propertyDescriptor : itemConfiguration.propertyDescriptors()) {
            final String propertyName = propertyDescriptor.getName();
            if (propertyName.equals(VERSION_ATTRIBUTE)) {
                attributeMap.put(propertyName, new AttributeValueUpdate().withAction(AttributeAction.PUT)
                        .withValue(new AttributeValue().withN(String.valueOf(version))));
            } else if (propertyDescriptor.getWriteMethod() != null) {
                final AttributeValue attributeValue = DynamoDbPropertyMarshaller.getValue(item, propertyDescriptor);
                if (attributeMap != null) {
                    // TODO Only add to attribute map if there is a difference
                    if (attributeValue != null) {
                        attributeMap.put(propertyName,
                                new AttributeValueUpdate().withAction(AttributeAction.PUT).withValue(attributeValue));
                    } else {
                        attributeMap.put(propertyName, new AttributeValueUpdate().withAction(AttributeAction.DELETE));
                    }
                }
            }
        }
        if (VariantItemConfiguration.class.isAssignableFrom(itemConfiguration.getClass())) {
            final VariantItemConfiguration variantItemConfiguration = (VariantItemConfiguration) itemConfiguration;
            attributeMap.put(variantItemConfiguration.parentItemConfiguration().discriminator(),
                    new AttributeValueUpdate().withAction(AttributeAction.PUT)
                            .withValue(new AttributeValue(variantItemConfiguration.discriminatorValue())));
        }
        return attributeMap;
    }

    @Override
    public <T extends Item> T update(final T item,
            final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {
        final ItemConfiguration itemConfiguration = getItemConfiguration(item.getClass());
        if (item.getVersion() == null) {
            return create(item);
        }
        final Collection<PropertyDescriptor> updatedUniqueConstraintPropertyDescriptors = new HashSet<>();
        T previousItem = null;
        if (!itemConfiguration.uniqueConstraints().isEmpty()) {
            final ItemId itemId = itemConfiguration.getItemId(item);
            previousItem = readWithOnlyUniqueConstraintProperties(itemId, itemConfiguration);
            final Collection<UniqueConstraint> updatedUniqueConstraints = getUpdatedUniqueConstraints(item,
                    previousItem, itemConfiguration);
            for (final UniqueConstraint uniqueConstraint : updatedUniqueConstraints) {
                updatedUniqueConstraintPropertyDescriptors.add(uniqueConstraint.propertyDescriptor());
            }
            createUniqueConstraintIndexes(item, itemConfiguration, updatedUniqueConstraintPropertyDescriptors);
        }
        final long newVersion = item.getVersion() + 1;
        final Map<String, AttributeValueUpdate> attributeMap = getAttributeUpdateMap(item, itemConfiguration,
                newVersion);
        final Map<String, ExpectedAttributeValue> expectedResults = new HashMap<>();
        expectedResults.put(VERSION_ATTRIBUTE,
                new ExpectedAttributeValue(new AttributeValue().withN(String.valueOf(item.getVersion()))));
        final String tableName = databaseSchemaHolder.schemaName() + "." + itemConfiguration.tableName();
        final Map<String, AttributeValue> key = generateKey(itemConfiguration.getItemId(item), itemConfiguration);
        for (final Entry<String, AttributeValue> entry : key.entrySet()) {
            attributeMap.remove(entry.getKey());
        }
        final UpdateItemRequest itemRequest = new UpdateItemRequest().withTableName(tableName).withKey(key)
                .withAttributeUpdates(attributeMap).withExpected(expectedResults);
        boolean itemRequestSucceeded = false;
        try {
            LoggingUtils.logWriteItemToDatabase(tableName, item);
            amazonDynamoDbClient.updateItem(itemRequest);
            itemRequestSucceeded = true;
        } catch (final ConditionalCheckFailedException conditionalCheckFailedException) {
            throw new OptimisticLockException("Conflicting write detected while updating item");
        } catch (final AmazonServiceException amazonServiceException) {
            throw new PersistenceResourceFailureException(
                    "Failure while attempting DynamoDb Put (update item: " + tableName + ")", amazonServiceException);
        } finally {
            if (!itemRequestSucceeded) {
                try {
                    deleteUniqueConstraintIndexes(item, itemConfiguration, updatedUniqueConstraintPropertyDescriptors);
                } catch (final Exception deleteUniqueConstraintIndexesException) {
                    logger.error(deleteUniqueConstraintIndexesException.getMessage(),
                            deleteUniqueConstraintIndexesException);
                }
            }
        }
        deleteUniqueConstraintIndexes(previousItem, itemConfiguration, updatedUniqueConstraintPropertyDescriptors);
        item.setVersion(newVersion);
        return item;
    }

    @SuppressWarnings("unchecked")
    private <T extends Item> T readWithOnlyUniqueConstraintProperties(final ItemId itemId,
            final ItemConfiguration itemConfiguration) {
        final Collection<String> attributesToGet = new ArrayList<>();
        attributesToGet.add(VERSION_ATTRIBUTE);
        for (final UniqueConstraint uniqueConstraint : itemConfiguration.uniqueConstraints()) {
            attributesToGet.add(uniqueConstraint.propertyName());
        }
        final Class<T> itemClass = (Class<T>) itemConfiguration.itemClass();
        final Map<String, AttributeValue> attributeMap = readRaw(itemId, itemClass, attributesToGet);
        if (itemConfiguration instanceof VariantItemConfiguration) {
            final String discriminator = ((VariantItemConfiguration) itemConfiguration).parentItemConfiguration()
                    .discriminator();
            final String discriminatorValue = ((VariantItemConfiguration) itemConfiguration).discriminatorValue();
            attributeMap.put(discriminator, new AttributeValue(discriminatorValue));
        }
        try {
            return marshallIntoObject(itemClass, attributeMap);
        } catch (final ItemClassDiscriminatorMismatchException e) {
            throw new NonExistentItemException(
                    String.format("The item of type [%s] with id [%s] does not exist", itemClass.getName(), itemId));
        }
    }

    public <T extends Item> Collection<UniqueConstraint> getUpdatedUniqueConstraints(final T item, final T previousItem,
            final ItemConfiguration itemConfiguration) {
        final Map<String, AttributeValue> previousItemAttributeMap = getAttributeMap(previousItem, itemConfiguration,
                item.getVersion());
        if (!previousItemAttributeMap.get(VERSION_ATTRIBUTE).getN().equals(String.valueOf(item.getVersion()))) {
            throw new ConditionalCheckFailedException("Version attribute has changed: Conflict!");
        }
        final Map<String, AttributeValue> updateItemAttributeMap = getAttributeMap(item, itemConfiguration,
                item.getVersion());
        final Collection<String> updatedProperties = getUpdateProperties(previousItemAttributeMap,
                updateItemAttributeMap);
        final Collection<UniqueConstraint> updatedUniqueConstraints = new HashSet<>();
        for (final UniqueConstraint uniqueConstraint : itemConfiguration.uniqueConstraints()) {
            final String propertyName = uniqueConstraint.propertyDescriptor().getName();
            if (updatedProperties.contains(propertyName)) {
                final AttributeValue previousAttributeValue = previousItemAttributeMap.get(propertyName);
                final AttributeValue updatedAttributeValue = updateItemAttributeMap.get(propertyName);
                if (previousAttributeValue == null || updatedAttributeValue == null
                        || !previousAttributeValue.getS().equalsIgnoreCase(updatedAttributeValue.getS())) {
                    updatedUniqueConstraints.add(uniqueConstraint);
                }
            }
        }
        return updatedUniqueConstraints;
    }

    private Collection<String> getUpdateProperties(final Map<String, AttributeValue> previousItemAttributeMap,
            final Map<String, AttributeValue> newItemAttributeMap) {
        final Collection<String> updatedProperties = new ArrayList<>();
        for (final Entry<String, AttributeValue> entry : previousItemAttributeMap.entrySet()) {
            final String propertyName = entry.getKey();
            final AttributeValue previousAttributeValue = entry.getValue();
            final AttributeValue newAttributeValue = newItemAttributeMap.get(propertyName);
            boolean propertyUpdated = false;
            if (previousAttributeValue != null) {
                propertyUpdated = !previousAttributeValue.equals(newAttributeValue);
            } else {
                propertyUpdated = newAttributeValue != null;
            }
            if (propertyUpdated) {
                updatedProperties.add(propertyName);
            }
        }
        return updatedProperties;
    }

    @Override
    public void delete(final Item item, final PersistenceExceptionHandler<?>... persistenceExceptionHandlers) {
        final ItemConfiguration itemConfiguration = getItemConfiguration(item.getClass());
        final ItemId itemId = itemConfiguration.getItemId(item);
        final Map<String, AttributeValue> key = new HashMap<>();
        final PrimaryKeyDefinition primaryKeyDefinition = itemConfiguration.primaryKeyDefinition();
        key.put(primaryKeyDefinition.propertyName(), new AttributeValue(itemId.value()));
        if (CompoundPrimaryKeyDefinition.class.isAssignableFrom(primaryKeyDefinition.getClass())) {
            final CompoundPrimaryKeyDefinition compoundPrimaryKeyDefinition = (CompoundPrimaryKeyDefinition) primaryKeyDefinition;
            key.put(compoundPrimaryKeyDefinition.supportingPropertyName(),
                    new AttributeValue(itemId.supportingValue()));
        }
        final Map<String, ExpectedAttributeValue> expectedResults = new HashMap<>();
        expectedResults.put(VERSION_ATTRIBUTE,
                new ExpectedAttributeValue(new AttributeValue().withN(String.valueOf(item.getVersion()))));
        final String tableName = databaseSchemaHolder.schemaName() + "." + itemConfiguration.tableName();
        final DeleteItemRequest deleteItemRequest = new DeleteItemRequest().withTableName(tableName).withKey(key)
                .withExpected(expectedResults);
        try {
            LoggingUtils.logWriteItemToDatabase(tableName, item);
            amazonDynamoDbClient.deleteItem(deleteItemRequest);
        } catch (final ConditionalCheckFailedException e) {
            throw new OptimisticLockException("Conflicting write detected while deleting item");
        } catch (final AmazonServiceException e) {
            throw new PersistenceResourceFailureException(
                    "Failure while attempting DynamoDb Delete (" + tableName + "):", e);
        }

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

        if (!query.getCondition().containsNonNullOrEmptyValues()) {
            return new ArrayList<>();
        }

        final Map<String, com.amazonaws.services.dynamodbv2.model.Condition> conditions;
        try {
            conditions = createDynamoDbConditionsMap(query, itemConfiguration);
        } catch (final Exception e) {
            throw new PersistenceResourceFailureException("Failure while attempting DynamoDb Query (" + query + ")", e);
        }

        final List<T> totalItems = new ArrayList<>();
        Map<String, AttributeValue> lastEvaluatedKey = null;
        final String tableName = databaseSchemaHolder.schemaName() + "." + itemConfiguration.tableName();
        if (itemConfiguration.hasIndexForQuery(query)) {
            do {
                final String queryAttributeName = query.getAttributeName();
                final QueryRequest queryRequest = new QueryRequest().withTableName(tableName)
                        .withKeyConditions(conditions).withExclusiveStartKey(lastEvaluatedKey);
                if (isQueryOnIndex(query, itemConfiguration, queryAttributeName)) {
                    final String indexName = itemConfiguration.indexNameForQuery(query);
                    queryRequest.withIndexName(indexName);
                }

                final QueryResult queryResult;
                try {
                    queryResult = amazonDynamoDbClient.query(queryRequest);
                } catch (final AmazonServiceException e) {
                    throw new PersistenceResourceFailureException(
                            "Failure while attempting DynamoDb Query (" + tableName + ")", e);
                }
                totalItems.addAll(marshallIntoObjects(itemClass, queryResult.getItems()));
                lastEvaluatedKey = queryResult.getLastEvaluatedKey();
            } while (lastEvaluatedKey != null);

        } else {
            logger.debug("Performing table scan with query: " + query);
            do {
                final ScanRequest scanRequest = new ScanRequest().withTableName(tableName).withScanFilter(conditions)
                        .withExclusiveStartKey(lastEvaluatedKey);
                final ScanResult scanResult;
                try {
                    scanResult = amazonDynamoDbClient.scan(scanRequest);
                } catch (final AmazonServiceException e) {
                    throw new PersistenceResourceFailureException(
                            "Failure while attempting DynamoDb Scan (" + tableName + ")", e);
                }
                totalItems.addAll(marshallIntoObjects(itemClass, scanResult.getItems()));
                lastEvaluatedKey = scanResult.getLastEvaluatedKey();
            } while (lastEvaluatedKey != null);
        }
        LoggingUtils.logReadItemFromDatabase(tableName, totalItems);
        return totalItems;
    }

    public <T extends Item> Collection<T> executeQuery(final KeySetQuery query, final Class<T> itemClass) {
        final ItemConfiguration itemConfiguration = getItemConfiguration(itemClass);
        final Collection<Map<String, AttributeValue>> keys = new ArrayList<>();
        if (query.itemIds().size() == 0) {
            return new ArrayList<>();
        }
        final PrimaryKeyDefinition primaryKeyDefinition = itemConfiguration.primaryKeyDefinition();
        for (final ItemId itemId : query.itemIds()) {
            final Map<String, AttributeValue> key = new HashMap<>();
            key.put(primaryKeyDefinition.propertyName(), new AttributeValue(itemId.value()));
            if (CompoundPrimaryKeyDefinition.class.isAssignableFrom(primaryKeyDefinition.getClass())) {
                final CompoundPrimaryKeyDefinition compoundPrimaryKeyDefinition = (CompoundPrimaryKeyDefinition) primaryKeyDefinition;
                key.put(compoundPrimaryKeyDefinition.supportingPropertyName(),
                        new AttributeValue(itemId.supportingValue()));
            }
            keys.add(key);
        }
        final Map<String, KeysAndAttributes> requestItems = new HashMap<>();
        final KeysAndAttributes keysAndAttributes = new KeysAndAttributes();
        keysAndAttributes.setKeys(keys);
        final String tableName = databaseSchemaHolder.schemaName() + "." + itemConfiguration.tableName();
        requestItems.put(tableName, keysAndAttributes);
        final BatchGetItemRequest batchGetItemRequest = new BatchGetItemRequest().withRequestItems(requestItems);
        final BatchGetItemResult batchGetItemResult;
        try {
            batchGetItemResult = amazonDynamoDbClient.batchGetItem(batchGetItemRequest);
        } catch (final AmazonServiceException e) {
            throw new PersistenceResourceFailureException(
                    "Failure while attempting DynamoDb Batch Get Item (" + tableName + ")", e);
        }
        final List<Map<String, AttributeValue>> itemAttributeMaps = batchGetItemResult.getResponses().get(tableName);
        final Collection<T> fetchedItems = marshallIntoObjects(itemClass, itemAttributeMaps);
        LoggingUtils.logReadItemFromDatabase(tableName, fetchedItems);
        return fetchedItems;
    }

    /**
     * Turns the items into DynamoDb PutRequests to allow them to be batch written. These are then bound inside a
     * BatchWriteItemRequest which allows us to batch write them into DynamoDB. Any requests in the batch that fail to
     * write are removed from the results, then each successfully written Property has it's version set accordingly.
     * Maps are used to keep track of which versions belong to which items, and also a map to keep track of which
     * PutRequest object relates to which Item, so we can remove unsuccessful writes. This will throw an
     * IllegalArgumentException if the item being batch written has unique constraints. This method does not implement
     * row-level locking, you will need to implement your own locking to ensure consistency is achieved.
     */
    @Override
    public <T extends Item> List<T> batchWrite(final List<T> items, final Class<T> itemClass)
            throws IllegalArgumentException, PersistenceResourceFailureException {
        final ItemConfiguration itemConfiguration = getItemConfiguration(itemClass);
        final List<T> itemsWritten = new ArrayList<T>();
        final Map<T, Long> itemVersions = new HashMap<T, Long>();
        final Map<String, List<WriteRequest>> requestItems = new HashMap<String, List<WriteRequest>>();
        final Map<PutRequest, T> itemPutRequests = new HashMap<PutRequest, T>();

        if (!itemConfiguration.uniqueConstraints().isEmpty()) {
            throw new IllegalArgumentException("Cannot perform batch write for item of type" + itemClass);
        }

        createRequestItems(itemConfiguration, itemVersions, requestItems, items, itemPutRequests);

        final BatchWriteItemRequest itemRequest = new BatchWriteItemRequest().withRequestItems(requestItems);

        try {
            final BatchWriteItemResult itemResult = amazonDynamoDbClient.batchWriteItem(itemRequest);
            removeUnprocessedItems(itemsWritten, itemVersions, itemPutRequests, itemResult);
        } catch (final AmazonServiceException amazonServiceException) {
            throw new PersistenceResourceFailureException(
                    "Failed to do Dynamo DB batch write (" + itemConfiguration.tableName() + ")",
                    amazonServiceException);
        }

        // any items that were successfully processed will need their versions setting.
        for (final T item : itemsWritten) {
            item.setVersion(itemVersions.get(item));
        }

        return itemsWritten;
    }

    /**
     * This method removes items which did not process in the batch write. The results of the batch write tell us which
     * PutRequests were not processed, and from this we can use our maps to find out which items these belonged to.
     * These items can then be removed from the results and their versions won't be updated.
     * @param itemsWritten - the successfully written items
     * @param itemVersions - the map of version to items, so we know which versions belong to which items
     * @param itemPutRequests - the map of put requests to items, so we know which put request relates to which item
     * @param itemResult - the result of the batch write, we use this to get the unprocessed items
     */
    private <T extends Item> void removeUnprocessedItems(final List<T> itemsWritten, final Map<T, Long> itemVersions,
            final Map<PutRequest, T> itemPutRequests, final BatchWriteItemResult itemResult) {
        if (itemResult != null && itemResult.getUnprocessedItems() != null) {
            for (final String tableName : itemResult.getUnprocessedItems().keySet()) {
                for (final WriteRequest writeRequest : itemResult.getUnprocessedItems().get(tableName)) {
                    itemVersions.remove(itemPutRequests.get(writeRequest.getPutRequest()));
                    itemPutRequests.remove(writeRequest.getPutRequest());
                }
            }

            itemsWritten.addAll(itemPutRequests.values());
        }

    }

    /**
     * Creates the PutRequests to allow us to perform the batch write. Takes a batch of items and creates a bunch of
     * WriteRequests for the DynamoDB table we're writing to, then attaches PutRequests to these WriteRequests. We keep
     * a track of the versions and PutRequests for each item so we can process the results afterwards.
     * @param itemConfiguration - allows us to know which table we're writing to
     * @param itemVersions - keeps track of which version belongs to which item
     * @param requestItems - the requests to be written to DynamoDB, map of table name as the key and the WriteRequest
     *            as the value.
     * @param batch - the batch of items to be converted into WriteRequests with PutRequests.
     * @param itemPutRequests - a map to keep track of which PutRequest belongs to which item to allow us to process the
     *            results.
     */
    private <T extends Item> void createRequestItems(final ItemConfiguration itemConfiguration,
            final Map<T, Long> itemVersions, final Map<String, List<WriteRequest>> requestItems, final List<T> batch,
            final Map<PutRequest, T> itemPutRequests) {
        for (final T item : batch) {
            final long newVersion = item.getVersion() != null ? item.getVersion() + 1 : 1l;
            final Map<String, AttributeValue> attributeMap = getAttributeMap(item, itemConfiguration, newVersion);
            final String tableName = databaseSchemaHolder.schemaName() + "." + itemConfiguration.tableName();
            List<WriteRequest> writeRequestsForTable = requestItems.get(tableName);
            if (writeRequestsForTable == null) {
                writeRequestsForTable = new ArrayList<WriteRequest>();
                requestItems.put(tableName, writeRequestsForTable);
            }

            final PutRequest putRequest = new PutRequest().withItem(attributeMap);
            itemPutRequests.put(putRequest, item);
            itemVersions.put(item, newVersion);
            writeRequestsForTable.add(new WriteRequest().withPutRequest(putRequest));
        }
    }

    private boolean isQueryOnIndex(final AttributeQuery query, final ItemConfiguration itemConfiguration,
            final String queryAttributeName) {
        return !(itemConfiguration.primaryKeyDefinition().propertyName().equals(queryAttributeName)
                && !(query instanceof CompoundAttributeQuery));
    }

    private <T extends Item> Map<String, com.amazonaws.services.dynamodbv2.model.Condition> createDynamoDbConditionsMap(
            final AttributeQuery query, final ItemConfiguration itemConfiguration) throws Exception {
        final Map<String, com.amazonaws.services.dynamodbv2.model.Condition> conditions = new HashMap<>();

        final com.amazonaws.services.dynamodbv2.model.Condition condition = createDynamoDbCondition(
                query.getCondition(), query.getAttributeName(), itemConfiguration);
        conditions.put(query.getAttributeName(), condition);

        if (CompoundAttributeQuery.class.isAssignableFrom(query.getClass())) {
            final CompoundAttributeQuery compoundAttributeQuery = (CompoundAttributeQuery) query;
            final com.amazonaws.services.dynamodbv2.model.Condition supportingCondition = createDynamoDbCondition(
                    compoundAttributeQuery.getSupportingCondition(),
                    compoundAttributeQuery.getSupportingAttributeName(), itemConfiguration);
            conditions.put(compoundAttributeQuery.getSupportingAttributeName(), supportingCondition);
        }

        return conditions;
    }

    private <T extends Item> com.amazonaws.services.dynamodbv2.model.Condition createDynamoDbCondition(
            final Condition condition, final String propertyName, final ItemConfiguration itemConfiguration)
                    throws Exception {
        final com.amazonaws.services.dynamodbv2.model.Condition dynamoDbCondition = new com.amazonaws.services.dynamodbv2.model.Condition();

        if (condition.getComparisonOperator() == Operators.NULL) {
            dynamoDbCondition.setComparisonOperator(ComparisonOperator.NULL);
        } else if (condition.getComparisonOperator() == Operators.NOT_NULL) {
            dynamoDbCondition.setComparisonOperator(ComparisonOperator.NOT_NULL);
        } else {
            if (condition.getComparisonOperator() == Operators.EQUALS) {
                dynamoDbCondition.setComparisonOperator(ComparisonOperator.EQ);
            } else if (condition.getComparisonOperator() == Operators.LESS_THAN_OR_EQUALS) {
                dynamoDbCondition.setComparisonOperator(ComparisonOperator.LE);
            } else if (condition.getComparisonOperator() == Operators.GREATER_THAN_OR_EQUALS) {
                dynamoDbCondition.setComparisonOperator(ComparisonOperator.GE);
            }

            final Collection<AttributeValue> attributeValueList = new ArrayList<>();

            for (final String stringValue : condition.getValues()) {
                if (stringValue != null && !stringValue.isEmpty()) {
                    final PropertyDescriptor propertyDescriptor = itemConfiguration.getPropertyDescriptor(propertyName);
                    final AttributeValue attributeValue = DynamoDbPropertyMarshaller.getAttributeValue(stringValue,
                            propertyDescriptor);
                    attributeValueList.add(attributeValue);
                }
            }

            dynamoDbCondition.setAttributeValueList(attributeValueList);
        }

        return dynamoDbCondition;
    }
}
