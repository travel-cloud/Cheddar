package com.clicktravel.cheddar.metrics;
public interface MetricCollector {

    void createOrganisation(MetricOrganisation metricOrganisation);

    void updateOrganisation(MetricOrganisation metricOrganisation);

    void createUser(MetricUser user);

    void updateUser(MetricUser user);

    void sendMetric(Metric metric);

}