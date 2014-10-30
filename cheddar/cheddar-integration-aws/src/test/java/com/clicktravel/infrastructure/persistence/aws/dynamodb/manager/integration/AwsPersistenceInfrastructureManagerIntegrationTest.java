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
package com.clicktravel.infrastructure.persistence.aws.dynamodb.manager.integration;

import static com.clicktravel.common.random.Randoms.randomInt;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.*;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.*;
import com.clicktravel.common.random.Randoms;
import com.clicktravel.infrastructure.integration.aws.AwsIntegration;
import com.clicktravel.infrastructure.persistence.aws.dynamodb.DynamoDbTemplate;
import com.clicktravel.infrastructure.persistence.aws.dynamodb.StubItem;
import com.clicktravel.infrastructure.persistence.aws.dynamodb.manager.DynamoDbTemplateInfrastructureManager;

@Category({ AwsIntegration.class })
public class AwsPersistenceInfrastructureManagerIntegrationTest {

    private static final String UNITTEST_SCHEMA_NAME = "unittest";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private AmazonDynamoDB amazonDynamoDbClient;
    private String tableName;
    private String fullItemTableName;
    private String fullSequenceTableName;
    private String fullIndexTableName;
    private int readThroughput;
    private int writeThroughput;

    @Before
    public void setup() {
        amazonDynamoDbClient = new AmazonDynamoDBClient(new BasicAWSCredentials(AwsIntegration.getAccessKeyId(),
                AwsIntegration.getSecretKeyId()));
        amazonDynamoDbClient.setEndpoint(AwsIntegration.getDynamoDbEndpoint());
        tableName = Randoms.randomString(10);
        fullItemTableName = UNITTEST_SCHEMA_NAME + "." + tableName;
        readThroughput = randomInt(9) + 1;
        writeThroughput = randomInt(9) + 1;
    }

    @Test
    public void shouldCreateTable_withPrimaryKey() {
        // given
        final ItemConfiguration stubItemConfiguration = new ItemConfiguration(StubItem.class, tableName);

        final Collection<ItemConfiguration> itemConfigurations = new ArrayList<>(Arrays.asList(stubItemConfiguration));
        final DatabaseSchemaHolder databaseSchemaHolder = new DatabaseSchemaHolder(UNITTEST_SCHEMA_NAME,
                itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);

        final Collection<DynamoDbTemplate> dynamodbTemplates = new ArrayList<>(Arrays.asList(dynamoDbTemplate));
        final DynamoDbTemplateInfrastructureManager persistenceManager = new DynamoDbTemplateInfrastructureManager(
                amazonDynamoDbClient, readThroughput, writeThroughput);
        persistenceManager.setDynamoDbTemplates(dynamodbTemplates);

        // when
        persistenceManager.init();
        logger.debug("created: " + tableName);

        // then
        final DescribeTableResult result = amazonDynamoDbClient.describeTable(new DescribeTableRequest(
                fullItemTableName));
        assertEquals(fullItemTableName, result.getTable().getTableName());
        assertEquals(1, result.getTable().getKeySchema().size());
        assertThat(result.getTable().getKeySchema(), hasItem(new KeySchemaElement("id", KeyType.HASH)));
        assertThat(result.getTable().getProvisionedThroughput().getReadCapacityUnits(), is((long) readThroughput));
        assertThat(result.getTable().getProvisionedThroughput().getWriteCapacityUnits(), is((long) writeThroughput));
    }

