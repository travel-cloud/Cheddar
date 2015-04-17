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
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
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
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.clicktravel.cheddar.infrastructure.persistence.database.ItemId;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.DatabaseSchemaHolder;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.ItemConfiguration;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.OptimisticLockException;
import com.clicktravel.common.random.Randoms;

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

    @SuppressWarnings("deprecation")
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

    @SuppressWarnings("deprecation")
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

    @SuppressWarnings("deprecation")
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

    @SuppressWarnings("deprecation")
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

    @SuppressWarnings("deprecation")
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

    @SuppressWarnings("deprecation")
    @Test
    public void shouldUpdate_withItem() {
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
        final StubItem returnedItem = dynamoDocumentStoreTemplate.update(stubItem);

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

    @SuppressWarnings("deprecation")
    @Test
    public void shouldNotUpdate_withItem() {
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

        doThrow(ConditionalCheckFailedException.class).when(mockTable).putItem(any(PutItemSpec.class));

        OptimisticLockException thrownException = null;

        // When
        try {
            dynamoDocumentStoreTemplate.update(stubItem);
        } catch (final OptimisticLockException optimisticLockException) {
            thrownException = optimisticLockException;
        }

        // Then
        assertNotNull(thrownException);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void shouldDelete_withItem() {
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
