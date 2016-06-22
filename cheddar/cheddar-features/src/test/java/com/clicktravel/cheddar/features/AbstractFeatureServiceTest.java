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
package com.clicktravel.cheddar.features;

import static com.clicktravel.common.random.Randoms.randomId;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import com.clicktravel.cheddar.features.StubFeatureService.StubFeature;

public class AbstractFeatureServiceTest {

    private StubFeatureService featureService;

    @Before
    public void setup() {
        featureService = new StubFeatureService();
    }

    @Test
    public void shouldEnableDefaultFeatures_withNullFeaturesContext() throws Exception {
        // Given
        featureService.setFeatures(null, Sets.newSet(StubFeature.FEATURE1));
        FeaturesContextHolder.set(null);

        // When
        final boolean enabled = featureService.isEnabled(StubFeature.FEATURE1);

        // Then
        assertTrue(enabled);
    }

    @Test
    public void shouldEnableFeature_withFeaturesContext() throws Exception {
        // Given
        final String featureSetId = randomId();
        featureService.setFeatures(null, Sets.newSet());
        featureService.setFeatures(featureSetId, Sets.newSet(StubFeature.FEATURE1));
        FeaturesContextHolder.set(new FeaturesContext(featureSetId));

        // When
        final boolean enabled = featureService.isEnabled(StubFeature.FEATURE1);

        // Then
        assertTrue(enabled);
    }

    @Test
    public void shouldNotEnableFeature_withIncorrectFeaturesContext() throws Exception {
        // Given
        final String featureSetId = randomId();
        featureService.setFeatures(null, Sets.newSet());
        featureService.setFeatures(featureSetId, Sets.newSet(StubFeature.FEATURE1));
        FeaturesContextHolder.set(new FeaturesContext(randomId()));

        // When
        final boolean enabled = featureService.isEnabled(StubFeature.FEATURE1);

        // Then
        assertFalse(enabled);
    }

    @Test
    public void shouldNotEnableNonDefaultFeature_withNullFeaturesContext() throws Exception {
        // Given
        final String featureSetId = randomId();
        featureService.setFeatures(null, Sets.newSet(StubFeature.FEATURE1));
        featureService.setFeatures(featureSetId, Sets.newSet(StubFeature.FEATURE2));
        FeaturesContextHolder.set(null);

        // When
        final boolean enabled = featureService.isEnabled(StubFeature.FEATURE2);

        // Then
        assertFalse(enabled);
    }

}
