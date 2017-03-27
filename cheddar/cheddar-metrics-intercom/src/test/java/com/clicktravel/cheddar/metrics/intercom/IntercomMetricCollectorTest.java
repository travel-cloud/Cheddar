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

import static com.clicktravel.cheddar.metrics.intercom.random.data.RandomIntercomDataGenerator.randomIntercomUser;
import static com.clicktravel.cheddar.metrics.intercom.random.data.RandomMetricDataGenerator.randomMetricOrganisation;
import static com.clicktravel.cheddar.metrics.intercom.random.data.RandomMetricDataGenerator.randomMetricUser;
import static com.clicktravel.common.random.Randoms.randomId;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.clicktravel.cheddar.metrics.MetricOrganisation;
import com.clicktravel.cheddar.metrics.MetricUser;
import com.clicktravel.cheddar.metrics.MetricUserNotFoundException;
import com.clicktravel.common.validation.ValidationException;

import io.intercom.api.Company;
import io.intercom.api.CustomAttribute;
import io.intercom.api.Tag;
import io.intercom.api.User;

@SuppressWarnings({ "unchecked", "rawtypes" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ User.class, Company.class, IntercomMetricCollector.class, Tag.class })
public class IntercomMetricCollectorTest {

    private MetricCustomAttributeToIntercomCustomAttributeMapper customAttributeMapper;
    private IntercomMetricCollector intercomMetricCollector;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(User.class);
        PowerMockito.mockStatic(Company.class);
        PowerMockito.mockStatic(Tag.class);
        final String personalAccessToken = randomString();
        customAttributeMapper = mock(MetricCustomAttributeToIntercomCustomAttributeMapper.class);
        intercomMetricCollector = new IntercomMetricCollector(personalAccessToken);
        intercomMetricCollector.setCustomAttributeMapper(customAttributeMapper);
    }

    @Test
    public void shouldCreateIntercomCompany_withMetricOrganisation() throws Exception {
        // Given
        final MetricOrganisation metricOrganisation = randomMetricOrganisation();

        // When
        intercomMetricCollector.createOrganisation(metricOrganisation);

        // Then
        verifyStatic();
        final ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
        Company.create(companyCaptor.capture());
        final Company company = companyCaptor.getValue();
        assertThat(company.getCompanyID(), is(metricOrganisation.id()));
        assertThat(company.getName(), is(metricOrganisation.name()));
    }

    @Test
    public void shouldUpdateIntercomCompany_withMetricOrganisation() throws Exception {
        // Given
        final MetricOrganisation metricOrganisation = randomMetricOrganisation();

        // When
        intercomMetricCollector.updateOrganisation(metricOrganisation);

        // Then
        verifyStatic();
        final ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
        Company.create(companyCaptor.capture());
        final Company company = companyCaptor.getValue();
        assertThat(company.getCompanyID(), is(metricOrganisation.id()));
        assertThat(company.getName(), is(metricOrganisation.name()));
    }

    @Test
    public void shouldTagIntercomOrganisation_withMetricOrganisation() throws Exception {
        // Given
        final String tagName = randomString();
        final MetricOrganisation metricOrganisation = randomMetricOrganisation();

        // When
        intercomMetricCollector.tagOrganisation(tagName, metricOrganisation);

        // Then
        verifyStatic();
        final ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
        final ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
        Tag.tag(tagCaptor.capture(), companyCaptor.capture());
        assertThat(tagCaptor.getValue().getName(), is(tagName));
        assertThat(companyCaptor.getValue().getCompanyID(), is(metricOrganisation.id()));
    }

    @Test
    public void shouldCreateUser_withMetricUser() throws Exception {
        // Given
        final MetricUser metricUser = randomMetricUser();
        final Map<String, CustomAttribute> customAttributes = mock(Map.class);
        when(customAttributeMapper.apply(metricUser.customAttributes())).thenReturn(customAttributes);
        DateTimeUtils.setCurrentMillisFixed(DateTime.now().getMillis());
        // When
        intercomMetricCollector.createUser(metricUser);

        // Then
        verifyStatic();
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        User.create(userCaptor.capture());
        final User intercomUser = userCaptor.getValue();
        assertThat(intercomUser.getId(), is(metricUser.id()));
        assertThat(intercomUser.getUserId(), is(metricUser.id()));
        assertThat(intercomUser.getCustomAttributes(), is(customAttributes));
        assertThat(intercomUser.getCompanyCollection().getPage().size(), is(metricUser.organisationIds().size()));
        assertThat(intercomUser.getSignedUpAt(), is(DateTime.now().getMillis() / 1000));
        intercomUser.getCompanyCollection().getPage()
                .forEach(company -> assertTrue(metricUser.organisationIds().contains(company.getCompanyID())));
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void shouldUpdateUser_withMetricUser() throws Exception {
        // Given
        final MetricUser metricUser = randomMetricUser();
        final Map<String, CustomAttribute> customAttributes = mock(Map.class);
        when(customAttributeMapper.apply(metricUser.customAttributes())).thenReturn(customAttributes);
        // When
        intercomMetricCollector.updateUser(metricUser);

        // Then
        verifyStatic();
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        User.update(userCaptor.capture());
        final User intercomUser = userCaptor.getValue();
        assertThat(intercomUser.getId(), is(metricUser.id()));
        assertThat(intercomUser.getUserId(), is(metricUser.id()));
        assertThat(intercomUser.getUserId(), is(metricUser.id()));
        assertThat(intercomUser.getCustomAttributes(), is(customAttributes));
        assertThat(intercomUser.getCompanyCollection().getPage().size(), is(metricUser.organisationIds().size()));
        intercomUser.getCompanyCollection().getPage()
                .forEach(company -> assertTrue(metricUser.organisationIds().contains(company.getCompanyID())));
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void shouldAddCustomAttributesToUser_withUserIdAndCustomAttributes() {
        // Given
        final String userId = randomId();
        final User mockUser = mock(User.class);
        final Map<String, Object> customAttributes = mock(Map.class);
        final Map<String, String> params = new HashMap<>();
        params.put("user_id", userId);
        when(User.find(params)).thenReturn(mockUser);
        final Map<String, CustomAttribute> mockCustomAttributes = mock(Map.class);
        when(customAttributeMapper.apply(customAttributes)).thenReturn(mockCustomAttributes);

        // When
        intercomMetricCollector.addCustomAttributesToUser(userId, customAttributes);

        // Then
        verifyStatic();
        final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        User.update(userCaptor.capture());
        assertThat(userCaptor.getValue().getCustomAttributes(), is(mockCustomAttributes));
    }

    @Test
    public void shouldNotAddCustomAttributesToUser_withIntercomFindUserException() {
        // Given
        final String userId = randomId();
        final Map<String, String> params = new HashMap<>();
        params.put("user_id", userId);
        when(User.find(params)).thenThrow(Exception.class);
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
        final Map<String, String> params = new HashMap<>();
        params.put("user_id", userId);
        final String organisationId = randomId();
        final User mockUser = mock(User.class);
        when(User.find(params)).thenReturn(mockUser);

        // When
        intercomMetricCollector.addOrganisationToUser(userId, organisationId);

        // Then
        final ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
        verify(mockUser).addCompany(companyCaptor.capture());
        assertThat(companyCaptor.getValue().getCompanyID(), is(organisationId));
        verifyStatic();
        User.update(mockUser);
    }

    @Test
    public void shouldNotAddOrganisationToUser_withUserIdAndOrganisationIdAndIntercomFindUserException() {
        // Given
        final String userId = randomId();
        final Map<String, String> params = new HashMap<>();
        params.put("user_id", userId);
        final String organisationId = randomId();
        when(User.find(params)).thenThrow(Exception.class);

        // When
        intercomMetricCollector.addOrganisationToUser(userId, organisationId);

        // Then
        verifyStatic(never());
        User.update(any(User.class));
    }

    public void shouldRemoveOrganisationFromUser_withUserIdAndOrganisationId() throws Exception {
        // Given
        final String userId = randomId();
        final Map<String, String> params = new HashMap<>();
        params.put("user_id", userId);
        final String organisationId = randomId();
        final User mockUser = mock(User.class);
        when(User.find(params)).thenReturn(mockUser);
        final Company mockCompany = mock(Company.class);
        when(Company.find(organisationId)).thenReturn(mockCompany);

        // When
        intercomMetricCollector.removeOrganisationFromUser(userId, organisationId);

        // Then
        final ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
        verify(mockUser).removeCompany(companyCaptor.capture());
        assertThat(companyCaptor.getValue().getCompanyID(), is(organisationId));
        verifyStatic();
        User.update(mockUser);
    }

    @Test
    public void shouldNotRemoveOrganisationFromUser_withUserIdAndOrganisationIdAndIntercomFindUserException() {
        // Given
        final String userId = randomId();
        final Map<String, String> params = new HashMap<>();
        params.put("user_id", userId);
        final String organisationId = randomId();
        when(User.find(params)).thenThrow(Exception.class);

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
        final Map<String, String> params = new HashMap<>();
        params.put("user_id", userId);
        final User mockUser = randomIntercomUser();
        when(User.find(params)).thenReturn(mockUser);

        // When
        final MetricUser result = intercomMetricCollector.getUser(userId);

        // Then
        assertNotNull(result);
        assertThat(result.id(), is(mockUser.getUserId()));
        mockUser.getCompanyCollection().forEachRemaining(company -> {
            assertTrue(result.organisationIds().contains(company.getId()));
        });
        assertThat(result.name(), is(mockUser.getName()));
        assertThat(result.emailAddress(), is(mockUser.getEmail()));
        assertThat(result.customAttributes().size(), is(mockUser.getCustomAttributes().size()));
        mockUser.getCustomAttributes().entrySet().forEach(entry -> {
            assertTrue(result.customAttributes().containsKey(entry.getKey()));
            assertThat(result.customAttributes().get(entry.getKey()), is(entry.getValue().getValue()));
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
        final Map<String, String> params = new HashMap<>();
        params.put("user_id", userId);

        when(User.find(params)).thenThrow(Exception.class);

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
        final Map<String, String> params = new HashMap<>();
        params.put("user_id", userId);
        final User user = null;

        when(User.find(params)).thenReturn(user);

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
}
