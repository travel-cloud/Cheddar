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
package com.clicktravel.infrastructure.persistence.inmemory.database;

import static com.clicktravel.common.random.Randoms.*;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.*;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import com.clicktravel.cheddar.infrastructure.persistence.database.GeneratedKeyHolder;
import com.clicktravel.cheddar.infrastructure.persistence.database.ItemId;
import com.clicktravel.cheddar.infrastructure.persistence.database.SequenceKeyGenerator;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.*;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.ItemConstraintViolationException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonUniqueResultException;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.*;
import com.clicktravel.common.random.Randoms;

public class InMemoryDatabaseTemplateTest {

    private DatabaseSchemaHolder databaseSchemaHolder;
    private final static InMemoryDbDataGenerator dataGenerator = new InMemoryDbDataGenerator();

    private static final String STRING_PROPERTY = "stringProperty";

    private final Collection<String> createdItemIds = new ArrayList<>();

    @Before
    public void init() throws Exception {
        final Collection<ItemConfiguration> itemConfigurations = new ArrayList<>();

        final ItemConfiguration stubItemConfiguration = new ItemConfiguration(StubItem.class,
                InMemoryDbDataGenerator.STUB_ITEM_TABLE_NAME);
        final ParentItemConfiguration stubParentItemConfiguration = new ParentItemConfiguration(StubParentItem.class,
                InMemoryDbDataGenerator.STUB_ITEM_TABLE_NAME);
        final ItemConfiguration stubItemWithRangeConfiguration = new ItemConfiguration(StubWithRangeItem.class,
                InMemoryDbDataGenerator.STUB_ITEM_WITH_RANGE_TABLE_NAME,
                new CompoundPrimaryKeyDefinition("id", "supportingId"));
        final ItemConfiguration stubItemwithGsiConfiguration = new ItemConfiguration(
                StubWithGlobalSecondaryIndexItem.class, InMemoryDbDataGenerator.STUB_ITEM_WITH_GSI_TABLE_NAME);
        stubItemwithGsiConfiguration
                .registerIndexes((Arrays.asList(new CompoundIndexDefinition("gsiHashProperty", "gsiRangeProperty"))));
        itemConfigurations.add(stubItemConfiguration);
        itemConfigurations.add(stubParentItemConfiguration);
        itemConfigurations.add(new VariantItemConfiguration(stubParentItemConfiguration, StubVariantItem.class, "a"));
        itemConfigurations
                .add(new VariantItemConfiguration(stubParentItemConfiguration, StubVariantTwoItem.class, "b"));
        itemConfigurations.add(stubItemWithRangeConfiguration);
        itemConfigurations.add(stubItemwithGsiConfiguration);
        databaseSchemaHolder = new DatabaseSchemaHolder(InMemoryDbDataGenerator.UNIT_TEST_SCHEMA_NAME,
                itemConfigurations);
    }

    @Test
    public void shouldConstructInMemeoryDataTemplate_withDataBaseSchemaHolder() {
        // Given
        final DatabaseSchemaHolder mockDatabaseSchemaHolder = mock(DatabaseSchemaHolder.class);

        // When
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(mockDatabaseSchemaHolder);

        // Then
        assertNotNull(databaseTemplate);
    }

    @Test
    public void shouldFetch_withKeySetQuery() {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final int itemCount = 1 + Randoms.randomInt(3);
        final HashSet<StubItem> items = new HashSet<>();
        final Set<ItemId> itemIds = new HashSet<>();

        for (int n = 0; n < itemCount; n++) {
            final StubItem item = dataGenerator.randomStubItem();
            databaseTemplate.create(item);
            items.add(item);
            itemIds.add(new ItemId(item.getId()));
        }

        final KeySetQuery query = new KeySetQuery(itemIds);

        // When
        final Collection<StubItem> returnedItems = databaseTemplate.fetch(query, StubItem.class);

        // Then
        assertNotNull(returnedItems);
        assertEquals(itemCount, returnedItems.size());
        final HashSet<StubItem> returnedItemSet = new HashSet<>(returnedItems);
        assertTrue(returnedItemSet.equals(items));
    }

    @Test
    public void shouldFetch_withKeySetQueryWithCompoundPk() {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final int itemCount = 1 + Randoms.randomInt(3);
        final HashSet<StubWithRangeItem> items = new HashSet<>();
        final Set<ItemId> itemIds = new HashSet<>();

        for (int n = 0; n < itemCount; n++) {
            final StubWithRangeItem item = dataGenerator.randomStubWithRangeItem();
            databaseTemplate.create(item);
            items.add(item);
            itemIds.add(new ItemId(item.getId(), item.getSupportingId()));
        }

        final KeySetQuery query = new KeySetQuery(itemIds);

        // When
        final Collection<StubWithRangeItem> returnedItems = databaseTemplate.fetch(query, StubWithRangeItem.class);

        // Then
        assertNotNull(returnedItems);
        assertEquals(itemCount, returnedItems.size());
        final HashSet<StubWithRangeItem> returnedItemSet = new HashSet<>(returnedItems);
        assertTrue(returnedItemSet.equals(items));
    }

