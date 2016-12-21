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
package com.clicktravel.infrastructure.persistence.aws.dynamodb.integration;

import static com.clicktravel.common.random.Randoms.randomBoolean;
import static com.clicktravel.common.random.Randoms.randomId;
import static com.clicktravel.common.random.Randoms.randomInt;
import static com.clicktravel.common.random.Randoms.randomString;

import java.util.*;

import org.mockito.internal.util.collections.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.*;
import com.clicktravel.infrastructure.persistence.aws.dynamodb.*;

public class DynamoDbDataGenerator {

    private final String unitTestSchemaName = "unittest";
    private final String stubItemTableName = "stub_item_" + randomString(10);
    private final String stubItemWithRangeTableName = "stub_item_with_range_" + randomString(10);
    private final String stubItemWithGsiTableName = "stub_item_with_gsi_" + randomString(10);

    public final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Collection<String> createdItemIds = new ArrayList<>();

    private final AmazonDynamoDBClient amazonDynamoDbClient;

    public DynamoDbDataGenerator(final AmazonDynamoDBClient amazonDynamoDbClient) {
        this.amazonDynamoDbClient = amazonDynamoDbClient;
    }

    public String getUnitTestSchemaName() {
        return unitTestSchemaName;
    }

    public String getStubItemTableName() {
        return stubItemTableName;
    }

    public String getStubItemWithRangeTableName() {
        return stubItemWithRangeTableName;
    }

    public String getStubItemWithGsiTableName() {
        return stubItemWithGsiTableName;
    }

    public Collection<String> getCreatedItemIds() {
        return createdItemIds;
    }

    public void createStubItemTable() throws Exception {
        final String tableName = unitTestSchemaName + "." + stubItemTableName;
        boolean tableCreated = false;
        try {
            final DescribeTableResult result = amazonDynamoDbClient.describeTable(tableName);
            if (isTableCreated(tableName, result)) {
                tableCreated = true;
            }
        } catch (final ResourceNotFoundException e) {
            tableCreated = false;
        }
        if (!tableCreated) {
            final Collection<AttributeDefinition> attributeDefinitions = new ArrayList<>();
            attributeDefinitions.add(new AttributeDefinition("id", ScalarAttributeType.S));
            final Collection<KeySchemaElement> keySchema = new ArrayList<>();
            keySchema.add(new KeySchemaElement("id", "S").withKeyType(KeyType.HASH));
            final CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
                    .withAttributeDefinitions(attributeDefinitions).withKeySchema(keySchema)
                    .withProvisionedThroughput(new ProvisionedThroughput(10L, 10L));
            amazonDynamoDbClient.createTable(createTableRequest);

            final long startTime = System.currentTimeMillis();
            do {
                Thread.sleep(1000);
                final DescribeTableResult describeTableResult = amazonDynamoDbClient.describeTable(tableName);
                tableCreated = isTableCreated(tableName, describeTableResult);
            } while (!tableCreated && System.currentTimeMillis() - startTime < 60000);
        }
    }

    public void createStubItemWithRangeTable() throws Exception {
        final String tableName = unitTestSchemaName + "." + stubItemWithRangeTableName;
        boolean tableCreated = false;
        try {
            final DescribeTableResult result = amazonDynamoDbClient.describeTable(tableName);
            if (isTableCreated(tableName, result)) {
                tableCreated = true;
            }
        } catch (final ResourceNotFoundException e) {
            tableCreated = false;
        }
        if (!tableCreated) {
            final Collection<AttributeDefinition> attributeDefinitions = new ArrayList<>();
            attributeDefinitions.add(new AttributeDefinition("id", ScalarAttributeType.S));
            attributeDefinitions.add(new AttributeDefinition("supportingId", ScalarAttributeType.S));
            final Collection<KeySchemaElement> keySchema = new ArrayList<>();
            keySchema.add(new KeySchemaElement("id", "S").withKeyType(KeyType.HASH));
            keySchema.add(new KeySchemaElement("supportingId", "S").withKeyType(KeyType.RANGE));
            final CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
                    .withAttributeDefinitions(attributeDefinitions).withKeySchema(keySchema)
                    .withProvisionedThroughput(new ProvisionedThroughput(10L, 10L));
            amazonDynamoDbClient.createTable(createTableRequest);

            final long startTime = System.currentTimeMillis();
            do {
                Thread.sleep(1000);
                final DescribeTableResult describeTableResult = amazonDynamoDbClient.describeTable(tableName);
                tableCreated = isTableCreated(tableName, describeTableResult);
            } while (!tableCreated && System.currentTimeMillis() - startTime < 60000);
        }
    }

