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
package com.clicktravel.cheddar.infrastructure.persistence.database.configuration;

import static com.clicktravel.common.random.Randoms.randomEnum;
import static com.clicktravel.common.random.Randoms.randomId;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.persistence.database.Item;
import com.clicktravel.cheddar.infrastructure.persistence.database.ItemId;
import com.clicktravel.cheddar.infrastructure.persistence.database.StubItem;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.AttributeQuery;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.CompoundAttributeQuery;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Condition;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Operators;

public class ItemConfigurationTest {

    @Test
    public void shouldCreateItemConfiguration_withItemClassAndTableName() throws Exception {
        // Given
        final Class<? extends Item> itemClass = StubItem.class;
        final String tableName = randomString(10);
        final Collection<PropertyDescriptor> propertyDescriptors = Arrays
                .asList(Introspector.getBeanInfo(itemClass).getPropertyDescriptors());

        // When
        final ItemConfiguration itemConfiguration = new ItemConfiguration(itemClass, tableName);

        // Then
        assertNotNull(itemConfiguration);
        assertEquals("id", itemConfiguration.primaryKeyDefinition().propertyName());
        assertEquals(String.class, itemConfiguration.primaryKeyDefinition().propertyType());
        assertEquals(itemClass, itemConfiguration.itemClass());
        assertEquals(tableName, itemConfiguration.tableName());
        assertThat(itemConfiguration.propertyDescriptors(), hasSize(propertyDescriptors.size()));
        assertEquals(0, itemConfiguration.indexDefinitions().size());
    }

    @Test
    public void shouldRegisterIndexes_withSingleIndexDefinition() throws Exception {
        // Given
        final Class<? extends Item> itemClass = StubItem.class;
        final String tableName = randomString(10);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(itemClass, tableName);
        final Collection<IndexDefinition> indexDefinitions = new ArrayList<>();
        final String propertyName = "stringProperty";
        final IndexDefinition indexDefinition = new IndexDefinition(propertyName);
        indexDefinitions.add(indexDefinition);

        // When
        itemConfiguration.registerIndexes(indexDefinitions);

        // Then
        assertTrue(itemConfiguration.hasIndexOn(propertyName));
        assertEquals(1, itemConfiguration.indexDefinitions().size());
    }

    @Test
    public void shouldRegisterIndexes_withCompoundIndexDefinition() throws Exception {
        // Given
        final Class<? extends Item> itemClass = StubItem.class;
        final String tableName = randomString(10);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(itemClass, tableName);
        final Collection<IndexDefinition> indexDefinitions = new ArrayList<>();
        final String stringPropertyName = "stringProperty";
        final String integerPropertyName = "integerProperty";
        final CompoundIndexDefinition indexDefinition = new CompoundIndexDefinition(stringPropertyName,
                integerPropertyName);
        indexDefinitions.add(indexDefinition);

        // When
        itemConfiguration.registerIndexes(indexDefinitions);

        // Then
        final String expectedIndexName = stringPropertyName + "_" + integerPropertyName;
        assertTrue(itemConfiguration.hasIndexOn(expectedIndexName));
        assertEquals(1, itemConfiguration.indexDefinitions().size());
    }

    @Test
    public void shouldReturnItemId_fromItemConfiguration() {
        // Given
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, randomString(10));
        final StubItem stubItem = new StubItem();
        final String stubItemId = randomId();
        stubItem.setId(stubItemId);

        // When
        final ItemId itemId = itemConfiguration.getItemId(stubItem);