    @Test
    public void shouldFetch_withNoIds() {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final Set<ItemId> itemIds = new HashSet<>();
        final KeySetQuery query = new KeySetQuery(itemIds);

        // When
        final Collection<StubItem> returnedItems = databaseTemplate.fetch(query, StubItem.class);

        // Then
        assertNotNull(returnedItems);
        assertEquals(0, returnedItems.size());
    }

    @Test
    public void shouldFetch_withUnknownIds() {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final Set<ItemId> unknownIds = new HashSet<>();
        final int idCount = 1 + Randoms.randomInt(5);
        for (int n = 0; n < idCount; n++) {
            unknownIds.add(new ItemId(Randoms.randomId()));
        }
        final KeySetQuery query = new KeySetQuery(unknownIds);

        // When
        final Collection<StubItem> returnedItems = databaseTemplate.fetch(query, StubItem.class);

        // Then
        assertNotNull(returnedItems);
        assertEquals(0, returnedItems.size());
    }

    @Test
    public void shouldFetch_withRandomKnownAndUnknownIds() {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);

        final int itemCount = 1 + Randoms.randomInt(3);
        final Set<ItemId> itemIds = new HashSet<>();
        for (int n = 0; n < itemCount; n++) {
            final StubItem item = dataGenerator.randomStubItem();
            databaseTemplate.create(item);
            itemIds.add(new ItemId(item.getId()));
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
        final Collection<StubItem> returnedItems = databaseTemplate.fetch(query, StubItem.class);

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
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final StubItem createdItem = dataGenerator.randomStubItem();
        databaseTemplate.create(createdItem);
        final Long originalVersion = createdItem.getVersion();
        final String stringProperty = randomString(10);
        final String stringProperty2 = randomString(10);
        final Set<String> newStringSetProperty = Sets.newSet(randomString(10), randomString(10), randomString(10));
        createdItem.setStringProperty(stringProperty);
        createdItem.setStringProperty2(stringProperty2);
        createdItem.setStringSetProperty(newStringSetProperty);
        final Long newVersion = originalVersion + 1;

        // When
        final StubItem updatedItem = databaseTemplate.update(createdItem);

        // Then
        assertEquals(newVersion, updatedItem.getVersion());
        assertEquals(createdItem.getId(), updatedItem.getId());
        assertEquals(stringProperty, updatedItem.getStringProperty());
        assertEquals(stringProperty2, updatedItem.getStringProperty2());
        assertEquals(newStringSetProperty, updatedItem.getStringSetProperty());

    }

    @Test
    public void shouldUpdate_withSingleItemWithCompoundPk() {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final StubWithRangeItem createdItem = dataGenerator.randomStubWithRangeItem();
        databaseTemplate.create(createdItem);
        final Long originalVersion = createdItem.getVersion();
        final String stringProperty = randomString(10);
        final boolean booleanProperty = randomBoolean();
        final Set<String> newStringSetProperty = Sets.newSet(randomString(10), randomString(10), randomString(10));
        createdItem.setStringProperty(stringProperty);
        createdItem.setBooleanProperty(booleanProperty);
        createdItem.setStringSetProperty(newStringSetProperty);
        final Long newVersion = originalVersion + 1;

        // When
        final StubWithRangeItem updatedItem = databaseTemplate.update(createdItem);

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
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final StubItem createdItem = dataGenerator.randomStubItem();
        databaseTemplate.create(createdItem);
        final String stringProperty = createdItem.getStringProperty();
        final StubItem stubItem = new StubItem();
        stubItem.setStringProperty(stringProperty);
        final Query query = new AttributeQuery(STRING_PROPERTY, new Condition(Operators.EQUALS, stringProperty));

        // When
        final Collection<StubItem> itemResults = databaseTemplate.fetch(query, StubItem.class);

        // Then
        assertNotNull(itemResults);
        assertEquals(1, itemResults.size());
        assertEquals(createdItem, itemResults.iterator().next());
    }

    @Test
    public void shouldFetch_withCompoundAttributeQuery() throws Exception {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);

