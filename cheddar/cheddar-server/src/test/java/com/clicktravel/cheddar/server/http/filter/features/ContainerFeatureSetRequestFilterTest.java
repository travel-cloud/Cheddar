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
package com.clicktravel.cheddar.server.http.filter.features;

import static com.clicktravel.common.random.Randoms.randomId;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.clicktravel.cheddar.features.FeaturesContext;
import com.clicktravel.cheddar.features.FeaturesContextHolder;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FeaturesContextHolder.class })
@PowerMockIgnore("javax.xml.*")
public class ContainerFeatureSetRequestFilterTest {

    private static final String FEATURE_SET_ID_HEADER = "Feature-Set-Id";

    @Test
    public void shouldSetFeatureSet_withFeatureSetHeader() throws Exception {
        // Given
        mockStatic(FeaturesContextHolder.class);
        final String featureSetId = randomId();
        final ContainerRequestContext mockContainerRequestContext = mock(ContainerRequestContext.class);
        final MultivaluedMap<String, String> headersMap = new MultivaluedHashMap<>();
        headersMap.add(FEATURE_SET_ID_HEADER, featureSetId);
        when(mockContainerRequestContext.getHeaders()).thenReturn(headersMap);
        final ContainerFeatureSetRequestFilter containerFeatureSetRequestFilter = new ContainerFeatureSetRequestFilter();

        // When
        containerFeatureSetRequestFilter.filter(mockContainerRequestContext);

        // Then
        final ArgumentCaptor<FeaturesContext> featuresContextCaptor = ArgumentCaptor.forClass(FeaturesContext.class);
        verifyStatic(FeaturesContextHolder.class);
        FeaturesContextHolder.set(featuresContextCaptor.capture());
        assertThat(featuresContextCaptor.getValue().featureSetId(), is(featureSetId));
    }

    @Test
    public void shouldNotSetFeatureSet_withNoHeader() throws Exception {
        // Given
        mockStatic(FeaturesContextHolder.class);
        final ContainerRequestContext mockContainerRequestContext = mock(ContainerRequestContext.class);
        final MultivaluedMap<String, String> headersMap = new MultivaluedHashMap<>();
        when(mockContainerRequestContext.getHeaders()).thenReturn(headersMap);
        final ContainerFeatureSetRequestFilter containerFeatureSetRequestFilter = new ContainerFeatureSetRequestFilter();

        // When
        containerFeatureSetRequestFilter.filter(mockContainerRequestContext);

        // Then
        verifyStatic(FeaturesContextHolder.class);
        FeaturesContextHolder.clear();
    }
}
