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
package com.clicktravel.infrastructure.persistence.aws.cloudsearch.client;

import static com.clicktravel.common.random.Randoms.randomString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.cloudsearchv2.AmazonCloudSearchClient;
import com.amazonaws.services.cloudsearchv2.model.DescribeDomainsResult;
import com.amazonaws.services.cloudsearchv2.model.DomainStatus;
import com.amazonaws.services.cloudsearchv2.model.ServiceEndpoint;
import com.clicktravel.common.http.client.HttpClient;
import com.clicktravel.common.random.Randoms;
import com.clicktravel.common.validation.ValidationException;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.Document;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.query.Query;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.query.QueryType;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.QueryBuilder;

@SuppressWarnings({ "unchecked", "rawtypes" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ AmazonCloudSearchClient.class, HttpClient.class, CloudSearchClient.class, QueryBuilder.class,
        JsonDocumentSearchResponseUnmarshaller.class })
public class CloudSearchClientTest {

    private BatchDocumentUpdateRequest mockBatchDocumentUpdateRequest;
    private DescribeDomainsResult mockDescribeDomainResult;
    private Response mockResponse;
    private AWSCredentials mockAwsCredentials;
    private AmazonCloudSearchClient mockAmazonCloudSearchClient;
    private HttpClient mockHttpClient;
    private JsonDocumentUpdateMarshaller mockJsonDocumentMarshaller;
    private QueryBuilder mockStructuredQueryBuilder;
    private JsonDocumentSearchResponseUnmarshaller mockJsonDocumentSearchResponseUnmarshaller;

    @Before
    public void setUp() throws Exception {
        mockAmazonCloudSearchClient = mock(AmazonCloudSearchClient.class);
        mockHttpClient = mock(HttpClient.class);
        mockJsonDocumentMarshaller = mock(JsonDocumentUpdateMarshaller.class);
        mockStructuredQueryBuilder = mock(QueryBuilder.class);
        mockJsonDocumentSearchResponseUnmarshaller = mock(JsonDocumentSearchResponseUnmarshaller.class);
        PowerMockito.whenNew(AmazonCloudSearchClient.class).withAnyArguments().thenReturn(mockAmazonCloudSearchClient);
        PowerMockito.whenNew(HttpClient.class).withAnyArguments().thenReturn(mockHttpClient);
        PowerMockito.whenNew(JsonDocumentUpdateMarshaller.class).withAnyArguments()
                .thenReturn(mockJsonDocumentMarshaller);
        PowerMockito.whenNew(QueryBuilder.class).withAnyArguments().thenReturn(mockStructuredQueryBuilder);
        PowerMockito.whenNew(JsonDocumentSearchResponseUnmarshaller.class).withAnyArguments()
                .thenReturn(mockJsonDocumentSearchResponseUnmarshaller);
        mockBatchDocumentUpdateRequest = mock(BatchDocumentUpdateRequest.class);
        mockDescribeDomainResult = mock(DescribeDomainsResult.class);
        mockResponse = mock(Response.class);
        mockAwsCredentials = mock(AWSCredentials.class);
    }

    @After
    public void tearDown() {
        reset(mockAmazonCloudSearchClient, mockHttpClient, mockJsonDocumentMarshaller, mockBatchDocumentUpdateRequest,
                mockDescribeDomainResult, mockResponse, mockAwsCredentials, mockStructuredQueryBuilder,
                mockJsonDocumentSearchResponseUnmarshaller);
    }