    @Test
    public void shouldCreateTable_withCompoundPrimaryKey() {

        // given
        final ItemConfiguration stubItemConfiguration = new ItemConfiguration(StubItem.class, tableName,
                new CompoundPrimaryKeyDefinition("id", "stringProperty"));

        final Collection<ItemConfiguration> itemConfigurations = new ArrayList<>(Arrays.asList(stubItemConfiguration));
        final DatabaseSchemaHolder databaseSchemaHolder = new DatabaseSchemaHolder(UNITTEST_SCHEMA_NAME,
                itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);

        final Collection<DynamoDbTemplate> dynamodbTemplates = new ArrayList<>(Arrays.asList(dynamoDbTemplate));
        final DynamoDbTemplateInfrastructureManager persistenceManager = new DynamoDbTemplateInfrastructureManager(
                amazonDynamoDbClient, readThroughput, writeThroughput);
        persistenceManager.setDynamoDbTemplates(dynamodbTemplates);

        // when
        persistenceManager.init();
        logger.debug("created: " + tableName);

        // then
        final DescribeTableResult result = amazonDynamoDbClient.describeTable(new DescribeTableRequest(
                fullItemTableName));
        assertEquals(fullItemTableName, result.getTable().getTableName());
        assertEquals(2, result.getTable().getKeySchema().size());
        assertThat(result.getTable().getKeySchema(), hasItem(new KeySchemaElement("id", KeyType.HASH)));
        assertThat(result.getTable().getKeySchema(), hasItem(new KeySchemaElement("stringProperty", KeyType.RANGE)));
        assertThat(result.getTable().getProvisionedThroughput().getReadCapacityUnits(), is((long) readThroughput));
        assertThat(result.getTable().getProvisionedThroughput().getWriteCapacityUnits(), is((long) writeThroughput));
    }

    @Test
    public void shouldCreateTable_withLongAttributeIndex() {

        // given
        final ItemConfiguration stubItemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final IndexDefinition versionPropertyIndex = new IndexDefinition("version");
        final Collection<IndexDefinition> indexes = new ArrayList<>();
        indexes.add(versionPropertyIndex);

        stubItemConfiguration.registerIndexes(indexes);

        final Collection<ItemConfiguration> itemConfigurations = new ArrayList<>(Arrays.asList(stubItemConfiguration));
        final DatabaseSchemaHolder databaseSchemaHolder = new DatabaseSchemaHolder(UNITTEST_SCHEMA_NAME,
                itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);

        final Collection<DynamoDbTemplate> dynamodbTemplates = new ArrayList<>(Arrays.asList(dynamoDbTemplate));
        final DynamoDbTemplateInfrastructureManager persistenceManager = new DynamoDbTemplateInfrastructureManager(
                amazonDynamoDbClient, readThroughput, writeThroughput);
        persistenceManager.setDynamoDbTemplates(dynamodbTemplates);

        // when
        persistenceManager.init();
        logger.debug("created: " + tableName);

        // then
        final DescribeTableResult result = amazonDynamoDbClient.describeTable(new DescribeTableRequest(
                fullItemTableName));
        assertEquals(fullItemTableName, result.getTable().getTableName());
        assertEquals("version_idx", result.getTable().getGlobalSecondaryIndexes().get(0).getIndexName());
        assertThat(result.getTable().getGlobalSecondaryIndexes().get(0).getProvisionedThroughput()
                .getReadCapacityUnits(), is((long) readThroughput));
        assertThat(result.getTable().getGlobalSecondaryIndexes().get(0).getProvisionedThroughput()
                .getWriteCapacityUnits(), is((long) writeThroughput));
        assertThat(result.getTable().getProvisionedThroughput().getReadCapacityUnits(), is((long) readThroughput));
        assertThat(result.getTable().getProvisionedThroughput().getWriteCapacityUnits(), is((long) writeThroughput));
    }

