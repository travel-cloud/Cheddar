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
import static com.clicktravel.common.random.Randoms.randomLong;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.util.collections.Sets;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.clicktravel.cheddar.infrastructure.persistence.database.GeneratedKeyHolder;
import com.clicktravel.cheddar.infrastructure.persistence.database.ItemId;
import com.clicktravel.cheddar.infrastructure.persistence.database.SequenceKeyGenerator;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.*;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.ItemConstraintViolationException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.OptimisticLockException;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.AttributeQuery;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Condition;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Operators;
import com.clicktravel.cheddar.infrastructure.persistence.exception.PersistenceResourceFailureException;
import com.clicktravel.common.random.Randoms;

@SuppressWarnings("unchecked")
public class DynamoDbTemplateTest {

    private DatabaseSchemaHolder mockDatabaseSchemaHolder;
    private String schemaName;
    private String tableName;
    private AmazonDynamoDB mockAmazonDynamoDbClient;

    @Before
    public void setup() {
        schemaName = randomString(10);
        tableName = randomString(10);
        mockDatabaseSchemaHolder = mock(DatabaseSchemaHolder.class);
        when(mockDatabaseSchemaHolder.schemaName()).thenReturn(schemaName);
        mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
    }

    @Test
    public void shouldRead_withItemIdAndItemClass() throws Exception {
        // Given
        final ItemId itemId = new ItemId(randomId());

        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final GetItemResult mockGetItemResult = mock(GetItemResult.class);
        final String stringProperty = randomString(10);
        final String stringProperty2 = randomString(10);
        final Set<String> stringSetProperty = Sets.newSet(randomString(10), randomString(10), randomString(10));
        final Map<String, AttributeValue> itemAttributeMap = new HashMap<>();
        itemAttributeMap.put("id", new AttributeValue(itemId.value()));
        itemAttributeMap.put("stringProperty", new AttributeValue(stringProperty));
        itemAttributeMap.put("stringProperty2", new AttributeValue(stringProperty2));
        itemAttributeMap.put("stringSetProperty", new AttributeValue().withSS(stringSetProperty));
        when(mockGetItemResult.getItem()).thenReturn(itemAttributeMap);
        when(mockAmazonDynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(mockGetItemResult);

        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);

        // When
        final StubItem returnedItem = dynamoDbTemplate.read(itemId, StubItem.class);

        // Then
        final ArgumentCaptor<GetItemRequest> getItemRequestCaptor = ArgumentCaptor.forClass(GetItemRequest.class);
        verify(mockAmazonDynamoDbClient).getItem(getItemRequestCaptor.capture());
        assertEquals(itemId.value(), getItemRequestCaptor.getValue().getKey().get("id").getS());
        assertEquals(itemId.value(), returnedItem.getId());
        assertEquals(stringProperty, returnedItem.getStringProperty());
        assertEquals(stringProperty2, returnedItem.getStringProperty2());
        assertEquals(stringSetProperty, returnedItem.getStringSetProperty());
    }

    @Test
    public void shouldNotRead_withAmazonServiceException() throws Exception {
        // Given
        final ItemId itemId = new ItemId(randomId());

        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        when(mockAmazonDynamoDbClient.getItem(any(GetItemRequest.class))).thenThrow(AmazonServiceException.class);

        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);

        // When
        PersistenceResourceFailureException actualException = null;
        try {
            dynamoDbTemplate.read(itemId, StubItem.class);
        } catch (final PersistenceResourceFailureException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        final ArgumentCaptor<GetItemRequest> getItemRequestCaptor = ArgumentCaptor.forClass(GetItemRequest.class);
        verify(mockAmazonDynamoDbClient).getItem(getItemRequestCaptor.capture());
        assertEquals(itemId.value(), getItemRequestCaptor.getValue().getKey().get("id").getS());
    }

    @Test
    public void shouldFetch_withAttributeQueryOnPrimaryKey() throws Exception {
        // Given
        final AttributeQuery query = mock(AttributeQuery.class);
        final Condition mockCondition = mock(Condition.class);
        when(mockCondition.getComparisonOperator()).thenReturn(Operators.EQUALS);
        final String itemId = randomId();
        final String stringProperty = randomString(10);
        final Set<String> stringPropertyValues = new HashSet<>(Arrays.asList(stringProperty));
        when(mockCondition.getValues()).thenReturn(stringPropertyValues);
        when(query.getAttributeName()).thenReturn("id");
        when(query.getCondition()).thenReturn(mockCondition);

        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);