    @Test
    public void shouldUploadBatchDocumentUpdate_withValidBatchDocumentUpdateRequest() {
        // Given
        final int responseStatusCode = Response.Status.OK.getStatusCode();
        final DocumentUpdate documentUpdate = randomDocumentUpdate();
        final String jsonBatchDocumentUpdateRequest = randomString();
        final String domainName = Randoms.randomString();
        final DomainStatus domainStatus = randomDomainStatusWithName(domainName);
        final List<DomainStatus> listOfDomainStatus = Arrays.asList(domainStatus);
        final List<DocumentUpdate> documentUpdateList = Arrays.asList(documentUpdate);

        when(mockAmazonCloudSearchClient.describeDomains()).thenReturn(mockDescribeDomainResult);
        when(mockDescribeDomainResult.getDomainStatusList()).thenReturn(listOfDomainStatus);
        when(mockBatchDocumentUpdateRequest.getSearchDomain()).thenReturn(domainName);
        when(mockBatchDocumentUpdateRequest.getDocumentUpdates()).thenReturn(documentUpdateList);
        when(mockJsonDocumentMarshaller.marshall(Mockito.anyCollectionOf(DocumentUpdate.class))).thenReturn(
                jsonBatchDocumentUpdateRequest);
        when(mockHttpClient.post(anyString(), anyString(), any(MediaType.class))).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(responseStatusCode);

        final CloudSearchClient cloudSearchClient = new CloudSearchClient(mockAwsCredentials);
        cloudSearchClient.initialize();

        // When
        cloudSearchClient.batchDocumentUpdate(mockBatchDocumentUpdateRequest);

        // Then
        verify(mockAmazonCloudSearchClient).describeDomains();
        verify(mockDescribeDomainResult).getDomainStatusList();
        verify(mockBatchDocumentUpdateRequest).getSearchDomain();
        verify(mockBatchDocumentUpdateRequest).getDocumentUpdates();
        verify(mockJsonDocumentMarshaller).marshall(documentUpdateList);
        verify(mockHttpClient).post("/batch", jsonBatchDocumentUpdateRequest, MediaType.APPLICATION_JSON_TYPE);
        verify(mockResponse).getStatus();
    }

    @Test
    public void shouldNotUploadBatchDocumentUpdate_withInvalidBatchDocumentUpdateRequest() throws Exception {
        // Given
        final String errorText = Randoms.randomString();
        final String jsonResponseError = String.format("{\"error\" : { \"message\" : \"%s\" }}", errorText);
        final int responseStatusCode = Response.Status.BAD_REQUEST.getStatusCode();
        final DocumentUpdate documentUpdate = randomDocumentUpdate();
        final String jsonBatchDocumentUpdateRequest = randomString();
        final String domainName = Randoms.randomString();
        final DomainStatus domainStaus = randomDomainStatusWithName(domainName);
        final List<DomainStatus> listOfDomainStatus = Arrays.asList(domainStaus);
        final List<DocumentUpdate> documentUpdateList = Arrays.asList(documentUpdate);

        when(mockAmazonCloudSearchClient.describeDomains()).thenReturn(mockDescribeDomainResult);
        when(mockDescribeDomainResult.getDomainStatusList()).thenReturn(listOfDomainStatus);
        when(mockBatchDocumentUpdateRequest.getSearchDomain()).thenReturn(domainName);
        when(mockBatchDocumentUpdateRequest.getDocumentUpdates()).thenReturn(documentUpdateList);
        when(mockJsonDocumentMarshaller.marshall(Mockito.anyCollectionOf(DocumentUpdate.class))).thenReturn(
                jsonBatchDocumentUpdateRequest);
        when(mockHttpClient.post(anyString(), anyString(), any(MediaType.class))).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(responseStatusCode);
        when(mockResponse.readEntity(any(Class.class))).thenReturn(jsonResponseError);

        final CloudSearchClient cloudSearchClient = new CloudSearchClient(mockAwsCredentials);
        cloudSearchClient.initialize();

        // When
        AmazonServiceException expectedException = null;
        try {
            cloudSearchClient.batchDocumentUpdate(mockBatchDocumentUpdateRequest);
        } catch (final AmazonServiceException e) {
            expectedException = e;
        }

        // Then
        verify(mockAmazonCloudSearchClient).describeDomains();
        verify(mockDescribeDomainResult).getDomainStatusList();
        verify(mockBatchDocumentUpdateRequest).getSearchDomain();
        verify(mockBatchDocumentUpdateRequest).getDocumentUpdates();
        verify(mockJsonDocumentMarshaller).marshall(documentUpdateList);
        verify(mockHttpClient).post("/batch", jsonBatchDocumentUpdateRequest, MediaType.APPLICATION_JSON_TYPE);
        verify(mockResponse).getStatus();

        assertNotNull(expectedException);
        assertTrue(expectedException.getMessage().contains(errorText));
    }

