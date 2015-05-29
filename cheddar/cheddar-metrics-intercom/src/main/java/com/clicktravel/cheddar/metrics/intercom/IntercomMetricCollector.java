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

import io.intercom.api.Event;
import io.intercom.api.Intercom;

import com.clicktravel.cheddar.metrics.Metric;
import com.clicktravel.cheddar.metrics.MetricCollector;
import com.clicktravel.cheddar.metrics.MetricOrganisation;
import com.clicktravel.cheddar.metrics.MetricUser;

public class IntercomMetricCollector implements MetricCollector {
    private final String APP_ID;
    private final String API_KEY;

    public IntercomMetricCollector(final String appId, final String apiKey) {
        APP_ID = appId;
        API_KEY = apiKey;
    }

    @Override
    public void createOrganisation(final MetricOrganisation metricOrganisation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateOrganisation(final MetricOrganisation metricOrganisation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void createUser(final MetricUser user) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateUser(final MetricUser user) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendMetric(final Metric metric) {
        Intercom.setAppID(APP_ID);
        Intercom.setApiKey(API_KEY);

        final Event event = new Event().setEventName(metric.name()).setUserID(metric.userId())
                .setCreatedAt(System.currentTimeMillis());

        if (metric.metaData() != null) {
            for (final String key : metric.metaData().keySet()) {
                if (metric.metaData().get(key).getClass().equals(String.class)) {
                    event.putMetadata(key, (String) metric.metaData().get(key));
                }
                if (metric.metaData().get(key).getClass().equals(boolean.class)
                        || metric.metaData().get(key).getClass().equals(Boolean.class)) {
                    event.putMetadata(key, (Boolean) metric.metaData().get(key));
                }
                if (metric.metaData().get(key).getClass().equals(double.class)
                        || metric.metaData().get(key).getClass().equals(Double.class)) {
                    event.putMetadata(key, (Double) metric.metaData().get(key));
                }
                if (metric.metaData().get(key).getClass().equals(float.class)
                        || metric.metaData().get(key).getClass().equals(Float.class)) {
                    event.putMetadata(key, (Float) metric.metaData().get(key));
                }
                if (metric.metaData().get(key).getClass().equals(int.class)
                        || metric.metaData().get(key).getClass().equals(Integer.class)) {
                    event.putMetadata(key, (Integer) metric.metaData().get(key));
                }
                if (metric.metaData().get(key).getClass().equals(long.class)
                        || metric.metaData().get(key).getClass().equals(Long.class)) {
                    event.putMetadata(key, (Long) metric.metaData().get(key));
                }
            }
        }
        Event.create(event);

    }
}
