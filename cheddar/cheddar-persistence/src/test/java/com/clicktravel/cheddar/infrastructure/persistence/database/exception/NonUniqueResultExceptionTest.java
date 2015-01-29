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
package com.clicktravel.cheddar.infrastructure.persistence.database.exception;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.persistence.database.Item;
import com.clicktravel.cheddar.infrastructure.persistence.database.StubItem;

public class NonUniqueResultExceptionTest {

    @Test
    public void shouldCreateException_withEmptyIdsAndClass() {
        // Given
        final Class<Item> itemClass = Item.class;
        final Collection<Item> itemArray = new ArrayList<Item>();

        // When
        final NonUniqueResultException nonUniqueResultException = new NonUniqueResultException(itemClass, itemArray);

        // Then
        assertNotNull(nonUniqueResultException);
        assertFalse(nonUniqueResultException.hasResults());
    }

    @Test
    public void shouldNotCreateException_withNullIds() {
        // Given
        final Class<Item> itemClass = Item.class;

        // When
        IllegalArgumentException actualException = null;
        try {
            new NonUniqueResultException(itemClass, null);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCreateException_withIdsAndClass() {
        // Given
        final Class<Item> itemClass = Item.class;
        final Item stubItem = new StubItem();
        final Item stubItem2 = new StubItem();
        final Collection<Item> itemArray = Arrays.asList(stubItem, stubItem2);

        // When
        final NonUniqueResultException nonUniqueResultException = new NonUniqueResultException(itemClass, itemArray);

        // Then
        assertNotNull(nonUniqueResultException);
        assertTrue(nonUniqueResultException.hasResults());
    }

    @Test
    public void shouldReturnMessage_withNoItemIds() {
        // Given
        final Class<Item> itemClass = Item.class;
        final Collection<Item> itemArray = new ArrayList<Item>();

        final NonUniqueResultException nonUniqueResultException = new NonUniqueResultException(itemClass, itemArray);

        // When
        final String validMessage = nonUniqueResultException.getMessage();

        // Then
        assertNotNull(validMessage);
        assertTrue(validMessage.contains(itemClass.getName()));
    }

    @Test
    public void shouldReturnMessage_withIds() {
        // Given
        final Class<Item> itemClass = Item.class;
        final Item stubItem = new StubItem();
        final Item stubItem2 = new StubItem();
        final Collection<Item> itemArray = Arrays.asList(stubItem, stubItem2);

        final NonUniqueResultException nonUniqueResultException = new NonUniqueResultException(itemClass, itemArray);

        // When
        final String validMessage = nonUniqueResultException.getMessage();

        // Then
        assertNotNull(validMessage);
        assertTrue(validMessage.contains(stubItem.toString()));
        assertTrue(validMessage.contains(stubItem2.toString()));
        assertTrue(validMessage.contains(itemClass.getName()));
    }

}
