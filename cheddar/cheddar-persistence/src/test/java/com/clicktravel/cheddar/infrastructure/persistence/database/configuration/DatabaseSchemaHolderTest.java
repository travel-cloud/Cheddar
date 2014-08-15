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

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.persistence.database.StubItem;

public class DatabaseSchemaHolderTest {

    @Test
    public void shouldCreateDatabaseSchemaHolder_withSchemaNameAndItemConfigurations() {
        // Given
        final String schemaName = randomString(10);
        final Collection<ItemConfiguration> itemConfigurations = randomItemConfigurations();

        // When
        final DatabaseSchemaHolder databaseSchemaHolder = new DatabaseSchemaHolder(schemaName, itemConfigurations);

        // Then
        assertNotNull(databaseSchemaHolder);
        assertEquals(schemaName, databaseSchemaHolder.schemaName());
        assertTrue(databaseSchemaHolder.itemConfigurations().containsAll(itemConfigurations));
        assertEquals(itemConfigurations.size(), databaseSchemaHolder.itemConfigurations().size());
    }

    @Test
    public void shouldCreateDatabaseSchemaHolder_withSchemaNameAndItemConfigurationsAndSequenceConfiguration() {
        // Given
        final String schemaName = randomString(10);
        final Collection<ItemConfiguration> itemConfigurations = randomItemConfigurations();
        final Collection<SequenceConfiguration> sequenceConfigurations = randomSequenceConfigurations();

        // When
        final DatabaseSchemaHolder databaseSchemaHolder = new DatabaseSchemaHolder(schemaName, itemConfigurations,
                sequenceConfigurations);

        // Then
        assertNotNull(databaseSchemaHolder);
        assertEquals(schemaName, databaseSchemaHolder.schemaName());
        assertTrue(databaseSchemaHolder.itemConfigurations().containsAll(itemConfigurations));
        assertEquals(itemConfigurations.size(), databaseSchemaHolder.itemConfigurations().size());
        assertTrue(databaseSchemaHolder.sequenceConfigurations().containsAll(sequenceConfigurations));
        assertEquals(sequenceConfigurations.size(), databaseSchemaHolder.sequenceConfigurations().size());
    }

    @Test
    public void shouldCreateDatabaseSchemaHolder_withEmptySchemaNameAndItemConfigurations() {
        // Given
        final String schemaName = "";
        final Collection<ItemConfiguration> itemConfigurations = randomItemConfigurations();

        // When
        IllegalArgumentException actualException = null;
        try {
            new DatabaseSchemaHolder(schemaName, itemConfigurations);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCreateDatabaseSchemaHolder_withNullSchemaNameAndItemConfigurations() {
        // Given
        final String schemaName = null;
        final Collection<ItemConfiguration> itemConfigurations = randomItemConfigurations();

        // When
        IllegalArgumentException actualException = null;
        try {
            new DatabaseSchemaHolder(schemaName, itemConfigurations);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCreateDatabaseSchemaHolder_withSchemaNameAndNullItemConfigurations() {
        // Given
        final String schemaName = randomString(10);
        final Collection<ItemConfiguration> itemConfigurations = null;

        // When
        IllegalArgumentException actualException = null;
        try {
            new DatabaseSchemaHolder(schemaName, itemConfigurations);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    private Collection<ItemConfiguration> randomItemConfigurations() {
        final ItemConfiguration itemConfiguration1 = new ItemConfiguration(StubItem.class, randomString(10));
        final ItemConfiguration itemConfiguration2 = new ItemConfiguration(StubItem.class, randomString(10));
        final ItemConfiguration itemConfiguration3 = new ItemConfiguration(StubItem.class, randomString(10));
        final Collection<ItemConfiguration> itemConfigurations = Arrays.asList(itemConfiguration1, itemConfiguration2,
                itemConfiguration3);
        return itemConfigurations;
    }

    private Collection<SequenceConfiguration> randomSequenceConfigurations() {
        final SequenceConfiguration sequenceConfiguration1 = new SequenceConfiguration(randomString(10));
        final SequenceConfiguration sequenceConfiguration2 = new SequenceConfiguration(randomString(10));
        final SequenceConfiguration sequenceConfiguration3 = new SequenceConfiguration(randomString(10));
        final Collection<SequenceConfiguration> sequenceConfigurations = Arrays.asList(sequenceConfiguration1,
                sequenceConfiguration2, sequenceConfiguration3);
        return sequenceConfigurations;
    }

}
