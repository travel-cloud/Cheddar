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

import static com.clicktravel.common.random.Randoms.randomId;
import static com.clicktravel.common.random.Randoms.randomInt;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.internal.IteratorSupport;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.clicktravel.cheddar.infrastructure.persistence.database.ItemId;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.CompoundIndexDefinition;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.DatabaseSchemaHolder;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.IndexDefinition;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.ItemConfiguration;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.OptimisticLockException;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.AttributeQuery;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.CompoundAttributeQuery;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Condition;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Operators;
import com.clicktravel.common.random.Randoms;

@SuppressWarnings({ "deprecation", "unchecked" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ DynamoDocumentStoreTemplate.class })
public class DynamoDocumentStoreTemplateTest {

    private DatabaseSchemaHolder mockDatabaseSchemaHolder;
    private String schemaName;
    private String tableName;
    private AmazonDynamoDB mockAmazonDynamoDbClient;
    private DynamoDB mockDynamoDBClient;

    @Before
    public void setup() throws Exception {
        schemaName = randomString(10);
        tableName = randomString(10);
        mockDatabaseSchemaHolder = mock(DatabaseSchemaHolder.class);
        when(mockDatabaseSchemaHolder.schemaName()).thenReturn(schemaName);
        mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        mockDynamoDBClient = mock(DynamoDB.class);
        whenNew(DynamoDB.class).withParameterTypes(AmazonDynamoDB.class).withArguments(eq(mockAmazonDynamoDbClient))
                .thenReturn(mockDynamoDBClient);
    }

    @Test
    public void shouldCreate_withItem() {
        // Given
        final ItemId itemId = new ItemId(randomId());
        final StubItem stubItem = generateRandomStubItem(itemId);

        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);

        final Table mockTable = mock(Table.class);
        when(mockDynamoDBClient.getTable(any(String.class))).thenReturn(mockTable);

        final DynamoDocumentStoreTemplate dynamoDocumentStoreTemplate = new DynamoDocumentStoreTemplate(
                mockDatabaseSchemaHolder);
        dynamoDocumentStoreTemplate.initialize(mockAmazonDynamoDbClient);

        final Item mockTableItem = mock(Item.class);
        when(mockTableItem.toJSON()).thenReturn(dynamoDocumentStoreTemplate.itemToString(stubItem));

        // When
        final StubItem returnedItem = dynamoDocumentStoreTemplate.create(stubItem);

        // Then
        final ArgumentCaptor<PutItemSpec> getItemRequestCaptor = ArgumentCaptor.forClass(PutItemSpec.class);
        verify(mockTable).putItem(getItemRequestCaptor.capture());

        final PutItemSpec spec = getItemRequestCaptor.getValue();
        assertEquals(itemId.value(), spec.getItem().get("id"));

