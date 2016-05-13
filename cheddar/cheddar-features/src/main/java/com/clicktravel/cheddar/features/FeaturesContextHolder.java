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

/**
 * Retains the features context for the scope of the current execution thread.
 */
public class FeaturesContextHolder {

    private final static ThreadLocal<FeaturesContext> FEATURES_CONTEXT = new ThreadLocal<FeaturesContext>() {
    };

    public static void set(final FeaturesContext featuresContext) {
        FEATURES_CONTEXT.set(featuresContext);
    }

    public static FeaturesContext get() {
        return FEATURES_CONTEXT.get();
    }

    public static void clear() {
        FEATURES_CONTEXT.remove();
    }

}