    @Test
    public void shouldCreateTable_withStringAttributeIndex() {

        // given
        final ItemConfiguration stubItemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final IndexDefinition stringPropertyIndex = new IndexDefinition("stringProperty");
        final Collection<IndexDefinition> indexes = new ArrayList<>();
        indexes.add(stringPropertyIndex);

        stubItemConfiguration.registerIndexes(indexes);

        final Collection<ItemConfiguration> itemConfigurations = new ArrayList<>(Arrays.asList(stubItemConfiguration));
        final DatabaseSchemaHolder databaseSchemaHolder = new DatabaseSchemaHolder(UNITTEST_SCHEMA_NAME,
                itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);

        final Collection<DynamoDbTemplate> dynamodbTemplates = new ArrayList<>(Arrays.asList(dynamoDbTemplate));
        final DynamoDbTemplateInfrastructureManager persistenceManager = new DynamoDbTemplateInfrastructureManager(
                amazonDynamoDbClient, readThroughput, writeThroughput);
        persistenceManager.setDynamoDbTemplates(dynamodbTemplates);

        // when
        persistenceManager.init();
        logger.debug("created: " + tableName);
        // then
        final DescribeTableResult result = amazonDynamoDbClient.describeTable(new DescribeTableRequest(
                fullItemTableName));
        assertEquals(fullItemTableName, result.getTable().getTableName());
        assertEquals("stringProperty_idx", result.getTable().getGlobalSecondaryIndexes().get(0).getIndexName());
        assertThat(result.getTable().getGlobalSecondaryIndexes().get(0).getProvisionedThroughput()
                .getReadCapacityUnits(), is((long) readThroughput));
        assertThat(result.getTable().getGlobalSecondaryIndexes().get(0).getProvisionedThroughput()
                .getWriteCapacityUnits(), is((long) writeThroughput));
        assertThat(result.getTable().getProvisionedThroughput().getReadCapacityUnits(), is((long) readThroughput));
        assertThat(result.getTable().getProvisionedThroughput().getWriteCapacityUnits(), is((long) writeThroughput));
    }

    @Test
    public void shouldCreateTable_withBooleanAttributeIndex() {

        // given
        final ItemConfiguration stubItemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final IndexDefinition booleanPropertyIndex = new IndexDefinition("booleanProperty");
        final Collection<IndexDefinition> indexes = new ArrayList<>();
        indexes.add(booleanPropertyIndex);

        stubItemConfiguration.registerIndexes(indexes);

        final Collection<ItemConfiguration> itemConfigurations = new ArrayList<>(Arrays.asList(stubItemConfiguration));
        final DatabaseSchemaHolder databaseSchemaHolder = new DatabaseSchemaHolder(UNITTEST_SCHEMA_NAME,
                itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);

        final Collection<DynamoDbTemplate> dynamodbTemplates = new ArrayList<>(Arrays.asList(dynamoDbTemplate));
        final DynamoDbTemplateInfrastructureManager persistenceManager = new DynamoDbTemplateInfrastructureManager(
                amazonDynamoDbClient, readThroughput, writeThroughput);
        persistenceManager.setDynamoDbTemplates(dynamodbTemplates);

        // when
        persistenceManager.init();
        logger.debug("created: " + tableName);

        // then
        final DescribeTableResult result = amazonDynamoDbClient.describeTable(new DescribeTableRequest(
                fullItemTableName));
        assertEquals(fullItemTableName, result.getTable().getTableName());
        assertEquals("booleanProperty_idx", result.getTable().getGlobalSecondaryIndexes().get(0).getIndexName());
        assertThat(result.getTable().getGlobalSecondaryIndexes().get(0).getProvisionedThroughput()
                .getReadCapacityUnits(), is((long) readThroughput));
        assertThat(result.getTable().getGlobalSecondaryIndexes().get(0).getProvisionedThroughput()
                .getWriteCapacityUnits(), is((long) writeThroughput));
        assertThat(result.getTable().getProvisionedThroughput().getReadCapacityUnits(), is((long) readThroughput));
        assertThat(result.getTable().getProvisionedThroughput().getWriteCapacityUnits(), is((long) writeThroughput));
    }