    public void createStubItemWithGlobalSecondaryIndexTable() throws Exception {
        final String tableName = unitTestSchemaName + "." + stubItemWithGsiTableName;
        boolean tableCreated = false;
        try {
            final DescribeTableResult result = amazonDynamoDbClient.describeTable(tableName);
            if (isTableCreated(tableName, result)) {
                tableCreated = true;
            }
        } catch (final ResourceNotFoundException e) {
            tableCreated = false;
        }
        if (!tableCreated) {
            final Collection<AttributeDefinition> attributeDefinitions = new ArrayList<>();
            attributeDefinitions.add(new AttributeDefinition("id", ScalarAttributeType.S));
            attributeDefinitions.add(new AttributeDefinition("gsi", ScalarAttributeType.S));
            attributeDefinitions.add(new AttributeDefinition("gsiSupportingValue", ScalarAttributeType.N));
            final Collection<KeySchemaElement> keySchema = new ArrayList<>();
            keySchema.add(new KeySchemaElement("id", "S").withKeyType(KeyType.HASH));
            final GlobalSecondaryIndex globalSecondaryIndex = new GlobalSecondaryIndex();
            final Collection<KeySchemaElement> globalSecondaryIndexKeySchema = new ArrayList<>();
            globalSecondaryIndexKeySchema.add(new KeySchemaElement("gsi", "S").withKeyType(KeyType.HASH));
            globalSecondaryIndexKeySchema
                    .add(new KeySchemaElement("gsiSupportingValue", "N").withKeyType(KeyType.RANGE));
            globalSecondaryIndex.setIndexName("gsi_gsiSupportingValue_idx");
            globalSecondaryIndex.setKeySchema(globalSecondaryIndexKeySchema);
            globalSecondaryIndex.setProvisionedThroughput(new ProvisionedThroughput(10L, 10L));
            final Projection projection = new Projection();
            projection.setProjectionType(ProjectionType.ALL);
            globalSecondaryIndex.setProjection(projection);
            final CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
                    .withAttributeDefinitions(attributeDefinitions).withKeySchema(keySchema)
                    .withGlobalSecondaryIndexes(globalSecondaryIndex)
                    .withProvisionedThroughput(new ProvisionedThroughput(10L, 10L));
            amazonDynamoDbClient.createTable(createTableRequest);

            final long startTime = System.currentTimeMillis();
            do {
                Thread.sleep(1000);
                final DescribeTableResult describeTableResult = amazonDynamoDbClient.describeTable(tableName);
                tableCreated = isTableCreated(tableName, describeTableResult);
            } while (!tableCreated && System.currentTimeMillis() - startTime < 60000);
        }
    }

    private boolean isTableCreated(final String fullStubItemTableName, final DescribeTableResult describeTableResult) {
        return fullStubItemTableName.equals(describeTableResult.getTable().getTableName())
                && "ACTIVE".equals(describeTableResult.getTable().getTableStatus());
    }

    public void deletedCreatedItems() {
        for (final String id : createdItemIds) {
            final Map<String, AttributeValue> key = new HashMap<>();
            key.put("id", new AttributeValue(id));
            final DeleteItemRequest deleteItemRequest = new DeleteItemRequest()
                    .withTableName(unitTestSchemaName + "." + stubItemTableName).withKey(key);
            try {
                amazonDynamoDbClient.deleteItem(deleteItemRequest);
            } catch (final Exception e) {
                // Ignore
            }
        }
    }

    public StubItem randomStubItem() {
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        stubItem.setStringProperty(randomString(10));
        stubItem.setStringProperty2(randomString(10));
        stubItem.setBooleanProperty(randomBoolean());
        stubItem.setStringSetProperty(new HashSet<>(Sets.newSet(randomString(10), randomString(10), randomString(10))));
        stubItem.setVersion((long) randomInt(100));
        return stubItem;
    }

