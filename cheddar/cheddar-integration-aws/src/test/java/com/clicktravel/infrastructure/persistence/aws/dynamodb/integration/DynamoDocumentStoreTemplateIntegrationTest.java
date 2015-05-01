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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.*;

import org.junit.*;
import org.junit.experimental.categories.Category;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.clicktravel.cheddar.infrastructure.persistence.database.ItemId;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.CompoundPrimaryKeyDefinition;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.DatabaseSchemaHolder;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.ItemConfiguration;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.*;
import com.clicktravel.common.random.Randoms;
import com.clicktravel.infrastructure.integration.aws.AwsIntegration;
import com.clicktravel.infrastructure.persistence.aws.dynamodb.DynamoDocumentStoreTemplate;
import com.clicktravel.infrastructure.persistence.aws.dynamodb.StubItem;
import com.clicktravel.infrastructure.persistence.aws.dynamodb.StubWithRangeItem;

@Category({ AwsIntegration.class })
public class DynamoDocumentStoreTemplateIntegrationTest {

    private static DynamoDbDataGenerator dataGenerator;
    private final Collection<String> createdItemIds = new ArrayList<>();
    private static AmazonDynamoDBClient amazonDynamoDbClient;
    private DatabaseSchemaHolder databaseSchemaHolder;

    @BeforeClass
    public static void createTables() throws Exception {
        amazonDynamoDbClient = new AmazonDynamoDBClient(new BasicAWSCredentials(AwsIntegration.getAccessKeyId(),
                AwsIntegration.getSecretKeyId()));
        amazonDynamoDbClient.setEndpoint(AwsIntegration.getDynamoDbEndpoint());
        dataGenerator = new DynamoDbDataGenerator(amazonDynamoDbClient);

        dataGenerator.createStubItemTable();
        dataGenerator.createStubItemWithRangeTable();
    }

    @Before
    public void init() throws Exception {
        createdItemIds.clear();
        dataGenerator.getCreatedItemIds().clear();
        final Collection<ItemConfiguration> itemConfigurations = new ArrayList<>();

        final ItemConfiguration stubItemConfiguration = new ItemConfiguration(StubItem.class,
                dataGenerator.getStubItemTableName());
        final ItemConfiguration stubItemWithRangeConfiguration = new ItemConfiguration(StubWithRangeItem.class,
                dataGenerator.getStubItemWithRangeTableName(), new CompoundPrimaryKeyDefinition("id", "supportingId"));
        itemConfigurations.add(stubItemConfiguration);
        itemConfigurations.add(stubItemWithRangeConfiguration);
        databaseSchemaHolder = new DatabaseSchemaHolder(dataGenerator.getUnitTestSchemaName(), itemConfigurations);
    }

    @After
    public void tearDown() {
        dataGenerator.getCreatedItemIds().addAll(createdItemIds);
        dataGenerator.deletedCreatedItems();
    }

    @AfterClass
    public static void deleteTables() {
        dataGenerator.deleteStubItemTable();
        dataGenerator.deleteStubItemWithRangeTable();
    }

    @Test
    public void shouldCreateNewItem_withItem() {
        // Given
        final DynamoDocumentStoreTemplate dynamoDbTemplate = new DynamoDocumentStoreTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);

        final StubItem stubItem = dataGenerator.randomStubItem();
        stubItem.setVersion(null);

        // When
        final StubItem item = dynamoDbTemplate.create(stubItem);