    @Test
    public void shouldNotUploadBatchDocumentUpdate_withIncorrectAccessLevel() {
        // Given
        final int responseStatusCode = Response.Status.FORBIDDEN.getStatusCode();
        final DocumentUpdate documentUpdate = randomDocumentUpdate();
        final String jsonBatchDocumentUpdateRequest = randomString();
        final String domainName = Randoms.randomString();
        final DomainStatus domainStaus = randomDomainStatusWithName(domainName);
        final List<DomainStatus> listOfDomainStatus = Arrays.asList(domainStaus);
        final List<DocumentUpdate> documentUpdateList = Arrays.asList(documentUpdate);

        when(mockAmazonCloudSearchClient.describeDomains()).thenReturn(mockDescribeDomainResult);
        when(mockDescribeDomainResult.getDomainStatusList()).thenReturn(listOfDomainStatus);
        when(mockBatchDocumentUpdateRequest.getSearchDomain()).thenReturn(domainName);
        when(mockBatchDocumentUpdateRequest.getDocumentUpdates()).thenReturn(documentUpdateList);
        when(mockJsonDocumentMarshaller.marshall(Mockito.anyCollectionOf(DocumentUpdate.class))).thenReturn(
                jsonBatchDocumentUpdateRequest);
        when(mockHttpClient.post(anyString(), anyString(), any(MediaType.class))).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(responseStatusCode);

        final CloudSearchClient cloudSearchClient = new CloudSearchClient(mockAwsCredentials);
        cloudSearchClient.initialize();

        // When
        AmazonServiceException expectedException = null;
        try {
            cloudSearchClient.batchDocumentUpdate(mockBatchDocumentUpdateRequest);
        } catch (final AmazonServiceException e) {
            expectedException = e;
        }

        // Then
        verify(mockAmazonCloudSearchClient).describeDomains();
        verify(mockDescribeDomainResult).getDomainStatusList();
        verify(mockBatchDocumentUpdateRequest).getSearchDomain();
        verify(mockBatchDocumentUpdateRequest).getDocumentUpdates();
        verify(mockJsonDocumentMarshaller).marshall(documentUpdateList);
        verify(mockHttpClient).post("/batch", jsonBatchDocumentUpdateRequest, MediaType.APPLICATION_JSON_TYPE);
        verify(mockResponse).getStatus();

        assertNotNull(expectedException);
        assertTrue(expectedException.getMessage().contains("Access"));
    }

    @Test
    public void shouldNotUploadBatchDocumentUpdate_withServiceFailure() {
        // Given
        final int responseStatusCode = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        final DocumentUpdate documentUpdate = randomDocumentUpdate();
        final String jsonBatchDocumentUpdateRequest = randomString();
        final String domainName = Randoms.randomString();
        final DomainStatus domainStaus = randomDomainStatusWithName(domainName);
        final List<DomainStatus> listOfDomainStatus = Arrays.asList(domainStaus);
        final List<DocumentUpdate> documentUpdateList = Arrays.asList(documentUpdate);

        when(mockAmazonCloudSearchClient.describeDomains()).thenReturn(mockDescribeDomainResult);
        when(mockDescribeDomainResult.getDomainStatusList()).thenReturn(listOfDomainStatus);
        when(mockBatchDocumentUpdateRequest.getSearchDomain()).thenReturn(domainName);
        when(mockBatchDocumentUpdateRequest.getDocumentUpdates()).thenReturn(documentUpdateList);
        when(mockJsonDocumentMarshaller.marshall(Mockito.anyCollectionOf(DocumentUpdate.class))).thenReturn(
                jsonBatchDocumentUpdateRequest);
        when(mockHttpClient.post(anyString(), anyString(), any(MediaType.class))).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(responseStatusCode);

        final CloudSearchClient cloudSearchClient = new CloudSearchClient(mockAwsCredentials);
        cloudSearchClient.initialize();

        // When
        AmazonServiceException expectedException = null;
        try {
            cloudSearchClient.batchDocumentUpdate(mockBatchDocumentUpdateRequest);
        } catch (final AmazonServiceException e) {
            expectedException = e;
        }

        // Then
        verify(mockAmazonCloudSearchClient).describeDomains();
        verify(mockDescribeDomainResult).getDomainStatusList();
        verify(mockBatchDocumentUpdateRequest).getSearchDomain();
        verify(mockBatchDocumentUpdateRequest).getDocumentUpdates();
        verify(mockJsonDocumentMarshaller).marshall(documentUpdateList);
        verify(mockHttpClient).post("/batch", jsonBatchDocumentUpdateRequest, MediaType.APPLICATION_JSON_TYPE);
        verify(mockResponse).getStatus();

        assertNotNull(expectedException);
    }

