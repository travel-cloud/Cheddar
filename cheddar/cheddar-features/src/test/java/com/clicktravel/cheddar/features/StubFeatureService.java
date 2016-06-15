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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mockito.internal.util.collections.Sets;

public class StubFeatureService extends AbstractFeatureService {

    public enum StubFeature implements Feature {
        FEATURE1,
        FEATURE2,
        FEATURE3
    }

    private final Map<String, Set<Feature>> features = new HashMap<>();

    @Override
    public Set<String> featureSetIds() {
        return Sets.newSet("FEATURE_SET_1", "FEATURE_SET_2", "FEATURE_SET_3");
    }

    @Override
    public Set<Feature> enabledFeatures(final String featureSetId) {
        final Set<Feature> features = this.features.get(featureSetId);
        if (features == null) {
            return Sets.newSet();
        }
        return features;
    }

    public void setFeatures(final String featureSetId, final Set<Feature> features) {
        this.features.put(featureSetId, features);
    }

}
