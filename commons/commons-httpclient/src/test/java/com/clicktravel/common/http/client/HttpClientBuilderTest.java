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
package com.clicktravel.common.http.client;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.clicktravel.common.random.Randoms;

public class HttpClientBuilderTest {

    @Test
    public void shouldBuildHttpClient_withBaseUrl() throws Exception {
        // Given
        final String baseUri = "http://localhost:8080";

        // When
        final HttpClient httpClient = HttpClient.Builder.httpClient().withBaseUri(baseUri).build();

        // Then
        assertNotNull(httpClient);
    }

    @Test
    public void shouldNotBuildHttpClient_withoutBaseUrl() throws Exception {
        // Given

        // When
        IllegalStateException expectedException = null;
        try {
            HttpClient.Builder.httpClient().build();
        } catch (final IllegalStateException e) {
            expectedException = e;
        }

        // Then
        assertNotNull(expectedException);
    }

    @Test
    public void shouldBuildHttpClient_withBaseUrlAndUsername() throws Exception {
        // Given
        final String baseUri = "http://localhost:8080";
        final String username = Randoms.randomString(10);

        // When
        final HttpClient httpClient = HttpClient.Builder.httpClient().withBaseUri(baseUri).withUsername(username)
                .build();

        // Then
        assertNotNull(httpClient);
    }

}
