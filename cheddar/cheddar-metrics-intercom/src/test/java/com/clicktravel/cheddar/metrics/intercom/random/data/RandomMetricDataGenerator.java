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
package com.clicktravel.cheddar.metrics.intercom.random.data;

import static com.clicktravel.common.random.Randoms.*;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.clicktravel.cheddar.metrics.MetricOrganisation;
import com.clicktravel.cheddar.metrics.MetricUser;

import io.intercom.api.CustomAttribute;

public class RandomMetricDataGenerator {

    public static MetricOrganisation randomMetricOrganisationWithCreatedAt() {
        return randomMetricOrganisation(true);
    }

    public static MetricOrganisation randomMetricOrganisation() {
        return randomMetricOrganisation(false);
    }

    public static MetricOrganisation randomMetricOrganisation(final boolean setCreatedAt) {
        final MetricOrganisation metricOrganisation = new MetricOrganisation(randomId(), randomString());
        if (setCreatedAt) {
            metricOrganisation.setCreatedAt(randomDateTime());
        }
        return metricOrganisation;
    }

    public static MetricUser randomMetricUser() {
        final int numberOfOrganisations = randomIntInRange(1, 10);
        final List<String> organisationIds = new ArrayList<>();
        for (int i = 0; i < numberOfOrganisations; i++) {
            organisationIds.add(randomId());
        }
        return new MetricUser(randomId(), organisationIds, randomString(), randomEmailAddress(),
                randomMetricCustomAttributes());
    }

    public static Map<String, Object> randomMetricCustomAttributes() {
        final Map<String, Object> customAttributes = new HashMap<>();
        final int numberOfCustomAttributes = randomIntInRange(1, 10);
        for (int i = 0; i < numberOfCustomAttributes; i++) {
            final String key = randomString();
            final Object mockCustomAttribute = mock(CustomAttribute.class);
            customAttributes.put(key, mockCustomAttribute);
        }

        return customAttributes;
    }

}