    @Test
    public void shouldCreateTable_withMultipleAttributeIndex() {

        // given
        final ItemConfiguration stubItemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final IndexDefinition stringPropertyIndex = new IndexDefinition("stringProperty");
        final IndexDefinition stringProperty2Index = new IndexDefinition("stringProperty2");
        final Collection<IndexDefinition> indexes = new ArrayList<>();
        indexes.add(stringPropertyIndex);
        indexes.add(stringProperty2Index);

        stubItemConfiguration.registerIndexes(indexes);

        final Collection<ItemConfiguration> itemConfigurations = new ArrayList<>(Arrays.asList(stubItemConfiguration));
        final DatabaseSchemaHolder databaseSchemaHolder = new DatabaseSchemaHolder(UNITTEST_SCHEMA_NAME,
                itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);

        final Collection<DynamoDbTemplate> dynamodbTemplates = new ArrayList<>(Arrays.asList(dynamoDbTemplate));
        final DynamoDbTemplateInfrastructureManager persistenceManager = new DynamoDbTemplateInfrastructureManager(
                amazonDynamoDbClient, readThroughput, writeThroughput);
        persistenceManager.setDynamoDbTemplates(dynamodbTemplates);

        // when
        persistenceManager.init();
        logger.debug("created: " + tableName);

        // then
        final DescribeTableResult result = amazonDynamoDbClient.describeTable(new DescribeTableRequest(
                fullItemTableName));
        assertEquals(fullItemTableName, result.getTable().getTableName());
        assertEquals(2, result.getTable().getGlobalSecondaryIndexes().size());
        assertThat(result.getTable().getGlobalSecondaryIndexes().get(0).getProvisionedThroughput()
                .getReadCapacityUnits(), is((long) readThroughput));
        assertThat(result.getTable().getGlobalSecondaryIndexes().get(0).getProvisionedThroughput()
                .getWriteCapacityUnits(), is((long) writeThroughput));
        assertThat(result.getTable().getGlobalSecondaryIndexes().get(1).getProvisionedThroughput()
                .getReadCapacityUnits(), is((long) readThroughput));
        assertThat(result.getTable().getGlobalSecondaryIndexes().get(1).getProvisionedThroughput()
                .getWriteCapacityUnits(), is((long) writeThroughput));
        assertThat(result.getTable().getProvisionedThroughput().getReadCapacityUnits(), is((long) readThroughput));
        assertThat(result.getTable().getProvisionedThroughput().getWriteCapacityUnits(), is((long) writeThroughput));
    }

    @Test
    public void shouldCreateTable_withSequences() {
        // Given
        final String schemaName = "unittest_" + randomString(10);
        final String sequenceName = randomString(10);
        final int startingValue = randomInt(10);
        final SequenceConfiguration sequenceConfiguration = new SequenceConfiguration(sequenceName, startingValue);
        final ItemConfiguration stubItemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = new ArrayList<>(Arrays.asList(stubItemConfiguration));
        final Collection<SequenceConfiguration> sequenceConfigurations = new ArrayList<>(
                Arrays.asList(sequenceConfiguration));
        final DatabaseSchemaHolder databaseSchemaHolder = new DatabaseSchemaHolder(schemaName, itemConfigurations,
                sequenceConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);

        final Collection<DynamoDbTemplate> dynamodbTemplates = new ArrayList<>(Arrays.asList(dynamoDbTemplate));
        final DynamoDbTemplateInfrastructureManager persistenceManager = new DynamoDbTemplateInfrastructureManager(
                amazonDynamoDbClient, readThroughput, writeThroughput);
        persistenceManager.setDynamoDbTemplates(dynamodbTemplates);
        fullItemTableName = schemaName + "." + tableName;
        fullSequenceTableName = schemaName + "-sequences";

        // When
        persistenceManager.init();

        // Then
        final DescribeTableResult describeTableResult = amazonDynamoDbClient.describeTable(new DescribeTableRequest(
                fullSequenceTableName));
        final ScanResult scanResult = amazonDynamoDbClient.scan(new ScanRequest(fullSequenceTableName));
        assertEquals(fullSequenceTableName, describeTableResult.getTable().getTableName());
        assertThat(describeTableResult.getTable().getAttributeDefinitions(), hasItems(new AttributeDefinition("name",
                "S")));
        assertNull(scanResult.getLastEvaluatedKey());
        assertEquals(1, scanResult.getItems().size());
        final Map<String, AttributeValue> sequenceMap = scanResult.getItems().iterator().next();
        assertEquals(sequenceName, sequenceMap.get("name").getS());
        assertEquals(String.valueOf(startingValue - 1), sequenceMap.get("currentValue").getN());
        assertThat(describeTableResult.getTable().getProvisionedThroughput().getReadCapacityUnits(),
                is((long) readThroughput));
        assertThat(describeTableResult.getTable().getProvisionedThroughput().getWriteCapacityUnits(),
                is((long) writeThroughput));
    }

