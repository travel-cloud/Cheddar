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
package com.clicktravel.common.http.client.authentication.oauth2;

import org.glassfish.jersey.client.oauth2.ClientIdentifier;

/**
 * Configuration object used by HTTP client to setup the values needed for OAuth authentication. If used in the building
 * of a HttpClient the client with assume that it is meant for OAuth authentication and begin the flows based on the
 * values provided.
 */
public class OAuthConfiguration {
    private final ClientIdentifier clientIdentifier;
    private final OAuthUserAgent serviceAuthenticationClient;
    private final String authorisationUri;
    private final String accessTokenUri;
    private final String redirectUri;
    private final String refreshTokenUri;

    /**
     * @param clientIdentifier OAuth client identifier object
     * @param oauthServiceAuthenticationClient OAuth service authenticator for contacting authentication service in
     *            three-legged OAuth
     * @param authorisationUri The URL to which the OAuth 2.0 authorisation code request will be made to
     * @param accessTokenUri The URL to which the OAuth 2.0 access token request will be made to
     * @param redirectUri The URL to which the OAuth 2.0 server will expect to redirect to
     * @param refreshTokenUri The URL to which the OAuth 2.0 refresh token request will be made to
     */
    public OAuthConfiguration(final ClientIdentifier clientIdentifier,
            final OAuthUserAgent serviceAuthenticationClient, final String authorisationUri,
            final String accessTokenUri, final String redirectUri, final String refreshTokenUri) {
        this.clientIdentifier = clientIdentifier;
        this.serviceAuthenticationClient = serviceAuthenticationClient;
        this.authorisationUri = authorisationUri;
        this.accessTokenUri = accessTokenUri;
        this.redirectUri = redirectUri;
        this.refreshTokenUri = refreshTokenUri;
    }

    public ClientIdentifier clientIdentifier() {
        return clientIdentifier;
    }

    public OAuthUserAgent oauthUserAgent() {
        return serviceAuthenticationClient;
    }

    public String authorisationUri() {
        return authorisationUri;
    }

    public String accessTokenUri() {
        return accessTokenUri;
    }

    public String redirectUri() {
        return redirectUri;
    }

    public String refreshTokenUri() {
        return refreshTokenUri;
    }

}