    @Test
    public void shouldNotUploadBatchDocumentUpdate_withUnknownServiceFailure() {
        // Given
        final int responseStatusCode = Response.Status.GATEWAY_TIMEOUT.getStatusCode();
        final DocumentUpdate documentUpdate = randomDocumentUpdate();
        final String jsonBatchDocumentUpdateRequest = randomString();
        final String domainName = Randoms.randomString();
        final DomainStatus domainStaus = randomDomainStatusWithName(domainName);
        final List<DomainStatus> listOfDomainStatus = Arrays.asList(domainStaus);
        final List<DocumentUpdate> documentUpdateList = Arrays.asList(documentUpdate);

        when(mockAmazonCloudSearchClient.describeDomains()).thenReturn(mockDescribeDomainResult);
        when(mockDescribeDomainResult.getDomainStatusList()).thenReturn(listOfDomainStatus);
        when(mockBatchDocumentUpdateRequest.getSearchDomain()).thenReturn(domainName);
        when(mockBatchDocumentUpdateRequest.getDocumentUpdates()).thenReturn(documentUpdateList);
        when(mockJsonDocumentMarshaller.marshall(Mockito.anyCollectionOf(DocumentUpdate.class))).thenReturn(
                jsonBatchDocumentUpdateRequest);
        when(mockHttpClient.post(anyString(), anyString(), any(MediaType.class))).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(responseStatusCode);

        final CloudSearchClient cloudSearchClient = new CloudSearchClient(mockAwsCredentials);
        cloudSearchClient.initialize();

        // When
        AmazonServiceException expectedException = null;
        try {
            cloudSearchClient.batchDocumentUpdate(mockBatchDocumentUpdateRequest);
        } catch (final AmazonServiceException e) {
            expectedException = e;
        }

        // Then
        verify(mockAmazonCloudSearchClient).describeDomains();
        verify(mockDescribeDomainResult).getDomainStatusList();
        verify(mockBatchDocumentUpdateRequest).getSearchDomain();
        verify(mockBatchDocumentUpdateRequest).getDocumentUpdates();
        verify(mockJsonDocumentMarshaller).marshall(documentUpdateList);
        verify(mockHttpClient).post("/batch", jsonBatchDocumentUpdateRequest, MediaType.APPLICATION_JSON_TYPE);
        verify(mockResponse, times(2)).getStatus();

        assertNotNull(expectedException);
        assertTrue(expectedException.getMessage().contains(String.valueOf(responseStatusCode)));
    }