    public StubItem createStubItem() {
        final StubItem stubItem = randomStubItem();
        dynamoCreateItem(stubItem);
        return stubItem;
    }

    public StubItem createStubItemWithStringProperty(final String stringProperty) {
        final StubItem stubItem = randomStubItem();
        stubItem.setStringProperty(stringProperty);
        dynamoCreateItem(stubItem);
        return stubItem;
    }

    public StubItem createStubItemWithNullValues() {
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        stubItem.setVersion((long) randomInt(100));
        dynamoCreateItem(stubItem);
        return stubItem;
    }

    public StubVariantItem createStubVariantItem() {
        final StubVariantItem stubItem = new StubVariantItem();
        stubItem.setId(randomId());
        stubItem.setStringProperty(randomString(10));
        stubItem.setStringProperty2(randomString(10));
        stubItem.setVersion((long) randomInt(100));
        final Map<String, AttributeValue> itemMap = new HashMap<>();
        itemMap.put("id", new AttributeValue(stubItem.getId()));
        if (stubItem.getStringProperty() != null) {
            itemMap.put("stringProperty", new AttributeValue(stubItem.getStringProperty()));
        }
        if (stubItem.getStringProperty2() != null) {
            itemMap.put("stringProperty2", new AttributeValue(stubItem.getStringProperty2()));
        }
        itemMap.put("version", new AttributeValue().withN(String.valueOf(stubItem.getVersion())));
        itemMap.put("discriminator", new AttributeValue().withS("a"));
        final PutItemRequest putItemRequest = new PutItemRequest()
                .withTableName(unitTestSchemaName + "." + stubItemTableName).withItem(itemMap);
        amazonDynamoDbClient.putItem(putItemRequest);
        logger.debug("Created stub item with id: " + stubItem.getId());
        createdItemIds.add(stubItem.getId());
        return stubItem;
    }

    public StubVariantTwoItem createStubVariantTwoItem() {
        final StubVariantTwoItem stubItem = new StubVariantTwoItem();
        stubItem.setId(randomId());
        stubItem.setStringProperty(randomString(10));
        stubItem.setStringPropertyTwo(randomString(10));
        stubItem.setVersion((long) randomInt(100));
        final Map<String, AttributeValue> itemMap = new HashMap<>();
        itemMap.put("id", new AttributeValue(stubItem.getId()));
        if (stubItem.getStringProperty() != null) {
            itemMap.put("stringProperty", new AttributeValue(stubItem.getStringProperty()));
        }
        if (stubItem.getStringPropertyTwo() != null) {
            itemMap.put("stringPropertyTwo", new AttributeValue(stubItem.getStringPropertyTwo()));
        }
        itemMap.put("version", new AttributeValue().withN(String.valueOf(stubItem.getVersion())));
        itemMap.put("discriminator", new AttributeValue().withS("b"));
        final PutItemRequest putItemRequest = new PutItemRequest()
                .withTableName(unitTestSchemaName + "." + stubItemTableName).withItem(itemMap);
        amazonDynamoDbClient.putItem(putItemRequest);
        logger.debug("Created stub item with id: " + stubItem.getId());
        createdItemIds.add(stubItem.getId());
        return stubItem;
    }

    public StubWithRangeItem createStubWithRangeItem() {
        final StubWithRangeItem stubItem = new StubWithRangeItem();
        stubItem.setId(randomId());
        stubItem.setSupportingId(randomId());
        stubItem.setStringProperty(randomString(10));
        stubItem.setBooleanProperty(randomBoolean());
        stubItem.setStringSetProperty(new HashSet<>(Sets.newSet(randomString(10), randomString(10), randomString(10))));
        stubItem.setVersion((long) randomInt(100));
        final Map<String, AttributeValue> itemMap = new HashMap<>();
        itemMap.put("id", new AttributeValue(stubItem.getId()));
        itemMap.put("supportingId", new AttributeValue(stubItem.getSupportingId()));
        if (stubItem.getStringProperty() != null) {
            itemMap.put("stringProperty", new AttributeValue(stubItem.getStringProperty()));
        }
        itemMap.put("booleanProperty", new AttributeValue().withN(stubItem.isBooleanProperty() ? "1" : "0"));
        if (!stubItem.getStringSetProperty().isEmpty()) {
            itemMap.put("stringSetProperty", new AttributeValue().withSS(stubItem.getStringSetProperty()));
        }
        itemMap.put("version", new AttributeValue().withN(String.valueOf(stubItem.getVersion())));
        final PutItemRequest putItemRequest = new PutItemRequest()
                .withTableName(unitTestSchemaName + "." + stubItemWithRangeTableName).withItem(itemMap);
        amazonDynamoDbClient.putItem(putItemRequest);
        logger.debug("Created stub item with id: " + stubItem.getId());
        createdItemIds.add(stubItem.getId());
        return stubItem;
    }

