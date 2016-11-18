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
package com.clicktravel.infrastructure.persistence.aws.cloudsearch;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudsearchdomain.AmazonCloudSearchDomain;
import com.amazonaws.services.cloudsearchdomain.model.*;
import com.amazonaws.services.cloudsearchv2.AmazonCloudSearch;
import com.amazonaws.services.cloudsearchv2.model.DescribeDomainsRequest;
import com.amazonaws.services.cloudsearchv2.model.DescribeDomainsResult;
import com.amazonaws.services.cloudsearchv2.model.DomainStatus;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.Document;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.DocumentSearchEngine;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.DocumentSearchResponse;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.DocumentConfiguration;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.DocumentConfigurationHolder;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.IndexDefinition;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.options.SearchOptions;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.query.Query;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.sort.SortOrder;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.sort.SortingOption;
import com.clicktravel.cheddar.infrastructure.persistence.exception.PersistenceResourceFailureException;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.client.*;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.client.DocumentUpdate.Type;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CloudSearchEngine implements DocumentSearchEngine {

    private static final int UPDATE_BATCH_SIZE = 1000;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DocumentConfigurationHolder documentConfigurationHolder;
    private final Map<Class<? extends Document>, DocumentConfiguration> documentConfigurations;
    private final Map<String, AmazonCloudSearchDomain> documentServiceClients = new HashMap<>();
    private final Map<String, AmazonCloudSearchDomain> searchServiceClients = new HashMap<>();
    private final AmazonCloudSearch cloudSearchClient;
    private boolean domainEndpointsCached;
    private final JsonDocumentSearchResponseUnmarshaller fieldParser;
    private final ObjectMapper objectMapper;

    public CloudSearchEngine(final DocumentConfigurationHolder documentConfigurationHolder,
            final AmazonCloudSearch cloudSearchClient) {
        if (documentConfigurationHolder == null) {
            throw new IllegalArgumentException("Document store configuration must not be null");
        }
        this.documentConfigurationHolder = documentConfigurationHolder;
        this.cloudSearchClient = cloudSearchClient;
        documentConfigurations = new HashMap<>();
        for (final DocumentConfiguration documentConfiguration : documentConfigurationHolder.documentConfigurations()) {
            documentConfigurations.put(documentConfiguration.documentClass(), documentConfiguration);
        }
        fieldParser = new JsonDocumentSearchResponseUnmarshaller();
        objectMapper = new ObjectMapper();
        cacheDomainEndpoints();
    }

    private void cacheDomainEndpoints() {
        if (cloudSearchClient != null && !domainEndpointsCached) {
            final Set<String> managedDomains = new HashSet<>();
            for (final DocumentConfiguration documentConfiguration : documentConfigurations.values()) {
                final String domainName = documentConfigurationHolder.schemaName() + "-"
                        + documentConfiguration.namespace();
                managedDomains.add(domainName);
            }
            final DescribeDomainsRequest describeDomainsRequest = new DescribeDomainsRequest();
            describeDomainsRequest.setDomainNames(managedDomains);
            final DescribeDomainsResult describeDomainsResult = cloudSearchClient
                    .describeDomains(describeDomainsRequest);
            final List<DomainStatus> domainStatusList = describeDomainsResult.getDomainStatusList();
            if (domainStatusList.size() != managedDomains.size()) {
                logger.info("Unable to cache CloudSearch document/search endpoints for: " + managedDomains);
            } else {
                for (final DomainStatus domainStatus : domainStatusList) {
                    if (domainStatus.isCreated() && !domainStatus.isDeleted()) {
                        final String documentServiceEndpoint = domainStatus.getDocService().getEndpoint();
                        final String searchServiceEndpoint = domainStatus.getSearchService().getEndpoint();
                        if (documentServiceEndpoint == null || searchServiceEndpoint == null) {
                            domainEndpointsCached = false;
                            return;
                        }
                        final AmazonCloudSearchDomain documentServiceClient = AmazonCloudSearchDomainClientBuilder
                                .build(documentServiceEndpoint);
                        final AmazonCloudSearchDomain searchServiceClient = AmazonCloudSearchDomainClientBuilder
                                .build(searchServiceEndpoint);
                        documentServiceClients.put(domainStatus.getDomainName(), documentServiceClient);
                        searchServiceClients.put(domainStatus.getDomainName(), searchServiceClient);
                    }
                }
                domainEndpointsCached = true;
            }
        }
    }

    public DocumentConfigurationHolder documentConfigurationHolder() {
        return documentConfigurationHolder;
    }

    private DocumentConfiguration getDocumentConfiguration(final Class<? extends Document> documentClass) {
        cacheDomainEndpoints();
        if (!domainEndpointsCached) {
            throw new IllegalStateException("CloudSearch endpoints not available");
        }
        final DocumentConfiguration documentConfiguration = documentConfigurations.get(documentClass);
        if (documentConfiguration == null) {
            throw new IllegalStateException("No document configuration found for: " + documentClass);
        }
        return documentConfiguration;
    }

    @Override
    public void update(final Document document) {
        update(Arrays.asList(document));
    }

    @Override
    public void update(final Collection<? extends Document> documents) {
        if (!documents.isEmpty()) {
            final Class<? extends Document> documentClass = documents.iterator().next().getClass();
            checkDocumentsHaveSameClass(documents, documentClass);

            final DocumentConfiguration documentConfiguration = getDocumentConfiguration(documentClass);
            final String searchDomain = documentConfigurationHolder.schemaName() + "-"
                    + documentConfiguration.namespace();
            for (final Collection<? extends Document> documentBatch : createDocumentBatches(documents)) {
                uploadBatch(documentBatch, documentConfiguration, searchDomain);
            }
        }
    }

    private void uploadBatch(final Collection<? extends Document> documents,
            final DocumentConfiguration documentConfiguration, final String searchDomain) {
        final BatchDocumentUpdateRequest batchDocumentUpdateRequest = new BatchDocumentUpdateRequest(searchDomain);

        for (final Document document : documents) {
            final DocumentUpdate documentUpdate = new DocumentUpdate(Type.ADD, document.getId());
            final Collection<Field> fields = new ArrayList<>();
            for (final IndexDefinition indexDefinition : documentConfiguration.indexDefinitions()) {
                final String indexName = indexDefinition.getName();
                final PropertyDescriptor propertyDescriptor = documentConfiguration.properties().get(indexName);
                if (propertyDescriptor == null) {
                    throw new IllegalStateException("No property found for index: " + indexName);
                }
                final Field field = new Field(indexName, getPropertyValue(document, propertyDescriptor));
                fields.add(field);
            }
            documentUpdate.withFields(fields);
            batchDocumentUpdateRequest.withDocument(documentUpdate);
        }
        getDocumentServiceClient(searchDomain).uploadDocuments(uploadDocumentsRequest(batchDocumentUpdateRequest));
    }

    private List<Collection<? extends Document>> createDocumentBatches(final Collection<? extends Document> documents) {
        final List<? extends Document> documentList = new ArrayList<>(documents);
        final List<Collection<? extends Document>> batches = new ArrayList<>();

        for (int i = 0; i < documentList.size(); i += UPDATE_BATCH_SIZE) {
            final List<? extends Document> batch = documentList.subList(i,
                    Math.min(documentList.size(), i + UPDATE_BATCH_SIZE));
            batches.add(batch);
        }

        return batches;
    }

    private AmazonCloudSearchDomain getDocumentServiceClient(final String domainName) {
        if (documentServiceClients.get(domainName) == null) {
            throw new IllegalStateException("Document Service client not present for: " + domainName);
        }
        return documentServiceClients.get(domainName);
    }

    private AmazonCloudSearchDomain getSearchServiceClient(final String domainName) {
        if (searchServiceClients.get(domainName) == null) {
            throw new IllegalStateException("Document Service client not present for: " + domainName);
        }
        return searchServiceClients.get(domainName);
    }

    private UploadDocumentsRequest uploadDocumentsRequest(final BatchDocumentUpdateRequest batchDocumentUpdateRequest) {
        final UploadDocumentsRequest uploadDocumentsRequest = new UploadDocumentsRequest();
        final byte[] documentUpdatesJsonBytes;
        final String documentUpdatesJson = JsonDocumentUpdateMarshaller
                .marshall(batchDocumentUpdateRequest.getDocumentUpdates());
        documentUpdatesJsonBytes = documentUpdatesJson.getBytes(Charset.forName("UTF-8"));
        final InputStream documents = new ByteArrayInputStream(documentUpdatesJsonBytes);
        uploadDocumentsRequest.setDocuments(documents);
        uploadDocumentsRequest.setContentLength((long) documentUpdatesJsonBytes.length);
        uploadDocumentsRequest.setContentType(MediaType.APPLICATION_JSON);
        return uploadDocumentsRequest;
    }

    private Object getPropertyValue(final Document document, final PropertyDescriptor propertyDescriptor) {
        try {
            final Object value = propertyDescriptor.getReadMethod().invoke(document);
            if (value != null && value instanceof String) {
                final String valueStr = ((String) value).trim();
                if (valueStr.isEmpty()) {
                    return null;
                }
            }
            return value;
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }

    }

    @Override
    public void delete(final Document document) {
        delete(Arrays.asList(document));
    }

    @Override
    public void delete(final Collection<? extends Document> documents) {
        if (!documents.isEmpty()) {
            final Class<? extends Document> documentClass = documents.iterator().next().getClass();
            checkDocumentsHaveSameClass(documents, documentClass);

            final DocumentConfiguration documentConfiguration = getDocumentConfiguration(documentClass);
            final String searchDomain = documentConfigurationHolder.schemaName() + "-"
                    + documentConfiguration.namespace();
            final BatchDocumentUpdateRequest batchDocumentUpdateRequest = new BatchDocumentUpdateRequest(searchDomain);
            for (final Document document : documents) {
                final DocumentUpdate csDocument = new DocumentUpdate(Type.DELETE, document.getId());
                batchDocumentUpdateRequest.withDocument(csDocument);
            }
            getDocumentServiceClient(searchDomain).uploadDocuments(uploadDocumentsRequest(batchDocumentUpdateRequest));
        }
    }

    private void checkDocumentsHaveSameClass(final Collection<? extends Document> documents,
            final Class<? extends Document> documentClass) {
        for (final Document document : documents) {
            if (document.getClass() != documentClass) {
                throw new IllegalArgumentException(
                        "All documents in the parameter collection should be instances of the same class.");
            }
        }
    }

    /**
     * Passes the query to the correct cloud search domain
     *
     * AWS could throw an exception on search for example the query may be invalid, we re throw this as
     * a @link(UnsuccessfulSearchException)
     */
    @Override
    public <T extends Document> DocumentSearchResponse<T> search(final Query query, final Integer start,
            final Integer size, final Class<T> documentClass) {
        return search(query, start, size, documentClass, SearchOptions.DEFAULT);
    }

    @Override
    public <T extends Document> DocumentSearchResponse<T> search(final Query query, final Integer start,
            final Integer size, final Class<T> documentClass, final SearchOptions options) {

        if (options == null) {
            throw new IllegalArgumentException("SearchOptions cannot be null");
        }

        try {
            final DocumentConfiguration documentConfiguration = getDocumentConfiguration(documentClass);
            final SearchRequest searchRequest = getSearchRequest(query);
            searchRequest.setStart((long) start);
            searchRequest.setSize((long) size);

            if (!options.getExpressions().isEmpty()) {
                searchRequest.setExpr(objectMapper.writeValueAsString(options.getExpressions()));
            }

            if (options.getSortOrder() != SortOrder.DEFAULT) {
                final StringBuilder sort = new StringBuilder();
                String direction = null;
                int count = 0;
                for (final SortingOption sortingOption : options.getSortOrder().sortingOptions()) {
                    count++;
                    sort.append(sortingOption.key() + " ");
                    switch (sortingOption.direction()) {
                        case ASCENDING:
                        default:
                            direction = "asc";
                            break;
                        case DESCENDING:
                            direction = "desc";
                            break;
                    }
                    sort.append(direction);
                    if (count < options.getSortOrder().sortingOptions().size()) {
                        sort.append(", ");
                    }
                }
                searchRequest.setSort(sort.toString());
            }

            final String searchDomain = documentConfigurationHolder.schemaName() + "-"
                    + documentConfiguration.namespace();
            final SearchResult searchResult = getSearchServiceClient(searchDomain).search(searchRequest);
            final List<T> documents = new ArrayList<>();
            for (final Hit hit : searchResult.getHits().getHit()) {
                try {
                    documents.add(getDocument(hit, documentClass, documentConfiguration.properties()));
                } catch (final Exception e) {
                    throw new IllegalStateException("Could not create Document from CloudSearch response: " + hit, e);
                }
            }
            final long totalResults = searchResult.getHits().getFound();
            final String cursor = searchResult.getHits().getCursor();
            return new DocumentSearchResponse<T>((int) totalResults, cursor, documents);
        } catch (final AmazonServiceException | JsonProcessingException e) {
            throw new PersistenceResourceFailureException("Unable to perform CloudSearch query", e);
        }
    }

    private SearchRequest getSearchRequest(final Query query) {
        final SearchRequest searchRequest = new SearchRequest();
        final String queryString = new QueryBuilder().buildQuery(query);
        searchRequest.setQuery(queryString);
        switch (query.queryType()) {
            case LUCENE:
                searchRequest.setQueryParser(QueryParser.Lucene);
                break;
            case STRUCTURED:
                searchRequest.setQueryParser(QueryParser.Structured);
                break;
            case SIMPLE:
            default:
                searchRequest.setQueryParser(QueryParser.Simple);
                break;
        }
        return searchRequest;
    }

    private <T extends Document> T getDocument(final Hit hit, final Class<T> documentClass,
            final Map<String, PropertyDescriptor> properties) throws Exception {
        final T document = fieldParser.unmarshall(hit.getFields(), documentClass);
        document.setId(hit.getId());
        return document;
    }

}
