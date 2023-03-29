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

import java.util.*;

public class MetricUser {

    private final String id;
    private final List<String> organisationIds;
    private String name;
    private String emailAddress;
    private String phoneNumber;
    private final Map<String, Object> customAttributes;

    public MetricUser(final String id, final String organisationId, final String name, final String emailAddress, final String phoneNumber) {
        super();
        this.id = id;
        organisationIds = organisationId != null ? Arrays.asList(organisationId) : new ArrayList<>();
        this.name = name;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        customAttributes = new HashMap<>();
    }

    public MetricUser(final String id, final List<String> organisationIds, final String name, final String emailAddress,
            final String phoneNumber, final Map<String, Object> customAttributes) {
        super();
        this.id = id;
        this.organisationIds = organisationIds;
        this.name = name;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.customAttributes = customAttributes;
    }

    public void updateName(final String name) {
        this.name = name;
    }

    public void updateEmailAddress(final String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void updatePhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String id() {
        return id;
    }

    public List<String> organisationIds() {
        return organisationIds;
    }

    public String organisationId() {
        return organisationIds != null ? organisationIds.get(0) : null;
    }

    public String name() {
        return name;
    }

    public String emailAddress() {
        return emailAddress;
    }

    public String phoneNumber() {
        return phoneNumber;
    }

    public Map<String, Object> customAttributes() {
        return customAttributes;
    }

}