    @Test
    public void shouldNotUploadBatchDocumentUpdate_whenNotInitialized() {
        // Given
        final DocumentUpdate documentUpdate = randomDocumentUpdate();
        final String jsonBatchDocumentUpdateRequest = randomString();
        final List<DocumentUpdate> documentUpdateList = Arrays.asList(documentUpdate);

        when(mockBatchDocumentUpdateRequest.getDocumentUpdates()).thenReturn(documentUpdateList);
        when(mockJsonDocumentMarshaller.marshall(Mockito.anyCollectionOf(DocumentUpdate.class))).thenReturn(
                jsonBatchDocumentUpdateRequest);

        final CloudSearchClient cloudSearchClient = new CloudSearchClient(mockAwsCredentials);

        // When
        IllegalStateException expectedException = null;
        try {
            cloudSearchClient.batchDocumentUpdate(mockBatchDocumentUpdateRequest);
        } catch (final IllegalStateException e) {
            expectedException = e;
        }

        // Then
        verify(mockBatchDocumentUpdateRequest).getDocumentUpdates();
        verify(mockJsonDocumentMarshaller).marshall(documentUpdateList);

        assertNotNull(expectedException);
    }

    @Test(expected = ValidationException.class)
    public void shouldFailToSearchAsStartLessThanZero() {
        // Given
        final int responseStatusCode = Response.Status.BAD_REQUEST.getStatusCode();
        final String errorText = randomString();
        final String jsonResponseError = String.format("{\"error\" : { \"message\" : \"%s\" }}", errorText);
        final String searchDomainName = Randoms.randomString();
        final DomainStatus domainStatus = randomDomainStatusWithName(searchDomainName);
        final List<DomainStatus> listOfDomainStatus = Arrays.asList(domainStatus);
        final Class<Document> documentClazz = Document.class;
        final String queryString = randomString();
        final Query mockQuery = mock(Query.class);

        final int size = Randoms.randomInt(100) + 1;

        final QueryType qType = Randoms.randomEnum(QueryType.class);
        when(mockQuery.queryType()).thenReturn(qType);

        when(mockAmazonCloudSearchClient.describeDomains()).thenReturn(mockDescribeDomainResult);
        when(mockDescribeDomainResult.getDomainStatusList()).thenReturn(listOfDomainStatus);
        when(mockStructuredQueryBuilder.buildQuery(any(Query.class))).thenReturn(queryString);
        when(mockHttpClient.get(anyString(), any(MultivaluedHashMap.class))).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(responseStatusCode);
        when(mockResponse.readEntity(any(Class.class))).thenReturn(jsonResponseError);

        final CloudSearchClient cloudSearchClient = new CloudSearchClient(mockAwsCredentials);
        cloudSearchClient.initialize();

        // When
        cloudSearchClient.searchDocuments(mockQuery, -1, size, searchDomainName, documentClazz);
    }

    @Test(expected = ValidationException.class)
    public void shouldFailToSearchAsSizeLessThanZero() {
        // Given
        final int responseStatusCode = Response.Status.BAD_REQUEST.getStatusCode();
        final String errorText = randomString();
        final String jsonResponseError = String.format("{\"error\" : { \"message\" : \"%s\" }}", errorText);
        final String searchDomainName = Randoms.randomString();
        final DomainStatus domainStatus = randomDomainStatusWithName(searchDomainName);
        final List<DomainStatus> listOfDomainStatus = Arrays.asList(domainStatus);
        final Class<Document> documentClazz = Document.class;
        final String queryString = randomString();
        final Query mockQuery = mock(Query.class);

        final int start = Randoms.randomInt(100);

        final QueryType qType = Randoms.randomEnum(QueryType.class);
        when(mockQuery.queryType()).thenReturn(qType);

        when(mockAmazonCloudSearchClient.describeDomains()).thenReturn(mockDescribeDomainResult);
        when(mockDescribeDomainResult.getDomainStatusList()).thenReturn(listOfDomainStatus);
        when(mockStructuredQueryBuilder.buildQuery(any(Query.class))).thenReturn(queryString);
        when(mockHttpClient.get(anyString(), any(MultivaluedHashMap.class))).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(responseStatusCode);
        when(mockResponse.readEntity(any(Class.class))).thenReturn(jsonResponseError);

        final CloudSearchClient cloudSearchClient = new CloudSearchClient(mockAwsCredentials);
        cloudSearchClient.initialize();

        // When
        cloudSearchClient.searchDocuments(mockQuery, start, -1, searchDomainName, documentClazz);
    }