        // Then
        createdItemIds.add(item.getId());
        assertEquals(new Long(1), item.getVersion());
        assertEquals(stubItem.getId(), item.getId());
        assertEquals(stubItem.getStringProperty(), item.getStringProperty());
        assertEquals(stubItem.getStringProperty2(), item.getStringProperty2());
        assertEquals(stubItem.getStringSetProperty(), item.getStringSetProperty());
    }

    @Test
    public void shouldReadItem_withItem() {
        // Given
        final DynamoDocumentStoreTemplate dynamoDbTemplate = new DynamoDocumentStoreTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);

        final StubItem stubItem = dataGenerator.randomStubItem();
        stubItem.setVersion(null);
        dynamoDbTemplate.create(stubItem);

        // When
        final StubItem item = dynamoDbTemplate.read(new ItemId(stubItem.getId()), StubItem.class);

        // Then
        createdItemIds.add(item.getId());
        assertEquals(new Long(1), item.getVersion());
        assertEquals(stubItem.getId(), item.getId());
        assertEquals(stubItem.getStringProperty(), item.getStringProperty());
        assertEquals(stubItem.getStringProperty2(), item.getStringProperty2());
        assertEquals(stubItem.getStringSetProperty(), item.getStringSetProperty());
    }

    @Test
    public void shouldUpdateItem_withItem() {
        // Given
        final DynamoDocumentStoreTemplate dynamoDbTemplate = new DynamoDocumentStoreTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);

        StubItem stubItem = dataGenerator.randomStubItem();
        stubItem.setVersion(null);
        stubItem = dynamoDbTemplate.create(stubItem);
        stubItem.setStringProperty(Randoms.randomString());
        stubItem.setStringProperty2(Randoms.randomString());
        final Set<String> propSet = new HashSet<String>();
        for (int i = 0; i < Randoms.randomInt(10); i++) {
            propSet.add(Randoms.randomString());
        }
        stubItem.setStringSetProperty(propSet);

        // When
        final StubItem item = dynamoDbTemplate.update(stubItem);

        // Then
        createdItemIds.add(item.getId());
        assertEquals(new Long(2), item.getVersion());
        assertEquals(stubItem.getId(), item.getId());
        assertEquals(stubItem.getStringProperty(), item.getStringProperty());
        assertEquals(stubItem.getStringProperty2(), item.getStringProperty2());
        assertEquals(stubItem.getStringSetProperty(), item.getStringSetProperty());
    }

    @Test
    public void shouldDeleteItem_withItem() {
        // Given
        final DynamoDocumentStoreTemplate dynamoDbTemplate = new DynamoDocumentStoreTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);

        StubItem stubItem = dataGenerator.randomStubItem();
        stubItem.setVersion(null);
        stubItem = dynamoDbTemplate.create(stubItem);

        // When
        dynamoDbTemplate.delete(stubItem);

        // Then
        NonExistentItemException expectedException = null;
        try {
            dynamoDbTemplate.read(new ItemId(stubItem.getId()), StubItem.class);
        } catch (final NonExistentItemException e) {
            expectedException = e;
        }
        assertNotNull(expectedException);
    }

    @Test
    public void shouldFetch_withEqualsAttributeQuery() {
        // Given
        final DynamoDocumentStoreTemplate dynamoDbTemplate = new DynamoDocumentStoreTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);

        final List<StubItem> expectedMatchingItems = new ArrayList<StubItem>();
        final String fetchCriteriaValue = Randoms.randomString(10);
        final Query query = new AttributeQuery("stringProperty", new Condition(Operators.EQUALS, fetchCriteriaValue));
        for (int i = 0; i < 20; i++) {
            final StubItem item = dataGenerator.randomStubItem();
            if (Randoms.randomBoolean() || item.getStringProperty().equals(fetchCriteriaValue)) {
                item.setStringProperty(fetchCriteriaValue);
                expectedMatchingItems.add(item);
            }
            dynamoDbTemplate.create(item);
            createdItemIds.add(item.getId());
        }

        // When
        final Collection<StubItem> allItems = dynamoDbTemplate.fetch(query, StubItem.class);

        // Then
        assertEquals(expectedMatchingItems.size(), allItems.size());
        assertTrue(allItems.containsAll(expectedMatchingItems));
    }

    @Test
    public void shouldFetch_withGreaterThanOrEqualsAttributeQuery() {
        // Given
        final DynamoDocumentStoreTemplate dynamoDbTemplate = new DynamoDocumentStoreTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);

        final List<StubItem> expectedMatchingItems = new ArrayList<StubItem>();
        final Long criteriaValue = Long.valueOf(2);
        final Query query = new AttributeQuery("version", new Condition(Operators.GREATER_THAN_OR_EQUALS,
                criteriaValue.toString()));
        for (int i = 0; i < 20; i++) {
            final StubItem item = dataGenerator.randomStubItem();
            if (Randoms.randomBoolean()) {
                expectedMatchingItems.add(item);
            }
            dynamoDbTemplate.create(item); // v1 objects
            createdItemIds.add(item.getId());
        }
        for (final StubItem matchingItem : expectedMatchingItems) {
            matchingItem.setStringProperty(Randoms.randomString(10));
            dynamoDbTemplate.update(matchingItem);// v2 objects
            if (Randoms.randomBoolean()) {
                matchingItem.setStringProperty(Randoms.randomString(10));
                dynamoDbTemplate.update(matchingItem);// v3 objects
            }
        }

        // When
        final Collection<StubItem> allItems = dynamoDbTemplate.fetch(query, StubItem.class);

        // Then
        assertEquals(expectedMatchingItems.size(), allItems.size());
        assertTrue(allItems.containsAll(expectedMatchingItems));
    }

    @Test
    public void shouldFetch_withLessThanOrEqualsAttributeQuery() {
        // Given
        final DynamoDocumentStoreTemplate dynamoDbTemplate = new DynamoDocumentStoreTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);

        final List<StubItem> expectedMatchingItems = new ArrayList<StubItem>();
        final Long criteriaLowValue = Long.valueOf(2);
        final Query query = new AttributeQuery("version", new Condition(Operators.LESS_THAN_OR_EQUALS,
                criteriaLowValue.toString()));
        for (int i = 0; i < 20; i++) {
            final StubItem item = dataGenerator.randomStubItem();
            expectedMatchingItems.add(item);
            dynamoDbTemplate.create(item);// v1 objects
            createdItemIds.add(item.getId());
        }
        final StubItem[] matchingItemsArray = expectedMatchingItems.toArray(new StubItem[expectedMatchingItems.size()]);
        for (final StubItem matchingItem : matchingItemsArray) {
            if (Randoms.randomBoolean()) {
                matchingItem.setStringProperty(Randoms.randomString(10));
                dynamoDbTemplate.update(matchingItem);// v2 objects
                if (Randoms.randomBoolean()) {
                    matchingItem.setStringProperty(Randoms.randomString(10));
                    dynamoDbTemplate.update(matchingItem);// v3 objects, not expected
                    expectedMatchingItems.remove(matchingItem);
                }
            }

        }

        // When
        final Collection<StubItem> allItems = dynamoDbTemplate.fetch(query, StubItem.class);

        // Then
        assertEquals(expectedMatchingItems.size(), allItems.size());
        assertTrue(allItems.containsAll(expectedMatchingItems));

    }

    @Test
    public void shouldFetchUnique_withEqualsAttributeQuery() {
        // Given
        final DynamoDocumentStoreTemplate dynamoDbTemplate = new DynamoDocumentStoreTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);

        StubItem expectedMatchingItem = null;
        final String fetchCriteriaValue = Randoms.randomString(10);
        final Query query = new AttributeQuery("stringProperty", new Condition(Operators.EQUALS, fetchCriteriaValue));
        final int itemCount = 20;
        final int randomIndex = Randoms.randomInt(itemCount);
        for (int i = 0; i < itemCount; i++) {
            final StubItem item = dataGenerator.randomStubItem();
            if (i == randomIndex) {
                item.setStringProperty(fetchCriteriaValue);
                expectedMatchingItem = item;
            } else {
                while (item.getStringProperty().equals(fetchCriteriaValue)) {// ensure value unique
                    item.setStringProperty(Randoms.randomString(10));
                }
            }
            dynamoDbTemplate.create(item);
            createdItemIds.add(item.getId());
        }

        // When
        final StubItem returnedItem = dynamoDbTemplate.fetchUnique(query, StubItem.class);

        // Then
        assertEquals(expectedMatchingItem, returnedItem);
    }

    @Test
    public void shouldFetch_withKeySetQueryFromDocumentStoreTemplate() {
        // Given
        // create 50 large objects to trigger multiple fetches
        final DynamoDocumentStoreTemplate dynamoDbTemplate = new DynamoDocumentStoreTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);
        final KeySetQuery q = new KeySetQuery(new ArrayList<ItemId>());
        final List<String> savedKeys = new ArrayList<String>();
        final int itemsToCreate = 50;
        for (int i = 0; i < itemsToCreate; i++) {
            final StubItem item = new StubItem();
            item.setId(Randoms.randomString());
            final char[] chars = new char[100000];
            Arrays.fill(chars, 'X');
            item.setStringProperty(new String(chars));
            dynamoDbTemplate.create(item);
            savedKeys.add(item.getId());
            q.itemIds().add(new ItemId(item.getId()));
        }

        // When
        final Collection<StubItem> allItems = dynamoDbTemplate.fetch(q, StubItem.class);

        // Then
        assertEquals(itemsToCreate, allItems.size());
        for (final StubItem i : allItems) {
            assertTrue(savedKeys.contains(i.getId()));
        }
    }
}
