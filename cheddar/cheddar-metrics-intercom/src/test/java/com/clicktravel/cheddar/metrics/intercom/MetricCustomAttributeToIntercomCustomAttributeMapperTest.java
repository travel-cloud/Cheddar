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
package com.clicktravel.cheddar.metrics.intercom;

import static com.clicktravel.common.random.Randoms.randomBoolean;
import static com.clicktravel.common.random.Randoms.randomIntInRange;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.intercom.api.CustomAttribute;
import io.intercom.api.CustomAttribute.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CustomAttribute.class })
@SuppressWarnings("rawtypes")
public class MetricCustomAttributeToIntercomCustomAttributeMapperTest {

    private MetricCustomAttributeToIntercomCustomAttributeMapper metricToIntercomMapper;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(CustomAttribute.class);
        metricToIntercomMapper = new MetricCustomAttributeToIntercomCustomAttributeMapper();
    }

    @Test
    public void shouldMapToBooleanCustomAttribute_withBooleanAttribute() {
        // Given
        final Map<String, Object> metricCustomAttribute = new HashMap<>();
        final String name = randomString();
        final Boolean value = new Boolean(randomBoolean());
        metricCustomAttribute.put(name, value);
        final BooleanAttribute mockBooleanAttribute = mock(BooleanAttribute.class);
        when(CustomAttribute.newBooleanAttribute(name, value)).thenReturn(mockBooleanAttribute);

        // When
        final Map<String, CustomAttribute> result = metricToIntercomMapper.apply(metricCustomAttribute);

        // Then
        assertNotNull(result);
        assertThat(result.get(name), is(mockBooleanAttribute));

    }

    @Test
    public void shouldMapToStringCustomAttribute_withStringAttribute() {
        // Given
        final Map<String, Object> metricCustomAttribute = new HashMap<>();
        final String name = randomString();
        final String value = new String(randomString());
        metricCustomAttribute.put(name, value);
        final StringAttribute mockStringAttribute = mock(StringAttribute.class);
        when(CustomAttribute.newStringAttribute(name, value)).thenReturn(mockStringAttribute);

        // When
        final Map<String, CustomAttribute> result = metricToIntercomMapper.apply(metricCustomAttribute);

        // Then
        assertNotNull(result);
        assertThat(result.get(name), is(mockStringAttribute));
    }

    @Test
    public void shouldMapToIntegerCustomAttribute_withIntegerAttribute() {
        // Given
        final Map<String, Object> metricCustomAttribute = new HashMap<>();
        final String name = randomString();
        final Integer value = new Integer(randomIntInRange(-10, 10));
        metricCustomAttribute.put(name, value);
        final IntegerAttribute mockIntegerAttribute = mock(IntegerAttribute.class);
        when(CustomAttribute.newIntegerAttribute(name, value)).thenReturn(mockIntegerAttribute);

        // When
        final Map<String, CustomAttribute> result = metricToIntercomMapper.apply(metricCustomAttribute);

        // Then
        assertNotNull(result);
        assertThat(result.get(name), is(mockIntegerAttribute));
    }

    @Test
    public void shouldMapToDoubleCustomAttribute_withDoubleAttribute() {
        // Given
        final Map<String, Object> metricCustomAttribute = new HashMap<>();
        final String name = randomString();
        final Double value = new Double(randomIntInRange(-10, 10));
        metricCustomAttribute.put(name, value);
        final DoubleAttribute mockDoubleAttribute = mock(DoubleAttribute.class);
        when(CustomAttribute.newDoubleAttribute(name, value)).thenReturn(mockDoubleAttribute);

        // When
        final Map<String, CustomAttribute> result = metricToIntercomMapper.apply(metricCustomAttribute);

        // Then
        assertNotNull(result);
        assertThat(result.get(name), is(mockDoubleAttribute));
    }

    @Test
    public void shouldMapToLongCustomAttribute_withLongAttribute() {
        // Given
        final Map<String, Object> metricCustomAttribute = new HashMap<>();
        final String name = randomString();
        final Long value = new Long(randomIntInRange(-10, 10));
        metricCustomAttribute.put(name, value);
        final LongAttribute mockLongAttribute = mock(LongAttribute.class);
        when(CustomAttribute.newLongAttribute(name, value)).thenReturn(mockLongAttribute);

        // When
        final Map<String, CustomAttribute> result = metricToIntercomMapper.apply(metricCustomAttribute);

        // Then
        assertNotNull(result);
        assertThat(result.get(name), is(mockLongAttribute));
    }

    @Test
    public void shouldMapToFloatCustomAttribute_withFloatAttribute() {
        final Map<String, Object> metricCustomAttribute = new HashMap<>();
        final String name = randomString();
        final Float value = new Float(randomIntInRange(-10, 10));
        metricCustomAttribute.put(name, value);
        final FloatAttribute mockFloatAttribute = mock(FloatAttribute.class);
        when(CustomAttribute.newFloatAttribute(name, value)).thenReturn(mockFloatAttribute);

        // When
        final Map<String, CustomAttribute> result = metricToIntercomMapper.apply(metricCustomAttribute);

        // Then
        assertNotNull(result);
        assertThat(result.get(name), is(mockFloatAttribute));
    }

    @Test
    public void shouldMapToNullCustomAttribute_withNullAttribute() {
        // Given
        final Map<String, Object> metricCustomAttribute = new HashMap<>();
        final String name = randomString();
        metricCustomAttribute.put(name, null);
        final StringAttribute mockNullAttribute = mock(StringAttribute.class);
        when(CustomAttribute.newStringAttribute(name, null)).thenReturn(mockNullAttribute);

        // When
        final Map<String, CustomAttribute> result = metricToIntercomMapper.apply(metricCustomAttribute);

        // Then
        assertNotNull(result);
        assertThat(result.get(name), is(mockNullAttribute));
    }

    @Test
    public void shouldAddValidCustomAttributesToUser_withValidAndInvalidAttribute() {
        // Given
        final Map<String, CustomAttribute> expectedAttributes = new HashMap<>();
        final Map<String, Object> metricCustomAttribute = new HashMap<>();
        metricCustomAttribute.put(randomString(5), new Object());

        final BooleanAttribute booleanAttribute = mock(BooleanAttribute.class);
        when(CustomAttribute.newBooleanAttribute(any(String.class), any(Boolean.class))).thenReturn(booleanAttribute);
        final String booleanKey = randomString(5);
        metricCustomAttribute.put(booleanKey, randomBoolean());
        expectedAttributes.put(booleanKey, booleanAttribute);

        final StringAttribute stringAttribute = mock(StringAttribute.class);
        when(CustomAttribute.newStringAttribute(any(String.class), any(String.class))).thenReturn(stringAttribute);
        final String stringKey = randomString(5);
        metricCustomAttribute.put(stringKey, randomString(5));
        expectedAttributes.put(stringKey, stringAttribute);

        final IntegerAttribute integerAttribute = mock(IntegerAttribute.class);
        when(CustomAttribute.newIntegerAttribute(any(String.class), any(Integer.class))).thenReturn(integerAttribute);
        final String integerKey = randomString(5);
        metricCustomAttribute.put(integerKey, Integer.valueOf(randomIntInRange(-10, 10)));
        expectedAttributes.put(integerKey, integerAttribute);

        // When
        final Map<String, CustomAttribute> result = metricToIntercomMapper.apply(metricCustomAttribute);

        // Then
        assertNotNull(result);
        assertThat(result.size(), is(expectedAttributes.size()));
        assertEquals(result, expectedAttributes);

    }

}