    @Test
    public void shouldNotSearchAndReturnADocument_withInvalidQueryRequest() {
        // Given
        final int responseStatusCode = Response.Status.BAD_REQUEST.getStatusCode();
        final String errorText = randomString();
        final String jsonResponseError = String.format("{\"error\" : { \"message\" : \"%s\" }}", errorText);
        final String searchDomainName = Randoms.randomString();
        final DomainStatus domainStatus = randomDomainStatusWithName(searchDomainName);
        final List<DomainStatus> listOfDomainStatus = Arrays.asList(domainStatus);
        final Class<Document> documentClazz = Document.class;
        final String queryString = randomString();
        final Query mockQuery = mock(Query.class);

        final int size = Randoms.randomInt(100) + 1;
        final int start = Randoms.randomInt(100);

        final QueryType qType = Randoms.randomEnum(QueryType.class);
        when(mockQuery.queryType()).thenReturn(qType);

        when(mockAmazonCloudSearchClient.describeDomains()).thenReturn(mockDescribeDomainResult);
        when(mockDescribeDomainResult.getDomainStatusList()).thenReturn(listOfDomainStatus);
        when(mockStructuredQueryBuilder.buildQuery(any(Query.class))).thenReturn(queryString);
        when(mockHttpClient.get(anyString(), any(MultivaluedHashMap.class))).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(responseStatusCode);
        when(mockResponse.readEntity(any(Class.class))).thenReturn(jsonResponseError);

        final CloudSearchClient cloudSearchClient = new CloudSearchClient(mockAwsCredentials);
        cloudSearchClient.initialize();

        // When
        AmazonServiceException expectedException = null;
        try {
            cloudSearchClient.searchDocuments(mockQuery, start, size, searchDomainName, documentClazz);
        } catch (final AmazonServiceException e) {
            expectedException = e;
        }

        // Then
        final ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<MultivaluedHashMap> multiValuedHashMapArgumentCaptor = ArgumentCaptor
                .forClass(MultivaluedHashMap.class);

        verify(mockAmazonCloudSearchClient).describeDomains();
        verify(mockDescribeDomainResult).getDomainStatusList();
        verify(mockHttpClient).get(stringArgumentCaptor.capture(), multiValuedHashMapArgumentCaptor.capture());
        verify(mockResponse).getStatus();
        verify(mockResponse).readEntity(String.class);

        assertNotNull(expectedException);
        assertTrue(expectedException.getMessage().contains(errorText));
        assertThat(stringArgumentCaptor.getValue(), Is.is("/search"));
        assertThat(multiValuedHashMapArgumentCaptor.getValue().get("q").get(0).toString(), is(queryString));
        assertThat(multiValuedHashMapArgumentCaptor.getValue().get("q.parser").get(0).toString(),
                equalToIgnoringCase(qType.name()));
        assertThat(multiValuedHashMapArgumentCaptor.getValue().get("start").get(0).toString(),
                is(Integer.toString(start)));
        assertThat(multiValuedHashMapArgumentCaptor.getValue().get("size").get(0).toString(),
                is(Integer.toString(size)));
    }

