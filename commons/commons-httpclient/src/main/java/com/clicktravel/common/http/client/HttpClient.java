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

import static com.clicktravel.common.random.Randoms.randomId;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.*;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.oauth2.OAuth2ClientSupport;
import org.glassfish.jersey.client.oauth2.OAuth2CodeGrantFlow;
import org.glassfish.jersey.client.oauth2.OAuth2CodeGrantFlow.Phase;
import org.glassfish.jersey.client.oauth2.OAuth2Parameters;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.common.http.client.authentication.AuthenticationException;
import com.clicktravel.common.http.client.authentication.oauth2.OAuthConfiguration;

/**
 * A wrapper around Jersey's Client to perform basic, high-level HTTP interaction.
 */
public class HttpClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final WebTarget target;
    private final MediaType accept;
    private final MultivaluedHashMap<String, String> headers;
    private final MultivaluedHashMap<String, Object> clientHeaders;
    private final boolean ignoreSslErrors;

    /**
     * Private constructor to initiate a HttpClient with a base URL.
     *
     * @param baseUri The base URL to which all HTTP requests are to be made
     * @param ignoreSslErrors Flag can be set to true when any SSL-related errors should be ignored.
     * @param accept The media type which is acceptable for all request by this given HTTP client
     */
    private HttpClient(final String baseUri, final boolean ignoreSslErrors, final MediaType accept,
            final MultivaluedHashMap<String, String> headers) {
        final ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        this.ignoreSslErrors = ignoreSslErrors;
        if (this.ignoreSslErrors) {
            buildIgnoreSslErrorClient(clientBuilder);
        }
        final Client client = clientBuilder.withConfig(clientConfig()).build();
        client.register(MultiPartFeature.class);
        target = client.target(baseUri);
        clientHeaders = mapHeadersToClientHeaders(headers);
        this.headers = headers;
        this.accept = accept;
    }

    private MultivaluedHashMap<String, Object> mapHeadersToClientHeaders(
            final MultivaluedHashMap<String, String> headers) {
        final MultivaluedHashMap<String, Object> clientHeaders = new MultivaluedHashMap<>();
        for (final Entry<String, List<String>> entry : headers.entrySet()) {
            final List<Object> values = new ArrayList<>();
            values.addAll(entry.getValue());
            clientHeaders.put(entry.getKey(), values);
        }
        return clientHeaders;
    }

    /**
     * Private constructor to initiate HttpClient with base URL and HTTP basic authentication
     *
     * @param baseUri The base URL to which all HTTP requests are to be made
     * @param username The username part of HTTP basic authentication
     * @param password The password part of HTTP basic authentication
     * @param ignoreSslErrors Flag can be set to true when any SSL-related errors should be ignored.
     * @param accept The media type which is acceptable for all request by this given HTTP client
     */
    private HttpClient(final String baseUri, final String username, final String password,
            final boolean ignoreSslErrors, final MediaType accept, final MultivaluedHashMap<String, String> headers) {
        this(baseUri, ignoreSslErrors, accept, headers);
        target.register(HttpAuthenticationFeature.basicBuilder().credentials(username, password).build());
    }

    /**
     * Private constructor to initiate HttpClient with base URL and OAuth 2.0 authentication.
     *
     * @param baseUri The base URL to which all HTTP requests are to be made
     * @param accept The media type which is acceptable for all request by this given HTTP client
     * @param headers The header values found within the HTTP request
     * @param oauthConfiguration The configuration for OAuth authentication
     */

    private HttpClient(final String baseUri, final boolean ignoreSslErrors, final MediaType accept,
            final MultivaluedHashMap<String, String> headers, final OAuthConfiguration oauthConfiguration) {
        target = getOAuthAuthenticatedTarget(baseUri, oauthConfiguration);
        clientHeaders = mapHeadersToClientHeaders(headers);
        this.headers = headers;
        this.accept = accept;
        this.ignoreSslErrors = ignoreSslErrors;
    }

    private WebTarget getOAuthAuthenticatedTarget(final String baseUri, final OAuthConfiguration oauthConfiguration)
            throws AuthenticationException {
        final Client client = buildIgnoreSslErrorClient(ClientBuilder.newBuilder().withConfig(clientConfig())).build();
        final String state = randomId();
        final OAuth2CodeGrantFlow oauth2GrantFlow = OAuth2ClientSupport
                .authorizationCodeGrantFlowBuilder(oauthConfiguration.clientIdentifier(),
                        oauthConfiguration.authorisationUri(), oauthConfiguration.accessTokenUri()).client(client)
                .redirectUri(oauthConfiguration.redirectUri()).scope("Default")
                .refreshTokenUri(oauthConfiguration.refreshTokenUri())
                .property(Phase.ALL, OAuth2Parameters.STATE, state).build();

        final String oauthRedirectUrl = oauth2GrantFlow.start();

        logger.debug("The oauth redirect uri is: " + oauthRedirectUrl);

        URI authenticationServerUri = null;
        final Response response = client.target(oauthRedirectUrl).request().get();
        logger.debug("The response for get was : " + response.getStatus());
        authenticationServerUri = response.getLocation();

        final String authCode = oauthConfiguration.oauthUserAgent().authenticate(authenticationServerUri);

        oauth2GrantFlow.finish(authCode, state);
        return oauth2GrantFlow.getAuthorizedClient().target(baseUri);
    }

    /**
     * Private static method to build a Jersey client which ignores SSL errors
     * @param clientBuilder
     * @return The build Jersey client builder configured to ignore SSL errors
     */
    private static ClientBuilder buildIgnoreSslErrorClient(final ClientBuilder clientBuilder) {
        final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
            }

            @Override
            public void checkServerTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
            }
        } };
        try {
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final HostnameVerifier verifier = new HostnameVerifier() {
                @Override
                public boolean verify(final String string, final SSLSession ssls) {
                    return true;
                }
            };
            clientBuilder.sslContext(sslContext).hostnameVerifier(verifier);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
        return clientBuilder;
    }

    public MultivaluedHashMap<String, String> headers() {
        return headers;
    }

    /**
     * Returns the redirect from a GET request, or <code>null</code> if no redirect is available.
     *
     * @param path The path which wil be requested via GET
     * @param params The URL params which will be passed in the request
     * @return The URI for the redirect or <code>null</code> if there is no redirect
     */
    public URI getRedirectLocation(final String path, final MultivaluedHashMap<String, String> params) {
        try {
            requestBuilder(path, params).get(ClientResponse.class);
            return null;
        } catch (final RedirectionException e) {
            return e.getLocation();
        } catch (final NotAuthorizedException | ForbiddenException | NotFoundException e) {
            logger.debug(e.getMessage());
            return null;
        }
    }

    /**
     * Convenience method to return redirect URL without specifying any query parameters.
     *
     * @see#getRedirectLocation(String, Map)
     *
     * @param path The path which will be requested via GET
     * @return The URI returned for the redirect. Returns null if there is no redirect
     */
    public URI getRedirectLocation(final String path) {
        return getRedirectLocation(path, new MultivaluedHashMap<String, String>());
    }

    private Invocation.Builder requestBuilder(final String path, final MultivaluedHashMap<String, String> params) {
        if (path == null || params == null) {
            throw new IllegalStateException("Path or params cannot be null");
        }
        WebTarget newTarget = target.path(path);
        for (final Map.Entry<String, List<String>> entry : params.entrySet()) {
            newTarget = newTarget.queryParam(entry.getKey(), entry.getValue().toArray());
        }
        return newTarget.request().headers(clientHeaders);
    }

    /**
     * Convenience method to get hold of the client directly allowing you to call more complex methods on the client
     * such as a post entity with variant language headers
     *
     * @param path The path to which the request needs to be made, required
     * @param params The query parameters appended to the path, required but can be empty
     * @return
     */
    public Invocation.Builder getHttpClientBuilder(final String path, final MultivaluedHashMap<String, String> params) {
        return requestBuilder(path, params);
    }

    /**
     * Convenience method to make HTTP GET request to a given URL without specifying any query parameters.
     *
     * @see #get(String, Map)
     *
     * @param path The path to which the GET request needs to be made
     * @return The HTTP response which is returned as a result of the GET request
     */
    public Response get(final String path) {
        final Response response = get(path, new MultivaluedHashMap<String, String>());
        return response;
    }

    /**
     * Makes a HTTP GET request to a given URL
     *
     * @param path The path to which the GET request needs to be made
     * @return The HTTP response which is returned as a result of the GET request
     */
    public Response get(final String path, final MultivaluedHashMap<String, String> params) {
        final Response response = requestBuilder(path, params).accept(accept).get();
        return response;
    }

    /**
     * Convenience method to make HTTP DELETE request to a given URL without specifying any query parameters.
     *
     * @see #get(String, Map)
     *
     * @param path The path to which the DELETE request needs to be made
     * @return The HTTP response which is returned as a result of the DELETE request
     */
    public Response delete(final String path) {
        final Response response = delete(path, new MultivaluedHashMap<String, String>());
        return response;
    }

    /**
     * Makes a HTTP DELETE request to a given URL
     *
     * @param path The path to which the DELETE request needs to be made
     * @return The HTTP response which is returned as a result of the DELETE request
     */
    public Response delete(final String path, final MultivaluedHashMap<String, String> params) {
        final Response response = requestBuilder(path, params).accept(accept).delete();
        return response;
    }

    /**
     * Makes HTTP POST request with a specified entity
     *
     * @see #put(String, Object, MediaType)
     *
     * @param path The path to which the request should be made
     * @param entity The entity which is to be POSTed
     * @param contentType The content type of the entity to be POSTed
     * @return The HTTP response which is returned as a result of the POST request
     */
    public Response post(final String path, final Object entity, final MediaType contentType) {
        final Response response = requestBuilder(path, new MultivaluedHashMap<String, String>()).post(
                Entity.entity(entity, contentType));
        return response;
    }

    /**
     * Makes HTTP POST request with a specified entity
     *
     * @param path The path to which the request should be made
     * @param params The query parameters appended to the path
     * @param entity The entity which is to be POSTed
     * @param contentType The content type of the entity to be POSTed
     * @return The HTTP response which is returned as a result of the POST request
     */
    public Response post(final String path, final MultivaluedHashMap<String, String> params, final Object entity,
            final MediaType contentType) {
        final Response response = requestBuilder(path, params).post(Entity.entity(entity, contentType));
        return response;
    }

    /**
     * Makes HTTP PUT request with a specified entity
     *
     * @see post(String, Object, MediaType)
     *
     * @param path The path to which the request should be made
     * @param entity The entity which is to be PUT
     * @param contentType The content type of the entity to be PUT
     * @return The HTTP response which is returned as a result of the PUT request
     */
    public Response put(final String path, final Object entity, final MediaType contentType) {
        final Response response = requestBuilder(path, new MultivaluedHashMap<String, String>()).put(
                Entity.entity(entity, contentType));
        return response;
    }

    private ClientConfig clientConfig() {
        final ClientConfig clientConfig = new ClientConfig();
        clientConfig.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE);
        clientConfig.property(ClientProperties.CONNECT_TIMEOUT, 30000);
        clientConfig.property(ClientProperties.READ_TIMEOUT, 90000);
        return clientConfig;
    }

    /**
     * Builder class to ensure that the HttpClient is built with the correct parameters
     */
    public static class Builder {

        private String baseUri;
        private String username;
        private String password;
        private boolean ignoreSslErrors = false;
        private final MediaType acceptType = MediaType.WILDCARD_TYPE;
        private MultivaluedHashMap<String, String> headers = new MultivaluedHashMap<>();
        private OAuthConfiguration oauthConfiguration;

        public static Builder httpClient() {
            return new Builder();
        }

        public Builder withBaseUri(final String baseUri) {
            this.baseUri = baseUri;
            return this;
        }

        public Builder withUsername(final String username) {
            this.username = username;
            return this;
        }

        public Builder withPassword(final String password) {
            this.password = password;
            return this;
        }

        public Builder ignoreSslErrors() {
            ignoreSslErrors = true;
            return this;
        }

        public Builder withHeaders(final MultivaluedHashMap<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder withOauthConfig(final OAuthConfiguration oauthConfig) {
            oauthConfiguration = oauthConfig;
            return this;
        }

        public static HttpClient.Builder clone(final HttpClient httpClient) {
            final HttpClient.Builder httpClientBuilder = HttpClient.Builder.httpClient()
                    .withBaseUri(httpClient.target.getUri().toString()).withHeaders(httpClient.headers());
            if (httpClient.ignoreSslErrors) {
                httpClientBuilder.ignoreSslErrors();
            }
            // TODO Clone username and password
            return httpClientBuilder;
        }

        public HttpClient build() {
            if (baseUri == null) {
                throw new IllegalStateException("BaseURI must not be null");
            }
            if (oauthConfiguration != null) {
                return new HttpClient(baseUri, ignoreSslErrors, acceptType, headers, oauthConfiguration);
            } else if (username != null || password != null) {
                return new HttpClient(baseUri, username, password, ignoreSslErrors, acceptType, headers);
            } else {
                return new HttpClient(baseUri, ignoreSslErrors, acceptType, headers);
            }
        }

    }

}