        final QueryResult mockQueryResult = mock(QueryResult.class);
        final Map<String, AttributeValue> mockItem = new HashMap<>();
        mockItem.put("id", new AttributeValue(itemId));
        mockItem.put("stringProperty", new AttributeValue(stringProperty));
        final List<Map<String, AttributeValue>> mockItems = Arrays.asList(mockItem);
        when(mockQueryResult.getItems()).thenReturn(mockItems);
        when(mockQueryResult.getLastEvaluatedKey()).thenReturn(null);
        when(mockAmazonDynamoDbClient.query(any(QueryRequest.class))).thenReturn(mockQueryResult);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);

        // When
        final Collection<StubItem> returnedItems = dynamoDbTemplate.fetch(query, StubItem.class);

        // Then
        final ArgumentCaptor<QueryRequest> queryRequestArgumentCaptor = ArgumentCaptor.forClass(QueryRequest.class);
        verify(mockAmazonDynamoDbClient).query(queryRequestArgumentCaptor.capture());
        final QueryRequest queryRequest = queryRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "." + tableName, queryRequest.getTableName());
        assertNull(queryRequest.getIndexName());
        assertEquals(1, queryRequest.getKeyConditions().size());
        assertEquals("EQ", queryRequest.getKeyConditions().get("id").getComparisonOperator());
        assertEquals(1, queryRequest.getKeyConditions().get("id").getAttributeValueList().size());
        assertEquals(new AttributeValue(stringProperty), queryRequest.getKeyConditions().get("id")
                .getAttributeValueList().get(0));
        assertNotNull(returnedItems);
        assertEquals(1, returnedItems.size());
    }

    @Test
    public void shouldFetch_withAttributeQueryOnIndex() throws Exception {
        // Given
        final AttributeQuery query = mock(AttributeQuery.class);
        final Condition mockCondition = mock(Condition.class);
        when(mockCondition.getComparisonOperator()).thenReturn(Operators.EQUALS);
        final String itemId = randomId();
        final String stringProperty = randomString(10);
        final Set<String> stringPropertyValues = new HashSet<>(Arrays.asList(stringProperty));
        when(mockCondition.getValues()).thenReturn(stringPropertyValues);
        when(query.getAttributeName()).thenReturn("stringProperty");
        when(query.getCondition()).thenReturn(mockCondition);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        itemConfiguration.registerIndexes(Arrays.asList(new IndexDefinition("stringProperty")));
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final QueryResult mockQueryResult = mock(QueryResult.class);
        final Map<String, AttributeValue> mockItem = new HashMap<>();
        mockItem.put("id", new AttributeValue(itemId));
        mockItem.put("stringProperty", new AttributeValue(stringProperty));
        final List<Map<String, AttributeValue>> mockItems = Arrays.asList(mockItem);
        when(mockQueryResult.getItems()).thenReturn(mockItems);
        when(mockQueryResult.getLastEvaluatedKey()).thenReturn(null);
        when(mockAmazonDynamoDbClient.query(any(QueryRequest.class))).thenReturn(mockQueryResult);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);

        // When
        final Collection<StubItem> returnedItems = dynamoDbTemplate.fetch(query, StubItem.class);

        // Then
        final ArgumentCaptor<QueryRequest> queryRequestArgumentCaptor = ArgumentCaptor.forClass(QueryRequest.class);
        verify(mockAmazonDynamoDbClient).query(queryRequestArgumentCaptor.capture());
        final QueryRequest queryRequest = queryRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "." + tableName, queryRequest.getTableName());
        assertEquals("stringProperty_idx", queryRequest.getIndexName());
        assertEquals(1, queryRequest.getKeyConditions().size());
        assertEquals("EQ", queryRequest.getKeyConditions().get("stringProperty").getComparisonOperator());
        assertEquals(1, queryRequest.getKeyConditions().get("stringProperty").getAttributeValueList().size());
        assertEquals(new AttributeValue(stringProperty), queryRequest.getKeyConditions().get("stringProperty")
                .getAttributeValueList().get(0));
        assertNotNull(returnedItems);
        assertEquals(1, returnedItems.size());
    }

    @Test
    public void shouldNotFetch_withAmazonServiceException() throws Exception {
        // Given
        final AttributeQuery query = mock(AttributeQuery.class);
        final Condition mockCondition = mock(Condition.class);
        when(mockCondition.getComparisonOperator()).thenReturn(Operators.EQUALS);
        final String stringProperty = randomString(10);
        final Set<String> stringPropertyValues = new HashSet<>(Arrays.asList(stringProperty));
        when(mockCondition.getValues()).thenReturn(stringPropertyValues);
        when(query.getAttributeName()).thenReturn("id");
        when(query.getCondition()).thenReturn(mockCondition);

        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        when(mockAmazonDynamoDbClient.query(any(QueryRequest.class))).thenThrow(AmazonServiceException.class);

        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);

        // When
        PersistenceResourceFailureException actualException = null;
        try {
            dynamoDbTemplate.fetch(query, StubItem.class);
        } catch (final PersistenceResourceFailureException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        final ArgumentCaptor<QueryRequest> queryRequestArgumentCaptor = ArgumentCaptor.forClass(QueryRequest.class);
        verify(mockAmazonDynamoDbClient).query(queryRequestArgumentCaptor.capture());
        final QueryRequest queryRequest = queryRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "." + tableName, queryRequest.getTableName());
        assertNull(queryRequest.getIndexName());
        assertEquals(1, queryRequest.getKeyConditions().size());
        assertEquals("EQ", queryRequest.getKeyConditions().get("id").getComparisonOperator());
        assertEquals(1, queryRequest.getKeyConditions().get("id").getAttributeValueList().size());
        assertEquals(new AttributeValue(stringProperty), queryRequest.getKeyConditions().get("id")
                .getAttributeValueList().get(0));
    }

    @Test
    public void shouldCreateItem_withStubItem() throws Exception {
        // Given
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        final String stringPropertyValue = randomString(10);
        stubItem.setStringProperty(stringPropertyValue);

        // When
        dynamoDbTemplate.create(stubItem);

        // Then
        final ArgumentCaptor<PutItemRequest> putItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(PutItemRequest.class);
        verify(mockAmazonDynamoDbClient).putItem(putItemRequestArgumentCaptor.capture());
        final PutItemRequest putItemRequest = putItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "." + tableName, putItemRequest.getTableName());
        assertEquals(new AttributeValue(stubItem.getId()), putItemRequest.getItem().get("id"));
        assertEquals(new AttributeValue(stringPropertyValue), putItemRequest.getItem().get("stringProperty"));
        assertEquals(new ExpectedAttributeValue(false), putItemRequest.getExpected().get("id"));
    }

    @Test
    public void shouldNotCreateItem_withStubItemWithExistingPrimaryKey() throws Exception {
        // Given
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        final String stringPropertyValue = randomString(10);
        stubItem.setStringProperty(stringPropertyValue);
        when(mockAmazonDynamoDbClient.putItem(any(PutItemRequest.class))).thenThrow(
                ConditionalCheckFailedException.class);

        // When
        ItemConstraintViolationException actualException = null;
        try {
            dynamoDbTemplate.create(stubItem);
        } catch (final ItemConstraintViolationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCreateItem_withUniqueConstraint() throws Exception {
        // Given
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        itemConfiguration.registerUniqueConstraints(Arrays.asList(new UniqueConstraint("stringProperty")));
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        final String stringPropertyValue = randomString(10);
        stubItem.setStringProperty(stringPropertyValue);

        // When
        dynamoDbTemplate.create(stubItem);

        // Then
        final ArgumentCaptor<PutItemRequest> putItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(PutItemRequest.class);
        verify(mockAmazonDynamoDbClient, times(2)).putItem(putItemRequestArgumentCaptor.capture());
        final List<PutItemRequest> putItemRequests = putItemRequestArgumentCaptor.getAllValues();
        final Iterator<PutItemRequest> iterator = putItemRequests.iterator();
        final PutItemRequest putItemRequest1 = iterator.next();

        assertEquals(schemaName + "-indexes." + tableName, putItemRequest1.getTableName());
        assertEquals(2, putItemRequest1.getItem().size());
        assertEquals(new AttributeValue("stringProperty"), putItemRequest1.getItem().get("property"));
        assertEquals(new AttributeValue(stringPropertyValue.toUpperCase()), putItemRequest1.getItem().get("value"));
        assertEquals(new ExpectedAttributeValue(false), putItemRequest1.getExpected().get("value"));

        final PutItemRequest putItemRequest2 = iterator.next();
        assertEquals(schemaName + "." + tableName, putItemRequest2.getTableName());
        assertEquals(new AttributeValue(stubItem.getId()), putItemRequest2.getItem().get("id"));
        assertEquals(new AttributeValue(stringPropertyValue), putItemRequest2.getItem().get("stringProperty"));
        assertEquals(new ExpectedAttributeValue(false), putItemRequest2.getExpected().get("id"));
    }

    @Test
    public void shouldNotCreateItem_withUniqueConstraintAndDuplicate() throws Exception {
        // Given
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final UniqueConstraint uniqueConstraint = new UniqueConstraint("stringProperty");
        itemConfiguration.registerUniqueConstraints(Arrays.asList(uniqueConstraint));
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);
        when(mockAmazonDynamoDbClient.putItem(any(PutItemRequest.class))).thenThrow(
                ConditionalCheckFailedException.class);
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        final String stringPropertyValue = randomString(10);
        stubItem.setStringProperty(stringPropertyValue);

        // When
        ItemConstraintViolationException actualException = null;
        try {
            dynamoDbTemplate.create(stubItem);
        } catch (final ItemConstraintViolationException e) {
            actualException = e;
        }

        // Then
        final ArgumentCaptor<PutItemRequest> putItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(PutItemRequest.class);
        verify(mockAmazonDynamoDbClient, times(1)).putItem(putItemRequestArgumentCaptor.capture());
        final PutItemRequest putItemRequest1 = putItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "-indexes." + tableName, putItemRequest1.getTableName());
        assertEquals(2, putItemRequest1.getItem().size());
        assertEquals(new AttributeValue("stringProperty"), putItemRequest1.getItem().get("property"));
        assertEquals(new AttributeValue(stringPropertyValue.toUpperCase()), putItemRequest1.getItem().get("value"));
        assertEquals(new ExpectedAttributeValue(false), putItemRequest1.getExpected().get("value"));
        assertNotNull(actualException);
        verify(mockAmazonDynamoDbClient, never()).deleteItem(any(DeleteItemRequest.class));
    }

    @Test
    public void shouldNotCreateItem_withUniqueConstraintAndDuplicateWithDifferentCase() throws Exception {
        // Given
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final UniqueConstraint uniqueConstraint = new UniqueConstraint("stringProperty");
        itemConfiguration.registerUniqueConstraints(Arrays.asList(uniqueConstraint));
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);
        when(mockAmazonDynamoDbClient.putItem(any(PutItemRequest.class))).thenThrow(
                ConditionalCheckFailedException.class);
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        final String stringPropertyValue = randomString(10);
        stubItem.setStringProperty(stringPropertyValue.toLowerCase());

        // When
        ItemConstraintViolationException actualException = null;
        try {
            dynamoDbTemplate.create(stubItem);
        } catch (final ItemConstraintViolationException e) {
            actualException = e;
        }

        // Then
        final ArgumentCaptor<PutItemRequest> putItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(PutItemRequest.class);
        verify(mockAmazonDynamoDbClient, times(1)).putItem(putItemRequestArgumentCaptor.capture());
        final PutItemRequest putItemRequest1 = putItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "-indexes." + tableName, putItemRequest1.getTableName());
        assertEquals(2, putItemRequest1.getItem().size());
        assertEquals(new AttributeValue("stringProperty"), putItemRequest1.getItem().get("property"));
        assertEquals(new AttributeValue(stringPropertyValue.toUpperCase()), putItemRequest1.getItem().get("value"));
        assertEquals(new ExpectedAttributeValue(false), putItemRequest1.getExpected().get("value"));
        assertNotNull(actualException);
        verify(mockAmazonDynamoDbClient, never()).deleteItem(any(DeleteItemRequest.class));
    }

    @Test
    public void shouldNotCreateItem_withUniqueConstraintAndFailedIndexCreation() throws Exception {
        // Given
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final UniqueConstraint uniqueConstraint = new UniqueConstraint("stringProperty");
        itemConfiguration.registerUniqueConstraints(Arrays.asList(uniqueConstraint));
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);
        when(mockAmazonDynamoDbClient.putItem(any(PutItemRequest.class))).thenReturn(null).thenThrow(
                AmazonServiceException.class);
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        final String stringPropertyValue = randomString(10);
        stubItem.setStringProperty(stringPropertyValue);

        // When
        PersistenceResourceFailureException actualException = null;
        try {
            dynamoDbTemplate.create(stubItem);
        } catch (final PersistenceResourceFailureException e) {
            actualException = e;
        }

        // Then
        final ArgumentCaptor<PutItemRequest> putItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(PutItemRequest.class);
        final ArgumentCaptor<DeleteItemRequest> deleteItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(DeleteItemRequest.class);
        verify(mockAmazonDynamoDbClient, times(2)).putItem(putItemRequestArgumentCaptor.capture());
        verify(mockAmazonDynamoDbClient).deleteItem(deleteItemRequestArgumentCaptor.capture());
        final List<PutItemRequest> putItemRequests = putItemRequestArgumentCaptor.getAllValues();
        final Iterator<PutItemRequest> iterator = putItemRequests.iterator();
        final PutItemRequest putItemRequest1 = iterator.next();

        assertEquals(schemaName + "-indexes." + tableName, putItemRequest1.getTableName());
        assertEquals(2, putItemRequest1.getItem().size());
        assertEquals(new AttributeValue("stringProperty"), putItemRequest1.getItem().get("property"));
        assertEquals(new AttributeValue(stringPropertyValue.toUpperCase()), putItemRequest1.getItem().get("value"));
        assertEquals(new ExpectedAttributeValue(false), putItemRequest1.getExpected().get("value"));

        final PutItemRequest putItemRequest2 = iterator.next();
        assertEquals(schemaName + "." + tableName, putItemRequest2.getTableName());
        assertEquals(new AttributeValue(stubItem.getId()), putItemRequest2.getItem().get("id"));
        assertEquals(new AttributeValue(stringPropertyValue), putItemRequest2.getItem().get("stringProperty"));
        assertEquals(new ExpectedAttributeValue(false), putItemRequest2.getExpected().get("id"));
        assertNotNull(actualException);

        final DeleteItemRequest deleteItemRequest = deleteItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "-indexes." + tableName, deleteItemRequest.getTableName());
        assertEquals(2, deleteItemRequest.getKey().size());
        assertEquals(new AttributeValue("stringProperty"), deleteItemRequest.getKey().get("property"));
        assertEquals(new AttributeValue(stubItem.getStringProperty().toUpperCase()),
                deleteItemRequest.getKey().get("value"));
    }

    @Test
    public void shouldUpdateItem_withStubItem() throws Exception {
        // Given
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        final String stringPropertyValue = randomString(10);
        stubItem.setStringProperty(stringPropertyValue);
        final Long oldVersion = randomLong();
        stubItem.setVersion(oldVersion);

        // When
        dynamoDbTemplate.update(stubItem);

        // Then
        final ArgumentCaptor<UpdateItemRequest> updateItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(UpdateItemRequest.class);
        verify(mockAmazonDynamoDbClient).updateItem(updateItemRequestArgumentCaptor.capture());
        final UpdateItemRequest updateItemRequest = updateItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "." + tableName, updateItemRequest.getTableName());
        final Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue(stubItem.getId()));
        assertEquals(key, updateItemRequest.getKey());
        assertEquals(updateItemRequest.getAttributeUpdates().size(), 5);
        assertEquals(
                new AttributeValueUpdate().withAction(AttributeAction.PUT).withValue(
                        new AttributeValue(stringPropertyValue)),
                        updateItemRequest.getAttributeUpdates().get("stringProperty"));
        assertEquals(new ExpectedAttributeValue(new AttributeValue().withN(String.valueOf(oldVersion))),
                updateItemRequest.getExpected().get("version"));
    }

    @Test
    public void shouldNotUpdateItem_withOptimisticLockException() throws Exception {
        // Given
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        final String stringPropertyValue = randomString(10);
        stubItem.setStringProperty(stringPropertyValue);
        final Long oldVersion = randomLong();
        stubItem.setVersion(oldVersion);
        when(mockAmazonDynamoDbClient.updateItem(any(UpdateItemRequest.class))).thenThrow(
                ConditionalCheckFailedException.class);

        // When
        OptimisticLockException actualException = null;
        try {
            dynamoDbTemplate.update(stubItem);
        } catch (final OptimisticLockException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotUpdateItem_withAmazonServiceException() throws Exception {
        // Given
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        final String stringPropertyValue = randomString(10);
        stubItem.setStringProperty(stringPropertyValue);
        final Long oldVersion = randomLong();
        stubItem.setVersion(oldVersion);
        when(mockAmazonDynamoDbClient.updateItem(any(UpdateItemRequest.class))).thenThrow(AmazonServiceException.class);

        // When
        PersistenceResourceFailureException actualException = null;
        try {
            dynamoDbTemplate.update(stubItem);
        } catch (final PersistenceResourceFailureException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldUpdateItem_withStubItemAndCompoundPk() throws Exception {
        // Given
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName,
                new CompoundPrimaryKeyDefinition("id", "stringProperty"));
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        final String stringPropertyValue = randomString(10);
        stubItem.setStringProperty(stringPropertyValue);
        stubItem.setStringProperty2(randomString(10));
        final Long oldVersion = randomLong();
        stubItem.setVersion(oldVersion);

        // When
        dynamoDbTemplate.update(stubItem);

        // Then
        final ArgumentCaptor<UpdateItemRequest> updateItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(UpdateItemRequest.class);
        verify(mockAmazonDynamoDbClient).updateItem(updateItemRequestArgumentCaptor.capture());
        final UpdateItemRequest updateItemRequest = updateItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "." + tableName, updateItemRequest.getTableName());
        final Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue(stubItem.getId()));
        key.put("stringProperty", new AttributeValue(stubItem.getStringProperty()));
        assertEquals(key, updateItemRequest.getKey());
        assertEquals(updateItemRequest.getAttributeUpdates().size(), 4);
        assertEquals(
                new AttributeValueUpdate().withAction(AttributeAction.PUT).withValue(
                        new AttributeValue(stubItem.getStringProperty2())), updateItemRequest.getAttributeUpdates()
                        .get("stringProperty2"));
        assertEquals(new ExpectedAttributeValue(new AttributeValue().withN(String.valueOf(oldVersion))),
                updateItemRequest.getExpected().get("version"));
    }

    @Test
    public void shouldUpdateItem_withStubItemAndUniqueConstraintProperty() throws Exception {
        // Given
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        final String stringPropertyValue = randomString(10);
        final String previousStringPropertyValue = randomString(10);
        stubItem.setStringProperty(stringPropertyValue);
        final long oldVersion = randomInt(100);
        stubItem.setVersion(oldVersion);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        itemConfiguration.registerUniqueConstraints(Arrays.asList(new UniqueConstraint("stringProperty")));
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        final GetItemResult mockGetItemResult = mock(GetItemResult.class);
        when(mockAmazonDynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(mockGetItemResult);
        final Map<String, AttributeValue> attributeMap = new HashMap<>();
        attributeMap.put("id", new AttributeValue(stubItem.getId()));
        attributeMap.put("stringProperty", new AttributeValue(previousStringPropertyValue));
        attributeMap.put("version", new AttributeValue().withN(String.valueOf(oldVersion)));
        when(mockGetItemResult.getItem()).thenReturn(attributeMap);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);

        // When
        dynamoDbTemplate.update(stubItem);

        // Then
        final ArgumentCaptor<GetItemRequest> getItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(GetItemRequest.class);
        final ArgumentCaptor<PutItemRequest> putItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(PutItemRequest.class);
        final ArgumentCaptor<UpdateItemRequest> updateItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(UpdateItemRequest.class);
        final ArgumentCaptor<DeleteItemRequest> deleteItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(DeleteItemRequest.class);
        verify(mockAmazonDynamoDbClient).getItem(getItemRequestArgumentCaptor.capture());
        verify(mockAmazonDynamoDbClient).putItem(putItemRequestArgumentCaptor.capture());
        verify(mockAmazonDynamoDbClient).updateItem(updateItemRequestArgumentCaptor.capture());
        verify(mockAmazonDynamoDbClient).deleteItem(deleteItemRequestArgumentCaptor.capture());
        final GetItemRequest getItemRequest = getItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "." + tableName, getItemRequest.getTableName());
        assertEquals(new AttributeValue(stubItem.getId()), getItemRequest.getKey().get("id"));

        final PutItemRequest putIndexRequest = putItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "-indexes." + tableName, putIndexRequest.getTableName());
        assertEquals(2, putIndexRequest.getItem().size());
        assertEquals(new AttributeValue("stringProperty"), putIndexRequest.getItem().get("property"));
        assertEquals(new AttributeValue(stringPropertyValue.toUpperCase()), putIndexRequest.getItem().get("value"));
        assertEquals(new ExpectedAttributeValue(false), putIndexRequest.getExpected().get("value"));

        final UpdateItemRequest updateItemRequest = updateItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "." + tableName, updateItemRequest.getTableName());
        final Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue(stubItem.getId()));
        assertEquals(key, updateItemRequest.getKey());
        assertEquals(updateItemRequest.getAttributeUpdates().size(), 5);
        assertEquals(
                new AttributeValueUpdate().withAction(AttributeAction.PUT).withValue(
                        new AttributeValue(stringPropertyValue)),
                        updateItemRequest.getAttributeUpdates().get("stringProperty"));
        assertEquals(new ExpectedAttributeValue(new AttributeValue().withN(String.valueOf(oldVersion))),
                updateItemRequest.getExpected().get("version"));

        final DeleteItemRequest deleteIndexRequest = deleteItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "-indexes." + tableName, deleteIndexRequest.getTableName());
        assertEquals(2, deleteIndexRequest.getKey().size());
        assertEquals(new AttributeValue("stringProperty"), deleteIndexRequest.getKey().get("property"));
        assertEquals(new AttributeValue(previousStringPropertyValue.toUpperCase()),
                deleteIndexRequest.getKey().get("value"));
    }

    @Test
    public void shouldUpdateItem_withStubItemAndUniqueConstraintPropertyButPreviousValueAsNull() throws Exception {
        // Given
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        final String stringPropertyValue = randomString(10);
        stubItem.setStringProperty(stringPropertyValue);
        final long oldVersion = randomInt(100);
        stubItem.setVersion(oldVersion);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        itemConfiguration.registerUniqueConstraints(Arrays.asList(new UniqueConstraint("stringProperty")));
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        final GetItemResult mockGetItemResult = mock(GetItemResult.class);
        when(mockAmazonDynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(mockGetItemResult);
        final Map<String, AttributeValue> attributeMap = new HashMap<>();
        attributeMap.put("id", new AttributeValue(stubItem.getId()));
        attributeMap.put("version", new AttributeValue().withN(String.valueOf(oldVersion)));
        when(mockGetItemResult.getItem()).thenReturn(attributeMap);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);

        // When
        dynamoDbTemplate.update(stubItem);

        // Then
        final ArgumentCaptor<GetItemRequest> getItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(GetItemRequest.class);
        final ArgumentCaptor<UpdateItemRequest> updateItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(UpdateItemRequest.class);
        final ArgumentCaptor<PutItemRequest> putItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(PutItemRequest.class);
        verify(mockAmazonDynamoDbClient).getItem(getItemRequestArgumentCaptor.capture());
        verify(mockAmazonDynamoDbClient).updateItem(updateItemRequestArgumentCaptor.capture());
        verify(mockAmazonDynamoDbClient).putItem(putItemRequestArgumentCaptor.capture());
        verify(mockAmazonDynamoDbClient, never()).deleteItem(any(DeleteItemRequest.class));
        final GetItemRequest getItemRequest = getItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "." + tableName, getItemRequest.getTableName());
        assertEquals(new AttributeValue(stubItem.getId()), getItemRequest.getKey().get("id"));

        final PutItemRequest putIndexRequest = putItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "-indexes." + tableName, putIndexRequest.getTableName());
        assertEquals(2, putIndexRequest.getItem().size());
        assertEquals(new AttributeValue("stringProperty"), putIndexRequest.getItem().get("property"));
        assertEquals(new AttributeValue(stringPropertyValue.toUpperCase()), putIndexRequest.getItem().get("value"));
        assertEquals(new ExpectedAttributeValue(false), putIndexRequest.getExpected().get("value"));

        final UpdateItemRequest updateItemRequest = updateItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "." + tableName, updateItemRequest.getTableName());

        final Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue(stubItem.getId()));
        assertEquals(key, updateItemRequest.getKey());
        assertEquals(updateItemRequest.getAttributeUpdates().size(), 5);
        assertEquals(
                new AttributeValueUpdate().withAction(AttributeAction.PUT).withValue(
                        new AttributeValue(stringPropertyValue)),
                        updateItemRequest.getAttributeUpdates().get("stringProperty"));
        assertEquals(new ExpectedAttributeValue(new AttributeValue().withN(String.valueOf(oldVersion))),
                updateItemRequest.getExpected().get("version"));
    }

    @Test
    public void shouldUpdateItem_withStubItemAndUniqueConstraintPropertyButUpdatedValueAsNull() throws Exception {
        // Given
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        final String previousStringPropertyValue = randomString(10);
        final String stringPropertyValue = null;
        stubItem.setStringProperty(stringPropertyValue);
        final long oldVersion = randomInt(100);
        stubItem.setVersion(oldVersion);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        itemConfiguration.registerUniqueConstraints(Arrays.asList(new UniqueConstraint("stringProperty")));
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        final GetItemResult mockGetItemResult = mock(GetItemResult.class);
        when(mockAmazonDynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(mockGetItemResult);
        final Map<String, AttributeValue> attributeMap = new HashMap<>();
        attributeMap.put("id", new AttributeValue(stubItem.getId()));
        attributeMap.put("stringProperty", new AttributeValue(previousStringPropertyValue));
        attributeMap.put("version", new AttributeValue().withN(String.valueOf(oldVersion)));
        when(mockGetItemResult.getItem()).thenReturn(attributeMap);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);

        // When
        dynamoDbTemplate.update(stubItem);

        // Then
        final ArgumentCaptor<GetItemRequest> getItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(GetItemRequest.class);
        final ArgumentCaptor<UpdateItemRequest> updateItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(UpdateItemRequest.class);
        final ArgumentCaptor<DeleteItemRequest> deleteItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(DeleteItemRequest.class);
        verify(mockAmazonDynamoDbClient).getItem(getItemRequestArgumentCaptor.capture());
        verify(mockAmazonDynamoDbClient).updateItem(updateItemRequestArgumentCaptor.capture());
        verify(mockAmazonDynamoDbClient).deleteItem(deleteItemRequestArgumentCaptor.capture());
        final GetItemRequest getItemRequest = getItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "." + tableName, getItemRequest.getTableName());
        assertEquals(new AttributeValue(stubItem.getId()), getItemRequest.getKey().get("id"));

        final UpdateItemRequest updateItemRequest = updateItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "." + tableName, updateItemRequest.getTableName());
        final Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue(stubItem.getId()));
        assertEquals(key, updateItemRequest.getKey());
        assertEquals(updateItemRequest.getAttributeUpdates().size(), 5);
        assertEquals(new AttributeValueUpdate().withAction(AttributeAction.DELETE), updateItemRequest
                .getAttributeUpdates().get("stringProperty"));
        assertEquals(new ExpectedAttributeValue(new AttributeValue().withN(String.valueOf(oldVersion))),
                updateItemRequest.getExpected().get("version"));

        final DeleteItemRequest deleteIndexRequest = deleteItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "-indexes." + tableName, deleteIndexRequest.getTableName());
        assertEquals(2, deleteIndexRequest.getKey().size());
        assertEquals(new AttributeValue("stringProperty"), deleteIndexRequest.getKey().get("property"));
        assertEquals(new AttributeValue(previousStringPropertyValue.toUpperCase()),
                deleteIndexRequest.getKey().get("value"));
    }

    @Test
    public void shouldUpdateItem_withStubItemAndUniqueConstraintPropertyAndFailedItemUpdate() throws Exception {
        // Given
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        final String newStringPropertyValue = randomString(10);
        final String previousStringPropertyValue = randomString(10);
        stubItem.setStringProperty(newStringPropertyValue);
        final long oldVersion = randomInt(100);
        stubItem.setVersion(oldVersion);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        itemConfiguration.registerUniqueConstraints(Arrays.asList(new UniqueConstraint("stringProperty")));
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        final GetItemResult mockGetItemResult = mock(GetItemResult.class);
        when(mockAmazonDynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(mockGetItemResult);
        when(mockAmazonDynamoDbClient.updateItem(any(UpdateItemRequest.class))).thenThrow(AmazonServiceException.class);
        final Map<String, AttributeValue> attributeMap = new HashMap<>();
        attributeMap.put("id", new AttributeValue(stubItem.getId()));
        attributeMap.put("stringProperty", new AttributeValue(previousStringPropertyValue));
        attributeMap.put("version", new AttributeValue().withN(String.valueOf(oldVersion)));
        when(mockGetItemResult.getItem()).thenReturn(attributeMap);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);

        // When
        PersistenceResourceFailureException actualException = null;
        try {
            dynamoDbTemplate.update(stubItem);

        } catch (final PersistenceResourceFailureException e) {
            actualException = e;
        }

        // Then
        final ArgumentCaptor<GetItemRequest> getItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(GetItemRequest.class);
        final ArgumentCaptor<PutItemRequest> putItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(PutItemRequest.class);
        final ArgumentCaptor<UpdateItemRequest> updateItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(UpdateItemRequest.class);
        final ArgumentCaptor<DeleteItemRequest> deleteItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(DeleteItemRequest.class);
        verify(mockAmazonDynamoDbClient).getItem(getItemRequestArgumentCaptor.capture());
        verify(mockAmazonDynamoDbClient).putItem(putItemRequestArgumentCaptor.capture());
        verify(mockAmazonDynamoDbClient).updateItem(updateItemRequestArgumentCaptor.capture());
        verify(mockAmazonDynamoDbClient).deleteItem(deleteItemRequestArgumentCaptor.capture());
        final GetItemRequest getItemRequest = getItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "." + tableName, getItemRequest.getTableName());
        assertEquals(new AttributeValue(stubItem.getId()), getItemRequest.getKey().get("id"));

        final PutItemRequest putIndexRequest = putItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "-indexes." + tableName, putIndexRequest.getTableName());
        assertEquals(2, putIndexRequest.getItem().size());
        assertEquals(new AttributeValue("stringProperty"), putIndexRequest.getItem().get("property"));
        assertEquals(new AttributeValue(newStringPropertyValue.toUpperCase()), putIndexRequest.getItem().get("value"));
        assertEquals(new ExpectedAttributeValue(false), putIndexRequest.getExpected().get("value"));

        final UpdateItemRequest updateItemRequest = updateItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "." + tableName, updateItemRequest.getTableName());
        final Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue(stubItem.getId()));
        assertEquals(key, updateItemRequest.getKey());
        assertEquals(updateItemRequest.getAttributeUpdates().size(), 5);
        assertEquals(
                new AttributeValueUpdate().withAction(AttributeAction.PUT).withValue(
                        new AttributeValue(newStringPropertyValue)),
                        updateItemRequest.getAttributeUpdates().get("stringProperty"));
        assertEquals(new ExpectedAttributeValue(new AttributeValue().withN(String.valueOf(oldVersion))),
                updateItemRequest.getExpected().get("version"));

        final DeleteItemRequest deleteIndexRequest = deleteItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "-indexes." + tableName, deleteIndexRequest.getTableName());
        assertEquals(2, deleteIndexRequest.getKey().size());
        assertEquals(new AttributeValue("stringProperty"), deleteIndexRequest.getKey().get("property"));
        assertEquals(new AttributeValue(newStringPropertyValue.toUpperCase()), deleteIndexRequest.getKey().get("value"));

        assertNotNull(actualException);
    }

    @Test
    public void shouldUpdateItem_withStubItemAndUniqueConstraintPropertyNotModified() throws Exception {
        // Given
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        final String stringPropertyValue = randomString(10);
        stubItem.setStringProperty(stringPropertyValue);
        final long oldVersion = randomInt(100);
        stubItem.setVersion(oldVersion);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        itemConfiguration.registerUniqueConstraints(Arrays.asList(new UniqueConstraint("stringProperty")));
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        final GetItemResult mockGetItemResult = mock(GetItemResult.class);
        when(mockAmazonDynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(mockGetItemResult);
        final Map<String, AttributeValue> attributeMap = new HashMap<>();
        attributeMap.put("stringProperty", new AttributeValue(stringPropertyValue));
        attributeMap.put("version", new AttributeValue().withN(String.valueOf(oldVersion)));
        when(mockGetItemResult.getItem()).thenReturn(attributeMap);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);

        // When
        dynamoDbTemplate.update(stubItem);

        // Then
        final ArgumentCaptor<GetItemRequest> getItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(GetItemRequest.class);
        final ArgumentCaptor<UpdateItemRequest> updateItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(UpdateItemRequest.class);
        verify(mockAmazonDynamoDbClient).getItem(getItemRequestArgumentCaptor.capture());
        verify(mockAmazonDynamoDbClient).updateItem(updateItemRequestArgumentCaptor.capture());
        final GetItemRequest getItemRequest = getItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "." + tableName, getItemRequest.getTableName());
        assertEquals(new AttributeValue(stubItem.getId()), getItemRequest.getKey().get("id"));
        assertThat(getItemRequest.getAttributesToGet(), hasItems("version", "stringProperty"));

        final UpdateItemRequest updateItemRequest = updateItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "." + tableName, updateItemRequest.getTableName());
        final Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue(stubItem.getId()));
        assertEquals(key, updateItemRequest.getKey());
        assertEquals(updateItemRequest.getAttributeUpdates().size(), 5);
        assertEquals(
                new AttributeValueUpdate().withAction(AttributeAction.PUT).withValue(
                        new AttributeValue(stringPropertyValue)),
                        updateItemRequest.getAttributeUpdates().get("stringProperty"));
        assertEquals(new ExpectedAttributeValue(new AttributeValue().withN(String.valueOf(oldVersion))),
                updateItemRequest.getExpected().get("version"));
    }

    @Test
    public void shouldUpdateItem_withStubItemAndUniqueConstraintPropertyModifiedButDifferentCase() throws Exception {
        // Given
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        final String stringPropertyValue = randomString(10);
        stubItem.setStringProperty(stringPropertyValue);
        final long oldVersion = randomInt(100);
        stubItem.setVersion(oldVersion);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        itemConfiguration.registerUniqueConstraints(Arrays.asList(new UniqueConstraint("stringProperty")));
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        final GetItemResult mockGetItemResult = mock(GetItemResult.class);
        when(mockAmazonDynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(mockGetItemResult);
        final Map<String, AttributeValue> attributeMap = new HashMap<>();
        attributeMap.put("stringProperty", new AttributeValue(stringPropertyValue.toLowerCase()));
        attributeMap.put("version", new AttributeValue().withN(String.valueOf(oldVersion)));
        when(mockGetItemResult.getItem()).thenReturn(attributeMap);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);

        // When
        dynamoDbTemplate.update(stubItem);

        // Then
        final ArgumentCaptor<GetItemRequest> getItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(GetItemRequest.class);
        final ArgumentCaptor<UpdateItemRequest> updateItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(UpdateItemRequest.class);
        verify(mockAmazonDynamoDbClient).getItem(getItemRequestArgumentCaptor.capture());
        verify(mockAmazonDynamoDbClient).updateItem(updateItemRequestArgumentCaptor.capture());
        final GetItemRequest getItemRequest = getItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "." + tableName, getItemRequest.getTableName());
        assertEquals(new AttributeValue(stubItem.getId()), getItemRequest.getKey().get("id"));
        assertThat(getItemRequest.getAttributesToGet(), hasItems("version", "stringProperty"));

        final UpdateItemRequest updateItemRequest = updateItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "." + tableName, updateItemRequest.getTableName());
        final Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue(stubItem.getId()));
        assertEquals(key, updateItemRequest.getKey());
        assertEquals(updateItemRequest.getAttributeUpdates().size(), 5);
        assertEquals(
                new AttributeValueUpdate().withAction(AttributeAction.PUT).withValue(
                        new AttributeValue(stringPropertyValue)),
                        updateItemRequest.getAttributeUpdates().get("stringProperty"));
        assertEquals(new ExpectedAttributeValue(new AttributeValue().withN(String.valueOf(oldVersion))),
                updateItemRequest.getExpected().get("version"));
    }

    @Test
    public void shouldDeleteItem_withStubItem() throws Exception {
        // Given
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        stubItem.setVersion(randomLong());
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);

        // When
        dynamoDbTemplate.delete(stubItem);

        // Then
        final ArgumentCaptor<DeleteItemRequest> deleteItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(DeleteItemRequest.class);
        verify(mockAmazonDynamoDbClient).deleteItem(deleteItemRequestArgumentCaptor.capture());
        final DeleteItemRequest deleteItemRequest = deleteItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "." + tableName, deleteItemRequest.getTableName());
        assertEquals(1, deleteItemRequest.getKey().size());
        assertEquals(new AttributeValue(stubItem.getId()), deleteItemRequest.getKey().get("id"));
        assertEquals(new ExpectedAttributeValue(new AttributeValue().withN(String.valueOf(stubItem.getVersion()))),
                deleteItemRequest.getExpected().get("version"));
    }

    @Test
    public void shouldNotDeleteItem_withAmazonServiceException() throws Exception {
        // Given
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        stubItem.setVersion(randomLong());
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        when(mockAmazonDynamoDbClient.deleteItem(any(DeleteItemRequest.class))).thenThrow(AmazonServiceException.class);

        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);

        // When
        PersistenceResourceFailureException actualException = null;
        try {
            dynamoDbTemplate.delete(stubItem);
        } catch (final PersistenceResourceFailureException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        final ArgumentCaptor<DeleteItemRequest> deleteItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(DeleteItemRequest.class);
        verify(mockAmazonDynamoDbClient).deleteItem(deleteItemRequestArgumentCaptor.capture());
        final DeleteItemRequest deleteItemRequest = deleteItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "." + tableName, deleteItemRequest.getTableName());
        assertEquals(1, deleteItemRequest.getKey().size());
        assertEquals(new AttributeValue(stubItem.getId()), deleteItemRequest.getKey().get("id"));
        assertEquals(new ExpectedAttributeValue(new AttributeValue().withN(String.valueOf(stubItem.getVersion()))),
                deleteItemRequest.getExpected().get("version"));
    }

    @Test
    public void shouldDeleteItem_withStubItemAndCompoundPk() throws Exception {
        // Given
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        stubItem.setStringProperty(randomString(10));
        stubItem.setVersion(randomLong());
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName,
                new CompoundPrimaryKeyDefinition("id", "stringProperty"));
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);

        // When
        dynamoDbTemplate.delete(stubItem);

        // Then
        final ArgumentCaptor<DeleteItemRequest> deleteItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(DeleteItemRequest.class);
        verify(mockAmazonDynamoDbClient).deleteItem(deleteItemRequestArgumentCaptor.capture());
        final DeleteItemRequest deleteItemRequest = deleteItemRequestArgumentCaptor.getValue();
        assertEquals(schemaName + "." + tableName, deleteItemRequest.getTableName());
        assertEquals(2, deleteItemRequest.getKey().size());
        assertEquals(new AttributeValue(stubItem.getId()), deleteItemRequest.getKey().get("id"));
        assertEquals(new AttributeValue(stubItem.getStringProperty()), deleteItemRequest.getKey().get("stringProperty"));
        assertEquals(new ExpectedAttributeValue(new AttributeValue().withN(String.valueOf(stubItem.getVersion()))),
                deleteItemRequest.getExpected().get("version"));
    }

    @Test
    public void shouldDeleteItem_withStubItemWithUniqueConstraint() throws Exception {
        // Given
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        stubItem.setStringProperty(randomString(10));
        stubItem.setVersion(randomLong());
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        itemConfiguration.registerUniqueConstraints(Arrays.asList(new UniqueConstraint("stringProperty")));
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);

        // When
        dynamoDbTemplate.delete(stubItem);

        // Then
        final ArgumentCaptor<DeleteItemRequest> deleteItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(DeleteItemRequest.class);
        verify(mockAmazonDynamoDbClient, times(2)).deleteItem(deleteItemRequestArgumentCaptor.capture());
        final List<DeleteItemRequest> deleteItemRequests = deleteItemRequestArgumentCaptor.getAllValues();
        final Iterator<DeleteItemRequest> iterator = deleteItemRequests.iterator();

        final DeleteItemRequest deleteItemRequest1 = iterator.next();
        assertEquals(schemaName + "." + tableName, deleteItemRequest1.getTableName());
        assertEquals(1, deleteItemRequest1.getKey().size());
        assertEquals(new AttributeValue(stubItem.getId()), deleteItemRequest1.getKey().get("id"));
        assertEquals(new ExpectedAttributeValue(new AttributeValue().withN(String.valueOf(stubItem.getVersion()))),
                deleteItemRequest1.getExpected().get("version"));

        final DeleteItemRequest deleteItemRequest2 = iterator.next();
        assertEquals(schemaName + "-indexes." + tableName, deleteItemRequest2.getTableName());
        assertEquals(2, deleteItemRequest2.getKey().size());
        assertEquals(new AttributeValue("stringProperty"), deleteItemRequest2.getKey().get("property"));
        assertEquals(new AttributeValue(stubItem.getStringProperty().toUpperCase()),
                deleteItemRequest2.getKey().get("value"));
    }

    @Test
    public void shouldGenerateKeys_withSequenceKeyGenerator() throws Exception {
        // Given
        final String sequenceName = randomString(10);
        final SequenceConfiguration sequenceConfiguration = new SequenceConfiguration(sequenceName);
        final Collection<SequenceConfiguration> sequenceConfigurations = new ArrayList<>();
        sequenceConfigurations.add(sequenceConfiguration);
        when(mockDatabaseSchemaHolder.sequenceConfigurations()).thenReturn(sequenceConfigurations);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        final SequenceKeyGenerator sequenceKeyGenerator = new SequenceKeyGenerator(sequenceName);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);
        final UpdateItemResult mockUpdateItemResult = mock(UpdateItemResult.class);
        final Map<String, AttributeValue> mockUpdateItemResultAttributes = mock(Map.class);
        final AttributeValue mockCurrentValueAttributeValue = mock(AttributeValue.class);
        when(mockUpdateItemResultAttributes.get("currentValue")).thenReturn(mockCurrentValueAttributeValue);
        when(mockUpdateItemResult.getAttributes()).thenReturn(mockUpdateItemResultAttributes);
        when(mockAmazonDynamoDbClient.updateItem(any(UpdateItemRequest.class))).thenReturn(mockUpdateItemResult);
        when(mockCurrentValueAttributeValue.getN()).thenReturn("1");

        // When
        final GeneratedKeyHolder generatedKeyHolder = dynamoDbTemplate.generateKeys(sequenceKeyGenerator);

        // Then
        final ArgumentCaptor<UpdateItemRequest> updateItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(UpdateItemRequest.class);
        verify(mockAmazonDynamoDbClient).updateItem(updateItemRequestArgumentCaptor.capture());
        assertEquals(schemaName + "-sequences", updateItemRequestArgumentCaptor.getValue().getTableName());
        assertThat(generatedKeyHolder.keys(), hasItems(1l));
        assertEquals(generatedKeyHolder.keys().size(), 1);
    }

    @Test
    public void shouldGenerateKeys_withSequenceKeyGeneratorWithMultipleKeyCount() throws Exception {
        // Given
        final String sequenceName = randomString(10);
        final SequenceKeyGenerator sequenceKeyGenerator = new SequenceKeyGenerator(sequenceName, 5);
        final SequenceConfiguration sequenceConfiguration = new SequenceConfiguration(sequenceName);
        final Collection<SequenceConfiguration> sequenceConfigurations = new ArrayList<>();
        sequenceConfigurations.add(sequenceConfiguration);
        when(mockDatabaseSchemaHolder.sequenceConfigurations()).thenReturn(sequenceConfigurations);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);
        final UpdateItemResult mockUpdateItemResult = mock(UpdateItemResult.class);
        final Map<String, AttributeValue> mockUpdateItemResultAttributes = mock(Map.class);
        final AttributeValue mockCurrentValueAttributeValue = mock(AttributeValue.class);
        when(mockUpdateItemResultAttributes.get("currentValue")).thenReturn(mockCurrentValueAttributeValue);
        when(mockUpdateItemResult.getAttributes()).thenReturn(mockUpdateItemResultAttributes);
        when(mockAmazonDynamoDbClient.updateItem(any(UpdateItemRequest.class))).thenReturn(mockUpdateItemResult);
        when(mockCurrentValueAttributeValue.getN()).thenReturn("5");

        // When
        final GeneratedKeyHolder generatedKeyHolder = dynamoDbTemplate.generateKeys(sequenceKeyGenerator);

        // Then
        final ArgumentCaptor<UpdateItemRequest> updateItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(UpdateItemRequest.class);
        verify(mockAmazonDynamoDbClient).updateItem(updateItemRequestArgumentCaptor.capture());
        assertEquals(schemaName + "-sequences", updateItemRequestArgumentCaptor.getValue().getTableName());
        assertThat(generatedKeyHolder.keys(), hasItems(1l, 2l, 3l, 4l, 5l));
        assertEquals(generatedKeyHolder.keys().size(), 5);
    }

    @Test
    public void shouldNotGenerateKeys_withNoSequenceConfigurations() throws Exception {
        // Given
        final String sequenceName = randomString(10);
        final Collection<SequenceConfiguration> sequenceConfigurations = new ArrayList<>();
        when(mockDatabaseSchemaHolder.sequenceConfigurations()).thenReturn(sequenceConfigurations);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        final SequenceKeyGenerator sequenceKeyGenerator = new SequenceKeyGenerator(sequenceName);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);

        // When
        IllegalStateException actualException = null;
        try {
            dynamoDbTemplate.generateKeys(sequenceKeyGenerator);
        } catch (final IllegalStateException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldBatchWriteItems_withListOfItems_NoUnprocessedItems() throws Exception {
        // Given
        final String schemaName = randomString(10);
        final String tableName = randomString(10);
        final int numberOfItems = randomInt(24) + 1; // we can batch write a max 25 items

        final DatabaseSchemaHolder mockDatabaseSchemaHolder = mock(DatabaseSchemaHolder.class);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);

        when(mockDatabaseSchemaHolder.schemaName()).thenReturn(schemaName);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);

        final List<StubItem> stubItems = new ArrayList<StubItem>();
        for (int i = 0; i < numberOfItems; i++) {
            final boolean shouldSetVersion = Randoms.randomBoolean();
            final StubItem stubItem = new StubItem();
            stubItem.setId(randomId());
            final String stringPropertyValue = randomString(10);
            stubItem.setStringProperty(stringPropertyValue);
            if (shouldSetVersion) {
                stubItem.setVersion(Randoms.randomLong());
            }
            stubItems.add(stubItem);
        }

        final BatchWriteItemResult result = new BatchWriteItemResult();
        when(mockAmazonDynamoDbClient.batchWriteItem(any(BatchWriteItemRequest.class))).thenReturn(result);

        // When
        dynamoDbTemplate.batchWrite(stubItems, StubItem.class);

        // Then
        final ArgumentCaptor<BatchWriteItemRequest> batchWriteItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(BatchWriteItemRequest.class);
        verify(mockAmazonDynamoDbClient, times(1)).batchWriteItem(batchWriteItemRequestArgumentCaptor.capture());

    }

    @Test
    public void shouldBatchWriteItems_withListOfItems_NullWriteResultObject() throws Exception {
        // Given
        final String schemaName = randomString(10);
        final String tableName = randomString(10);
        final int numberOfItems = randomInt(24) + 1; // max 25 items can be used in a batch write

        final DatabaseSchemaHolder mockDatabaseSchemaHolder = mock(DatabaseSchemaHolder.class);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);

        when(mockDatabaseSchemaHolder.schemaName()).thenReturn(schemaName);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);

        final List<StubItem> stubItems = new ArrayList<StubItem>();
        for (int i = 0; i < numberOfItems; i++) {
            final boolean shouldSetVersion = Randoms.randomBoolean();
            final StubItem stubItem = new StubItem();
            stubItem.setId(randomId());
            final String stringPropertyValue = randomString(10);
            stubItem.setStringProperty(stringPropertyValue);
            if (shouldSetVersion) {
                stubItem.setVersion(Randoms.randomLong());
            }
            stubItems.add(stubItem);
        }

        final BatchWriteItemResult result = null;
        when(mockAmazonDynamoDbClient.batchWriteItem(any(BatchWriteItemRequest.class))).thenReturn(result);

        // When
        final List<StubItem> returnedItems = dynamoDbTemplate.batchWrite(stubItems, StubItem.class);

        // Then
        final ArgumentCaptor<BatchWriteItemRequest> batchWriteItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(BatchWriteItemRequest.class);
        verify(mockAmazonDynamoDbClient, times(1)).batchWriteItem(batchWriteItemRequestArgumentCaptor.capture());

        assertEquals(0, returnedItems.size());

    }

    @Test
    public void shouldBatchWriteItems_withListOfItems_WithUnprocessedItems() throws Exception {
        // Given
        final String schemaName = randomString(10);
        final String tableName = randomString(10);
        final int numberOfItems = randomInt(24) + 1; // max 25 items can be used in a batch write

        final DatabaseSchemaHolder mockDatabaseSchemaHolder = mock(DatabaseSchemaHolder.class);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);

        when(mockDatabaseSchemaHolder.schemaName()).thenReturn(schemaName);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);

        final List<StubItem> stubItems = new ArrayList<StubItem>();
        for (int i = 0; i < numberOfItems; i++) {
            final boolean shouldSetVersion = Randoms.randomBoolean();
            final StubItem stubItem = new StubItem();
            stubItem.setId(randomId());
            final String stringPropertyValue = randomString(10);
            stubItem.setStringProperty(stringPropertyValue);
            if (shouldSetVersion) {
                stubItem.setVersion(Randoms.randomLong());
            }
            stubItems.add(stubItem);
        }

        final BatchWriteItemResult result = new BatchWriteItemResult();

        final PutRequest putRequest = new PutRequest();
        final WriteRequest unprocessedWriteRequest = new WriteRequest().withPutRequest(putRequest);
        final Map<String, List<WriteRequest>> unprocessedItems = new HashMap<String, List<WriteRequest>>();
        final List<WriteRequest> writeRequests = new ArrayList<WriteRequest>();
        writeRequests.add(unprocessedWriteRequest);
        final String randomTableName = Randoms.randomString();
        unprocessedItems.put(randomTableName, writeRequests);
        result.setUnprocessedItems(unprocessedItems);

        when(mockAmazonDynamoDbClient.batchWriteItem(any(BatchWriteItemRequest.class))).thenReturn(result);

        // When
        dynamoDbTemplate.batchWrite(stubItems, StubItem.class);

        // Then
        final ArgumentCaptor<BatchWriteItemRequest> batchWriteItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(BatchWriteItemRequest.class);
        verify(mockAmazonDynamoDbClient, times(1)).batchWriteItem(batchWriteItemRequestArgumentCaptor.capture());

    }

    @Test
    public void shouldNotBatchWriteItems_withListOfItems_UniqueConstraintRestriction() throws Exception {
        // Given
        final String schemaName = randomString(10);
        final String tableName = randomString(10);
        final int numberOfItems = randomInt(1000) + 1;

        final DatabaseSchemaHolder mockDatabaseSchemaHolder = mock(DatabaseSchemaHolder.class);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        itemConfiguration.registerUniqueConstraints(Arrays.asList(new UniqueConstraint("stringProperty")));
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);

        when(mockDatabaseSchemaHolder.schemaName()).thenReturn(schemaName);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);

        final List<StubItem> stubItems = new ArrayList<StubItem>();
        for (int i = 0; i < numberOfItems; i++) {
            final StubItem stubItem = new StubItem();
            stubItem.setId(randomId());
            final String stringPropertyValue = randomString(10);
            stubItem.setStringProperty(stringPropertyValue);
            stubItems.add(stubItem);
        }

        IllegalArgumentException expected = null;

        // When
        try {
            dynamoDbTemplate.batchWrite(stubItems, StubItem.class);
        } catch (final IllegalArgumentException e) {
            expected = e;
        }

        // Then
        final ArgumentCaptor<BatchWriteItemRequest> batchWriteItemRequestArgumentCaptor = ArgumentCaptor
                .forClass(BatchWriteItemRequest.class);

        assertNotNull(expected);
        verify(mockAmazonDynamoDbClient, times(0)).batchWriteItem(batchWriteItemRequestArgumentCaptor.capture());

    }

    @Test
    public void shouldNotBatchWriteItems_withListOfItemsAndFailedIndexCreation() throws Exception {
        // Given
        final String schemaName = randomString(10);
        final String tableName = randomString(10);
        final int numberOfItems = randomInt(1000) + 1;

        final DatabaseSchemaHolder mockDatabaseSchemaHolder = mock(DatabaseSchemaHolder.class);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, tableName);
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration);
        when(mockDatabaseSchemaHolder.schemaName()).thenReturn(schemaName);
        when(mockDatabaseSchemaHolder.itemConfigurations()).thenReturn(itemConfigurations);
        final DynamoDbTemplate dynamoDbTemplate = new DynamoDbTemplate(mockDatabaseSchemaHolder);
        final AmazonDynamoDB mockAmazonDynamoDbClient = mock(AmazonDynamoDB.class);
        dynamoDbTemplate.initialize(mockAmazonDynamoDbClient);

        when(mockAmazonDynamoDbClient.batchWriteItem(any(BatchWriteItemRequest.class))).thenThrow(
                AmazonServiceException.class);
        final List<StubItem> stubItems = new ArrayList<StubItem>();
        for (int i = 0; i < numberOfItems; i++) {
            final StubItem stubItem = new StubItem();
            stubItem.setId(randomId());
            final String stringPropertyValue = randomString(10);
            stubItem.setStringProperty(stringPropertyValue);
            stubItems.add(stubItem);
        }

        PersistenceResourceFailureException expected = null;

        // When
        try {
            dynamoDbTemplate.batchWrite(stubItems, StubItem.class);
        } catch (final PersistenceResourceFailureException e) {
            expected = e;
        }

        assertNotNull(expected);

    }

}
