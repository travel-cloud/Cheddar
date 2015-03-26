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
package com.clicktravel.infrastructure.persistence.aws.dynamodb.manager;

import static com.amazonaws.services.dynamodbv2.datamodeling.DynamoDbPropertyMarshaller.getAttributeType;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.*;
import com.clicktravel.infrastructure.persistence.aws.dynamodb.AbstractDynamoDbTemplate;

/**
 * Amazon Web Services Persistence Infrastructure Manager
 *
 * The responsibility of implementing classes is to create tables
 */
public class DynamoDbTemplateInfrastructureManager {

    private static final long TABLE_CREATION_TIMEOUT_MS = 60000;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AmazonDynamoDB amazonDynamoDbClient;
    private final Collection<AbstractDynamoDbTemplate> dynamoDbTemplates;
    private final int readThroughput;
    private final int writeThroughput;

    public DynamoDbTemplateInfrastructureManager(final AmazonDynamoDB amazonDynamoDbClient, final int readThroughput,
            final int writeThroughput) {
        this.amazonDynamoDbClient = amazonDynamoDbClient;
        dynamoDbTemplates = new HashSet<>();
        if (readThroughput < 1 || writeThroughput < 1) {
            throw new IllegalStateException("Throughput values must not be less than 1");
        }
        this.readThroughput = readThroughput;
        this.writeThroughput = writeThroughput;
    }

    public void setDynamoDbTemplates(final Collection<AbstractDynamoDbTemplate> dynamoDbTemplates) {
        if (dynamoDbTemplates != null) {
            this.dynamoDbTemplates.addAll(dynamoDbTemplates);
        }
    }

