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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.metrics.*;
import com.clicktravel.common.functional.Equals;
import com.clicktravel.common.validation.Check;

import io.intercom.api.*;

public class IntercomMetricCollector implements MetricCollector {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private MetricCustomAttributeToIntercomCustomAttributeMapper metricToIntercomCustomAttributeMapper;
    private IntercomCustomAttributeToMetricCustomAttributeMapper intercomToMetricCustomAttributeMapper;

    public IntercomMetricCollector(final String personalAccessToken) {
        Intercom.setToken(personalAccessToken);
        metricToIntercomCustomAttributeMapper = new MetricCustomAttributeToIntercomCustomAttributeMapper();
        intercomToMetricCustomAttributeMapper = new IntercomCustomAttributeToMetricCustomAttributeMapper();
    }

    void setMetricToIntercomCustomAttributeMapper(
            final MetricCustomAttributeToIntercomCustomAttributeMapper customAttributeMapper) {
        metricToIntercomCustomAttributeMapper = customAttributeMapper;
    }

    void setIntercomToMetricCustomAttributeMapper(
            final IntercomCustomAttributeToMetricCustomAttributeMapper customAttributeMapper) {
        intercomToMetricCustomAttributeMapper = customAttributeMapper;
    }

    @Override
    public void createOrganisation(final MetricOrganisation organisation) {
        if (organisation == null) {
            return;
        }
        final Company company = new Company();
        company.setCompanyID(organisation.id());
        company.setName(organisation.name());
        company.setRemoteCreatedAt(organisation.createdAt().getMillis());
        try {
            Company.create(company);
        } catch (final Exception e) {
            logger.warn("Error creating an Intercom company: {} - {}", company, e.getMessage());
        }
    }

    @Override
    public void updateOrganisation(final MetricOrganisation organisation) {
        if (organisation == null) {
            return;
        }
        final Company company = Company.find(organisation.id());
        company.setName(organisation.name());
        try {
            Company.update(company);
        } catch (final Exception e) {
            logger.warn("Error updating an Intercom company: {} - {}", company, e.getMessage());
        }
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
        final User intercomUser = createIntercomUser(user);
        intercomUser.setSignedUpAt(DateTime.now().getMillis() / 1000);
        try {
            User.create(intercomUser);
        } catch (final Exception e) {
            logger.warn("Error creating a Intercom user: {} - {}", intercomUser, e.getMessage());
        }
    }

    @Override
    public void updateUser(final MetricUser user) {
        final User intercomUser = createIntercomUser(user);
        try {
            User.update(intercomUser);
        } catch (final Exception e) {
            logger.warn("Error updating a Intercom user: {} - {}", intercomUser, e.getMessage());
        }
    }

    @Override
    public void addCustomAttributesToUser(final String userId, final Map<String, Object> customAttributes) {
        try {
            final User intercomUser = findIntercomUserByUserId(userId);
            intercomUser.setCustomAttributes(metricToIntercomCustomAttributeMapper.apply(customAttributes));
            User.update(intercomUser);
        } catch (final Exception e) {
            logger.warn("Error adding custom attributes to  Intercom user with id: {} - {} ", userId, e.getMessage());
        }
    }

    @Override
    public void addOrganisationToUser(final String userId, final String organisationId) {
        try {
            final User intercomUser = findIntercomUserByUserId(userId);
            final Company company = new Company();
            company.setCompanyID(organisationId);

            intercomUser.addCompany(company);
            User.update(intercomUser);
        } catch (final Exception e) {
            throw new MetricOrganisationUpdateException(organisationId);
        }
    }

    @Override
    public void removeOrganisationFromUser(final String userId, final String organisationId) {
        try {
            final User intercomUser = findIntercomUserByUserId(userId);
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

    @Override
    public MetricUser getUser(final String userId) {
        Check.isNotNull("userId", userId);

        User intercomUser = null;
        try {
            intercomUser = findIntercomUserByUserId(userId);
        } catch (final Exception e) {
            logger.warn("Failed to get user with Id: {} from Intercom - {}", userId, e.getMessage());
            throw new MetricUserNotFoundException(userId);
        }

        if (intercomUser == null) {
            throw new MetricUserNotFoundException(userId);
        } else {
            return new MetricUser(intercomUser.getUserId(), getUserOrganisations(intercomUser), intercomUser.getName(),
                    intercomUser.getEmail(), getCustomAttributes(intercomUser));
        }

    }

    @Override
    public void convertExistingContactToUser(final String contactId, final MetricUser metricUser) {
        final User intercomUser = createIntercomUser(metricUser);
        try {
            final Contact contact = Contact.findByUserID(contactId);
            logger.debug("Converting existing Intercom contact {} with id {} to user: {} - {}", contact, contactId,
                    intercomUser);
            Contact.convert(contact, intercomUser);
        } catch (final Exception e) {
            logger.debug("Error converting existing Intercom contact to user: {}", e.getMessage());
            throw new MetricException(e.getMessage());
        }
    }

    private User createIntercomUser(final MetricUser user) {
        if (user == null) {
            return null;
        }

        final User intercomUser = new User();
        intercomUser.setId(user.id());
        intercomUser.setUserId(user.id());
        intercomUser.setName(user.name());
        intercomUser.setCustomAttributes(metricToIntercomCustomAttributeMapper.apply(user.customAttributes()));
        if (!Equals.isNullOrBlank(user.emailAddress())) {
            intercomUser.setEmail(user.emailAddress());
        }

        user.organisationIds().forEach(organisationId -> {
            final Company company = new Company();
            company.setCompanyID(organisationId);
            intercomUser.addCompany(company);
        });

        return intercomUser;
    }

    @SuppressWarnings("rawtypes")
    private Map<String, Object> getCustomAttributes(final User user) {
        final Map<String, CustomAttribute> customAttributes = user.getCustomAttributes();
        return intercomToMetricCustomAttributeMapper.apply(customAttributes);
    }

    private List<String> getUserOrganisations(final User user) {
        final CompanyCollection companyCollection = user.getCompanyCollection();
        final List<String> companies = new ArrayList<>();
        while (companyCollection.hasNext()) {
            companies.add(companyCollection.next().getCompanyID());
        }
        return companies;
    }

    private User findIntercomUserByUserId(final String travelCloudUserId) {
        final Map<String, String> params = new HashMap<>();
        params.put("user_id", travelCloudUserId);
        return User.find(params);
    }

}
