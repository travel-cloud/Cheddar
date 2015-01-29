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

import java.net.URI;

import com.clicktravel.common.http.client.authentication.AuthenticationException;

/**
 * Should hold a set of credentials and the knowledge of how to authenticate to a given service that wishes to
 * participate in a three legged OAuth 2.0 authorisation code flow.
 */
public interface OAuthUserAgent {

    /**
     * Used to add the credentials needed to authenticate with the chosen service.
     *
     * @param username
     * @param password
     */
    void setCredentials(String username, String password);

    /**
     * Used to authenticate against the chosen service. The result of this authentication should be the OAuth 2.0
     * authorisation code (Authcode) received by the participating service from the OAuth 2.0 server.
     *
     * @return Authcode: OAuth 2.0 Authorisation code from the successful authentication of the resource owner
     *         credentials.
     * @throws AuthenticationException
     */
    String authenticate(URI authenticationServerUri) throws AuthenticationException;

}
