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
import static com.clicktravel.common.random.Randoms.randomId;
import static com.clicktravel.common.random.Randoms.randomIntInRange;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.intercom.api.CustomAttribute;
import io.intercom.api.CustomAttribute.*;
import io.intercom.api.User;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CustomAttribute.class, User.class })
@SuppressWarnings({ "rawtypes", "unchecked" })
public class IntercomMetricCollectorTest {

    private IntercomMetricCollector intercomMetricCollector;
    private List<Class<?>> classTypes;
    private List<CustomAttribute<?>> expectedAttributes;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(CustomAttribute.class);
        PowerMockito.mockStatic(User.class);
        classTypes = Arrays.asList(Boolean.class, String.class, Integer.class, Double.class, Long.class, Float.class);
        expectedAttributes = new ArrayList<>();
        final String personalAccessToken = randomString();
        intercomMetricCollector = new IntercomMetricCollector(personalAccessToken);
    }
    //
    // @Test
    // public void shouldAddCustomAttributesToUser_withUserIdAndValidAttribute() {
    // // Given
    // final String userId = randomId();
    // final User mockUser = mock(User.class);
    // when(User.find(userId)).thenReturn(mockUser);
    // final Map<String, Object> customAttributes = mockCustomAttributes();
    //
    // // When
    // intercomMetricCollector.addCustomAttributesToUser(userId, customAttributes);
    //
    // // Then
    // final ArgumentCaptor<CustomAttribute> argumentCaptor = ArgumentCaptor.forClass(CustomAttribute.class);
    // verify(mockUser, times(classTypes.size())).addCustomAttribute(argumentCaptor.capture());
    // assertThat(argumentCaptor.getAllValues(),
    // containsInAnyOrder(expectedAttributes.toArray(new CustomAttribute[expectedAttributes.size()])));
    // verifyStatic();
    // User.update(mockUser);
    // }

    @Test
    public void shouldAddBooleanCustomAttributeToUser_withBooleanAttribute() {
        // Given
        final String userId = randomId();
        final User mockUser = mock(User.class);
        when(User.find(userId)).thenReturn(mockUser);
        final Map<String, Object> customAttributes = new HashMap<>();
        final BooleanAttribute booleanAttribute = mock(BooleanAttribute.class);
        final String name = randomString(5);
        final boolean value = randomBoolean();
        when(CustomAttribute.newBooleanAttribute(name, value)).thenReturn(booleanAttribute);
        customAttributes.put(name, value);

        // When
        intercomMetricCollector.addCustomAttributesToUser(userId, customAttributes);

        // Then
        final ArgumentCaptor<CustomAttribute> argumentCaptor = ArgumentCaptor.forClass(CustomAttribute.class);
        verify(mockUser).addCustomAttribute(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue(), is(booleanAttribute));
        verifyStatic();
        User.update(mockUser);
    }

    @Test
    public void shouldAddStringCustomAttributeToUser_withStringAttribute() {
        // Given
        final String userId = randomId();
        final User mockUser = mock(User.class);
        when(User.find(userId)).thenReturn(mockUser);
        final Map<String, Object> customAttributes = new HashMap<>();
        final StringAttribute stringAttribute = mock(StringAttribute.class);
        final String name = randomString(5);
        final String value = randomString(5);
        when(CustomAttribute.newStringAttribute(name, value)).thenReturn(stringAttribute);
        customAttributes.put(name, value);

        // When
        intercomMetricCollector.addCustomAttributesToUser(userId, customAttributes);

        // Then
        final ArgumentCaptor<CustomAttribute> argumentCaptor = ArgumentCaptor.forClass(CustomAttribute.class);
        verify(mockUser).addCustomAttribute(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue(), is(stringAttribute));
        verifyStatic();
        User.update(mockUser);
    }

    @Test
    public void shouldAddIntegerCustomAttributeToUser_withIntegerAttribute() {
        // Given
        final String userId = randomId();
        final User mockUser = mock(User.class);
        when(User.find(userId)).thenReturn(mockUser);
        final Map<String, Object> customAttributes = new HashMap<>();
        final IntegerAttribute integerAttribute = mock(IntegerAttribute.class);
        final String name = randomString(5);
        final Integer value = Integer.valueOf(randomIntInRange(-10, 10));
        when(CustomAttribute.newIntegerAttribute(name, value)).thenReturn(integerAttribute);
        customAttributes.put(name, value);

        // When
        intercomMetricCollector.addCustomAttributesToUser(userId, customAttributes);

        // Then
        final ArgumentCaptor<CustomAttribute> argumentCaptor = ArgumentCaptor.forClass(CustomAttribute.class);
        verify(mockUser).addCustomAttribute(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue(), is(integerAttribute));
        verifyStatic();
        User.update(mockUser);
    }

    @Test
    public void shouldAddDoubleCustomAttributeToUser_withDoubleAttribute() {
        // Given
        final String userId = randomId();
        final User mockUser = mock(User.class);
        when(User.find(userId)).thenReturn(mockUser);
        final Map<String, Object> customAttributes = new HashMap<>();
        final DoubleAttribute doubleAttribute = mock(DoubleAttribute.class);
        final String name = randomString(5);
        final Double value = Double.valueOf(randomIntInRange(-10, 10));
        when(CustomAttribute.newDoubleAttribute(name, value)).thenReturn(doubleAttribute);
        customAttributes.put(name, value);

        // When
        intercomMetricCollector.addCustomAttributesToUser(userId, customAttributes);

        // Then
        final ArgumentCaptor<CustomAttribute> argumentCaptor = ArgumentCaptor.forClass(CustomAttribute.class);
        verify(mockUser).addCustomAttribute(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue(), is(doubleAttribute));
        verifyStatic();
        User.update(mockUser);
    }

    @Test
    public void shouldAddLongCustomAttributeToUser_withLongAttribute() {
        // Given
        final String userId = randomId();
        final User mockUser = mock(User.class);
        when(User.find(userId)).thenReturn(mockUser);
        final Map<String, Object> customAttributes = new HashMap<>();
        final LongAttribute longAttribute = mock(LongAttribute.class);
        final String name = randomString(5);
        final Long value = Long.valueOf(randomIntInRange(-10, 10));
        when(CustomAttribute.newLongAttribute(name, value)).thenReturn(longAttribute);
        customAttributes.put(name, value);

        // When
        intercomMetricCollector.addCustomAttributesToUser(userId, customAttributes);

        // Then
        final ArgumentCaptor<CustomAttribute> argumentCaptor = ArgumentCaptor.forClass(CustomAttribute.class);
        verify(mockUser).addCustomAttribute(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue(), is(longAttribute));
        verifyStatic();
        User.update(mockUser);
    }

    @Test
    public void shouldAddFloatCustomAttributeToUser_withFloatAttribute() {
        // Given
        final String userId = randomId();
        final User mockUser = mock(User.class);
        when(User.find(userId)).thenReturn(mockUser);
        final Map<String, Object> customAttributes = new HashMap<>();
        final FloatAttribute floatAttribute = mock(FloatAttribute.class);
        final String name = randomString(5);
        final Float value = Float.valueOf(randomIntInRange(-10, 10));
        when(CustomAttribute.newFloatAttribute(name, value)).thenReturn(floatAttribute);
        customAttributes.put(name, value);

        // When
        intercomMetricCollector.addCustomAttributesToUser(userId, customAttributes);

        // Then
        final ArgumentCaptor<CustomAttribute> argumentCaptor = ArgumentCaptor.forClass(CustomAttribute.class);
        verify(mockUser).addCustomAttribute(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue(), is(floatAttribute));
        verifyStatic();
        User.update(mockUser);
    }

    @Test
    public void shouldAddValidCustomAttributesToUser_withValidAndInvalidAttribute() {
        // Given
        final String userId = randomId();
        final User mockUser = mock(User.class);
        when(User.find(userId)).thenReturn(mockUser);
        final Map<String, Object> customAttributes = mockCustomAttributes();
        customAttributes.put(randomString(), new Object());

        // When
        intercomMetricCollector.addCustomAttributesToUser(userId, customAttributes);

        // Then
        final ArgumentCaptor<CustomAttribute> argumentCaptor = ArgumentCaptor.forClass(CustomAttribute.class);
        verify(mockUser, times(classTypes.size())).addCustomAttribute(argumentCaptor.capture());
        assertThat(argumentCaptor.getAllValues(),
                containsInAnyOrder(expectedAttributes.toArray(new CustomAttribute[expectedAttributes.size()])));
        verifyStatic();
        User.update(mockUser);
    }

    @Test
    public void shouldNotAddCustomAttributesToUser_withIntercomFindUserException() {
        // Given
        final String userId = randomId();
        when(User.find(userId)).thenThrow(Exception.class);
        final Map<String, Object> customAttributes = mockCustomAttributes();

        // When
        intercomMetricCollector.addCustomAttributesToUser(userId, customAttributes);

        // Then
        verifyStatic(never());
        User.update(any(User.class));
    }

    private Map<String, Object> mockCustomAttributes() {
        final Map<String, Object> customAttributes = new HashMap<>();
        classTypes.forEach(classType -> {
            if (classType.equals(Boolean.class)) {
                final String name = randomString(5);
                final boolean value = randomBoolean();
                customAttributes.put(name, value);
                final BooleanAttribute mockBooleanAttribute = mock(BooleanAttribute.class);
                when(CustomAttribute.newBooleanAttribute(name, value)).thenReturn(mockBooleanAttribute);
                expectedAttributes.add(mockBooleanAttribute);
            } else if (classType.equals(String.class)) {
                final String name = randomString(5);
                final String value = randomString(5);
                customAttributes.put(name, value);
                final StringAttribute mockStringAttribute = mock(StringAttribute.class);
                when(CustomAttribute.newStringAttribute(name, value)).thenReturn(mockStringAttribute);
                expectedAttributes.add(mockStringAttribute);
            } else if (classType.equals(Integer.class)) {
                final String name = randomString(5);
                final Integer value = Integer.valueOf(randomIntInRange(-10, 10));
                customAttributes.put(name, value);
                final IntegerAttribute mockIntegerAttribute = mock(IntegerAttribute.class);
                when(CustomAttribute.newIntegerAttribute(name, value)).thenReturn(mockIntegerAttribute);
                expectedAttributes.add(mockIntegerAttribute);
            } else if (classType.equals(Double.class)) {
                final String name = randomString(5);
                final Double value = Double.valueOf(randomIntInRange(-10, 10));
                customAttributes.put(name, value);
                final DoubleAttribute mockDoubleAttribute = mock(DoubleAttribute.class);
                when(CustomAttribute.newDoubleAttribute(name, value)).thenReturn(mockDoubleAttribute);
                expectedAttributes.add(mockDoubleAttribute);
            } else if (classType.equals(Long.class)) {
                final String name = randomString(5);
                final Long value = Long.valueOf(randomIntInRange(-10, 10));
                customAttributes.put(name, value);
                final LongAttribute mockLongAttribute = mock(LongAttribute.class);
                when(CustomAttribute.newLongAttribute(name, value)).thenReturn(mockLongAttribute);
                expectedAttributes.add(mockLongAttribute);
            } else if (classType.equals(Float.class)) {
                final String name = randomString(5);
                final Float value = Float.valueOf(randomIntInRange(-10, 10));
                customAttributes.put(name, value);
                final FloatAttribute mockFloatAttribute = mock(FloatAttribute.class);
                when(CustomAttribute.newFloatAttribute(name, value)).thenReturn(mockFloatAttribute);
                expectedAttributes.add(mockFloatAttribute);
            }
        });
        return customAttributes;
    }

}
