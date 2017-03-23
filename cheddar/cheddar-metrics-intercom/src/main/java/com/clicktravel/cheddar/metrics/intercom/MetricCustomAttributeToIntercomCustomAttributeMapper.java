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
public class MetricCustomAttributeToIntercomCustomAttributeMapper
        implements Function<Map<String, Object>, Map<String, CustomAttribute>> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Map<String, CustomAttribute> apply(final Map<String, Object> metricCustomAttributes) {
        if (metricCustomAttributes == null) {
            return null;
        }
        final Map<String, CustomAttribute> customAttributes = new HashMap<>();
        metricCustomAttributes.entrySet().stream().forEach(entry -> {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (value.getClass().equals(Boolean.class)) {
                customAttributes.put(key, CustomAttribute.newBooleanAttribute(key, (Boolean) value));
            } else if (value.getClass().equals(Integer.class)) {
                customAttributes.put(key, CustomAttribute.newIntegerAttribute(key, (Integer) value));
            } else if (value.getClass().equals(Double.class)) {
                customAttributes.put(key, CustomAttribute.newDoubleAttribute(key, (Double) value));
            } else if (value.getClass().equals(Long.class)) {
                customAttributes.put(key, CustomAttribute.newLongAttribute(key, (Long) value));
            } else if (value.getClass().equals(Float.class)) {
                customAttributes.put(key, CustomAttribute.newFloatAttribute(key, (Float) value));
            } else if (value.getClass().equals(String.class)) {
                customAttributes.put(key, CustomAttribute.newStringAttribute(key, (String) value));
            } else {
                logger.warn("Unsupported custom attribute class : {}", value.getClass().getSimpleName());
            }
        });
        return customAttributes;
    }
}
