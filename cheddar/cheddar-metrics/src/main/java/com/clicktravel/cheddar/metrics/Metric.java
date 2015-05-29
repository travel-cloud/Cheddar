package com.clicktravel.cheddar.metrics;
import java.util.HashMap;
import java.util.Map;

public class Metric {

    private final String userId;
    private final String name;
    private final Map<String, Object> metaData;

    public Metric(final String userId, final String name) {
        this.userId = userId;
        this.name = name;
        metaData = new HashMap<>();
    }

    public void addMetric(final String key, final Object value) {
        metaData.put(key, value);
    }

    public String userId() {
        return userId;
    }

    public String name() {
        return name;
    }

    public Map<String, Object> metaData() {
        return metaData;
    }

}