    @Test
    public void shouldNotSearch_withInvalidAccessToCloudSearch() {
        // Given
        final int responseStatusCode = Response.Status.FORBIDDEN.getStatusCode();
        final String searchDomainName = Randoms.randomString();
        final DomainStatus domainStatus = randomDomainStatusWithName(searchDomainName);
        final List<DomainStatus> listOfDomainStatus = Arrays.asList(domainStatus);
        final Class<Document> documentClazz = Document.class;
        final String queryString = randomString();
        final Query mockQuery = mock(Query.class);

        final QueryType qType = Randoms.randomEnum(QueryType.class);
        when(mockQuery.queryType()).thenReturn(qType);

        final int size = Randoms.randomInt(100) + 1;
        final int start = Randoms.randomInt(100);

        when(mockAmazonCloudSearchClient.describeDomains()).thenReturn(mockDescribeDomainResult);
        when(mockDescribeDomainResult.getDomainStatusList()).thenReturn(listOfDomainStatus);
        when(mockStructuredQueryBuilder.buildQuery(any(Query.class))).thenReturn(queryString);
        when(mockHttpClient.get(anyString(), any(MultivaluedHashMap.class))).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(responseStatusCode);

        final CloudSearchClient cloudSearchClient = new CloudSearchClient(mockAwsCredentials);
        cloudSearchClient.initialize();

        // When
        AmazonServiceException expectedException = null;
        try {
            cloudSearchClient.searchDocuments(mockQuery, start, size, searchDomainName, documentClazz);
        } catch (final AmazonServiceException e) {
            expectedException = e;
        }

        // Then
        final ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<MultivaluedHashMap> multiValuedHashMapArgumentCaptor = ArgumentCaptor
                .forClass(MultivaluedHashMap.class);

        verify(mockAmazonCloudSearchClient).describeDomains();
        verify(mockDescribeDomainResult).getDomainStatusList();
        verify(mockHttpClient).get(stringArgumentCaptor.capture(), multiValuedHashMapArgumentCaptor.capture());
        verify(mockResponse).getStatus();

        assertNotNull(expectedException);
        assertThat(stringArgumentCaptor.getValue(), Is.is("/search"));
        assertThat(multiValuedHashMapArgumentCaptor.getValue().get("q").get(0).toString(), is(queryString));
        assertThat(multiValuedHashMapArgumentCaptor.getValue().get("q.parser").get(0).toString(),
                equalToIgnoringCase(qType.name()));
        assertThat(multiValuedHashMapArgumentCaptor.getValue().get("start").get(0).toString(),
                is(Integer.toString(start)));
        assertThat(multiValuedHashMapArgumentCaptor.getValue().get("size").get(0).toString(),
                is(Integer.toString(size)));
    }

    @Test
    public void shouldNotSearch_withInternalServerError() {
        // Given
        final int responseStatusCode = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        final String searchDomainName = Randoms.randomString();
        final DomainStatus domainStatus = randomDomainStatusWithName(searchDomainName);
        final List<DomainStatus> listOfDomainStatus = Arrays.asList(domainStatus);
        final Class<Document> documentClazz = Document.class;
        final String queryString = randomString();
        final Query mockQuery = mock(Query.class);

        final int size = Randoms.randomInt(100) + 1;
        final int start = Randoms.randomInt(100);

        final QueryType qType = Randoms.randomEnum(QueryType.class);
        when(mockQuery.queryType()).thenReturn(qType);

        when(mockAmazonCloudSearchClient.describeDomains()).thenReturn(mockDescribeDomainResult);
        when(mockDescribeDomainResult.getDomainStatusList()).thenReturn(listOfDomainStatus);
        when(mockStructuredQueryBuilder.buildQuery(any(Query.class))).thenReturn(queryString);
        when(mockHttpClient.get(anyString(), any(MultivaluedHashMap.class))).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(responseStatusCode);

        final CloudSearchClient cloudSearchClient = new CloudSearchClient(mockAwsCredentials);
        cloudSearchClient.initialize();

        // When
        AmazonServiceException expectedException = null;
        try {
            cloudSearchClient.searchDocuments(mockQuery, start, size, searchDomainName, documentClazz);
        } catch (final AmazonServiceException e) {
            expectedException = e;
        }

        // Then
        final ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<MultivaluedHashMap> multiValuedHashMapArgumentCaptor = ArgumentCaptor
                .forClass(MultivaluedHashMap.class);

        verify(mockAmazonCloudSearchClient).describeDomains();
        verify(mockDescribeDomainResult).getDomainStatusList();
        verify(mockHttpClient).get(stringArgumentCaptor.capture(), multiValuedHashMapArgumentCaptor.capture());
        verify(mockResponse).getStatus();

        assertNotNull(expectedException);
        assertThat(stringArgumentCaptor.getValue(), Is.is("/search"));
        assertThat(multiValuedHashMapArgumentCaptor.getValue().get("q").get(0).toString(), is(queryString));
        assertThat(multiValuedHashMapArgumentCaptor.getValue().get("q.parser").get(0).toString(),
                equalToIgnoringCase(qType.name()));
        assertThat(multiValuedHashMapArgumentCaptor.getValue().get("start").get(0).toString(),
                is(Integer.toString(start)));
        assertThat(multiValuedHashMapArgumentCaptor.getValue().get("size").get(0).toString(),
                is(Integer.toString(size)));
    }