        final List<StubWithGlobalSecondaryIndexItem> expectedMatchingItems = new ArrayList<StubWithGlobalSecondaryIndexItem>();
        final String gsiFetchCriteriaValue = Randoms.randomString(10);
        final Integer gsiSupportingFetchCriteriaValue = Randoms.randomInt(20);
        final Query query = new CompoundAttributeQuery("gsiHashProperty",
                new Condition(Operators.EQUALS, gsiFetchCriteriaValue), "gsiRangeProperty",
                new Condition(Operators.EQUALS, gsiSupportingFetchCriteriaValue.toString()));

        for (int i = 0; i < 20; i++) {
            final StubWithGlobalSecondaryIndexItem item = dataGenerator.randomStubWithGlobalSecondaryIndexItem();

            if (Randoms.randomBoolean() || item.getGsiHashProperty().equals(gsiFetchCriteriaValue)) {
                item.setGsiHashProperty(gsiFetchCriteriaValue);

                if (Randoms.randomBoolean() || item.getGsiRangeProperty().equals(gsiSupportingFetchCriteriaValue)) {
                    item.setGsiRangeProperty(gsiSupportingFetchCriteriaValue);
                    expectedMatchingItems.add(item);
                }
            }

            databaseTemplate.create(item);
        }

        // When
        final Collection<StubWithGlobalSecondaryIndexItem> itemResults = databaseTemplate.fetch(query,
                StubWithGlobalSecondaryIndexItem.class);

