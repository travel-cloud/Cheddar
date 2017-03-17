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

import java.util.List;
import java.util.Map;

public class MetricUser {

    private final String id;
    private final List<String> organisationIds;
    private final String name;
    private final String emailAddress;
    private final Map<String, Object> customAttributes;

    public MetricUser(final String id, final List<String> organisationIds, final String name, final String emailAddress,
            final Map<String, Object> customAttributes) {
        super();
        this.id = id;
        this.organisationIds = organisationIds;
        this.name = name;
        this.emailAddress = emailAddress;
        this.customAttributes = customAttributes;
    }

    public String id() {
        return id;
    }

    public List<String> organisationIds() {
        return organisationIds;
    }

    public String name() {
        return name;
    }

    public String emailAddress() {
        return emailAddress;
    }

    public Map<String, Object> customAttributes() {
        return customAttributes;
    }

}