        // Then
        assertEquals(stubItemId, itemId.value());

    }

    @Test
    public void shouldReturnHasIndexForQuery_withAttributeQueryOnPrimaryKey() throws Exception {
        // Given
        final Class<? extends Item> itemClass = StubItem.class;
        final String tableName = randomString(10);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(itemClass, tableName);

        final Condition condition = new Condition(randomEnum(Operators.class), randomString());
        final AttributeQuery attributeQuery = new AttributeQuery("id", condition);

        // When
        final boolean hasIndexForQuery = itemConfiguration.hasIndexForQuery(attributeQuery);

        // Then
        assertTrue(hasIndexForQuery);
    }

    @Test
    public void shouldReturnHasIndexForQuery_withAttributeQuery() throws Exception {
        // Given
        final Class<? extends Item> itemClass = StubItem.class;
        final String tableName = randomString(10);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(itemClass, tableName);
        final Collection<IndexDefinition> indexDefinitions = new ArrayList<>();
        final String propertyName = "stringProperty";
        final IndexDefinition indexDefinition = new IndexDefinition(propertyName);
        indexDefinitions.add(indexDefinition);
        itemConfiguration.registerIndexes(indexDefinitions);

        final Condition condition = new Condition(randomEnum(Operators.class), randomString());
        final AttributeQuery attributeQuery = new AttributeQuery(propertyName, condition);

        // When
        final boolean hasIndexForQuery = itemConfiguration.hasIndexForQuery(attributeQuery);

        // Then
        assertTrue(hasIndexForQuery);
    }

    @Test
    public void shouldNotReturnHasIndexForQuery_withAttributeQuery() throws Exception {
        // Given
        final Class<? extends Item> itemClass = StubItem.class;
        final String tableName = randomString(10);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(itemClass, tableName);
        final String propertyName = "stringProperty";

        final Condition condition = new Condition(randomEnum(Operators.class), randomString());
        final AttributeQuery attributeQuery = new AttributeQuery(propertyName, condition);

        // When
        final boolean hasIndexForQuery = itemConfiguration.hasIndexForQuery(attributeQuery);

        // Then
        assertFalse(hasIndexForQuery);
    }

    @Test
    public void shouldReturnHasIndexForQuery_withCompositeAttributeQuery() throws Exception {
        // Given
        final Class<? extends Item> itemClass = StubItem.class;
        final String tableName = randomString(10);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(itemClass, tableName);
        final Collection<IndexDefinition> indexDefinitions = new ArrayList<>();
        final String stringPropertyName = "stringProperty";
        final String integerPropertyName = "integerProperty";
        final CompoundIndexDefinition compoundIndexDefinition = new CompoundIndexDefinition(stringPropertyName,
                integerPropertyName);
        indexDefinitions.add(compoundIndexDefinition);
        itemConfiguration.registerIndexes(indexDefinitions);

        final Condition condition = new Condition(randomEnum(Operators.class), randomString());
        final CompoundAttributeQuery compundAttributeQuery = new CompoundAttributeQuery(stringPropertyName, condition,
                integerPropertyName, condition);

        // When
        final boolean hasIndexForQuery = itemConfiguration.hasIndexForQuery(compundAttributeQuery);

        // Then
        assertTrue(hasIndexForQuery);
    }

    @Test
    public void shouldNotReturnHasIndexForQuery_withCompositeAttributeQuery() throws Exception {
        // Given
        final Class<? extends Item> itemClass = StubItem.class;
        final String tableName = randomString(10);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(itemClass, tableName);
        final String stringPropertyName = "stringProperty";
        final String integerPropertyName = "integerProperty";

        final Condition condition = new Condition(randomEnum(Operators.class), randomString());
        final CompoundAttributeQuery compundAttributeQuery = new CompoundAttributeQuery(stringPropertyName, condition,
                integerPropertyName, condition);

        // When
        final boolean hasIndexForQuery = itemConfiguration.hasIndexForQuery(compundAttributeQuery);

        // Then
        assertFalse(hasIndexForQuery);
    }

    @Test
    public void shouldNotReturnHasIndexForQuery_withCompositeAttributeQueryAndNonCompositeIndex() throws Exception {
        // Given
        final Class<? extends Item> itemClass = StubItem.class;
        final String tableName = randomString(10);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(itemClass, tableName);
        final Collection<IndexDefinition> indexDefinitions = new ArrayList<>();
        final String stringPropertyName = "stringProperty";
        final String integerPropertyName = "integerProperty";
        final IndexDefinition indexDefinition = new IndexDefinition(stringPropertyName);
        indexDefinitions.add(indexDefinition);
        itemConfiguration.registerIndexes(indexDefinitions);

        final Condition condition = new Condition(randomEnum(Operators.class), randomString());
        final CompoundAttributeQuery compundAttributeQuery = new CompoundAttributeQuery(stringPropertyName, condition,
                integerPropertyName, condition);

        // When
        final boolean hasIndexForQuery = itemConfiguration.hasIndexForQuery(compundAttributeQuery);

        // Then
        assertFalse(hasIndexForQuery);
    }

    @Test
    public void shouldNotReturnHasIndexForQuery_withAttributeQueryAndCompositeIndex() throws Exception {
        // Given
        final Class<? extends Item> itemClass = StubItem.class;
        final String tableName = randomString(10);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(itemClass, tableName);
        final Collection<IndexDefinition> indexDefinitions = new ArrayList<>();
        final String stringPropertyName = "stringProperty";
        final String integerPropertyName = "integerProperty";
        final CompoundIndexDefinition compoundIndexDefinition = new CompoundIndexDefinition(stringPropertyName,
                integerPropertyName);
        indexDefinitions.add(compoundIndexDefinition);
        itemConfiguration.registerIndexes(indexDefinitions);

        final Condition condition = new Condition(randomEnum(Operators.class), randomString());
        final AttributeQuery attributeQuery = new AttributeQuery(stringPropertyName, condition);

        // When
        final boolean hasIndexForQuery = itemConfiguration.hasIndexForQuery(attributeQuery);

        // Then
        assertFalse(hasIndexForQuery);
    }
}