        assertEquals(itemId.value(), returnedItem.getId());
        assertEquals(stubItem.getStringProperty(), returnedItem.getStringProperty());
        assertEquals(stubItem.getStringProperty2(), returnedItem.getStringProperty2());
        assertEquals(stubItem.getStringSetProperty(), returnedItem.getStringSetProperty());
    }

    @Test
    public void shouldQueryTable() {
        // Given
        final ItemId itemId = new ItemId(randomId());

        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);

        final Table mockTable = mock(Table.class);
        when(mockDynamoDBClient.getTable(any(String.class))).thenReturn(mockTable);

        final DynamoDocumentStoreTemplate dynamoDocumentStoreTemplate = new DynamoDocumentStoreTemplate(
                mockDatabaseSchemaHolder);
        dynamoDocumentStoreTemplate.initialize(mockAmazonDynamoDbClient);

        final ItemCollection<QueryOutcome> outcome = mock(ItemCollection.class);
        when(mockTable.query(any(QuerySpec.class))).thenReturn(outcome);
        // when
        dynamoDocumentStoreTemplate.fetch(new AttributeQuery("id", new Condition(Operators.EQUALS, itemId.value())),
                StubItem.class);
        // then
        verify(mockTable.query(any(QuerySpec.class)));
    }

    @Test
    public void shouldQueryIndex_withAttributeQuery() {
        // Given
        final ItemId itemId = new ItemId(randomId());

        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        itemConfiguration.registerIndexes(Arrays.asList(new IndexDefinition("stringProperty")));
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);

        final Table mockTable = mock(Table.class);
        when(mockDynamoDBClient.getTable(any(String.class))).thenReturn(mockTable);

        final DynamoDocumentStoreTemplate dynamoDocumentStoreTemplate = new DynamoDocumentStoreTemplate(
                mockDatabaseSchemaHolder);
        dynamoDocumentStoreTemplate.initialize(mockAmazonDynamoDbClient);

        final Index mockIndex = mock(Index.class);
        when(mockTable.getIndex(anyString())).thenReturn(mockIndex);

        final ItemCollection<QueryOutcome> outcome = mock(ItemCollection.class);
        when(mockIndex.query(any(QuerySpec.class))).thenReturn(outcome);

        // when
        dynamoDocumentStoreTemplate.fetch(
                new AttributeQuery("stringProperty", new Condition(Operators.EQUALS, itemId.value())), StubItem.class);

        // then
        verify(mockIndex.query(any(QuerySpec.class)));
    }

    @Test
    public void shouldQueryIndex_withCompoundAttributeQuery() {
        // Given
        final ItemId itemId = new ItemId(randomId());

        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubWithGlobalSecondaryIndexItem.class,
                tableName);
        itemConfiguration.registerIndexes((Arrays.asList(new CompoundIndexDefinition("gsi", "gsiSupportingValue"))));
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);

        final Table mockTable = mock(Table.class);
        when(mockDynamoDBClient.getTable(any(String.class))).thenReturn(mockTable);

        final DynamoDocumentStoreTemplate dynamoDocumentStoreTemplate = new DynamoDocumentStoreTemplate(
                mockDatabaseSchemaHolder);
        dynamoDocumentStoreTemplate.initialize(mockAmazonDynamoDbClient);

        final Index mockIndex = mock(Index.class);
        when(mockTable.getIndex(anyString())).thenReturn(mockIndex);

        final ItemCollection<QueryOutcome> mockOutcome = mock(ItemCollection.class);
        when(mockIndex.query(any(QuerySpec.class))).thenReturn(mockOutcome);

        final IteratorSupport<Item, QueryOutcome> mockIterator = mock(IteratorSupport.class);
        final Item mockItem = new Item();
        mockItem.withString(randomString(), randomString());

        when(mockOutcome.iterator()).thenReturn(mockIterator);
        when(mockIterator.hasNext()).thenReturn(true, false);
        when(mockIterator.next()).thenReturn(mockItem);

        // When
        final Collection<StubWithGlobalSecondaryIndexItem> stubWithGlobalSecondaryIndexItemCollection = dynamoDocumentStoreTemplate
                .fetch(new CompoundAttributeQuery("gsi", new Condition(Operators.EQUALS, itemId.value()),
                        "gsiSupportingValue", new Condition(Operators.EQUALS, String.valueOf(randomInt(10)))),
                        StubWithGlobalSecondaryIndexItem.class);

        // Then
        assertTrue(stubWithGlobalSecondaryIndexItemCollection.size() == 1);
        final ArgumentCaptor<QuerySpec> querySpecCaptor = ArgumentCaptor.forClass(QuerySpec.class);
        verify(mockIndex).query(querySpecCaptor.capture());
        assertNotNull(querySpecCaptor.getValue().getRangeKeyCondition());
    }

    @Test
    public void shouldNotCreate_withItem() {
        // Given
        final ItemId itemId = new ItemId(randomId());
        final StubItem stubItem = generateRandomStubItem(itemId);

        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);

        final Table mockTable = mock(Table.class);
        when(mockDynamoDBClient.getTable(any(String.class))).thenReturn(mockTable);

        final DynamoDocumentStoreTemplate dynamoDocumentStoreTemplate = new DynamoDocumentStoreTemplate(
                mockDatabaseSchemaHolder);
        dynamoDocumentStoreTemplate.initialize(mockAmazonDynamoDbClient);

        final Item mockTableItem = mock(Item.class);
        when(mockTableItem.toJSON()).thenReturn(dynamoDocumentStoreTemplate.itemToString(stubItem));

        doThrow(RuntimeException.class).when(mockTable).putItem(any(PutItemSpec.class));
        RuntimeException thrownException = null;

        // When
        try {
            dynamoDocumentStoreTemplate.create(stubItem);
        } catch (final RuntimeException runtimeException) {
            thrownException = runtimeException;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldRead_withItemIdAndItemClass() throws Exception {
        // Given
        final ItemId itemId = new ItemId(randomId());

        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);

        final DynamoDocumentStoreTemplate dynamoDocumentStoreTemplate = new DynamoDocumentStoreTemplate(
                mockDatabaseSchemaHolder);
        dynamoDocumentStoreTemplate.initialize(mockAmazonDynamoDbClient);

        final Table mockTable = mock(Table.class);
        when(mockDynamoDBClient.getTable(any(String.class))).thenReturn(mockTable);

        final Item mockTableItem = mock(Item.class);
        when(mockTable.getItem(any(GetItemSpec.class))).thenReturn(mockTableItem);

        final StubItem stubItem = generateRandomStubItem(itemId);
        when(mockTableItem.toJSON()).thenReturn(dynamoDocumentStoreTemplate.itemToString(stubItem));

        // When
        final StubItem returnedItem = dynamoDocumentStoreTemplate.read(itemId, StubItem.class);

        // Then
        final ArgumentCaptor<GetItemSpec> getItemRequestCaptor = ArgumentCaptor.forClass(GetItemSpec.class);
        verify(mockTable).getItem(getItemRequestCaptor.capture());

        final GetItemSpec spec = getItemRequestCaptor.getValue();
        assertEquals(1, spec.getKeyComponents().size());
        assertEquals(itemId.value(), spec.getKeyComponents().iterator().next().getValue());

        assertEquals(itemId.value(), returnedItem.getId());
        assertEquals(stubItem.getStringProperty(), returnedItem.getStringProperty());
        assertEquals(stubItem.getStringProperty2(), returnedItem.getStringProperty2());
        assertEquals(stubItem.getStringSetProperty(), returnedItem.getStringSetProperty());
    }

    @Test
    public void shouldNotRead_withNonExistentItemExceptionNoItem() throws Exception {
        // Given
        final ItemId itemId = new ItemId(randomId());

        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);

        final DynamoDocumentStoreTemplate dynamoDocumentStoreTemplate = new DynamoDocumentStoreTemplate(
                mockDatabaseSchemaHolder);
        dynamoDocumentStoreTemplate.initialize(mockAmazonDynamoDbClient);

        final Table mockTable = mock(Table.class);
        when(mockDynamoDBClient.getTable(any(String.class))).thenReturn(mockTable);

        when(mockTable.getItem(any(GetItemSpec.class))).thenReturn(null);

        NonExistentItemException thrownException = null;
        // When
        try {
            dynamoDocumentStoreTemplate.read(itemId, StubItem.class);
        } catch (final NonExistentItemException nonExistentItemException) {
            thrownException = nonExistentItemException;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldNotRead_withNonExistentItemExceptionNoContent() throws Exception {
        // Given
        final ItemId itemId = new ItemId(randomId());

        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);

        final DynamoDocumentStoreTemplate dynamoDocumentStoreTemplate = new DynamoDocumentStoreTemplate(
                mockDatabaseSchemaHolder);
        dynamoDocumentStoreTemplate.initialize(mockAmazonDynamoDbClient);

        final Table mockTable = mock(Table.class);
        when(mockDynamoDBClient.getTable(any(String.class))).thenReturn(mockTable);

        final Item mockTableItem = mock(Item.class);
        when(mockTable.getItem(any(GetItemSpec.class))).thenReturn(mockTableItem);

        when(mockTableItem.toJSON()).thenReturn("");

        NonExistentItemException thrownException = null;
        // When
        try {
            dynamoDocumentStoreTemplate.read(itemId, StubItem.class);
        } catch (final NonExistentItemException nonExistentItemException) {
            thrownException = nonExistentItemException;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldUpdate_withItem() {
        // Given
        final ItemId itemId = new ItemId(randomId());
        final StubItem stubItem = generateRandomStubItem(itemId);
        final StubItem previousStubItem = generateRandomStubItem(itemId);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        final Table mockTable = mock(Table.class);
        final Item mockTableItem = mock(Item.class);
        final PrimaryKey primaryKey = new PrimaryKey();
        primaryKey.addComponent("id", itemId.value());
        final Item previousItem = mock(Item.class);

        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        when(mockDynamoDBClient.getTable(schemaName + "." + tableName)).thenReturn(mockTable);
        when(mockTable.getItem(any(PrimaryKey.class))).thenReturn(previousItem);

        final DynamoDocumentStoreTemplate dynamoDocumentStoreTemplate = new DynamoDocumentStoreTemplate(
                mockDatabaseSchemaHolder);
        when(previousItem.toJSON()).thenReturn(dynamoDocumentStoreTemplate.itemToString(previousStubItem));
        when(mockTableItem.toJSON()).thenReturn(dynamoDocumentStoreTemplate.itemToString(stubItem));

        dynamoDocumentStoreTemplate.initialize(mockAmazonDynamoDbClient);

        // When
        final StubItem returnedItem = dynamoDocumentStoreTemplate.update(stubItem);

        // Then
        final ArgumentCaptor<PutItemSpec> putItemRequestCaptor = ArgumentCaptor.forClass(PutItemSpec.class);
        verify(mockTable).putItem(putItemRequestCaptor.capture());
        final PutItemSpec spec = putItemRequestCaptor.getValue();
        assertEquals(itemId.value(), spec.getItem().get("id"));
        assertEquals(itemId.value(), returnedItem.getId());
        assertEquals(stubItem.getStringProperty(), returnedItem.getStringProperty());
        assertEquals(stubItem.getStringProperty2(), returnedItem.getStringProperty2());
        assertEquals(stubItem.getStringSetProperty(), returnedItem.getStringSetProperty());
    }

    @Test
    public void shouldNotUpdate_withPutItemException() {
        // Given
        final ItemId itemId = new ItemId(randomId());
        final StubItem stubItem = generateRandomStubItem(itemId);
        final StubItem previousStubItem = generateRandomStubItem(itemId);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        final Table mockTable = mock(Table.class);
        final Item mockTableItem = mock(Item.class);
        final PrimaryKey primaryKey = new PrimaryKey();
        primaryKey.addComponent("id", itemId.value());
        final Item previousItem = mock(Item.class);

        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        when(mockDynamoDBClient.getTable(schemaName + "." + tableName)).thenReturn(mockTable);
        when(mockTable.getItem(any(PrimaryKey.class))).thenReturn(previousItem);

        final DynamoDocumentStoreTemplate dynamoDocumentStoreTemplate = new DynamoDocumentStoreTemplate(
                mockDatabaseSchemaHolder);
        when(previousItem.toJSON()).thenReturn(dynamoDocumentStoreTemplate.itemToString(previousStubItem));
        when(mockTableItem.toJSON()).thenReturn(dynamoDocumentStoreTemplate.itemToString(stubItem));
        when(mockTable.putItem(any(PutItemSpec.class))).thenThrow(ConditionalCheckFailedException.class);

        dynamoDocumentStoreTemplate.initialize(mockAmazonDynamoDbClient);

        // When
        OptimisticLockException thrownException = null;
        try {
            dynamoDocumentStoreTemplate.update(stubItem);
        } catch (final OptimisticLockException optimisticLockException) {
            thrownException = optimisticLockException;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldDelete_withItem() {
        // Given
        final ItemId itemId = new ItemId(randomId());
        final StubItem stubItem = generateRandomStubItem(itemId);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        final Table mockTable = mock(Table.class);

        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        when(mockDynamoDBClient.getTable(any(String.class))).thenReturn(mockTable);

        final DynamoDocumentStoreTemplate dynamoDocumentStoreTemplate = new DynamoDocumentStoreTemplate(
                mockDatabaseSchemaHolder);
        dynamoDocumentStoreTemplate.initialize(mockAmazonDynamoDbClient);

        // When
        dynamoDocumentStoreTemplate.delete(stubItem);

        // Then
        final ArgumentCaptor<DeleteItemSpec> getItemRequestCaptor = ArgumentCaptor.forClass(DeleteItemSpec.class);
        verify(mockTable).deleteItem(getItemRequestCaptor.capture());
    }

    private StubItem generateRandomStubItem(final ItemId itemId) {
        final StubItem item = new StubItem();
        item.setBooleanProperty(Randoms.randomBoolean());
        item.setId(itemId.value());
        item.setStringProperty(Randoms.randomString());
        item.setStringProperty2(Randoms.randomString());
        item.setVersion(Randoms.randomLong());
        final Set<String> stringSet = new HashSet<String>();
        for (int i = 0; i < Randoms.randomInt(20); i++) {
            stringSet.add(Randoms.randomString());
        }
        item.setStringSetProperty(stringSet);
        return item;
    }

}