        // Then
        assertEquals(expectedMatchingItems.size(), itemResults.size());
        assertTrue(itemResults.containsAll(expectedMatchingItems));
    }

    @Test
    public void shouldGetEmptySet_withNullAttributeQuery() {
        // Given
        final String stringProperty = null;
        final Query query = new AttributeQuery(STRING_PROPERTY, new Condition(Operators.EQUALS, stringProperty));
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);

        // When
        final Collection<StubItem> itemResults = databaseTemplate.fetch(query, StubItem.class);

        // Then
        assertNotNull(itemResults);
        assertEquals(0, itemResults.size());
    }

    @Test
    public void shouldGetEmptySet_withEmptyAttributeQuery() {
        // Given
        final String stringProperty = "";
        final Query query = new AttributeQuery(STRING_PROPERTY, new Condition(Operators.EQUALS, stringProperty));
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);

        // When
        final Collection<StubItem> itemResults = databaseTemplate.fetch(query, StubItem.class);

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
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final StubItem createdItem1 = dataGenerator.stubItemWithStringProperty(stringProperty);
        final StubItem createdItem2 = dataGenerator.stubItemWithStringProperty(stringProperty);
        databaseTemplate.create(createdItem1);
        databaseTemplate.create(createdItem2);

        // When
        final Collection<StubItem> itemResults = databaseTemplate.fetch(query, StubItem.class);

        // Then
        assertNotNull(itemResults);
        assertEquals(2, itemResults.size());
        assertThat(itemResults, hasItems(createdItem1, createdItem2));
    }

    @Test
    public void shouldFetchUnique_withAttributeQuery() throws Exception {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final StubItem createdItem = dataGenerator.randomStubItem();
        databaseTemplate.create(createdItem);
        final String property = createdItem.getStringProperty();
        final StubItem stubItem = new StubItem();
        stubItem.setStringProperty(property);
        final AttributeQuery query = new AttributeQuery(STRING_PROPERTY, new Condition(Operators.EQUALS, property));

        // When
        final StubItem itemResult = databaseTemplate.fetchUnique(query, StubItem.class);

        // Then
        assertNotNull(itemResult);
        assertEquals(createdItem, itemResult);
    }

    @Test
    public void shouldNotFetchUnique_withMultipleMatches() {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final String stringProperty = randomString(10);
        final StubItem createdItem1 = dataGenerator.stubItemWithStringProperty(stringProperty);
        final StubItem createdItem2 = dataGenerator.stubItemWithStringProperty(stringProperty);
        databaseTemplate.create(createdItem1);
        databaseTemplate.create(createdItem2);
        final AttributeQuery query = new AttributeQuery(STRING_PROPERTY,
                new Condition(Operators.EQUALS, stringProperty));

        // When
        NonUniqueResultException expectedException = null;
        try {
            databaseTemplate.fetchUnique(query, StubItem.class);
        } catch (final NonUniqueResultException e) {
            expectedException = e;
        }

        // Then
        assertNotNull(expectedException);
    }

    @Test
    public void shouldReadItem_withNullValues() throws Exception {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final StubItem createdStubItem = dataGenerator.stubItemWithNullValues();
        databaseTemplate.create(createdStubItem);
        final ItemId itemId = new ItemId(createdStubItem.getId());

        // When
        final StubItem item = databaseTemplate.read(itemId, StubItem.class);

        // Then
        assertEquals(createdStubItem, item);
    }

    @Test
    public void shouldDeleteItem_withItem() throws Exception {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final StubItem createdItem = dataGenerator.randomStubItem();
        databaseTemplate.create(createdItem);

        // When
        databaseTemplate.delete(createdItem);

        // Then
        NonExistentItemException actualException = null;
        try {
            databaseTemplate.read(new ItemId(createdItem.getId()), StubItem.class);
        } catch (final NonExistentItemException e) {
            actualException = e;
        }

        assertNotNull(actualException);
    }

    @Test
    public void shouldDeleteItem_withItemWithCompoundPk() throws Exception {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final StubWithRangeItem createdItem = dataGenerator.randomStubWithRangeItem();
        databaseTemplate.create(createdItem);

        // When
        databaseTemplate.delete(createdItem);

        // Then
        NonExistentItemException actualException = null;
        try {
            databaseTemplate.read(new ItemId(createdItem.getId()), StubItem.class);
        } catch (final NonExistentItemException e) {
            actualException = e;
        }

        assertNotNull(actualException);
    }

    @Test
    public void shouldCreateItem_withVariantItem() throws Exception {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final StubVariantItem stubVariantItem = new StubVariantItem();
        stubVariantItem.setId(randomId());
        stubVariantItem.setStringProperty(randomString(10));
        stubVariantItem.setStringProperty2(randomString(10));
        createdItemIds.add(stubVariantItem.getId());

        // When
        final StubVariantItem item = databaseTemplate.create(stubVariantItem);

        //
        assertEquals(stubVariantItem, item);
    }

    @Test
    public void shouldReadItem_withVariantItem() throws Exception {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final StubVariantItem createdStubVariantItem = dataGenerator.randomStubVariantItem();
        databaseTemplate.create(createdStubVariantItem);
        final ItemId stubVariantItemId = new ItemId(createdStubVariantItem.getId());

        // When
        final StubVariantItem stubVariantItem = databaseTemplate.read(stubVariantItemId, StubVariantItem.class);

        // Then
        assertEquals(createdStubVariantItem, stubVariantItem);
    }

    @Test
    public void shouldNotReadItem_withDifferentVariantItemType() throws Exception {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final StubVariantItem createdStubVariantItem = dataGenerator.randomStubVariantItem();
        databaseTemplate.create(createdStubVariantItem);
        final ItemId stubVariantItemId = new ItemId(createdStubVariantItem.getId());

        // When
        NonExistentItemException actualException = null;
        try {
            databaseTemplate.read(stubVariantItemId, StubVariantTwoItem.class);
        } catch (final NonExistentItemException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldReadItem_withVariantItemByParent() throws Exception {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final StubVariantItem createdStubVariantItem = dataGenerator.randomStubVariantItem();
        databaseTemplate.create(createdStubVariantItem);
        final ItemId stubVariantItemId = new ItemId(createdStubVariantItem.getId());

        // When
        final StubParentItem stubParentItem = databaseTemplate.read(stubVariantItemId, StubParentItem.class);

        // Then
        assertTrue(stubParentItem instanceof StubVariantItem);
        assertEquals(createdStubVariantItem, stubParentItem);

    }

    @Test
    public void shouldFetch_withKeySetQueryWithVariants() {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final int itemCount = 1 + Randoms.randomInt(3);
        final HashSet<StubParentItem> items = new HashSet<>();
        final Set<ItemId> itemIds = new HashSet<>();

        for (int n = 0; n < itemCount; n++) {
            final StubVariantItem item = dataGenerator.randomStubVariantItem();
            databaseTemplate.create(item);
            items.add(item);
            itemIds.add(new ItemId(item.getId()));
        }

        final KeySetQuery query = new KeySetQuery(itemIds);

        // When
        final Collection<StubParentItem> returnedItems = databaseTemplate.fetch(query, StubParentItem.class);

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
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final Set<ItemId> itemIds = new HashSet<>();

        final StubVariantItem item1 = dataGenerator.randomStubVariantItem();
        databaseTemplate.create(item1);
        itemIds.add(new ItemId(item1.getId()));

        final StubVariantTwoItem item2 = dataGenerator.randomStubVariantTwoItem();
        databaseTemplate.create(item2);
        itemIds.add(new ItemId(item2.getId()));

        final KeySetQuery query = new KeySetQuery(itemIds);

        // When
        final Collection<StubParentItem> returnedItems = databaseTemplate.fetch(query, StubParentItem.class);

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
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final HashSet<StubParentItem> items = new HashSet<>();
        final Set<ItemId> itemIds = new HashSet<>();

        final StubVariantItem item1 = dataGenerator.randomStubVariantItem();
        databaseTemplate.create(item1);
        items.add(item1);
        itemIds.add(new ItemId(item1.getId()));

        final StubVariantTwoItem item2 = dataGenerator.randomStubVariantTwoItem();
        databaseTemplate.create(item2);
        items.add(item2);
        itemIds.add(new ItemId(item2.getId()));

        final KeySetQuery query = new KeySetQuery(itemIds);

        // When
        final Collection<StubVariantTwoItem> returnedItems = databaseTemplate.fetch(query, StubVariantTwoItem.class);

        // Then
        assertNotNull(returnedItems);
        assertEquals(1, returnedItems.size());
        final StubVariantTwoItem returnedItem1 = returnedItems.iterator().next();
        assertTrue(returnedItem1 instanceof StubVariantTwoItem);
    }

    @Test
    public void shouldReadBack_withSingleItem() {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final StubItem createdStubItem = dataGenerator.randomStubItem();
        databaseTemplate.create(createdStubItem);
        final ItemId itemId = new ItemId(createdStubItem.getId());

        // When

        final StubItem returnedItem = databaseTemplate.read(itemId, StubItem.class);

        // Then
        assertEquals(createdStubItem, returnedItem);
    }

    @Test
    public void shouldReadBack_withSingleItemAndCompoundPk() {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final StubWithRangeItem createdStubWithRangeItem = dataGenerator.randomStubWithRangeItem();
        databaseTemplate.create(createdStubWithRangeItem);
        final ItemId itemId = new ItemId(createdStubWithRangeItem.getId(), createdStubWithRangeItem.getSupportingId());

        // When

        final StubWithRangeItem returnedItem = databaseTemplate.read(itemId, StubWithRangeItem.class);

        // Then
        assertEquals(createdStubWithRangeItem, returnedItem);
    }

    @Test
    public void shouldNotReadBack_withUnknownId() {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final ItemId unknownId = new ItemId(randomId());

        // When
        NonExistentItemException expectedException = null;
        try {
            databaseTemplate.read(unknownId, StubItem.class);
        } catch (final NonExistentItemException e) {
            expectedException = e;
        }

        // Then
        assertNotNull(expectedException);
    }

    @Test
    public void shouldNotReadBack_withUnknownCompoundPk() {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final ItemId unknownId = new ItemId(randomId(), randomId());

        // When
        NonExistentItemException expectedException = null;
        try {
            databaseTemplate.read(unknownId, StubItem.class);
        } catch (final NonExistentItemException e) {
            expectedException = e;
        }

        // Then
        assertNotNull(expectedException);
    }

    @Test
    public void shouldSaveNewItem_withItem() {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final StubItem stubItem = dataGenerator.randomStubItem();
        stubItem.setVersion(null);
        createdItemIds.add(stubItem.getId());

        // When
        final StubItem item = databaseTemplate.create(stubItem);

        // Then
        assertEquals(new Long(1), item.getVersion());
        assertEquals(stubItem.getId(), item.getId());
        assertEquals(stubItem.getStringProperty(), item.getStringProperty());
        assertEquals(stubItem.getStringSetProperty(), item.getStringSetProperty());
    }

    @Test
    public void shoudNotSaveItem_withExisitingItem() {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final StubItem stubItem = dataGenerator.randomStubItem();
        databaseTemplate.create(stubItem);

        // When
        ItemConstraintViolationException actualException = null;
        try {
            databaseTemplate.create(stubItem);
        } catch (final ItemConstraintViolationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shoudSaveItemAndItsUniqueConstraints_withItemWithUniqueConstraint() {
        // Given
        final StubItem stubItem = dataGenerator.randomStubItem();
        final String uniqueConstraintAttributeName = "stringProperty";
        final String stubItemIndexAttributeValue = stubItem.getStringProperty();
        final ItemConfiguration stubItemConfigurationWithUniqueConstraints = new ItemConfiguration(stubItem.getClass(),
                "stubTable");
        stubItemConfigurationWithUniqueConstraints
                .registerUniqueConstraints(Arrays.asList(new UniqueConstraint(uniqueConstraintAttributeName)));
        final DatabaseSchemaHolder databaseSchemaHolderWithUniqueConstraints = databaseSchemaHolderWithItemConfiguration(
                stubItemConfigurationWithUniqueConstraints);
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(
                databaseSchemaHolderWithUniqueConstraints);

        // When
        final StubItem returnedItem = databaseTemplate.create(stubItem);

        // Then
        assertNotNull(returnedItem);
        assertEquals(stubItem, databaseTemplate.read(new ItemId(stubItem.getId()), stubItem.getClass()));
        assertEquals(returnedItem, stubItem);
        final StubItem uniqueStubItem = new StubItem();
        uniqueStubItem.setId(randomId());
        uniqueStubItem.setStringProperty(stubItemIndexAttributeValue);
        assertTrue(databaseTemplate.hasMatchingUniqueConstraint(stubItem, uniqueConstraintAttributeName,
                stubItem.getStringProperty()));
    }

    @Test
    public void shoudNotSaveItem_withAlreadyExistingUniqueConstraintValue() {
        // Given
        final StubItem stubItem = dataGenerator.randomStubItem();
        final StubItem secondItem = dataGenerator.randomStubItem();
        final String uniqueConstraintAttributeName = "stringProperty";
        final String stubItemIndexAttributeValue = stubItem.getStringProperty();
        final ItemConfiguration stubItemConfigurationWithUniqueConstraints = new ItemConfiguration(stubItem.getClass(),
                "stubTable");
        stubItemConfigurationWithUniqueConstraints
                .registerUniqueConstraints(Arrays.asList(new UniqueConstraint(uniqueConstraintAttributeName)));
        final DatabaseSchemaHolder databaseSchemaHolderWithUniqueConstraints = databaseSchemaHolderWithItemConfiguration(
                stubItemConfigurationWithUniqueConstraints);
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(
                databaseSchemaHolderWithUniqueConstraints);

        databaseTemplate.create(stubItem);
        secondItem.setStringProperty(stubItemIndexAttributeValue);

        // When
        ItemConstraintViolationException actualException = null;

        try {
            databaseTemplate.create(secondItem);
        } catch (final ItemConstraintViolationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertEquals(stubItem, databaseTemplate.read(new ItemId(stubItem.getId()), stubItem.getClass()));
        assertTrue(databaseTemplate.hasMatchingUniqueConstraint(stubItem, uniqueConstraintAttributeName,
                stubItem.getStringProperty()));
    }

    @Test
    public void shoudNotSaveItem_withAlreadyExistingUniqueConstraintValueButDifferentCase() {
        // Given
        final StubItem stubItem = dataGenerator.randomStubItem();
        final StubItem secondItem = dataGenerator.randomStubItem();
        final String uniqueConstraintAttributeName = "stringProperty";
        final String stubItemIndexAttributeValue = stubItem.getStringProperty();
        final ItemConfiguration stubItemConfigurationWithUniqueConstraints = new ItemConfiguration(stubItem.getClass(),
                "stubTable");
        stubItemConfigurationWithUniqueConstraints
                .registerUniqueConstraints(Arrays.asList(new UniqueConstraint(uniqueConstraintAttributeName)));
        final DatabaseSchemaHolder databaseSchemaHolderWithUniqueConstraints = databaseSchemaHolderWithItemConfiguration(
                stubItemConfigurationWithUniqueConstraints);
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(
                databaseSchemaHolderWithUniqueConstraints);

        databaseTemplate.create(stubItem);
        secondItem.setStringProperty(stubItemIndexAttributeValue.toUpperCase());

        // When
        ItemConstraintViolationException actualException = null;

        try {
            databaseTemplate.create(secondItem);
        } catch (final ItemConstraintViolationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertEquals(stubItem, databaseTemplate.read(new ItemId(stubItem.getId()), stubItem.getClass()));
        assertTrue(databaseTemplate.hasMatchingUniqueConstraint(stubItem, uniqueConstraintAttributeName,
                stubItem.getStringProperty()));
    }

    @Test
    public void shouldUpdateItemAndUniqueConstraints_withUpdatedItemWithUnchangedUniqueConstraint() {
        // Given
        final StubItem stubItem = dataGenerator.randomStubItem();
        final String propertyValue = stubItem.getStringProperty();
        final String updatedProperty2Value = randomString();
        final Set<String> updatedSetValue = new HashSet<>();
        final String uniqueConstraintAttributeName = "stringProperty";
        final ItemConfiguration stubItemConfigurationWithUniqueConstraints = new ItemConfiguration(stubItem.getClass(),
                "stubTable");
        stubItemConfigurationWithUniqueConstraints
                .registerUniqueConstraints(Arrays.asList(new UniqueConstraint(uniqueConstraintAttributeName)));
        final DatabaseSchemaHolder databaseSchemaHolderWithUniqueConstraints = databaseSchemaHolderWithItemConfiguration(
                stubItemConfigurationWithUniqueConstraints);
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(
                databaseSchemaHolderWithUniqueConstraints);

        databaseTemplate.create(stubItem);
        stubItem.setStringProperty2(updatedProperty2Value);
        stubItem.setStringSetProperty(updatedSetValue);

        // When
        final StubItem updatedItem = databaseTemplate.update(stubItem);

        // Then
        assertNotNull(updatedItem);
        assertThat(updatedItem.getStringProperty(), Is.is(propertyValue));
        assertThat(updatedItem.getStringProperty2(), Is.is(updatedProperty2Value));
        assertThat(updatedItem.getStringSetProperty(), Is.is(updatedSetValue));
        final StubItem uniqueStubItem = new StubItem();
        uniqueStubItem.setId(randomId());
        uniqueStubItem.setStringProperty(propertyValue);
        assertTrue(databaseTemplate.hasMatchingUniqueConstraint(stubItem, uniqueConstraintAttributeName,
                stubItem.getStringProperty()));
    }

    @Test
    public void shouldUpdateItemAndUniqueConstraint_withItemWithUpdatedUniqueConstraint() {
        // Given
        final StubItem stubItem = dataGenerator.randomStubItem();
        final String uniqueConstraintAttributeName = "stringProperty";
        final String updatedPropertyValue = randomString();
        final ItemConfiguration stubItemConfigurationWithUniqueConstraints = new ItemConfiguration(stubItem.getClass(),
                "stubTable");
        stubItemConfigurationWithUniqueConstraints
                .registerUniqueConstraints(Arrays.asList(new UniqueConstraint(uniqueConstraintAttributeName)));
        final DatabaseSchemaHolder databaseSchemaHolderWithUniqueConstraints = databaseSchemaHolderWithItemConfiguration(
                stubItemConfigurationWithUniqueConstraints);
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(
                databaseSchemaHolderWithUniqueConstraints);

        databaseTemplate.create(stubItem);
        stubItem.setStringProperty(updatedPropertyValue);

        // When
        final StubItem updatedItem = databaseTemplate.update(stubItem);

        // Then
        assertNotNull(updatedItem);
        final StubItem uniqueStubItem = new StubItem();
        uniqueStubItem.setId(randomId());
        uniqueStubItem.setStringProperty(updatedPropertyValue);
        assertTrue(databaseTemplate.hasMatchingUniqueConstraint(stubItem, uniqueConstraintAttributeName,
                updatedPropertyValue));
    }

    @Test
    public void shouldNotUpdateItemAndUniqueConstraint_withItemExistingUpdatedUniqueConstraintValue() {
        // Given
        final StubItem stubItem = dataGenerator.randomStubItem();
        final String originalStubItemContstraintValue = stubItem.getStringProperty();
        final StubItem existingStubItem = dataGenerator.randomStubItem();
        final String alreadyExistingUniqueConstraint = existingStubItem.getStringProperty();
        final String uniqueConstraintAttributeName = "stringProperty";
        final ItemConfiguration stubItemConfigurationWithUniqueConstraints = new ItemConfiguration(stubItem.getClass(),
                "stubTable");
        stubItemConfigurationWithUniqueConstraints
                .registerUniqueConstraints(Arrays.asList(new UniqueConstraint(uniqueConstraintAttributeName)));
        final DatabaseSchemaHolder databaseSchemaHolderWithUniqueConstraints = databaseSchemaHolderWithItemConfiguration(
                stubItemConfigurationWithUniqueConstraints);
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(
                databaseSchemaHolderWithUniqueConstraints);

        databaseTemplate.create(existingStubItem);
        databaseTemplate.create(stubItem);
        stubItem.setStringProperty(alreadyExistingUniqueConstraint);

        // When
        ItemConstraintViolationException actualException = null;
        try {
            databaseTemplate.update(stubItem);
        } catch (final ItemConstraintViolationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertTrue(databaseTemplate.hasMatchingUniqueConstraint(existingStubItem, uniqueConstraintAttributeName,
                alreadyExistingUniqueConstraint));
        assertTrue(databaseTemplate.hasMatchingUniqueConstraint(stubItem, uniqueConstraintAttributeName,
                originalStubItemContstraintValue));
    }

    @Test
    public void shouldDeleteUniqueConstraints_withDeletedItem() {
        // Given
        final StubItem stubItem = dataGenerator.randomStubItem();
        final String uniqueConstraintAttributeName = "stringProperty";
        final String existingUniqueConstraint = stubItem.getStringProperty();
        final ItemConfiguration stubItemConfigurationWithUniqueConstraints = new ItemConfiguration(stubItem.getClass(),
                "stubTable");
        stubItemConfigurationWithUniqueConstraints
                .registerUniqueConstraints(Arrays.asList(new UniqueConstraint(uniqueConstraintAttributeName)));
        final DatabaseSchemaHolder databaseSchemaHolderWithUniqueConstraints = databaseSchemaHolderWithItemConfiguration(
                stubItemConfigurationWithUniqueConstraints);
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(
                databaseSchemaHolderWithUniqueConstraints);

        databaseTemplate.create(stubItem);

        // When
        databaseTemplate.delete(stubItem);

        // Then
        final StubItem uniqueStubItem = new StubItem();
        uniqueStubItem.setId(randomId());
        uniqueStubItem.setStringProperty(existingUniqueConstraint);
        assertFalse(databaseTemplate.hasMatchingUniqueConstraint(stubItem, uniqueConstraintAttributeName,
                existingUniqueConstraint));
    }

    @Test
    public void shouldNotFetchUniqueItem_withIncorrectQueryValue() {
        // Given
        final StubItem stubItem = dataGenerator.randomStubItem();
        final String randomQueryValue = randomString();
        final StubItem secondStubItem = dataGenerator.randomStubItem();
        final String attributeName = "stringProperty";
        final Condition condition = new Condition(Operators.EQUALS, randomQueryValue);
        final Query query = new AttributeQuery(attributeName, condition);
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);

        databaseTemplate.create(stubItem);
        databaseTemplate.create(secondStubItem);

        // When
        NonUniqueResultException actualException = null;
        try {
            databaseTemplate.fetchUnique(query, stubItem.getClass());
        } catch (final NonUniqueResultException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotUpdateItem_withItemVersionNotEqualToOldItem() {
        // Given
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);
        final StubItem stubItem = dataGenerator.randomStubItem();

        databaseTemplate.create(stubItem);

        stubItem.setVersion(randomLong());

        // When
        IllegalAccessError actualException = null;
        try {
            databaseTemplate.update(stubItem);
        } catch (final IllegalAccessError e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldGenerateKeyHolder_withSequenceKeyGenerator() {
        // Given
        final int keyCount = 1 + Randoms.randomInt(100);
        final SequenceKeyGenerator sequenceKeyGenerator = new SequenceKeyGenerator(randomString(), keyCount);
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);

        // When
        final GeneratedKeyHolder genratedKeyHolder = databaseTemplate.generateKeys(sequenceKeyGenerator);

        // Then
        assertNotNull(genratedKeyHolder);
        assertThat(genratedKeyHolder.keys().size(), Is.is(sequenceKeyGenerator.keyCount()));
        final Set<Long> setOfKeysToCheckForDuplicates = new HashSet<>(genratedKeyHolder.keys());
        assertThat(setOfKeysToCheckForDuplicates.size(), Is.is(genratedKeyHolder.keys().size()));
    }

    @Test
    public void shouldUniquelyGenerateKeyHolders_withManySequenceKeyGenerator() {
        // Given
        final Collection<SequenceKeyGenerator> sequnceKeyGeneratorCollection = new ArrayList<>();
        final Collection<GeneratedKeyHolder> generatedKeyHolderCollection = new ArrayList<>();
        int totalKeyCount = 0;
        for (int i = 0; i < randomInt(9) + 1; i++) {
            final int keyCount = 1 + Randoms.randomInt(100);
            totalKeyCount += keyCount;
            final SequenceKeyGenerator sequenceKeyGenerator = new SequenceKeyGenerator(randomString(), keyCount);
            sequnceKeyGeneratorCollection.add(sequenceKeyGenerator);
        }
        final InMemoryDatabaseTemplate databaseTemplate = new InMemoryDatabaseTemplate(databaseSchemaHolder);

        // When
        for (final SequenceKeyGenerator sequenceKeyGenerator : sequnceKeyGeneratorCollection) {
            generatedKeyHolderCollection.add(databaseTemplate.generateKeys(sequenceKeyGenerator));
        }

        // Then
        assertFalse(generatedKeyHolderCollection.isEmpty());
        final Set<Long> allGeneratedKeys = new HashSet<>();
        for (final GeneratedKeyHolder generatedKeyHolder : generatedKeyHolderCollection) {
            allGeneratedKeys.addAll(generatedKeyHolder.keys());
        }
        assertTrue(allGeneratedKeys.size() == totalKeyCount);
    }

    private DatabaseSchemaHolder databaseSchemaHolderWithItemConfiguration(final ItemConfiguration itemConfiguration) {
        final Collection<ItemConfiguration> itemConfigurations = new HashSet<>();
        itemConfigurations.add(itemConfiguration);
        return new DatabaseSchemaHolder("testStub", itemConfigurations);

    }

}
