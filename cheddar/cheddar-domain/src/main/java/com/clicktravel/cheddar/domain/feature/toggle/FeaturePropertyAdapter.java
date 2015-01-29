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
package com.clicktravel.cheddar.domain.feature.toggle;

/**
 * Adapter to translate from a Feature to an associated Property key.
 *
 */
public interface FeaturePropertyAdapter {

    /**
     * Translates a Feature to a property which will be used to look up the value of the Feature toggle.
     *
     * The value of the property will be looked up to determine if the Feature is enabled/disabled. If the value is
     * "true" (case-insensitive) the feature will be enabled, otherwise the feature disabled.
     *
     * @param feature The feature for which the property key is to be determined.
     * @return The property key associated with the Feature
     */
    String toPropertyKey(Feature feature);

}
