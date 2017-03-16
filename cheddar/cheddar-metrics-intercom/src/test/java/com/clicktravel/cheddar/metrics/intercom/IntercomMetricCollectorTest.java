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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Before
    public void setUp() {
        PowerMockito.mockStatic(CustomAttribute.class);
        PowerMockito.mockStatic(User.class);
        final String personalAccessToken = randomString();
        intercomMetricCollector = new IntercomMetricCollector(personalAccessToken);
    }

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

        final Map<String, Object> customAttributes = new HashMap<>();
        customAttributes.put(randomString(5), new Object());

        final BooleanAttribute booleanAttribute = mock(BooleanAttribute.class);
        when(CustomAttribute.newBooleanAttribute(any(String.class), any(Boolean.class))).thenReturn(booleanAttribute);
        customAttributes.put(randomString(5), randomBoolean());

        final StringAttribute stringAttribute = mock(StringAttribute.class);
        when(CustomAttribute.newStringAttribute(any(String.class), any(String.class))).thenReturn(stringAttribute);
        customAttributes.put(randomString(5), randomString(5));

        final IntegerAttribute integerAttribute = mock(IntegerAttribute.class);
        when(CustomAttribute.newIntegerAttribute(any(String.class), any(Integer.class))).thenReturn(integerAttribute);
        customAttributes.put(randomString(5), Integer.valueOf(randomIntInRange(-10, 10)));
        final List<CustomAttribute<?>> expectedAttributes = Arrays.asList(booleanAttribute, stringAttribute,
                integerAttribute);

        // When
        intercomMetricCollector.addCustomAttributesToUser(userId, customAttributes);
        // Then
        final ArgumentCaptor<CustomAttribute> argumentCaptor = ArgumentCaptor.forClass(CustomAttribute.class);
        verify(mockUser, times(customAttributes.size() - 1)).addCustomAttribute(argumentCaptor.capture());
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
        final Map<String, Object> customAttributes = mock(Map.class);

        // When
        intercomMetricCollector.addCustomAttributesToUser(userId, customAttributes);

        // Then
        verifyStatic(never());
        User.update(any(User.class));
    }

}
