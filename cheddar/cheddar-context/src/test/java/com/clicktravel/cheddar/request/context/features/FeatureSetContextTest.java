package com.clicktravel.cheddar.request.context.features;

import static com.clicktravel.common.random.Randoms.randomId;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class FeatureSetContextTest {

    @Test
    public void shouldCreateFeatureSetContext_withFeatureSetId() throws Exception {
        // Given
        final String featureSetId = randomId();

        // When
        final FeatureSetContext featureSetContext = new FeatureSetContext(featureSetId);

        // Then
        assertNotNull(featureSetContext);
        assertThat(featureSetContext.featureSetId(), is(featureSetId));
    }

}
