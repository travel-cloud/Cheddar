package com.clicktravel.cheddar.metrics;
public class MetricUser {

    private final String id;
    private final String organisationId;
    private final String name;

    public MetricUser(final String id, final String organisationId, final String name) {
        super();
        this.id = id;
        this.organisationId = organisationId;
        this.name = name;
    }

    public String id() {
        return id;
    }

    public String organisationId() {
        return organisationId;
    }

    public String name() {
        return name;
    }

}
