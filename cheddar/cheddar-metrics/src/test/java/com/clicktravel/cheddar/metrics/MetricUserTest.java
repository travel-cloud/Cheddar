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
package com.clicktravel.cheddar.metrics;

import static com.clicktravel.common.random.Randoms.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

@SuppressWarnings("unchecked")
public class MetricUserTest {

    @Test
    public void shouldCreateMetricUser_withIdAndOrganisationIdAndNameAndEmailAddress() {
        // Given
        final String id = randomId();
        final String organisationId = randomId();
        final String name = randomString();
        final String emailAddress = randomEmailAddress();
        final String phoneNumber = randomPhoneNumber();

        // When
        final MetricUser metricUser = new MetricUser(id, organisationId, name, emailAddress, phoneNumber);

        // Then
        assertEquals(id, metricUser.id());
        assertEquals(organisationId, metricUser.organisationId());
        assertEquals(1, metricUser.organisationIds().size());
        assertEquals(organisationId, metricUser.organisationIds().get(0));
        assertEquals(name, metricUser.name());
        assertEquals(emailAddress, metricUser.emailAddress());
        assertEquals(phoneNumber, metricUser.phoneNumber());
        assertTrue(metricUser.customAttributes().isEmpty());
    }

    @Test
    public void shouldCreateMetricUser_withIdAndOrganisationIdsAndNameAndEmailAddress() {
        // Given
        final String id = randomId();
        final List<String> organisationIds = Arrays.asList(randomId(), randomId());
        final String name = randomString();
        final String emailAddress = randomEmailAddress();
        final String phoneNumber = randomPhoneNumber();
        final Map<String, Object> mockCustomAttributes = mock(Map.class);

        // When
        final MetricUser metricUser = new MetricUser(id, organisationIds, name, emailAddress, phoneNumber, mockCustomAttributes);

        // Then
        assertEquals(id, metricUser.id());
        assertEquals(organisationIds, metricUser.organisationIds());
        assertEquals(name, metricUser.name());
        assertEquals(emailAddress, metricUser.emailAddress());
        assertEquals(phoneNumber, metricUser.phoneNumber());
        assertEquals(mockCustomAttributes, metricUser.customAttributes());
    }

    @Test
    public void shouldUpdateName_withName() {
        // Given
        final String id = randomId();
        final String organisationId = randomId();
        final String name = randomString();
        final String emailAddress = randomEmailAddress();
        final String phoneNumber = randomPhoneNumber();
        final MetricUser metricUser = new MetricUser(id, organisationId, name, emailAddress, phoneNumber);
        final String newName = randomString();

        // When
        metricUser.updateName(newName);

        // Then
        assertEquals(newName, metricUser.name());
    }

    @Test
    public void shouldUpdateEmailAddress_withEmailAddress() {
        // Given
        final String id = randomId();
        final String organisationId = randomId();
        final String name = randomString();
        final String emailAddress = randomEmailAddress();
        final String phoneNumber = randomPhoneNumber();
        final MetricUser metricUser = new MetricUser(id, organisationId, name, emailAddress, phoneNumber);
        final String newEmailAddress = randomEmailAddress();

        // When
        metricUser.updateEmailAddress(newEmailAddress);

        // Then
        assertEquals(newEmailAddress, metricUser.emailAddress());
    }

    @Test
    public void shouldUpdatePhoneNumber_withPhoneNumber() {
        // Given
        final String id = randomId();
        final String organisationId = randomId();
        final String name = randomString();
        final String emailAddress = randomEmailAddress();
        final String phoneNumber = randomPhoneNumber();
        final MetricUser metricUser = new MetricUser(id, organisationId, name, emailAddress, phoneNumber);
        final String newPhoneNumber = randomPhoneNumber();

        // When
        metricUser.updatePhoneNumber(newPhoneNumber);

        // Then
        assertEquals(newPhoneNumber, metricUser.phoneNumber());
    }
}
