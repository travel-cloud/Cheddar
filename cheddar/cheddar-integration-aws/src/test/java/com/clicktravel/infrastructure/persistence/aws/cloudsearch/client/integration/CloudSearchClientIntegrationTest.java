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
package com.clicktravel.infrastructure.persistence.aws.cloudsearch.client.integration;

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.clicktravel.common.http.client.HttpClient;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.DocumentSearchResponse;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.query.*;
import com.clicktravel.infrastructure.integration.aws.AwsIntegration;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.QueryBuilder;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.client.*;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.client.DocumentUpdate.Type;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Category(AwsIntegration.class)
public class CloudSearchClientIntegrationTest {

    private static final String CLOUD_SEARCH_END_POINT = AwsIntegration.getCloudSearchEndpoint();
    private static final String SEARCH_DOMAIN = "unittest-test1234";
    private static final String SEARCH_DOMAIN_SEARCH_END_POINT = "http://search-unittest-test1234-foy4v7wbuo32jtgkyffz2nrqze.eu-west-1.cloudsearch.amazonaws.com/2013-01-01/search";
    private static final String ACCESS_KEY_ID = AwsIntegration.getAccessKeyId();
    private static final String SECRET_KEY = AwsIntegration.getSecretKeyId();
    private final Collection<String> documentsToDelete = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @After
    public void tearDown() {
        if (!documentsToDelete.isEmpty()) {
            final BatchDocumentUpdateRequest batchDocumentUpdateRequest = new BatchDocumentUpdateRequest(SEARCH_DOMAIN);
            for (final String documentId : documentsToDelete) {
                final DocumentUpdate documentUpdate = new DocumentUpdate(Type.DELETE, documentId);
                batchDocumentUpdateRequest.withDocument(documentUpdate);
            }
            final CloudSearchClient cloudSearchClient = new CloudSearchClient(new BasicAWSCredentials(ACCESS_KEY_ID,
                    SECRET_KEY));
            cloudSearchClient.setEndpoint(CLOUD_SEARCH_END_POINT);
            cloudSearchClient.initialize();
            try {
                cloudSearchClient.batchDocumentUpdate(batchDocumentUpdateRequest);
            } catch (final Exception e) {
            }
        }
    }

    @Test
    public void shouldCacheDomainEndPoints_whenInitialized() throws Exception {
        // Given
        final String endpoint = CLOUD_SEARCH_END_POINT;
        final CloudSearchClient cloudSearchClient = new CloudSearchClient(new BasicAWSCredentials(ACCESS_KEY_ID,
                SECRET_KEY));
        cloudSearchClient.setEndpoint(endpoint);

        // When
        cloudSearchClient.initialize();

        // Then
        assertEquals(1, cloudSearchClient.domains().size());
        assertEquals(SEARCH_DOMAIN, cloudSearchClient.domains().iterator().next());
    }

    @Test
    public void shouldBatchDocumentUpdate_withBatchDocumentUpdateRequest() throws Exception {
        // Given
        final BatchDocumentUpdateRequest batchDocumentUpdateRequest = new BatchDocumentUpdateRequest(SEARCH_DOMAIN);
        final String documentId = "unittest-id-" + randomString(20);
        final DocumentUpdate documentUpdate = new DocumentUpdate(Type.ADD, documentId);
        final String fieldValue = "unittest-" + randomString() + "-" + documentId;
        final Field field = new Field("title", fieldValue);
        final Collection<Field> fields = Arrays.asList(field);
        documentUpdate.withFields(fields);

        batchDocumentUpdateRequest.withDocument(documentUpdate);
        final CloudSearchClient cloudSearchClient = new CloudSearchClient(new BasicAWSCredentials(ACCESS_KEY_ID,
                SECRET_KEY));
        cloudSearchClient.setEndpoint(CLOUD_SEARCH_END_POINT);
        cloudSearchClient.initialize();
        documentsToDelete.add(documentId);

        // When
        cloudSearchClient.batchDocumentUpdate(batchDocumentUpdateRequest);

        // Then
        assertTrue("Document " + documentId + " does not exists", documentExists(documentId));
    }

