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

import static com.clicktravel.common.random.Randoms.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CustomAttribute.class })
@SuppressWarnings("rawtypes")
public class IntercomCustomAttributeToMetricCustomAttributeMapperTest {

    private IntercomCustomAttributeToMetricCustomAttributeMapper mapper;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(CustomAttribute.class);
        mapper = new IntercomCustomAttributeToMetricCustomAttributeMapper();
    }

    @Test
    public void shouldMapToBooleanAttribute_withBooleanCustomAttribute() {
        // Given
        final String name = randomString(10);
        final boolean value = new Boolean(randomBoolean());
        final CustomAttribute mockBooleanAttribute = mock(CustomAttribute.class);
        when(mockBooleanAttribute.getValueClass()).thenReturn(Boolean.class);
        when(mockBooleanAttribute.getName()).thenReturn(name);
        when(mockBooleanAttribute.getValue()).thenReturn(value);
        when(mockBooleanAttribute.booleanValue()).thenReturn(value);

        final Map<String, CustomAttribute> intercomCustomAttributes = new HashMap<>();
        intercomCustomAttributes.put(name, mockBooleanAttribute);

        // When
        final Map<String, Object> result = mapper.apply(intercomCustomAttributes);

        // Then
        assertNotNull(result);
        assertEquals(result.get(name), value);
    }

    @Test
    public void shouldMapToIntegerAttribute_withIntegerCustomAttribute() {
        // Given
        final String name = randomString(10);
        final int value = randomIntInRange(-10, 10);
        final CustomAttribute mockIntegerAttribute = mock(CustomAttribute.class);
        when(mockIntegerAttribute.getValueClass()).thenReturn(Integer.class);
        when(mockIntegerAttribute.getName()).thenReturn(name);
        when(mockIntegerAttribute.getValue()).thenReturn(value);
        when(mockIntegerAttribute.integerValue()).thenReturn(value);

        final Map<String, CustomAttribute> intercomCustomAttributes = new HashMap<>();
        intercomCustomAttributes.put(name, mockIntegerAttribute);

        // When
        final Map<String, Object> result = mapper.apply(intercomCustomAttributes);

        // Then
        assertNotNull(result);
        assertEquals(result.get(name), value);
    }

    @Test
    public void shouldMapToLongAttribute_withLongCustomAttribute() {
        // Given
        final String name = randomString(10);
        final long value = new Long(randomIntInRange(-10, 10));
        final CustomAttribute mockLongAttribute = mock(CustomAttribute.class);
        when(mockLongAttribute.getValueClass()).thenReturn(Long.class);
        when(mockLongAttribute.getName()).thenReturn(name);
        when(mockLongAttribute.getValue()).thenReturn(value);
        when(mockLongAttribute.longValue()).thenReturn(value);

        final Map<String, CustomAttribute> intercomCustomAttributes = new HashMap<>();
        intercomCustomAttributes.put(name, mockLongAttribute);

        // When
        final Map<String, Object> result = mapper.apply(intercomCustomAttributes);

        // Then
        assertNotNull(result);
        assertEquals(result.get(name), value);
    }

    @Test
    public void shouldMapToFloatAttribute_withFloatCustomAttribute() {
        // Given
        final String name = randomString(10);
        final float value = randomFloat();
        final CustomAttribute mockFloatAttribute = mock(CustomAttribute.class);
        when(mockFloatAttribute.getValueClass()).thenReturn(Float.class);
        when(mockFloatAttribute.getName()).thenReturn(name);
        when(mockFloatAttribute.getValue()).thenReturn(value);
        when(mockFloatAttribute.floatValue()).thenReturn(value);

        final Map<String, CustomAttribute> intercomCustomAttributes = new HashMap<>();
        intercomCustomAttributes.put(name, mockFloatAttribute);

        // When
        final Map<String, Object> result = mapper.apply(intercomCustomAttributes);

        // Then
        assertNotNull(result);
        assertEquals(result.get(name), value);
    }

    @Test
    public void shouldMapToDoubleAttribute_withDoubleCustomAttribute() {
        // Given
        final String name = randomString(10);
        final double value = randomDouble();
        final CustomAttribute mockFloatAttribute = mock(CustomAttribute.class);
        when(mockFloatAttribute.getValueClass()).thenReturn(Double.class);
        when(mockFloatAttribute.getName()).thenReturn(name);
        when(mockFloatAttribute.getValue()).thenReturn(value);
        when(mockFloatAttribute.doubleValue()).thenReturn(value);

        final Map<String, CustomAttribute> intercomCustomAttributes = new HashMap<>();
        intercomCustomAttributes.put(name, mockFloatAttribute);

        // When
        final Map<String, Object> result = mapper.apply(intercomCustomAttributes);

        // Then
        assertNotNull(result);
        assertEquals(result.get(name), value);
    }

    @Test
    public void shouldMapToStringAttribute_withStringCustomAttribute() {
        // Given
        final String name = randomString(10);
        final String value = randomString();
        final CustomAttribute mockStringAttribute = mock(CustomAttribute.class);
        when(mockStringAttribute.getValueClass()).thenReturn(String.class);
        when(mockStringAttribute.getName()).thenReturn(name);
        when(mockStringAttribute.getValue()).thenReturn(value);
        when(mockStringAttribute.textValue()).thenReturn(value);

        final Map<String, CustomAttribute> intercomCustomAttributes = new HashMap<>();
        intercomCustomAttributes.put(name, mockStringAttribute);
        // When
        final Map<String, Object> result = mapper.apply(intercomCustomAttributes);

        // Then
        assertNotNull(result);
        assertEquals(result.get(name), value);
    }

    @Test
    public void shouldMapToNullAttribute_withNullCustomAttribute() {
        // Given
        final Map<String, CustomAttribute> intercomCustomAttributes = new HashMap<>();
        final String name = randomString();
        final CustomAttribute intercomCustomAttribute = CustomAttribute.newStringAttribute(name, null);
        intercomCustomAttributes.put(name, intercomCustomAttribute);

        // When
        final Map<String, Object> result = mapper.apply(intercomCustomAttributes);

        // Then
        assertNotNull(result);
        assertNull(result.get(name));
    }

}
