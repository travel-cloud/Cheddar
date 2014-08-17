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

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.cloudsearchv2.AmazonCloudSearch;
import com.amazonaws.services.cloudsearchv2.AmazonCloudSearchClient;
import com.amazonaws.services.cloudsearchv2.model.*;
import com.clicktravel.common.http.client.HttpClient;
import com.clicktravel.common.validation.ValidationException;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.Document;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.DocumentSearchResponse;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.query.Query;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.QueryBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CloudSearchClient implements AmazonCloudSearch {

    private final AmazonCloudSearch amazonCloudSearchConfigurationClient;
    private final JsonDocumentUpdateMarshaller jsonDocumentUpdateMarshaller;
    private final Map<String, DomainEndPoint> domainEndPoints = new HashMap<>();

    public CloudSearchClient(final AWSCredentials awsCredentials) {
        amazonCloudSearchConfigurationClient = new AmazonCloudSearchClient(awsCredentials);
        jsonDocumentUpdateMarshaller = new JsonDocumentUpdateMarshaller();
    }

    /**
     * @return the collection of Domain names available in the current account
     */

    public Collection<String> domains() {
        return domainEndPoints.keySet();
    }

    /**
     * Method will contact CloudSearch and request all Domain information for the current account such as active
     * endpoints for search etc for use in later requests. Should be called before any further request are made to Cloud
     * Search.
     */

    public void initialize() {
        final DescribeDomainsResult describeDomainsResult = amazonCloudSearchConfigurationClient.describeDomains();
        for (final DomainStatus domainStatus : describeDomainsResult.getDomainStatusList()) {
            final String domainName = domainStatus.getDomainName();
            final String documentServiceEndPoint = domainStatus.getDocService().getEndpoint();
            final String searchServiceEndPoint = domainStatus.getSearchService().getEndpoint();
            if (domainStatus.isCreated() && !domainStatus.isDeleted()) {
                domainEndPoints.put(domainName, new DomainEndPoint(documentServiceEndPoint, searchServiceEndPoint));
            }
        }
    }

    /**
     * Method used to upload a batch of updates for documents stored in CloudSearch
     * 
     * @param batchDocumentUpdateRequest - a request object containing a batch of add and delete commands for Cloud
     *            Search documents
     */

    public void batchDocumentUpdate(final BatchDocumentUpdateRequest batchDocumentUpdateRequest) {
        final String updateBatch = jsonDocumentUpdateMarshaller.marshall(batchDocumentUpdateRequest
                .getDocumentUpdates());
        final String documentServiceEndPoint = getDomainEndPoint(batchDocumentUpdateRequest.getSearchDomain())
                .documentServiceEndPoint();
        final Response documentUploadResponse = HttpClient.Builder.httpClient().withBaseUri(documentServiceEndPoint)
                .build().post("/batch", updateBatch, MediaType.APPLICATION_JSON_TYPE);
        switch (Response.Status.fromStatusCode(documentUploadResponse.getStatus())) {
            case OK:
                break;
            case BAD_REQUEST:
                final String errorResponseJsonString = documentUploadResponse.readEntity(String.class);
                throw new AmazonServiceException(errorResponseJsonString);
            case FORBIDDEN:
                throw new AmazonServiceException("Access to CloudSearch has been denied");
            case INTERNAL_SERVER_ERROR:
                throw new AmazonServiceException("CloudSearch responded with a server error");
            default:
                throw new AmazonServiceException(String.format(
                        "Unexpected HTTP response returned from CloudSearch. The received response code was [%s]",
                        documentUploadResponse.getStatus()));
        }
    }

    private DomainEndPoint getDomainEndPoint(final String domain) {
        final DomainEndPoint domainEndPoint = domainEndPoints.get(domain);
        if (domainEndPoint == null) {
            throw new IllegalStateException("Domain does not exist in CloudSearch");
        }
        return domainEndPoint;
    }

    private JsonNode parseStringAsJson(final String inputJsonString) {
        final ObjectMapper jsonMapper = new ObjectMapper();
        try {
            return jsonMapper.readTree(inputJsonString);
        } catch (final IOException e) {
            throw new IllegalArgumentException(String.format("Failed to parse string [%s] into json object",
                    inputJsonString));
        }
    }

    /**
     * 
     * @param query to be passed to the domain
     * @param start the number of the first document to be returned defaults to DEFAULT_PAGE_START_POSITION
     * @param size the number of documents to return defaults to DEFAULT_SEARCH_PAGE_SIZE
     * @param searchDomain the domain to search
     * @param documentClass the type of documents to be returned
     * @return DocumentSearchResponse containing a list of documents that meet the query along with result metadata
     */
    public <T extends Document> DocumentSearchResponse<T> searchDocuments(final Query query, final int start,
            final int size, final String searchDomain, final Class<T> documentClass) {
        if (start < 0) {
            throw new ValidationException(String.format("Value is below the allowed minimum of 0 : value -> [%s]",
                    start), "start");
        }

        if (size < 0) {
            throw new ValidationException(
                    String.format("Value is below the allowed minimum of 0 : value -> [%s]", size), "size");
        }

        final String searchServiceEndPoint = getDomainEndPoint(searchDomain).searchServiceEndPoint();
        final String queryString = new QueryBuilder().buildQuery(query);
        final JsonDocumentSearchResponseUnmarshaller<T> unmarshaller = new JsonDocumentSearchResponseUnmarshaller<>(
                documentClass);

        final String searchJsonResponse = search(searchServiceEndPoint, queryString, parser(query), size, start);

        return unmarshaller.unmarshall(searchJsonResponse);
    }

    private String parser(final Query query) {
        return query.queryType().name().toLowerCase();
    }

    private String search(final String searchServiceEndPoint, final String queryString, final String parser,
            final int size, final int start) {
        final MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("q", queryString);
        params.putSingle("q.parser", parser);
        params.putSingle("size", Integer.toString(size));
        params.putSingle("start", Integer.toString(start));
        final Response searchResponse = HttpClient.Builder.httpClient().withBaseUri(searchServiceEndPoint).build()
                .get("/search", params);

        switch (Response.Status.fromStatusCode(searchResponse.getStatus())) {
            case OK:
                final String searchJsonResponse = searchResponse.readEntity(String.class);
                return searchJsonResponse;
            case BAD_REQUEST:
                final String errorResponseJsonString = searchResponse.readEntity(String.class);
                final String errorMessage = parseStringAsJson(errorResponseJsonString).get("error").get("message")
                        .textValue();
                throw new AmazonServiceException(errorMessage);
            case FORBIDDEN:
                throw new AmazonServiceException("Access to CloudSearch has been denied");
            case INTERNAL_SERVER_ERROR:
                throw new AmazonServiceException("CloudSearch responded with a server error");
            default:
                throw new AmazonServiceException(String.format(
                        "Unexpected HTTP response returned from CloudSearch. The received response code was [%s]",
                        searchResponse.getStatus()));
        }
    }

    // *** The following methods all proxy through to the composed AmazonCloudSearch object without additional logic ***

    @Override
    public CreateDomainResult createDomain(final CreateDomainRequest createDomainRequest) {
        return amazonCloudSearchConfigurationClient.createDomain(createDomainRequest);
    }

    @Override
    public void setEndpoint(final String endpoint) {
        amazonCloudSearchConfigurationClient.setEndpoint(endpoint);
    }

    @Override
    public void setRegion(final Region region) throws IllegalArgumentException {
        amazonCloudSearchConfigurationClient.setRegion(region);
    }

    @Override
    public DescribeAnalysisSchemesResult describeAnalysisSchemes(
            final DescribeAnalysisSchemesRequest describeAnalysisSchemesRequest) throws AmazonServiceException,
            AmazonClientException {
        return amazonCloudSearchConfigurationClient.describeAnalysisSchemes(describeAnalysisSchemesRequest);
    }

    @Override
    public DeleteIndexFieldResult deleteIndexField(final DeleteIndexFieldRequest deleteIndexFieldRequest)
            throws AmazonServiceException, AmazonClientException {
        return amazonCloudSearchConfigurationClient.deleteIndexField(deleteIndexFieldRequest);
    }

    @Override
    public UpdateAvailabilityOptionsResult updateAvailabilityOptions(
            final UpdateAvailabilityOptionsRequest updateAvailabilityOptionsRequest) throws AmazonServiceException,
            AmazonClientException {
        return amazonCloudSearchConfigurationClient.updateAvailabilityOptions(updateAvailabilityOptionsRequest);
    }

    @Override
    public DescribeIndexFieldsResult describeIndexFields(final DescribeIndexFieldsRequest describeIndexFieldsRequest)
            throws AmazonServiceException, AmazonClientException {
        return amazonCloudSearchConfigurationClient.describeIndexFields(describeIndexFieldsRequest);
    }

    @Override
    public DefineExpressionResult defineExpression(final DefineExpressionRequest defineExpressionRequest)
            throws AmazonServiceException, AmazonClientException {
        return amazonCloudSearchConfigurationClient.defineExpression(defineExpressionRequest);
    }

    @Override
    public UpdateServiceAccessPoliciesResult updateServiceAccessPolicies(
            final UpdateServiceAccessPoliciesRequest updateServiceAccessPoliciesRequest) throws AmazonServiceException,
            AmazonClientException {
        return amazonCloudSearchConfigurationClient.updateServiceAccessPolicies(updateServiceAccessPoliciesRequest);
    }

    @Override
    public DefineSuggesterResult defineSuggester(final DefineSuggesterRequest defineSuggesterRequest)
            throws AmazonServiceException, AmazonClientException {
        return amazonCloudSearchConfigurationClient.defineSuggester(defineSuggesterRequest);
    }

    @Override
    public DeleteAnalysisSchemeResult deleteAnalysisScheme(final DeleteAnalysisSchemeRequest deleteAnalysisSchemeRequest)
            throws AmazonServiceException, AmazonClientException {
        return amazonCloudSearchConfigurationClient.deleteAnalysisScheme(deleteAnalysisSchemeRequest);
    }

    @Override
    public IndexDocumentsResult indexDocuments(final IndexDocumentsRequest indexDocumentsRequest)
            throws AmazonServiceException, AmazonClientException {
        return amazonCloudSearchConfigurationClient.indexDocuments(indexDocumentsRequest);
    }

    @Override
    public DescribeSuggestersResult describeSuggesters(final DescribeSuggestersRequest describeSuggestersRequest)
            throws AmazonServiceException, AmazonClientException {
        return amazonCloudSearchConfigurationClient.describeSuggesters(describeSuggestersRequest);
    }

    @Override
    public UpdateScalingParametersResult updateScalingParameters(
            final UpdateScalingParametersRequest updateScalingParametersRequest) throws AmazonServiceException,
            AmazonClientException {
        return amazonCloudSearchConfigurationClient.updateScalingParameters(updateScalingParametersRequest);
    }

    @Override
    public ListDomainNamesResult listDomainNames(final ListDomainNamesRequest listDomainNamesRequest)
            throws AmazonServiceException, AmazonClientException {
        return amazonCloudSearchConfigurationClient.listDomainNames();
    }

    @Override
    public DefineIndexFieldResult defineIndexField(final DefineIndexFieldRequest defineIndexFieldRequest)
            throws AmazonServiceException, AmazonClientException {
        return amazonCloudSearchConfigurationClient.defineIndexField(defineIndexFieldRequest);
    }

    @Override
    public DeleteSuggesterResult deleteSuggester(final DeleteSuggesterRequest deleteSuggesterRequest)
            throws AmazonServiceException, AmazonClientException {
        return amazonCloudSearchConfigurationClient.deleteSuggester(deleteSuggesterRequest);
    }

    @Override
    public DeleteExpressionResult deleteExpression(final DeleteExpressionRequest deleteExpressionRequest)
            throws AmazonServiceException, AmazonClientException {
        return amazonCloudSearchConfigurationClient.deleteExpression(deleteExpressionRequest);
    }

    @Override
    public DescribeAvailabilityOptionsResult describeAvailabilityOptions(
            final DescribeAvailabilityOptionsRequest describeAvailabilityOptionsRequest) throws AmazonServiceException,
            AmazonClientException {
        return amazonCloudSearchConfigurationClient.describeAvailabilityOptions(describeAvailabilityOptionsRequest);
    }

    @Override
    public DefineAnalysisSchemeResult defineAnalysisScheme(final DefineAnalysisSchemeRequest defineAnalysisSchemeRequest)
            throws AmazonServiceException, AmazonClientException {
        return amazonCloudSearchConfigurationClient.defineAnalysisScheme(defineAnalysisSchemeRequest);
    }

    @Override
    public BuildSuggestersResult buildSuggesters(final BuildSuggestersRequest buildSuggestersRequest)
            throws AmazonServiceException, AmazonClientException {
        return amazonCloudSearchConfigurationClient.buildSuggesters(buildSuggestersRequest);
    }

    @Override
    public DescribeServiceAccessPoliciesResult describeServiceAccessPolicies(
            final DescribeServiceAccessPoliciesRequest describeServiceAccessPoliciesRequest)
            throws AmazonServiceException, AmazonClientException {
        return amazonCloudSearchConfigurationClient.describeServiceAccessPolicies(describeServiceAccessPoliciesRequest);
    }

    @Override
    public DeleteDomainResult deleteDomain(final DeleteDomainRequest deleteDomainRequest)
            throws AmazonServiceException, AmazonClientException {
        return amazonCloudSearchConfigurationClient.deleteDomain(deleteDomainRequest);
    }

    @Override
    public DescribeExpressionsResult describeExpressions(final DescribeExpressionsRequest describeExpressionsRequest)
            throws AmazonServiceException, AmazonClientException {
        return amazonCloudSearchConfigurationClient.describeExpressions(describeExpressionsRequest);
    }

    @Override
    public DescribeDomainsResult describeDomains(final DescribeDomainsRequest describeDomainsRequest)
            throws AmazonServiceException, AmazonClientException {
        return amazonCloudSearchConfigurationClient.describeDomains();
    }

    @Override
    public DescribeScalingParametersResult describeScalingParameters(
            final DescribeScalingParametersRequest describeScalingParametersRequest) throws AmazonServiceException,
            AmazonClientException {
        return amazonCloudSearchConfigurationClient.describeScalingParameters(describeScalingParametersRequest);
    }

    @Override
    public ListDomainNamesResult listDomainNames() throws AmazonServiceException, AmazonClientException {
        return amazonCloudSearchConfigurationClient.listDomainNames();
    }

    @Override
    public DescribeDomainsResult describeDomains() throws AmazonServiceException, AmazonClientException {
        return amazonCloudSearchConfigurationClient.describeDomains();
    }

    @Override
    public void shutdown() {
        amazonCloudSearchConfigurationClient.shutdown();
    }

    @Override
    public ResponseMetadata getCachedResponseMetadata(final AmazonWebServiceRequest request) {
        return amazonCloudSearchConfigurationClient.getCachedResponseMetadata(request);
    }
}