    public StubItem createStubItemWithExtraValues() {
        final StubItem stubItem = randomStubItem();
        final Map<String, AttributeValue> itemMap = new HashMap<>();
        itemMap.put("id", new AttributeValue(stubItem.getId()));
        if (stubItem.getStringProperty() != null) {
            itemMap.put("stringProperty", new AttributeValue(stubItem.getStringProperty()));
        }
        if (stubItem.getStringProperty2() != null) {
            itemMap.put("stringProperty2", new AttributeValue(stubItem.getStringProperty2()));
        }
        itemMap.put("booleanProperty", new AttributeValue().withN(stubItem.isBooleanProperty() ? "1" : "0"));
        if (!stubItem.getStringSetProperty().isEmpty()) {
            itemMap.put("stringSetProperty", new AttributeValue().withSS(stubItem.getStringSetProperty()));
        }
        // Add random properties
        for (int i = 0; i < randomInt(10); i++) {
            itemMap.put(randomString(10), new AttributeValue(randomString()));
        }
        itemMap.put("version", new AttributeValue().withN(String.valueOf(stubItem.getVersion())));
        final PutItemRequest putItemRequest = new PutItemRequest()
                .withTableName(unitTestSchemaName + "." + stubItemTableName).withItem(itemMap);
        amazonDynamoDbClient.putItem(putItemRequest);
        logger.debug("Created stub item with id: " + stubItem.getId());
        createdItemIds.add(stubItem.getId());
        return stubItem;

    }

    public StubWithGlobalSecondaryIndexItem randomStubWithGlobalSecondaryIndexItem() {
        final StubWithGlobalSecondaryIndexItem stubItem = new StubWithGlobalSecondaryIndexItem();
        stubItem.setId(randomId());
        stubItem.setGsi(randomString(10));
        stubItem.setGsiSupportingValue(randomInt(100));
        stubItem.setVersion((long) randomInt(100));
        return stubItem;
    }

    private void dynamoCreateItem(final StubItem stubItem) {
        final Map<String, AttributeValue> itemMap = new HashMap<>();
        itemMap.put("id", new AttributeValue(stubItem.getId()));
        if (stubItem.getStringProperty() != null) {
            itemMap.put("stringProperty", new AttributeValue(stubItem.getStringProperty()));
        }
        if (stubItem.getStringProperty2() != null) {
            itemMap.put("stringProperty2", new AttributeValue(stubItem.getStringProperty2()));
        }
        itemMap.put("booleanProperty", new AttributeValue().withN(stubItem.isBooleanProperty() ? "1" : "0"));
        if (!stubItem.getStringSetProperty().isEmpty()) {
            itemMap.put("stringSetProperty", new AttributeValue().withSS(stubItem.getStringSetProperty()));
        }
        itemMap.put("version", new AttributeValue().withN(String.valueOf(stubItem.getVersion())));
        final PutItemRequest putItemRequest = new PutItemRequest()
                .withTableName(unitTestSchemaName + "." + stubItemTableName).withItem(itemMap);
        amazonDynamoDbClient.putItem(putItemRequest);
        logger.debug("Created stub item with id: " + stubItem.getId());
        createdItemIds.add(stubItem.getId());
    }

    public void deleteStubItemTable() {
        amazonDynamoDbClient.deleteTable(unitTestSchemaName + "." + stubItemTableName);
    }

    public void deleteStubItemWithRangeTable() {
        amazonDynamoDbClient.deleteTable(unitTestSchemaName + "." + stubItemWithRangeTableName);
    }

}
