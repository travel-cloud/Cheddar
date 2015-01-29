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
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.persistence.database.Item;
import com.clicktravel.cheddar.infrastructure.persistence.database.StubItem;

public class VariantItemConfigurationTest {

    @Test
    public void shouldCreateVariantItemConfiguration_withParentItemConfigurationAndItemClassAndDiscriminatorValue()
            throws Exception {
        // Given
        final ParentItemConfiguration parentItemConfiguration = mock(ParentItemConfiguration.class);
        final Class<? extends Item> itemClass = StubItem.class;
        final String discriminatorValue = randomString(10);

        final IndexDefinition mockIndexDefinition = mock(IndexDefinition.class);
        final Collection<IndexDefinition> indexDefinitions = Arrays.asList(mockIndexDefinition);
        final UniqueConstraint mockUniqueConstraint = mock(UniqueConstraint.class);
        final Collection<UniqueConstraint> uniqueConstraints = Arrays.asList(mockUniqueConstraint);
        when(parentItemConfiguration.indexDefinitions()).thenReturn(indexDefinitions);
        when(parentItemConfiguration.uniqueConstraints()).thenReturn(uniqueConstraints);

        // When
        final VariantItemConfiguration itemConfiguration = new VariantItemConfiguration(parentItemConfiguration,
                itemClass, discriminatorValue);

        // Then
        verify(parentItemConfiguration).registerVariantItemClass(itemClass, discriminatorValue);
        assertNotNull(itemConfiguration);
        assertEquals(parentItemConfiguration, itemConfiguration.parentItemConfiguration());
        assertEquals(itemClass, itemConfiguration.itemClass());
        assertEquals(discriminatorValue, itemConfiguration.discriminatorValue());
        assertThat(itemConfiguration.indexDefinitions(), hasItem(mockIndexDefinition));
        assertThat(itemConfiguration.uniqueConstraints(), hasItem(mockUniqueConstraint));
    }

}
