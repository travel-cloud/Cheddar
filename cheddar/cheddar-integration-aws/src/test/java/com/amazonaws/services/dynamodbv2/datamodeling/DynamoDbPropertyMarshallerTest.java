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
package com.amazonaws.services.dynamodbv2.datamodeling;

import static com.clicktravel.common.random.Randoms.randomBoolean;
import static com.clicktravel.common.random.Randoms.randomInt;
import static com.clicktravel.common.random.Randoms.randomIntInRange;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.ItemConfiguration;
import com.clicktravel.infrastructure.persistence.aws.dynamodb.StubItem;
import com.clicktravel.infrastructure.persistence.aws.dynamodb.StubWithGlobalSecondaryIndexItem;

public class DynamoDbPropertyMarshallerTest {

    @Test
    public void shouldGetAttributeValue_withStringPropertyValue() {
        // Given
        final String propertyValue = randomString();
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, randomString());
        final PropertyDescriptor propertyDescriptor = itemConfiguration.getPropertyDescriptor("stringProperty");

        // When
        final AttributeValue attributeValue = DynamoDbPropertyMarshaller.getAttributeValue(propertyValue,
                propertyDescriptor);

        // Then
        assertEquals(propertyValue, attributeValue.getS());
    }

    @Test
    public void shouldGetAttributeValue_withBooleanPropertyValue() {
        // Given
        final Boolean propertyValue = randomBoolean();
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, randomString());
        final PropertyDescriptor propertyDescriptor = itemConfiguration.getPropertyDescriptor("booleanProperty");

        // When
        final AttributeValue attributeValue = DynamoDbPropertyMarshaller.getAttributeValue(propertyValue,
                propertyDescriptor);

        // Then
        final String expectedValue = propertyValue ? "1" : "0";
        assertEquals(expectedValue, attributeValue.getN());
    }

    @Test
    public void shouldGetAttributeValue_withStringSetPropertyValue() {
        // Given
        final Set<String> propertyValue = new HashSet<>();
        final int stringSetSize = randomIntInRange(1, 20);
        for (int i = 0; i < stringSetSize; i++) {
            propertyValue.add(randomString());
        }
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, randomString());
        final PropertyDescriptor propertyDescriptor = itemConfiguration.getPropertyDescriptor("stringSetProperty");

        // When
        final AttributeValue attributeValue = DynamoDbPropertyMarshaller.getAttributeValue(propertyValue,
                propertyDescriptor);

        // Then
        assertEquals(propertyValue.size(), attributeValue.getSS().size());
        for (final String expectedValue : propertyValue) {
            assertTrue(attributeValue.getSS().contains(expectedValue));
        }
    }

    @Test
    public void shouldGetAttributeValue_withIntegerPropertyValue() {
        // Given
        final Integer propertyValue = randomInt(20);
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubWithGlobalSecondaryIndexItem.class,
                randomString());
        final PropertyDescriptor propertyDescriptor = itemConfiguration.getPropertyDescriptor("gsiSupportingValue");

        // When
        final AttributeValue attributeValue = DynamoDbPropertyMarshaller.getAttributeValue(propertyValue,
                propertyDescriptor);

        // Then
        assertEquals(String.valueOf(propertyValue), attributeValue.getN());
    }

    @Test
    public void shouldGetAttributeValue_withNumberAsStringPropertyValue() {
        // Given
        final String propertyValue = String.valueOf(randomInt(20));
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, randomString());
        final PropertyDescriptor propertyDescriptor = itemConfiguration.getPropertyDescriptor("stringProperty");

        // When
        final AttributeValue attributeValue = DynamoDbPropertyMarshaller.getAttributeValue(propertyValue,
                propertyDescriptor);

        // Then
        assertEquals(propertyValue, attributeValue.getS());
    }

    @Test
    public void shouldGetNullAttributeValue_withNullPropertyValue() {
        // Given
        final String propertyValue = null;
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, randomString());
        final PropertyDescriptor propertyDescriptor = itemConfiguration.getPropertyDescriptor("stringProperty");

        // When
        final AttributeValue attributeValue = DynamoDbPropertyMarshaller.getAttributeValue(propertyValue,
                propertyDescriptor);

        // Then
        assertNull(attributeValue);
    }

    @Test
    public void shouldGetNullAttributeValue_withEmptySetPropertyValue() {
        // Given
        final Set<String> propertyValue = new HashSet<>();
        final ItemConfiguration itemConfiguration = new ItemConfiguration(StubItem.class, randomString());
        final PropertyDescriptor propertyDescriptor = itemConfiguration.getPropertyDescriptor("stringSetProperty");

        // When
        final AttributeValue attributeValue = DynamoDbPropertyMarshaller.getAttributeValue(propertyValue,
                propertyDescriptor);

        // Then
        assertNull(attributeValue);
    }
}