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

import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.metrics.Metric;
import com.clicktravel.cheddar.metrics.MetricCollector;
import com.clicktravel.cheddar.metrics.MetricOrganisation;
import com.clicktravel.cheddar.metrics.MetricUser;
import com.clicktravel.common.functional.Equals;

import io.intercom.api.*;

public class IntercomMetricCollector implements MetricCollector {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public IntercomMetricCollector(final String personalAccessToken) {
        Intercom.setToken(personalAccessToken);
    }

    @Override
    public void createOrganisation(final MetricOrganisation organisation) {
        createOrUpdateIntercomCompany(organisation);
    }

    @Override
    public void updateOrganisation(final MetricOrganisation organisation) {
        createOrUpdateIntercomCompany(organisation);
    }

    @Override
    public void tagOrganisation(final String tagName, final MetricOrganisation metricOrganisation) {
        try {
            Tag.tag(new Tag().setName(tagName), new Company().setCompanyID(metricOrganisation.id()));
        } catch (final Exception e) {
            logger.warn("Error tagging Intercom organisation: {} - {} ", metricOrganisation, e.getMessage());
        }
    }

    @Override
    public void createUser(final MetricUser user) {
        final User intercomUser = getIntercomUser(user);
        intercomUser.setSignedUpAt(DateTime.now().getMillis() / 1000);
        try {
            User.create(intercomUser);
        } catch (final Exception e) {
            logger.warn("Error creating a Intercom user: {} - {}", intercomUser, e.getMessage());
        }
    }

    @Override
    public void updateUser(final MetricUser user) {
        final User intercomUser = getIntercomUser(user);
        try {
            User.update(intercomUser);
        } catch (final Exception e) {
            logger.warn("Error updating a Intercom user: {} - {}", intercomUser, e.getMessage());
        }
    }

    @Override
    public void addCustomAttributesToUser(final String userId, final Map<String, Object> customAttributes) {
        try {
            final User intercomUser = User.find(userId);

            customAttributes.entrySet().stream().forEach(entry -> {
                final String key = entry.getKey();
                final Object value = entry.getValue();
                if (value.getClass().equals(Boolean.class)) {
                    intercomUser.addCustomAttribute(CustomAttribute.newBooleanAttribute(key, (Boolean) value));
                } else if (value.getClass().equals(Integer.class)) {
                    intercomUser.addCustomAttribute(CustomAttribute.newIntegerAttribute(key, (Integer) value));
                } else if (value.getClass().equals(Double.class)) {
                    intercomUser.addCustomAttribute(CustomAttribute.newDoubleAttribute(key, (Double) value));
                } else if (value.getClass().equals(Long.class)) {
                    intercomUser.addCustomAttribute(CustomAttribute.newLongAttribute(key, (Long) value));
                } else if (value.getClass().equals(Float.class)) {
                    intercomUser.addCustomAttribute(CustomAttribute.newFloatAttribute(key, (Float) value));
                } else if (value.getClass().equals(String.class)) {
                    intercomUser.addCustomAttribute(CustomAttribute.newStringAttribute(key, (String) value));
                } else {
                    logger.warn("Unsupported custom attribute class : {}", value.getClass().getSimpleName());
                }
            });
            User.update(intercomUser);
        } catch (final Exception e) {
            logger.warn("Error adding custom attributes to  Intercom user with id: {} - {} ", userId, e.getMessage());
        }
    }

    @Override
    public void addOrganisationToUser(final String userId, final String organisationId) {
        try {
            final User intercomUser = User.find(userId);
            final Company company = new Company();
            company.setCompanyID(organisationId);

            intercomUser.addCompany(company);
            User.update(intercomUser);
        } catch (final Exception e) {
            logger.warn("Error adding organisation with id: {} to  Intercom user with id: {} - {} ", organisationId,
                    userId, e.getMessage());
        }

    }

    @Override
    public void removeOrganisationFromUser(final String userId, final String organisationId) {
        try {
            final User intercomUser = User.find(userId);
            final Company company = new Company();
            company.setCompanyID(organisationId);

            intercomUser.removeCompany(company);
            User.update(intercomUser);
        } catch (final Exception e) {
            logger.warn("Error removing organisation with id: {} from  Intercom user with id: {} - {} ", organisationId,
                    userId, e.getMessage());
        }
    }

    @Override
    public void deleteUser(final String userId) {
        try {
            User.delete(userId);
        } catch (final Exception e) {
            logger.warn("Error deleting a Intercom user: {} - {}", userId, e.getMessage());
        }
    }

    @Override
    public void sendMetric(final Metric metric) {
        if (metric == null) {
            return;

        }

        try {
            final Event event = new Event().setEventName(metric.name()).setUserID(metric.userId())
                    .setCreatedAt(DateTime.now().getMillis() / 1000);

            if (metric.metaData() != null) {
                for (final String key : metric.metaData().keySet()) {
                    if (metric.metaData().get(key).getClass().equals(String.class)) {
                        event.putMetadata(key, (String) metric.metaData().get(key));
                    } else if (metric.metaData().get(key).getClass().equals(boolean.class)
                            || metric.metaData().get(key).getClass().equals(Boolean.class)) {
                        event.putMetadata(key, (Boolean) metric.metaData().get(key));
                    } else if (metric.metaData().get(key).getClass().equals(double.class)
                            || metric.metaData().get(key).getClass().equals(Double.class)) {
                        event.putMetadata(key, (Double) metric.metaData().get(key));
                    } else if (metric.metaData().get(key).getClass().equals(float.class)
                            || metric.metaData().get(key).getClass().equals(Float.class)) {
                        event.putMetadata(key, (Float) metric.metaData().get(key));
                    } else if (metric.metaData().get(key).getClass().equals(int.class)
                            || metric.metaData().get(key).getClass().equals(Integer.class)) {
                        event.putMetadata(key, (Integer) metric.metaData().get(key));
                    } else if (metric.metaData().get(key).getClass().equals(long.class)
                            || metric.metaData().get(key).getClass().equals(Long.class)) {
                        event.putMetadata(key, (Long) metric.metaData().get(key));
                    } else {
                        event.putMetadata(key, String.valueOf(metric.metaData().get(key)));
                    }
                }
            }
            Event.create(event);
        } catch (final Exception e) {
            logger.warn("Failed to send metric via Intercom", e);
        }
    }

    private User getIntercomUser(final MetricUser user) {
        if (user == null) {
            return null;
        }

        final User intercomUser = new User();
        intercomUser.setUserId(user.id());
        intercomUser.setName(user.name());

        if (!Equals.isNullOrBlank(user.emailAddress())) {
            intercomUser.setEmail(user.emailAddress());
        }

        if (!Equals.isNullOrBlank(user.organisationId())) {
            final Company company = new Company();
            company.setCompanyID(user.organisationId());
            intercomUser.addCompany(company);
        }

        return intercomUser;
    }

    private void createOrUpdateIntercomCompany(final MetricOrganisation organisation) {
        if (organisation == null) {
            return;
        }
        final Company company = new Company();

        final String companyId = organisation.id();
        final String name = organisation.name();

        company.setCompanyID(companyId);
        company.setName(name);
        try {
            Company.create(company);
        } catch (final Exception e) {
            logger.warn("Error creating/updating a Intercom company: {} - {}", company, e.getMessage());
        }
    }
}
