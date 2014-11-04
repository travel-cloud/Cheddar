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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Feature Registry to manage Features and report on their status: on or off.
 * 
 */
public class FeatureRegistry {

    private static Properties PROPERTIES = new Properties();
    private static FeaturePropertyAdapter FEATURE_PROPERTY_ADAPTER;
    private static final Map<Feature, Boolean> FEATURES = new HashMap<>();
    private static boolean initialized;

    /**
     * Initialises the FeatureRegistry with a class path location from which to load properties for the feature toggles.
     * 
     * The DefaultFeaturePropertyAdapter is used to translate from Feature to property key.
     * 
     * @param featureTogglePropertiesLocation The class path location of the properties file from which to read feature
     *            toggle properties
     */
    public static void init(final String featureTogglePropertiesLocation) {
        init(featureTogglePropertiesLocation, new DefaultFeaturePropertyAdapter());
    }

    /**
     * Initialises the FeatureRegistry with a classpath location from which to load properties for the feature toggles.
     * 
     * The DefaultFeaturePropertyAdapter is used to translate from Feature to property key.
     * 
     * @param featureTogglePropertiesLocation The class path location of the properties file from which to read feature
     *            toggle properties
     * @param featurePropertyAdapter The FeaturePropertyAdapter which is to be used to translate from Feature to
     *            property key.
     */
    public static void init(final String featureTogglePropertiesLocation,
            final FeaturePropertyAdapter featurePropertyAdapter) {
        if (initialized) {
            throw new IllegalStateException("Feature Registry already initialized");
        }
        initialized = true;
        FEATURE_PROPERTY_ADAPTER = featurePropertyAdapter;
        try {
            PROPERTIES
                    .load(FeatureRegistry.class.getClassLoader().getResourceAsStream(featureTogglePropertiesLocation));
        } catch (final IOException e) {
            throw new IllegalStateException("Could not load feature toggle properties from location: "
                    + featureTogglePropertiesLocation, e);
        }
    }

    /**
     * Reports the current status (on/off) for the given Feature
     * 
     * @param feature The Feature for which the status is to be reported.
     * @return The status of the Feature (true = on, false = off)
     */
    public static boolean isEnabled(final Feature feature) {
        if (FEATURES.get(feature) == null) {
            final String property = FEATURE_PROPERTY_ADAPTER.toPropertyKey(feature);
            final String toggleValue = PROPERTIES.getProperty(property);
            final boolean featureEnabled = toggleValue != null && toggleValue.equalsIgnoreCase("true");
            FEATURES.put(feature, featureEnabled);
        }
        return FEATURES.get(feature);
    }

}