    public void init() {
        for (final AbstractDynamoDbTemplate dynamoDbTemplate : dynamoDbTemplates) {
            final Collection<String> tablesPendingCreation = new ArrayList<>();
            final DatabaseSchemaHolder databaseSchemaHolder = dynamoDbTemplate.databaseSchemaHolder();
            for (final ItemConfiguration itemConfiguration : databaseSchemaHolder.itemConfigurations()) {
                if (VariantItemConfiguration.class.isAssignableFrom(itemConfiguration.getClass())) {
                    continue;
                }
                final String tableName = databaseSchemaHolder.schemaName() + "." + itemConfiguration.tableName();
                if (!tablesPendingCreation.contains(tableName) && !isTableCreated(tableName)) {
                    final List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
                    final PrimaryKeyDefinition primaryKeyDefinition = itemConfiguration.primaryKeyDefinition();
                    final String hashKey = primaryKeyDefinition.propertyName();
                    final ScalarAttributeType hashKeyType = getAttributeType(primaryKeyDefinition.propertyType());
                    attributeDefinitions.add(new AttributeDefinition().withAttributeName(hashKey).withAttributeType(
                            hashKeyType));
                    final List<KeySchemaElement> keySchema = new ArrayList<>();
                    keySchema.add(new KeySchemaElement().withAttributeName(hashKey).withKeyType(KeyType.HASH));
                    if (CompoundPrimaryKeyDefinition.class.isAssignableFrom(primaryKeyDefinition.getClass())) {
                        final CompoundPrimaryKeyDefinition compoundPrimaryKeyDefinition = (CompoundPrimaryKeyDefinition) primaryKeyDefinition;
                        final String rangeKey = compoundPrimaryKeyDefinition.supportingPropertyName();
                        final ScalarAttributeType rangeKeyType = getAttributeType(compoundPrimaryKeyDefinition
                                .supportingPropertyType());
                        attributeDefinitions.add(new AttributeDefinition().withAttributeName(rangeKey)
                                .withAttributeType(rangeKeyType));
                        keySchema.add(new KeySchemaElement().withAttributeName(rangeKey).withKeyType(KeyType.RANGE));
                    }

                    final Collection<GlobalSecondaryIndex> globalSecondaryIndexes = new ArrayList<>();
                    for (final IndexDefinition indexDefinition : itemConfiguration.indexDefinitions()) {
                        final ScalarAttributeType attributeType = getAttributeType(primaryKeyDefinition.propertyType());

                        // if there are any indexes, we need to add attributes for them
                        attributeDefinitions.add(new AttributeDefinition().withAttributeName(
                                indexDefinition.propertyName()).withAttributeType(attributeType));

                        final ProvisionedThroughput indexProvisionedThroughput = new ProvisionedThroughput()
                        .withReadCapacityUnits((long) readThroughput).withWriteCapacityUnits(
                                (long) writeThroughput);
                        final GlobalSecondaryIndex globalSecondaryIndex = new GlobalSecondaryIndex()
                        .withIndexName(indexDefinition.propertyName() + "_idx")
                        .withProvisionedThroughput(indexProvisionedThroughput)
                        .withProjection(new Projection().withProjectionType("ALL"));

                        final ArrayList<KeySchemaElement> indexKeySchema = new ArrayList<KeySchemaElement>();

                        indexKeySchema.add(new KeySchemaElement().withAttributeName(indexDefinition.propertyName())
                                .withKeyType(KeyType.HASH));

                        globalSecondaryIndex.setKeySchema(indexKeySchema);
                        globalSecondaryIndexes.add(globalSecondaryIndex);
                    }

                    final ProvisionedThroughput tableProvisionedThroughput = new ProvisionedThroughput()
                    .withReadCapacityUnits((long) readThroughput)
                    .withWriteCapacityUnits((long) writeThroughput);

                    CreateTableRequest request = new CreateTableRequest().withTableName(tableName)
                            .withAttributeDefinitions(attributeDefinitions).withKeySchema(keySchema)
                            .withProvisionedThroughput(tableProvisionedThroughput);

                    if (!globalSecondaryIndexes.isEmpty()) {
                        request = request.withGlobalSecondaryIndexes(globalSecondaryIndexes);
                    }

                    logger.debug("Creating table " + tableName);
                    createTable(request, globalSecondaryIndexes.isEmpty());
                    tablesPendingCreation.add(tableName);
                }

                // Create tables for unique constraints
                if (!itemConfiguration.uniqueConstraints().isEmpty()) {
                    final String uniqueConstraintTableName = databaseSchemaHolder.schemaName() + "-indexes."
                            + itemConfiguration.tableName();
                    if (!isTableCreated(uniqueConstraintTableName)) {
                        final List<KeySchemaElement> keySchema = new ArrayList<>();
                        keySchema.add(new KeySchemaElement("property", KeyType.HASH));
                        keySchema.add(new KeySchemaElement("value", KeyType.RANGE));
                        final List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
                        attributeDefinitions.add(new AttributeDefinition("property", ScalarAttributeType.S));
                        attributeDefinitions.add(new AttributeDefinition("value", ScalarAttributeType.S));
                        final ProvisionedThroughput tableProvisionedThroughput = new ProvisionedThroughput()
                        .withReadCapacityUnits((long) readThroughput).withWriteCapacityUnits(
                                (long) writeThroughput);
                        final CreateTableRequest createTableRequest = new CreateTableRequest()
                        .withTableName(uniqueConstraintTableName)
                        .withAttributeDefinitions(attributeDefinitions).withKeySchema(keySchema)
                        .withProvisionedThroughput(tableProvisionedThroughput);
                        createTable(createTableRequest, true);
                        tablesPendingCreation.add(uniqueConstraintTableName);
                    }
                }
            }

            // Create table for sequences
            if (!databaseSchemaHolder.sequenceConfigurations().isEmpty()) {
                final String sequenceTableName = databaseSchemaHolder.schemaName() + "-sequences";
                if (!isTableCreated(sequenceTableName)) {
                    final ProvisionedThroughput sequenceProvisionedThroughput = new ProvisionedThroughput()
                    .withReadCapacityUnits((long) readThroughput)
                    .withWriteCapacityUnits((long) writeThroughput);
                    final List<KeySchemaElement> keySchema = new ArrayList<>();
                    keySchema.add(new KeySchemaElement().withAttributeName("name").withKeyType(KeyType.HASH));
                    final List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
                    attributeDefinitions
                    .add(new AttributeDefinition().withAttributeName("name").withAttributeType("S"));
                    final CreateTableRequest request = new CreateTableRequest().withTableName(sequenceTableName)
                            .withAttributeDefinitions(attributeDefinitions).withKeySchema(keySchema)
                            .withProvisionedThroughput(sequenceProvisionedThroughput);
                    logger.debug("Creating sequence table " + sequenceTableName);
                    amazonDynamoDbClient.createTable(request);
                    tablesPendingCreation.add(sequenceTableName);
                }
            }

            logger.debug("Waiting for table creation: " + tablesPendingCreation);
            final Collection<String> tableNames = new ArrayList<>(tablesPendingCreation);
            final long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < TABLE_CREATION_TIMEOUT_MS) {
                for (final String tableName : tableNames) {
                    if (isTableCreated(tableName)) {
                        logger.debug("Table " + tableName + " successfully created");
                        tablesPendingCreation.remove(tableName);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (final InterruptedException e) {
                        throw new IllegalStateException(e);
                    }
                }
                if (tablesPendingCreation.size() == 0) {
                    break;
                }
                tableNames.clear();
                tableNames.addAll(tablesPendingCreation);
            }

            if (tablesPendingCreation.size() != 0) {
                throw new IllegalStateException("Unable to create tables for DynamoDb template: "
                        + tablesPendingCreation);
            }

            // Seed the sequences with their starting values
            if (!databaseSchemaHolder.sequenceConfigurations().isEmpty()) {
                final ScanRequest scanRequest = new ScanRequest(databaseSchemaHolder.schemaName() + "-sequences");
                final ScanResult scanResult = amazonDynamoDbClient.scan(scanRequest);
                Map<String, AttributeValue> lastEvaluatedKey = null;
                final Map<String, Map<String, AttributeValue>> sequenceItems = new HashMap<>();
                do {
                    for (final Map<String, AttributeValue> item : scanResult.getItems()) {
                        sequenceItems.put(item.get("name").getS(), item);
                    }
                    lastEvaluatedKey = scanResult.getLastEvaluatedKey();
                } while (lastEvaluatedKey != null);

                for (final SequenceConfiguration sequenceConfiguration : databaseSchemaHolder.sequenceConfigurations()) {
                    final Map<String, AttributeValue> sequenceItem = sequenceItems.get(sequenceConfiguration
                            .sequenceName());
                    if (sequenceItem == null) {
                        final Map<String, ExpectedAttributeValue> expectedResults = new HashMap<>();
                        expectedResults.put("name", new ExpectedAttributeValue(false));
                        final Map<String, AttributeValue> attributeMap = new HashMap<>();
                        attributeMap.put("name", new AttributeValue().withS(sequenceConfiguration.sequenceName()));
                        attributeMap.put("currentValue",
                                new AttributeValue().withN(String.valueOf(sequenceConfiguration.startingValue() - 1)));
                        final String tableName = databaseSchemaHolder.schemaName() + "-sequences";
                        final PutItemRequest itemRequest = new PutItemRequest().withTableName(tableName)
                                .withItem(attributeMap).withExpected(expectedResults);
                        amazonDynamoDbClient.putItem(itemRequest);
                    }
                }
            }

            dynamoDbTemplate.initialize(amazonDynamoDbClient);
        }
    }

