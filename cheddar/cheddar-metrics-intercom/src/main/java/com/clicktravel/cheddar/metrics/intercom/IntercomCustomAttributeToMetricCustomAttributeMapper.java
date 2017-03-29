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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.intercom.api.CustomAttribute;

@SuppressWarnings("rawtypes")
public class IntercomCustomAttributeToMetricCustomAttributeMapper
        implements Function<Map<String, CustomAttribute>, Map<String, Object>> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Map<String, Object> apply(final Map<String, CustomAttribute> intercomCustomAttributes) {
        if (intercomCustomAttributes == null) {
            return null;
        }
        final Map<String, Object> customAttributes = new HashMap<>();
        intercomCustomAttributes.entrySet().stream().forEach(entry -> {
            final String key = entry.getKey();
            final CustomAttribute value = entry.getValue();
            if (value == null || value.getValue() == null) {
                customAttributes.put(key, null);
            } else if (value.getValueClass().equals(Boolean.class)) {
                customAttributes.put(key, (value.booleanValue()));
            } else if (value.getValueClass().equals(Integer.class)) {
                customAttributes.put(key, (value.integerValue()));
            } else if (value.getValueClass().equals(Double.class)) {
                customAttributes.put(key, (value.doubleValue()));
            } else if (value.getValueClass().equals(Long.class)) {
                customAttributes.put(key, (value.longValue()));
            } else if (value.getValueClass().equals(Float.class)) {
                customAttributes.put(key, (value.floatValue()));
            } else if (value.getValueClass().equals(String.class)) {
                customAttributes.put(key, (value.textValue()));
            } else {
                logger.warn("Unsupported custom attribute class : {}", value.getValueClass().getSimpleName());
            }
        });
        return customAttributes;
    }
}