    @Test
    public void shouldBatchDocumentUpdate_withBatchDocumentUpdateRequestAsArray() throws Exception {
        // Given
        final BatchDocumentUpdateRequest batchDocumentUpdateRequest = new BatchDocumentUpdateRequest(SEARCH_DOMAIN);
        final String documentId = "unittest-id-" + randomString(20);
        final DocumentUpdate documentUpdate = new DocumentUpdate(Type.ADD, documentId);
        final Collection<String> fieldValue = Arrays.asList("unittest-" + randomString() + "-" + documentId);
        final Field field = new Field("actors", fieldValue);
        final Collection<Field> fields = Arrays.asList(field);
        documentUpdate.withFields(fields);

        batchDocumentUpdateRequest.withDocument(documentUpdate);
        final CloudSearchClient cloudSearchClient = new CloudSearchClient(new BasicAWSCredentials(ACCESS_KEY_ID,
                SECRET_KEY));
        cloudSearchClient.setEndpoint(CLOUD_SEARCH_END_POINT);
        cloudSearchClient.initialize();
        documentsToDelete.add(documentId);

        // When
        cloudSearchClient.batchDocumentUpdate(batchDocumentUpdateRequest);

        // Then
        assertTrue("Document " + documentId + " does not exists", documentExists(documentId));
    }

    @Test
    public void shouldBatchDocumentUpdate_withDeletedDocumentUpdate() throws Exception {
        // Given
        final BatchDocumentUpdateRequest batchDocumentUpdateRequest = new BatchDocumentUpdateRequest(SEARCH_DOMAIN);
        final String documentId = "unittest-id-" + randomString(20);
        final DocumentUpdate documentUpdate = new DocumentUpdate(Type.ADD, documentId);
        final String fieldValue = "unittest-" + randomString() + "-" + documentId;
        final Field field = new Field("title", fieldValue);
        final Collection<Field> fields = Arrays.asList(field);
        documentUpdate.withFields(fields);
        batchDocumentUpdateRequest.withDocument(documentUpdate);
        final CloudSearchClient cloudSearchClient = new CloudSearchClient(new BasicAWSCredentials(ACCESS_KEY_ID,
                SECRET_KEY));
        cloudSearchClient.setEndpoint(CLOUD_SEARCH_END_POINT);
        cloudSearchClient.initialize();
        final BatchDocumentUpdateRequest batchDocumentUpdateRequest2 = new BatchDocumentUpdateRequest(SEARCH_DOMAIN);
        final DocumentUpdate documentUpdate2 = new DocumentUpdate(Type.DELETE, documentId);
        batchDocumentUpdateRequest2.withDocument(documentUpdate2);

        cloudSearchClient.batchDocumentUpdate(batchDocumentUpdateRequest);
        final long startTime = System.currentTimeMillis();
        do {
            // nothing
        } while (!documentExists(documentId) && System.currentTimeMillis() - startTime < 60000);
        assertTrue(documentExists(documentId));
        documentsToDelete.add(documentId);

        // When
        cloudSearchClient.batchDocumentUpdate(batchDocumentUpdateRequest2);

        // Then
        assertFalse("Document " + documentId + " still exists", documentExists(documentId));
    }

    @Test
    public void shouldSearchAndReturnDocuments_withStringAndQuery() throws JsonProcessingException, IOException {
        // Given
        final String field1Name = "title";
        final String value1 = "Star";
        final String field2Name = "genres";
        final String value2 = "Adventure";
        final StructuredQuery termQuery1 = new TermQuery(field1Name, value1);
        final StructuredQuery termQuery2 = new TermQuery(field2Name, value2);
        final StructuredQuery andQuery = new AndQuery(Arrays.asList(termQuery1, termQuery2));
        final AWSCredentials awsCredentails = new BasicAWSCredentials(ACCESS_KEY_ID, SECRET_KEY);
        final CloudSearchClient cloudSearchClient = new CloudSearchClient(awsCredentails);
        cloudSearchClient.setEndpoint(CLOUD_SEARCH_END_POINT);
        cloudSearchClient.initialize();

        // When
        final DocumentSearchResponse<ImdbDocument> documentList = cloudSearchClient.searchDocuments(andQuery, 0, 100,
                SEARCH_DOMAIN, ImdbDocument.class);

        // Then
        assertNotNull(documentList);
        assertTrue(documentList.getHits().size() > 0);
        assertTrue(isDocumentListComplete(documentList, andQuery));
    }

