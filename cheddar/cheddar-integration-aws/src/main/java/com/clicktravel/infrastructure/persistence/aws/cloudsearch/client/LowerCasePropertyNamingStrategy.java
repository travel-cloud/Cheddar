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
package com.clicktravel.infrastructure.persistence.aws.cloudsearch.client;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;

/**
 * A property naming strategy used by Object Mapper to correctly parse the JSON returned by Cloud Search into document
 * objects due to the lack of support for camel case. Only lower case index names are currently supported.
 *
 * @see com.fasterxml.jackson.databind.ObjectMapper#setPropertyNamingStrategy(PropertyNamingStrategy)
 *
 * @author james b
 */

public class LowerCasePropertyNamingStrategy extends PropertyNamingStrategy {

    private static final long serialVersionUID = 1L;

    @Override
    public String nameForField(final MapperConfig<?> config, final AnnotatedField field, final String defaultName) {
        return formattedFieldName(defaultName);

    }

    @Override
    public String nameForGetterMethod(final MapperConfig<?> config, final AnnotatedMethod method,
            final String defaultName) {
        return formattedFieldName(defaultName);
    }

    @Override
    public String nameForSetterMethod(final MapperConfig<?> config, final AnnotatedMethod method,
            final String defaultName) {
        return formattedFieldName(defaultName);
    }

    private String formattedFieldName(final String defaultFieldName) {
        return defaultFieldName.toLowerCase();
    }
}
