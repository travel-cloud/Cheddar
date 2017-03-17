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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.clicktravel.cheddar.metrics.MetricUser;
import com.clicktravel.cheddar.metrics.MetricUserNotFoundException;
import com.clicktravel.common.validation.ValidationException;

import io.intercom.api.Company;
import io.intercom.api.CompanyCollection;
import io.intercom.api.CustomAttribute;
import io.intercom.api.CustomAttribute.*;
import io.intercom.api.User;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CustomAttribute.class, User.class, IntercomMetricCollector.class })
@SuppressWarnings({ "rawtypes", "unchecked" })
public class IntercomMetricCollectorTest {
    private IntercomMetricCollector intercomMetricCollector;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(CustomAttribute.class);
        PowerMockito.mockStatic(User.class);
        PowerMockito.mockStatic(Company.class);
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

    @Test
    public void shouldAddOrganisationToUser_withUserIdAndOrganisationId() throws Exception {
        // Given
        final String userId = randomId();
        final String organisationId = randomId();
        final User mockUser = mock(User.class);
        when(User.find(userId)).thenReturn(mockUser);
        final Company mockCompany = mock(Company.class);
        whenNew(Company.class).withNoArguments().thenReturn(mockCompany);

        // When
        intercomMetricCollector.addOrganisationToUser(userId, organisationId);

        // Then
        verify(mockCompany).setCompanyID(organisationId);
        verify(mockUser).addCompany(mockCompany);
        verifyStatic();
        User.update(mockUser);
    }

    @Test
    public void shouldNotAddOrganisationToUser_withUserIdAndOrganisationIdAndIntercomFindUserException() {
        // Given
        final String userId = randomId();
        final String organisationId = randomId();
        when(User.find(userId)).thenThrow(Exception.class);

        // When
        intercomMetricCollector.addOrganisationToUser(userId, organisationId);

        // Then
        verifyStatic(never());
        User.update(any(User.class));
    }

    public void shouldRemoveOrganisationFromUser_withUserIdAndOrganisationId() throws Exception {
        // Given
        final String userId = randomId();
        final String organisationId = randomId();
        final User mockUser = mock(User.class);
        when(User.find(userId)).thenReturn(mockUser);
        final Company mockCompany = mock(Company.class);
        whenNew(Company.class).withNoArguments().thenReturn(mockCompany);
        when(Company.find(organisationId)).thenReturn(mockCompany);

        // When
        intercomMetricCollector.removeOrganisationFromUser(userId, organisationId);

        // Then
        verify(mockCompany).setCompanyID(organisationId);
        verify(mockUser).removeCompany(mockCompany);
        verifyStatic();
        User.update(mockUser);
    }

    @Test
    public void shouldNotRemoveOrganisationFromUser_withUserIdAndOrganisationIdAndIntercomFindUserException() {
        // Given
        final String userId = randomId();
        final String organisationId = randomId();
        when(User.find(userId)).thenThrow(Exception.class);

        // When
        intercomMetricCollector.removeOrganisationFromUser(userId, organisationId);

        // Then
        verifyStatic(never());
        User.update(any(User.class));
    }

    @Test
    public void shouldReturnUser_withUserId() {
        // Given
        final String userId = randomId();
        final User mockUser = randomUser();
        when(User.find(userId)).thenReturn(mockUser);

        // When
        final MetricUser result = intercomMetricCollector.getUser(userId);

        // Then
        assertNotNull(result);
        assertThat(result.id(), is(mockUser.getId()));
        mockUser.getCompanyCollection().forEachRemaining(company -> {
            assertTrue(result.organisationIds().contains(company.getId()));
        });
        assertThat(result.name(), is(mockUser.getName()));
        assertThat(result.emailAddress(), is(mockUser.getEmail()));
        assertThat(result.customAttributes().size(), is(mockUser.getCustomAttributes().size()));
        mockUser.getCustomAttributes().entrySet().forEach(entry -> {
            assertTrue(result.customAttributes().containsKey(entry.getKey()));
            assertThat(result.customAttributes().get(entry.getKey()), is(entry.getValue()));
        });
    }

    @Test
    public void shouldNotReturnUser_withNullUserId() {
        // Given
        final String userId = null;

        // When
        ValidationException thrownException = null;
        try {
            intercomMetricCollector.getUser(userId);
        } catch (final ValidationException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldNotReturnUser_withExceptionThrownByIntercom() {
        // Given
        final String userId = randomId();

        when(User.find(userId)).thenThrow(Exception.class);

        // When
        MetricUserNotFoundException thrownException = null;
        try {
            intercomMetricCollector.getUser(userId);
        } catch (final MetricUserNotFoundException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldNotReturnUser_withNullUserReturnedByIntercom() {
        // Given
        final String userId = randomId();
        final User user = null;

        when(User.find(userId)).thenReturn(user);

        // When
        MetricUserNotFoundException thrownException = null;
        try {
            intercomMetricCollector.getUser(userId);
        } catch (final MetricUserNotFoundException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    private User randomUser() {
        final User user = new User();
        user.setId(randomId());
        user.setCompanyCollection(randomCompanyCollection());
        user.setName(randomString());
        user.setEmail(randomEmailAddress());
        user.setCustomAttributes(randomCustomAttributes());
        return user;
    }

    private CompanyCollection randomCompanyCollection() {
        final List<Company> companies = new ArrayList<>();
        final CompanyCollection companyCollection = new CompanyCollection(companies);

        final int numberOfCompanies = randomInt(10);

        for (int i = 0; i < numberOfCompanies; i++) {
            final Company mockCompany = mock(Company.class);
            final String companyId = randomId();
            when(mockCompany.getId()).thenReturn(companyId);

            companies.add(mockCompany);
        }

        return companyCollection;
    }

    private Map<String, CustomAttribute> randomCustomAttributes() {
        final Map<String, CustomAttribute> customAttributes = new HashMap<>();

        final int numberOfCompanies = randomInt(10);

        for (int i = 0; i < numberOfCompanies; i++) {
            final String key = randomString();
            final CustomAttribute mockCustomAttribute = mock(CustomAttribute.class);

            customAttributes.put(key, mockCustomAttribute);
        }

        return customAttributes;
    }
}