    @Test
    public void shouldSearchAndReturnDocuments_withStringOrQuery() throws JsonProcessingException, IOException {
        // Given
        final String field1Name = "actors";
        final String value1 = "Ford";
        final String field2Name = "actors";
        final String value2 = "Bob";
        final StructuredQuery termQuery1 = new TermQuery(field1Name, value1);
        final StructuredQuery termQuery2 = new TermQuery(field2Name, value2);
        final StructuredQuery orQuery = new OrQuery(Arrays.asList(termQuery1, termQuery2));
        final AWSCredentials awsCredentails = new BasicAWSCredentials(ACCESS_KEY_ID, SECRET_KEY);
        final CloudSearchClient cloudSearchClient = new CloudSearchClient(awsCredentails);
        cloudSearchClient.setEndpoint(CLOUD_SEARCH_END_POINT);
        cloudSearchClient.initialize();

        // When
        final DocumentSearchResponse<ImdbDocument> documentList = cloudSearchClient.searchDocuments(orQuery, 0, 100,
                SEARCH_DOMAIN, ImdbDocument.class);

        // Then
        assertNotNull(documentList);
        assertTrue(documentList.getHits().size() > 0);
        assertTrue(isDocumentListComplete(documentList, orQuery));
    }

    @Test
    public void shouldSearchAndReturnDocuments_withIntRangeQuery() throws JsonProcessingException, IOException {
        // Given
        final String fieldName = "year";
        final Integer lowerBoundary = 2000;
        final Integer upperBoundary = 2001;
        final Query rangeQuery = new RangeQuery(fieldName, lowerBoundary, upperBoundary, true, true);
        final AWSCredentials awsCredentails = new BasicAWSCredentials(ACCESS_KEY_ID, SECRET_KEY);
        final CloudSearchClient cloudSearchClient = new CloudSearchClient(awsCredentails);
        cloudSearchClient.setEndpoint(CLOUD_SEARCH_END_POINT);
        cloudSearchClient.initialize();

        // When
        final DocumentSearchResponse<ImdbDocument> documentList = cloudSearchClient.searchDocuments(rangeQuery, 0, 100,
                SEARCH_DOMAIN, ImdbDocument.class);

        // Then
        assertNotNull(documentList);
        assertTrue(documentList.getHits().size() > 0);
        assertTrue(isDocumentListComplete(documentList, rangeQuery));
    }

    @Test
    public void shouldSearchAndReturnDocuments_withDoubleTermQuery() throws JsonProcessingException, IOException {
        // Given
        final String fieldName = "rating";
        final Double fieldValue = 9.00;
        final Query rangeQuery = new TermQuery(fieldName, fieldValue);
        final AWSCredentials awsCredentails = new BasicAWSCredentials(ACCESS_KEY_ID, SECRET_KEY);
        final CloudSearchClient cloudSearchClient = new CloudSearchClient(awsCredentails);
        cloudSearchClient.setEndpoint(CLOUD_SEARCH_END_POINT);
        cloudSearchClient.initialize();

        // When
        final DocumentSearchResponse<ImdbDocument> documentList = cloudSearchClient.searchDocuments(rangeQuery, 0, 100,
                SEARCH_DOMAIN, ImdbDocument.class);

        // Then
        assertNotNull(documentList);
        assertTrue(documentList.getHits().size() > 0);
        assertTrue(isDocumentListComplete(documentList, rangeQuery));
    }

    private boolean isDocumentListComplete(final DocumentSearchResponse<ImdbDocument> documentList, final Query theQuery)
            throws JsonProcessingException, IOException {
        final HttpClient httpClient = HttpClient.Builder.httpClient().withBaseUri(SEARCH_DOMAIN_SEARCH_END_POINT)
                .build();
        final MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
        final String queryString = new QueryBuilder().buildQuery(theQuery);
        params.add("q", queryString);
        params.add("cursor", "initial");
        params.add("q.parser", "structured");
        final Response response = httpClient.get("", params);
        final String responseJson = response.readEntity(String.class);
        final JsonNode jsonNode = mapper.readTree(responseJson);
        final int matches = jsonNode.get("hits").get("found").intValue();
        return matches == documentList.getHits().size();
    }

    private boolean documentExists(final String documentId) throws Exception {
        final HttpClient httpClient = HttpClient.Builder.httpClient().withBaseUri(SEARCH_DOMAIN_SEARCH_END_POINT)
                .build();
        final MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
        params.add("q", documentId);
        Thread.sleep(15000);
        final Response response = httpClient.get("", params);
        final String responseJson = response.readEntity(String.class);
        final JsonNode jsonNode = mapper.readTree(responseJson);
        final int matches = jsonNode.get("hits").get("found").intValue();
        return matches == 1;
    }
}
