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
import static com.clicktravel.common.random.Randoms.randomString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.mockito.internal.util.collections.Sets;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.clicktravel.cheddar.infrastructure.persistence.database.ItemId;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.*;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonUniqueResultException;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.*;
import com.clicktravel.common.random.Randoms;
import com.clicktravel.infrastructure.integration.aws.AwsIntegration;
import com.clicktravel.infrastructure.persistence.aws.dynamodb.*;

@Category({ AwsIntegration.class })
public class DynamoDbTemplateIntegrationTest {

    private static DynamoDbDataGenerator dataGenerator;
    private static final String STRING_PROPERTY = "stringProperty";

    private static AmazonDynamoDBClient amazonDynamoDbClient;
    private DatabaseSchemaHolder databaseSchemaHolder;

    private final Collection<String> createdItemIds = new ArrayList<>();

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
        final Collection<ItemConfiguration> itemConfigurations = new ArrayList<>();

        final ItemConfiguration stubItemConfiguration = new ItemConfiguration(StubItem.class,
                dataGenerator.getStubItemTableName());
        final ParentItemConfiguration stubParentItemConfiguration = new ParentItemConfiguration(StubParentItem.class,
                dataGenerator.getStubItemTableName());
        final ItemConfiguration stubItemWithRangeConfiguration = new ItemConfiguration(StubWithRangeItem.class,
                dataGenerator.getStubItemWithRangeTableName(), new CompoundPrimaryKeyDefinition("id", "supportingId"));
        itemConfigurations.add(stubItemConfiguration);
        itemConfigurations.add(stubParentItemConfiguration);
        itemConfigurations.add(new VariantItemConfiguration(stubParentItemConfiguration, StubVariantItem.class, "a"));
        itemConfigurations
                .add(new VariantItemConfiguration(stubParentItemConfiguration, StubVariantTwoItem.class, "b"));
        itemConfigurations.add(stubItemWithRangeConfiguration);
        databaseSchemaHolder = new DatabaseSchemaHolder(dataGenerator.getUnitTestSchemaName(), itemConfigurations);
    }

    @After
    public void tearDown() {
        dataGenerator.deletedCreatedItems();
    }

    @AfterClass
    public static void deleteTables() {
        dataGenerator.deleteStubItemTable();
        dataGenerator.deleteStubItemWithRangeTable();
    }

    @Test
    public void shouldSaveNewItem_withItem() {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);

        final StubItem stubItem = dataGenerator.randomStubItem();
        stubItem.setVersion(null);
        createdItemIds.add(stubItem.getId());

        // When
        final StubItem item = dynamoDbTemplate.create(stubItem);

        // Then
        assertEquals(new Long(1), item.getVersion());
        assertEquals(stubItem.getId(), item.getId());
        assertEquals(stubItem.getStringProperty(), item.getStringProperty());
        assertEquals(stubItem.getStringSetProperty(), item.getStringSetProperty());
    }

    @Test
    public void shouldReadBack_withSingleItem() {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);
        final StubItem createdStubItem = dataGenerator.createStubItem();
        final ItemId itemId = new ItemId(createdStubItem.getId());

        // When

        final StubItem returnedItem = dynamoDbTemplate.read(itemId, StubItem.class);

        // Then
        assertEquals(createdStubItem, returnedItem);
    }

    @Test
    public void shouldReadBack_withSingleItemAndCompoundPk() {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);
        final StubWithRangeItem createdStubWithRangeItem = dataGenerator.createStubWithRangeItem();
        final ItemId itemId = new ItemId(createdStubWithRangeItem.getId(), createdStubWithRangeItem.getSupportingId());

        // When

        final StubWithRangeItem returnedItem = dynamoDbTemplate.read(itemId, StubWithRangeItem.class);

        // Then
        assertEquals(createdStubWithRangeItem, returnedItem);
    }

    @Test
    public void shouldNotReadBack_withUnknownId() {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);
        final ItemId unknownId = new ItemId(randomId());

        // When
        NonExistentItemException expectedException = null;
        try {
            dynamoDbTemplate.read(unknownId, StubItem.class);
        } catch (final NonExistentItemException e) {
            expectedException = e;
        }

        // Then
        assertNotNull(expectedException);
    }

    @Test
    public void shouldNotReadBack_withUnknownCompoundPk() {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);
        final ItemId unknownId = new ItemId(randomId(), randomId());

        // When
        NonExistentItemException expectedException = null;
        try {
            dynamoDbTemplate.read(unknownId, StubItem.class);
        } catch (final NonExistentItemException e) {
            expectedException = e;
        }

        // Then
        assertNotNull(expectedException);
    }

    @Test
    public void shouldFetch_withKeySetQuery() {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);
        final int itemCount = 1 + Randoms.randomInt(3);
        final HashSet<StubItem> items = new HashSet<>();
        final Set<ItemId> itemIds = new HashSet<>();

        for (int n = 0; n < itemCount; n++) {
            final StubItem item = dataGenerator.createStubItem();
            items.add(item);
            itemIds.add(new ItemId(item.getId()));
        }

        final KeySetQuery query = new KeySetQuery(itemIds);

        // When
        final Collection<StubItem> returnedItems = dynamoDbTemplate.fetch(query, StubItem.class);

        // Then
        assertNotNull(returnedItems);
        assertEquals(itemCount, returnedItems.size());
        final HashSet<StubItem> returnedItemSet = new HashSet<>(returnedItems);
        assertTrue(returnedItemSet.equals(items));
    }

    @Test
    public void shouldFetch_withKeySetQueryWithCompoundPk() {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);
        final int itemCount = 1 + Randoms.randomInt(3);
        final HashSet<StubWithRangeItem> items = new HashSet<>();
        final Set<ItemId> itemIds = new HashSet<>();

        for (int n = 0; n < itemCount; n++) {
            final StubWithRangeItem item = dataGenerator.createStubWithRangeItem();
            items.add(item);
            itemIds.add(new ItemId(item.getId(), item.getSupportingId()));
        }

        final KeySetQuery query = new KeySetQuery(itemIds);

        // When
        final Collection<StubWithRangeItem> returnedItems = dynamoDbTemplate.fetch(query, StubWithRangeItem.class);

        // Then
        assertNotNull(returnedItems);
        assertEquals(itemCount, returnedItems.size());
        final HashSet<StubWithRangeItem> returnedItemSet = new HashSet<>(returnedItems);
        assertTrue(returnedItemSet.equals(items));
    }

    @Test
    public void shouldReadEmptyBatch_withNoIds() {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);
        final Set<ItemId> itemIds = new HashSet<>();
        final KeySetQuery query = new KeySetQuery(itemIds);

        // When
        final Collection<StubItem> returnedItems = dynamoDbTemplate.fetch(query, StubItem.class);

        // Then
        assertNotNull(returnedItems);
        assertEquals(0, returnedItems.size());
    }

    @Test
    public void shouldReadBatch_withUnknownIds() {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);
        final Set<ItemId> unknownIds = new HashSet<>();
        final int idCount = 1 + Randoms.randomInt(5);
        for (int n = 0; n < idCount; n++) {
            unknownIds.add(new ItemId(Randoms.randomId()));
        }
        final KeySetQuery query = new KeySetQuery(unknownIds);

        // When
        final Collection<StubItem> returnedItems = dynamoDbTemplate.fetch(query, StubItem.class);

        // Then
        assertNotNull(returnedItems);
        assertEquals(0, returnedItems.size());
    }

    @Test
    public void shouldReadEmptyBatch_withRandomKnownAndUnknownIds() {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);

        final int itemCount = 1 + Randoms.randomInt(3);
        final Set<ItemId> itemIds = new HashSet<>();
        for (int n = 0; n < itemCount; n++) {
            itemIds.add(new ItemId(dataGenerator.createStubItem().getId()));
        }

        final Set<ItemId> unknownIds = new HashSet<>();
        final int idCount = 1 + Randoms.randomInt(5);
        for (int n = 0; n < idCount; n++) {
            unknownIds.add(new ItemId(Randoms.randomId()));
        }

        final HashSet<ItemId> knownAndUnknownIds = new HashSet<>();
        knownAndUnknownIds.addAll(itemIds);
        knownAndUnknownIds.addAll(unknownIds);

        final KeySetQuery query = new KeySetQuery(knownAndUnknownIds);

        // When
        final Collection<StubItem> returnedItems = dynamoDbTemplate.fetch(query, StubItem.class);

        // Then
        assertNotNull(returnedItems);
        assertEquals(itemIds.size(), returnedItems.size());
        final Collection<ItemId> returnedItemIds = new ArrayList<>();
        for (final StubItem stubItem : returnedItems) {
            returnedItemIds.add(new ItemId(stubItem.getId()));
        }
        for (final ItemId itemId : itemIds) {
            assertTrue(returnedItemIds.contains(itemId));
        }
        for (final ItemId unknownId : unknownIds) {
            assertFalse(returnedItemIds.contains(unknownId));
        }
    }

    @Test
    public void shouldUpdate_withSingleItem() {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);
        final StubItem createdItem = dataGenerator.createStubItem();
        final Long originalVersion = createdItem.getVersion();
        final String stringProperty = randomString(10);
        final String stringProperty2 = randomString(10);
        final Set<String> newStringSetProperty = Sets.newSet(randomString(10), randomString(10), randomString(10));
        createdItem.setStringProperty(stringProperty);
        createdItem.setStringProperty2(stringProperty2);
        createdItem.setStringSetProperty(newStringSetProperty);
        final Long newVersion = originalVersion + 1;

        // When
        final StubItem updatedItem = dynamoDbTemplate.update(createdItem);

        // Then
        assertEquals(newVersion, updatedItem.getVersion());
        assertEquals(createdItem.getId(), updatedItem.getId());
        assertEquals(stringProperty, updatedItem.getStringProperty());
        assertEquals(stringProperty2, updatedItem.getStringProperty2());
        assertEquals(newStringSetProperty, updatedItem.getStringSetProperty());
    }

    @Test
    public void shouldUpdate_withSingleItemHavingLessAttributes() {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);
        final StubItem createdItem = dataGenerator.createStubItem();
        final Long originalVersion = createdItem.getVersion();
        final String stringProperty = randomString(10);
        final String stringProperty2 = randomString(10);
        final Set<String> oldStringSetProperty = createdItem.getStringSetProperty();

        final StubVariantItem updatedItem = new StubVariantItem();
        final String itemId = createdItem.getId();
        updatedItem.setId(itemId);
        updatedItem.setStringProperty(stringProperty);
        updatedItem.setStringProperty2(stringProperty2);
        updatedItem.setVersion(originalVersion);
        final Long newVersion = originalVersion + 1;

        // When
        dynamoDbTemplate.update(updatedItem);

        // Then
        final StubItem updatedItemResult = dynamoDbTemplate.read(new ItemId(itemId), StubItem.class);
        final StubVariantItem updatedVariantItemResult = dynamoDbTemplate.read(new ItemId(itemId),
                StubVariantItem.class);
        assertEquals(newVersion, updatedItemResult.getVersion());
        assertEquals(itemId, updatedItemResult.getId());
        assertEquals(stringProperty, updatedItemResult.getStringProperty());
        assertEquals(stringProperty2, updatedItemResult.getStringProperty2());
        assertEquals(oldStringSetProperty, updatedItemResult.getStringSetProperty());
        assertEquals(newVersion, updatedItemResult.getVersion());
        assertEquals(itemId, updatedVariantItemResult.getId());
        assertEquals(stringProperty, updatedVariantItemResult.getStringProperty());
        assertEquals(stringProperty2, updatedVariantItemResult.getStringProperty2());
    }

    @Test
    public void shouldUpdate_withSingleItemWithCompoundPk() {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);
        final StubWithRangeItem createdItem = dataGenerator.createStubWithRangeItem();
        final Long originalVersion = createdItem.getVersion();
        final String stringProperty = randomString(10);
        final boolean booleanProperty = randomBoolean();
        final Set<String> newStringSetProperty = Sets.newSet(randomString(10), randomString(10), randomString(10));
        createdItem.setStringProperty(stringProperty);
        createdItem.setBooleanProperty(booleanProperty);
        createdItem.setStringSetProperty(newStringSetProperty);
        final Long newVersion = originalVersion + 1;

        // When
        final StubWithRangeItem updatedItem = dynamoDbTemplate.update(createdItem);

        // Then
        assertEquals(newVersion, updatedItem.getVersion());
        assertEquals(createdItem.getId(), updatedItem.getId());
        assertEquals(stringProperty, updatedItem.getStringProperty());
        assertEquals(booleanProperty, updatedItem.isBooleanProperty());
        assertEquals(newStringSetProperty, updatedItem.getStringSetProperty());
    }

    @Test
    public void shouldFetch_withAttributeQuery() throws Exception {
        // Given
        final StubItem createdItem = dataGenerator.createStubItem();
        final String stringProperty = createdItem.getStringProperty();
        final StubItem stubItem = new StubItem();
        stubItem.setStringProperty(stringProperty);
        final Query query = new AttributeQuery(STRING_PROPERTY, new Condition(Operators.EQUALS, stringProperty));
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);

        // When
        final Collection<StubItem> itemResults = dynamoDbTemplate.fetch(query, StubItem.class);

        // Then
        assertNotNull(itemResults);
        assertEquals(1, itemResults.size());
        assertEquals(createdItem, itemResults.iterator().next());
    }

    @Test
    public void shouldGetEmptySet_withNullAttributeQuery() {
        // Given
        final String stringProperty = null;
        final Query query = new AttributeQuery(STRING_PROPERTY, new Condition(Operators.EQUALS, stringProperty));
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);

        // When
        final Collection<StubItem> itemResults = dynamoDbTemplate.fetch(query, StubItem.class);

        // Then
        assertNotNull(itemResults);
        assertEquals(0, itemResults.size());
    }

    @Test
    public void shouldGetEmptySet_withEmptyAttributeQuery() {
        // Given
        final String stringProperty = "";
        final Query query = new AttributeQuery(STRING_PROPERTY, new Condition(Operators.EQUALS, stringProperty));
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);

        // When
        final Collection<StubItem> itemResults = dynamoDbTemplate.fetch(query, StubItem.class);

        // Then
        assertNotNull(itemResults);
        assertEquals(0, itemResults.size());
    }

    @Test
    public void shouldFetch_withAttributeQueryAndMultipleItems() throws Exception {
        // Given
        final String stringProperty = randomString(10);
        final AttributeQuery query = new AttributeQuery(STRING_PROPERTY,
                new Condition(Operators.EQUALS, stringProperty));
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);
        final StubItem createdItem1 = dataGenerator.createStubItemWithStringProperty(stringProperty);
        final StubItem createdItem2 = dataGenerator.createStubItemWithStringProperty(stringProperty);

        // When
        final Collection<StubItem> itemResults = dynamoDbTemplate.fetch(query, StubItem.class);

        // Then
        assertNotNull(itemResults);
        assertEquals(2, itemResults.size());
        assertThat(itemResults, hasItems(createdItem1, createdItem2));
    }

    @Test
    public void shouldFetchUnique_withAttributeQuery() throws Exception {
        // Given
        final StubItem createdItem = dataGenerator.createStubItem();
        final String property = createdItem.getStringProperty();
        final StubItem stubItem = new StubItem();
        stubItem.setStringProperty(property);
        final AttributeQuery query = new AttributeQuery(STRING_PROPERTY, new Condition(Operators.EQUALS, property));
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);

        // When
        final StubItem itemResult = dynamoDbTemplate.fetchUnique(query, StubItem.class);

        // Then
        assertNotNull(itemResult);
        assertEquals(createdItem, itemResult);
    }

    @Test
    public void shouldNotFetchUnique_withMultipleMatches() {
        // Given
        final String stringProperty = randomString(10);
        dataGenerator.createStubItemWithStringProperty(stringProperty);
        dataGenerator.createStubItemWithStringProperty(stringProperty);
        final AttributeQuery query = new AttributeQuery(STRING_PROPERTY,
                new Condition(Operators.EQUALS, stringProperty));
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);

        // When
        NonUniqueResultException expectedException = null;
        try {
            dynamoDbTemplate.fetchUnique(query, StubItem.class);
        } catch (final NonUniqueResultException e) {
            expectedException = e;
        }

        // Then
        assertNotNull(expectedException);
    }

    @Test
    public void shouldReadItem_withNullValues() throws Exception {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);
        final StubItem createdStubItem = dataGenerator.createStubItemWithNullValues();
        final ItemId itemId = new ItemId(createdStubItem.getId());

        // When
        final StubItem item = dynamoDbTemplate.read(itemId, StubItem.class);

        // Then
        assertEquals(createdStubItem, item);
    }

    @Test
    public void shouldReadItem_withExtraValues() throws Exception {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);
        final StubItem createdStubItem = dataGenerator.createStubItemWithExtraValues();
        final ItemId itemId = new ItemId(createdStubItem.getId());

        // When
        final StubItem item = dynamoDbTemplate.read(itemId, StubItem.class);

        // Then
        assertEquals(createdStubItem, item);
    }

    @Test
    public void shouldDeleteItem_withItem() throws Exception {
        // Given
        final StubItem createdItem = dataGenerator.createStubItem();
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);

        // When
        dynamoDbTemplate.delete(createdItem);

        // Then
        final Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue(createdItem.getId()));
        final GetItemResult result = amazonDynamoDbClient.getItem(dataGenerator.getUnitTestSchemaName() + "."
                + dataGenerator.getStubItemTableName(), key);
        assertNull(result.getItem());
    }

    @Test
    public void shouldDeleteItem_withItemWithCompoundPk() throws Exception {
        // Given
        final StubWithRangeItem createdItem = dataGenerator.createStubWithRangeItem();
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);

        // When
        dynamoDbTemplate.delete(createdItem);

        // Then
        final Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue(createdItem.getId()));
        key.put("supportingId", new AttributeValue(createdItem.getSupportingId()));
        final GetItemResult result = amazonDynamoDbClient.getItem(dataGenerator.getUnitTestSchemaName() + "."
                + dataGenerator.getStubItemWithRangeTableName(), key);
        assertNull(result.getItem());
    }

    @Test
    public void shouldCreateItem_withVariantItem() throws Exception {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);
        final StubVariantItem stubVariantItem = new StubVariantItem();
        stubVariantItem.setId(randomId());
        stubVariantItem.setStringProperty(randomString(10));
        stubVariantItem.setStringProperty2(randomString(10));
        createdItemIds.add(stubVariantItem.getId());

        // When
        final StubVariantItem item = dynamoDbTemplate.create(stubVariantItem);

        //
        assertEquals(stubVariantItem, item);
    }

    @Test
    public void shouldReadItem_withVariantItem() throws Exception {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);
        final StubVariantItem createdStubVariantItem = dataGenerator.createStubVariantItem();
        final ItemId stubVariantItemId = new ItemId(createdStubVariantItem.getId());

        // When
        final StubVariantItem stubVariantItem = dynamoDbTemplate.read(stubVariantItemId, StubVariantItem.class);

        // Then
        assertEquals(createdStubVariantItem, stubVariantItem);
    }

    @Test
    public void shouldNotReadItem_withDifferentVariantItemType() throws Exception {
        // Given
        final StubVariantItem createdStubVariantItem = dataGenerator.createStubVariantItem();
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);
        final ItemId stubVariantItemId = new ItemId(createdStubVariantItem.getId());

        // When
        NonExistentItemException actualException = null;
        try {
            dynamoDbTemplate.read(stubVariantItemId, StubVariantTwoItem.class);
        } catch (final NonExistentItemException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldReadItem_withVariantItemByParent() throws Exception {
        // Given
        final StubVariantItem createdStubVariantItem = dataGenerator.createStubVariantItem();
        final ItemId stubVariantItemId = new ItemId(createdStubVariantItem.getId());
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);

        // When
        final StubParentItem stubParentItem = dynamoDbTemplate.read(stubVariantItemId, StubParentItem.class);

        // Then
        assertTrue(stubParentItem instanceof StubVariantItem);
        assertEquals(createdStubVariantItem, stubParentItem);

    }

    @Test
    public void shouldFetch_withKeySetQueryWithVariants() {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);
        final int itemCount = 1 + Randoms.randomInt(3);
        final HashSet<StubParentItem> items = new HashSet<>();
        final Set<ItemId> itemIds = new HashSet<>();

        for (int n = 0; n < itemCount; n++) {
            final StubVariantItem item = dataGenerator.createStubVariantItem();
            items.add(item);
            itemIds.add(new ItemId(item.getId()));
        }

        final KeySetQuery query = new KeySetQuery(itemIds);

        // When
        final Collection<StubParentItem> returnedItems = dynamoDbTemplate.fetch(query, StubParentItem.class);

        // Then
        assertNotNull(returnedItems);
        assertEquals(itemCount, returnedItems.size());
        final HashSet<StubParentItem> returnedItemSet = new HashSet<>(returnedItems);
        assertTrue(returnedItemSet.equals(items));
        for (final StubParentItem returnedItem : returnedItems) {
            assertTrue(returnedItem instanceof StubVariantItem);
        }
    }

    @Test
    public void shouldFetch_withKeySetQueryWithMixedVariantsByParent() {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);
        final Set<ItemId> itemIds = new HashSet<>();

        final StubVariantItem item1 = dataGenerator.createStubVariantItem();
        itemIds.add(new ItemId(item1.getId()));

        final StubVariantTwoItem item2 = dataGenerator.createStubVariantTwoItem();
        itemIds.add(new ItemId(item2.getId()));

        final KeySetQuery query = new KeySetQuery(itemIds);

        // When
        final Collection<StubParentItem> returnedItems = dynamoDbTemplate.fetch(query, StubParentItem.class);

        // Then
        assertNotNull(returnedItems);
        assertEquals(2, returnedItems.size());
        assertTrue(returnedItems.contains(item1));
        assertTrue(returnedItems.contains(item2));
        final Iterator<StubParentItem> iterator = returnedItems.iterator();
        final StubParentItem returnedItem1 = iterator.next();
        final StubParentItem returnedItem2 = iterator.next();
        if (returnedItem1 instanceof StubVariantItem) {
            assertTrue(returnedItem2 instanceof StubVariantTwoItem);
        } else {
            assertTrue(returnedItem1 instanceof StubVariantTwoItem);
            assertTrue(returnedItem2 instanceof StubVariantItem);
        }
    }

    @Test
    public void shouldFetch_withKeySetQueryWithMixedVariants_partialResults() {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);
        final HashSet<StubParentItem> items = new HashSet<>();
        final Set<ItemId> itemIds = new HashSet<>();

        final StubVariantItem item1 = dataGenerator.createStubVariantItem();
        items.add(item1);
        itemIds.add(new ItemId(item1.getId()));

        final StubVariantTwoItem item2 = dataGenerator.createStubVariantTwoItem();
        items.add(item2);
        itemIds.add(new ItemId(item2.getId()));

        final KeySetQuery query = new KeySetQuery(itemIds);

        // When
        final Collection<StubVariantTwoItem> returnedItems = dynamoDbTemplate.fetch(query, StubVariantTwoItem.class);

        // Then
        assertNotNull(returnedItems);
        assertEquals(1, returnedItems.size());
        final StubVariantTwoItem returnedItem1 = returnedItems.iterator().next();
        assertTrue(returnedItem1 instanceof StubVariantTwoItem);
    }

    @Test
    public void shouldCreate_viaBatchWrite_withSingleItem() {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);

        final StubItem stubItem = dataGenerator.randomStubItem();
        stubItem.setVersion(null);
        createdItemIds.add(stubItem.getId());

        final List<StubItem> stubItems = new ArrayList<StubItem>();
        stubItems.add(stubItem);

        // When

        final List<StubItem> successfulStubItems = dynamoDbTemplate.batchWrite(stubItems, StubItem.class);

        // Then

        for (final StubItem aStubItem : successfulStubItems) {
            assertEquals(new Long(1), aStubItem.getVersion());

            for (final StubItem initialStubItem : stubItems) {
                if (aStubItem.getId().equals(initialStubItem.getId())) {
                    assertEquals(aStubItem.getStringProperty(), initialStubItem.getStringProperty());
                    assertEquals(aStubItem.getStringSetProperty(), initialStubItem.getStringSetProperty());
                }
            }
        }
    }

    @Test
    public void shouldUpdate_viaBatchWrite_withSingleItem() {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);
        final StubItem createdItem = dataGenerator.createStubItem();
        final Long originalVersion = createdItem.getVersion();
        final String stringProperty = randomString(10);
        final String stringProperty2 = randomString(10);
        final Set<String> newStringSetProperty = Sets.newSet(randomString(10), randomString(10), randomString(10));
        createdItem.setStringProperty(stringProperty);
        createdItem.setStringProperty2(stringProperty2);
        createdItem.setStringSetProperty(newStringSetProperty);
        final Long newVersion = originalVersion + 1;

        final List<StubItem> stubItems = new ArrayList<StubItem>();
        stubItems.add(createdItem);

        // When
        final List<StubItem> successfulStubItems = dynamoDbTemplate.batchWrite(stubItems, StubItem.class);

        // Then
        for (final StubItem aStubItem : successfulStubItems) {
            assertEquals(newVersion, aStubItem.getVersion());
            assertEquals(createdItem.getId(), aStubItem.getId());
            assertEquals(stringProperty, aStubItem.getStringProperty());
            assertEquals(stringProperty2, aStubItem.getStringProperty2());
            assertEquals(newStringSetProperty, aStubItem.getStringSetProperty());
        }
    }

    @Test
    public void shouldUpdate_viaBatchWrite_withSingleItemWithCompoundPk() {
        // Given
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(databaseSchemaHolder);
        dynamoDbTemplate.initialize(amazonDynamoDbClient);
        final StubWithRangeItem createdItem = dataGenerator.createStubWithRangeItem();
        final Long originalVersion = createdItem.getVersion();
        final String stringProperty = randomString(10);
        final boolean booleanProperty = randomBoolean();
        final Set<String> newStringSetProperty = Sets.newSet(randomString(10), randomString(10), randomString(10));
        createdItem.setStringProperty(stringProperty);
        createdItem.setBooleanProperty(booleanProperty);
        createdItem.setStringSetProperty(newStringSetProperty);
        final Long newVersion = originalVersion + 1;

        final List<StubWithRangeItem> stubWithRangeItems = new ArrayList<StubWithRangeItem>();
        stubWithRangeItems.add(createdItem);

        // When
        final List<StubWithRangeItem> successfulStubWithRangeItems = dynamoDbTemplate.batchWrite(stubWithRangeItems,
                StubWithRangeItem.class);

        // Then
        for (final StubWithRangeItem aStubWithRangeItem : successfulStubWithRangeItems) {
            assertEquals(newVersion, aStubWithRangeItem.getVersion());
            assertEquals(createdItem.getId(), aStubWithRangeItem.getId());
            assertEquals(stringProperty, aStubWithRangeItem.getStringProperty());
            assertEquals(booleanProperty, aStubWithRangeItem.isBooleanProperty());
            assertEquals(newStringSetProperty, aStubWithRangeItem.getStringSetProperty());
        }

    }

}
