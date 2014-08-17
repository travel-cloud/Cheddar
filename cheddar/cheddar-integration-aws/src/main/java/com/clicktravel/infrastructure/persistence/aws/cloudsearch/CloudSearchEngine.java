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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonServiceException;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.Document;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.DocumentSearchEngine;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.DocumentSearchResponse;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.DocumentConfiguration;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.DocumentConfigurationHolder;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.exception.UnsuccessfulSearchException;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.query.Query;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.client.*;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.client.DocumentUpdate.Type;

public class CloudSearchEngine implements DocumentSearchEngine {

    private final DocumentConfigurationHolder documentConfigurationHolder;
    private final Map<Class<? extends Document>, DocumentConfiguration> documentConfigurations;
    private CloudSearchClient cloudSearchClient;
    private boolean initialized;

    public CloudSearchEngine(final DocumentConfigurationHolder documentConfigurationHolder) {
        if (documentConfigurationHolder == null) {
            throw new IllegalArgumentException("Document store configuration must not be null");
        }
        this.documentConfigurationHolder = documentConfigurationHolder;
        documentConfigurations = new HashMap<>();
        for (final DocumentConfiguration documentConfiguration : this.documentConfigurationHolder
                .documentConfigurations()) {
            documentConfigurations.put(documentConfiguration.documentClass(), documentConfiguration);
        }
    }

    public void initialize(final CloudSearchClient cloudSearchClient) {
        this.cloudSearchClient = cloudSearchClient;
        initialized = true;
    }

    public DocumentConfigurationHolder documentConfigurationHolder() {
        return documentConfigurationHolder;
    }

    private DocumentConfiguration getDocumentConfiguration(final Class<? extends Document> documentClass) {
        if (!initialized) {
            throw new IllegalStateException("CloudSearchEngine not initialized");
        }
        final DocumentConfiguration documentConfiguration = documentConfigurations.get(documentClass);
        if (documentConfiguration == null) {
            throw new IllegalStateException("No document configuration found for: " + documentClass);
        }
        return documentConfiguration;
    }

    @Override
    public void update(final Document document) {
        final DocumentConfiguration documentConfiguration = getDocumentConfiguration(document.getClass());
        final String searchDomain = documentConfigurationHolder.schemaName() + "-" + documentConfiguration.namespace();
        final BatchDocumentUpdateRequest batchDocumentUpdateRequest = new BatchDocumentUpdateRequest(searchDomain);
        final DocumentUpdate csDocument = new DocumentUpdate(Type.ADD, document.getId());
        final Collection<Field> fields = new ArrayList<>();
        for (final PropertyDescriptor propertyDescriptor : documentConfiguration.properties().values()) {
            final Field field = new Field(propertyDescriptor.getName(), getPropertyValue(document, propertyDescriptor));
            fields.add(field);
        }
        csDocument.withFields(fields);
        batchDocumentUpdateRequest.withDocument(csDocument);
        cloudSearchClient.batchDocumentUpdate(batchDocumentUpdateRequest);
    }

    private Object getPropertyValue(final Document document, final PropertyDescriptor propertyDescriptor) {
        try {
            return propertyDescriptor.getReadMethod().invoke(document);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }

    }

    @Override
    public void delete(final Document document) {
        final DocumentConfiguration documentConfiguration = getDocumentConfiguration(document.getClass());
        final String searchDomain = documentConfigurationHolder.schemaName() + "-" + documentConfiguration.namespace();
        final BatchDocumentUpdateRequest batchDocumentUpdateRequest = new BatchDocumentUpdateRequest(searchDomain);
        final DocumentUpdate csDocument = new DocumentUpdate(Type.DELETE, document.getId());
        batchDocumentUpdateRequest.withDocument(csDocument);
        cloudSearchClient.batchDocumentUpdate(batchDocumentUpdateRequest);
    }

    /**
     * Passes the query to the correct cloud search domain
     * 
     * AWS could throw an exception on search for example the query may be invalid, we re throw this as a
     * @link(UnsuccessfulSearchException)
     */
    @Override
    public <T extends Document> DocumentSearchResponse<T> search(final Query query, final Integer start,
            final Integer size, final Class<T> documentClass) {
        try {
            final DocumentConfiguration documentConfiguration = getDocumentConfiguration(documentClass);
            final String searchDomain = documentConfigurationHolder.schemaName() + "-"
                    + documentConfiguration.namespace();

            return cloudSearchClient.searchDocuments(query, start, size, searchDomain, documentClass);
        } catch (final AmazonServiceException e) {
            throw new UnsuccessfulSearchException(e.getMessage());
        }
    }
}
