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
package com.clicktravel.cheddar.infrastructure.persistence.inmemory.database;

import static com.clicktravel.common.random.Randoms.randomBoolean;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.*;

public class InMemoryBatchDatabaseTemplateTest {

    private DatabaseSchemaHolder databaseSchemaHolder;

    private final static InMemoryDbDataGenerator dataGenerator = new InMemoryDbDataGenerator();

    @Before
    public void init() throws Exception {
        final Collection<ItemConfiguration> itemConfigurations = new ArrayList<>();

        final ItemConfiguration stubItemConfiguration = new ItemConfiguration(StubItem.class,
                InMemoryDbDataGenerator.STUB_ITEM_TABLE_NAME);
        final ParentItemConfiguration stubParentItemConfiguration = new ParentItemConfiguration(StubParentItem.class,
                InMemoryDbDataGenerator.STUB_ITEM_TABLE_NAME);
        final ItemConfiguration stubItemWithRangeConfiguration = new ItemConfiguration(StubWithRangeItem.class,
                InMemoryDbDataGenerator.STUB_ITEM_WITH_RANGE_TABLE_NAME, new CompoundPrimaryKeyDefinition("id",
                        "supportingId"));
        itemConfigurations.add(stubItemConfiguration);
        itemConfigurations.add(stubParentItemConfiguration);
        itemConfigurations.add(new VariantItemConfiguration(stubParentItemConfiguration, StubVariantItem.class, "a"));
        itemConfigurations
                .add(new VariantItemConfiguration(stubParentItemConfiguration, StubVariantTwoItem.class, "b"));
        itemConfigurations.add(stubItemWithRangeConfiguration);
        databaseSchemaHolder = new DatabaseSchemaHolder(InMemoryDbDataGenerator.UNIT_TEST_SCHEMA_NAME,
                itemConfigurations);
    }

    @Test
    public void shouldCreate_viaBatchWrite_withSingleItem() {
        // Given
        final InMemoryBatchDatabaseTemplate databaseTemplate = new InMemoryBatchDatabaseTemplate(databaseSchemaHolder);

        final StubItem stubItem = dataGenerator.randomStubItem();
        stubItem.setVersion(null);

        final List<StubItem> stubItems = new ArrayList<StubItem>();
        stubItems.add(stubItem);

        // When

        final List<StubItem> successfulStubItems = databaseTemplate.batchWrite(stubItems, StubItem.class);

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
        final InMemoryBatchDatabaseTemplate databaseTemplate = new InMemoryBatchDatabaseTemplate(databaseSchemaHolder);
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

        final List<StubItem> stubItems = new ArrayList<StubItem>();
        stubItems.add(createdItem);

        // When
        final List<StubItem> successfulStubItems = databaseTemplate.batchWrite(stubItems, StubItem.class);

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
        final InMemoryBatchDatabaseTemplate databaseTemplate = new InMemoryBatchDatabaseTemplate(databaseSchemaHolder);
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

        final List<StubWithRangeItem> stubWithRangeItems = new ArrayList<StubWithRangeItem>();
        stubWithRangeItems.add(createdItem);

        // When
        final List<StubWithRangeItem> successfulStubWithRangeItems = databaseTemplate.batchWrite(stubWithRangeItems,
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