    @Test
    public void shouldNotSearch_withUnknownResponse() {
        // Given
        final int responseStatusCode = Response.Status.CONFLICT.getStatusCode();
        final String searchDomainName = Randoms.randomString();
        final DomainStatus domainStatus = randomDomainStatusWithName(searchDomainName);
        final List<DomainStatus> listOfDomainStatus = Arrays.asList(domainStatus);
        final Class<Document> documentClazz = Document.class;
        final String queryString = randomString();
        final Query mockQuery = mock(Query.class);

        final QueryType qType = Randoms.randomEnum(QueryType.class);
        when(mockQuery.queryType()).thenReturn(qType);

        when(mockAmazonCloudSearchClient.describeDomains()).thenReturn(mockDescribeDomainResult);
        when(mockDescribeDomainResult.getDomainStatusList()).thenReturn(listOfDomainStatus);
        when(mockStructuredQueryBuilder.buildQuery(any(Query.class))).thenReturn(queryString);
        when(mockHttpClient.get(anyString(), any(MultivaluedHashMap.class))).thenReturn(mockResponse);
        when(mockResponse.getStatus()).thenReturn(responseStatusCode);

        final CloudSearchClient cloudSearchClient = new CloudSearchClient(mockAwsCredentials);
        cloudSearchClient.initialize();

        // When
        AmazonServiceException expectedException = null;
        try {
            cloudSearchClient.searchDocuments(mockQuery, 0, 100, searchDomainName, documentClazz);
        } catch (final AmazonServiceException e) {
            expectedException = e;
        }

        // Then
        final ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<MultivaluedHashMap> multiValuedHashMapArgumentCaptor = ArgumentCaptor
                .forClass(MultivaluedHashMap.class);

        verify(mockAmazonCloudSearchClient).describeDomains();
        verify(mockDescribeDomainResult).getDomainStatusList();
        verify(mockHttpClient).get(stringArgumentCaptor.capture(), multiValuedHashMapArgumentCaptor.capture());
        verify(mockResponse, times(2)).getStatus();

        assertNotNull(expectedException);
        assertTrue(expectedException.getMessage().contains(String.valueOf(responseStatusCode)));
        assertThat(stringArgumentCaptor.getValue(), Is.is("/search"));
        assertThat(multiValuedHashMapArgumentCaptor.getValue().get("q").get(0).toString(), is(queryString));
        assertThat(multiValuedHashMapArgumentCaptor.getValue().get("q.parser").get(0).toString(),
                equalToIgnoringCase(qType.name()));

        assertThat(multiValuedHashMapArgumentCaptor.getValue().get("start").get(0).toString(), is(Integer.toString(0)));
        assertThat(multiValuedHashMapArgumentCaptor.getValue().get("size").get(0).toString(), is(Integer.toString(100)));
    }

    private DocumentUpdate randomDocumentUpdate() {
        return new DocumentUpdate(Randoms.randomEnum(DocumentUpdate.Type.class), randomString(5));
    }

    private DomainStatus randomDomainStatusWithName(final String domainName) {
        final DomainStatus domainStatus = new DomainStatus();
        domainStatus.setDomainName(domainName);
        domainStatus.setDocService(new ServiceEndpoint().withEndpoint(randomString()));
        domainStatus.setSearchService(new ServiceEndpoint().withEndpoint(randomString()));
        domainStatus.setCreated(true);
        domainStatus.setDeleted(false);
        return domainStatus;
    }
}