    @Test
    public void shouldCreateTable_withUniqueConstraints() {
        // Given
        final String schemaName = "unittest_" + randomString(10);
        final ItemConfiguration stubItemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final UniqueConstraint uniqueConstraint = new UniqueConstraint("stringProperty2");
        final Collection<UniqueConstraint> uniqueConstraints = new ArrayList<>(Arrays.asList(uniqueConstraint));
        stubItemConfiguration.registerUniqueConstraints(uniqueConstraints);
        final Collection<ItemConfiguration> itemConfigurations = new ArrayList<>(Arrays.asList(stubItemConfiguration));

        final DatabaseSchemaHolder databaseSchemaHolder = new DatabaseSchemaHolder(schemaName, itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);

        final Collection<DynamoDbTemplate> dynamodbTemplates = new ArrayList<>(Arrays.asList(dynamoDbTemplate));
        final DynamoDbTemplateInfrastructureManager persistenceManager = new DynamoDbTemplateInfrastructureManager(
                amazonDynamoDbClient, readThroughput, writeThroughput);
        persistenceManager.setDynamoDbTemplates(dynamodbTemplates);
        fullItemTableName = schemaName + "." + tableName;
        fullIndexTableName = schemaName + "-indexes." + tableName;

        // When
        persistenceManager.init();

        // Then
        final DescribeTableResult describeTableResult = amazonDynamoDbClient.describeTable(new DescribeTableRequest(
                fullIndexTableName));
        assertEquals(fullIndexTableName, describeTableResult.getTable().getTableName());
        assertThat(describeTableResult.getTable().getKeySchema(), hasItems(new KeySchemaElement("property",
                KeyType.HASH)));
        assertThat(describeTableResult.getTable().getKeySchema(),
                hasItems(new KeySchemaElement("value", KeyType.RANGE)));
        assertThat(describeTableResult.getTable().getAttributeDefinitions(), hasItems(new AttributeDefinition(
                "property", "S")));
        assertThat(describeTableResult.getTable().getAttributeDefinitions(), hasItems(new AttributeDefinition("value",
                "S")));
        assertThat(describeTableResult.getTable().getProvisionedThroughput().getReadCapacityUnits(),
                is((long) readThroughput));
        assertThat(describeTableResult.getTable().getProvisionedThroughput().getWriteCapacityUnits(),
                is((long) writeThroughput));
    }

    @After
    public void tearDown() {
        try {
            logger.debug("deleting: " + fullItemTableName);
            amazonDynamoDbClient.deleteTable(new DeleteTableRequest(fullItemTableName));
        } catch (final Exception e) {
            // Ignore
        }
        try {
            logger.debug("deleting: " + fullSequenceTableName);
            amazonDynamoDbClient.deleteTable(new DeleteTableRequest(fullSequenceTableName));
        } catch (final Exception e) {
            // Ignore
        }
        try {
            logger.debug("deleting: " + fullIndexTableName);
            amazonDynamoDbClient.deleteTable(new DeleteTableRequest(fullIndexTableName));
        } catch (final Exception e) {
            // Ignore
        }
    }

}