    private void createTable(final CreateTableRequest createTableRequest, final boolean asynchronous) {
        boolean tableCreationError = false;
        final long startTime = System.currentTimeMillis();
        do {
            try {
                amazonDynamoDbClient.createTable(createTableRequest);
            } catch (final Exception e) {
                tableCreationError = true;
                try {
                    // wait for 10s
                    Thread.sleep(10000);
                } catch (final InterruptedException interruptedException) {
                    throw new IllegalStateException(interruptedException);
                }
            }
        } while (tableCreationError && System.currentTimeMillis() - startTime < TABLE_CREATION_TIMEOUT_MS);

        if (!asynchronous) {
            final long tableCreationStartTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - tableCreationStartTime < TABLE_CREATION_TIMEOUT_MS) {
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    throw new IllegalStateException(e);
                }
                if (isTableCreated(createTableRequest.getTableName())) {
                    break;
                }
            }
        }
    }

    private boolean isTableCreated(final String tableName) {
        try {
            final DescribeTableResult result = amazonDynamoDbClient.describeTable(new DescribeTableRequest(tableName));
            final TableDescription tableDescription = result.getTable();
            final String tableStatus = tableDescription.getTableStatus();
            final String returnedTableName = tableDescription.getTableName();
            return tableName.equals(returnedTableName) && TableStatus.ACTIVE.toString().equals(tableStatus);
        } catch (final ResourceNotFoundException e) {
            return false;
        }
    }

}
