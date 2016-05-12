package com.clicktravel.cheddar.request.context.features;

public class FeatureSetContext {

    private final String featureSetId;

    public FeatureSetContext(final String featureSetId) {
        this.featureSetId = featureSetId;
    }

    public String featureSetId() {
        return featureSetId;
    }

}
