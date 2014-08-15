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

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.AmazonServiceException;
import com.clicktravel.common.random.Randoms;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.Document;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.DocumentSearchResponse;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.DocumentConfiguration;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.DocumentConfigurationHolder;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.exception.UnsuccessfulSearchException;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.query.Query;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.client.*;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.client.DocumentUpdate.Type;

public class CloudSearchEngineTest {

    @Test
    public void shouldCreateCloudSearchEngine_withDocumentConfigurationHolder() throws Exception {
        // Given
        final DocumentConfigurationHolder documentConfigurationHolder = mock(DocumentConfigurationHolder.class);

        // When
        final CloudSearchEngine cloudSearchEngine = new CloudSearchEngine(documentConfigurationHolder);

        // Then
        assertNotNull(cloudSearchEngine);
        assertEquals(documentConfigurationHolder, cloudSearchEngine.documentConfigurationHolder());
    }

    @Test
    public void shouldNotCreateCloudSearchEngine_withNullDocumentConfigurationHolder() throws Exception {
        // Given
        final DocumentConfigurationHolder documentConfigurationHolder = null;

        // When
        IllegalArgumentException actualException = null;
        try {
            new CloudSearchEngine(documentConfigurationHolder);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldInitializeCloudSearchEngine_withCloudSearchClient() throws Exception {
        // Given
        final DocumentConfigurationHolder documentConfigurationHolder = mock(DocumentConfigurationHolder.class);
        final CloudSearchClient cloudSearchClient = mock(CloudSearchClient.class);
        final CloudSearchEngine cloudSearchEngine = new CloudSearchEngine(documentConfigurationHolder);

        // When
        Exception actualException = null;
        try {
            cloudSearchEngine.initialize(cloudSearchClient);
        } catch (final Exception e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
    }

    @Test
    public void shouldUpdate_withDocument() throws Exception {
        // Given
        final StubDocument document = randomStubDocument();
        final String documentId = document.getId();
        final DocumentConfigurationHolder documentConfigurationHolder = mock(DocumentConfigurationHolder.class);
        final String namespace = documentId;
        final DocumentConfiguration mockStubDocumentConfiguration = mock(DocumentConfiguration.class);
        doReturn(StubDocument.class).when(mockStubDocumentConfiguration).documentClass();
        when(mockStubDocumentConfiguration.namespace()).thenReturn(namespace);
        final Map<String, PropertyDescriptor> properties = getStubDocumentPropertyDescriptors();
        when(mockStubDocumentConfiguration.properties()).thenReturn(properties);
        final Collection<DocumentConfiguration> documentConfigurations = Arrays.asList(mockStubDocumentConfiguration);
        final String schemaName = randomString(10);
        when(documentConfigurationHolder.schemaName()).thenReturn(schemaName);
        when(documentConfigurationHolder.documentConfigurations()).thenReturn(documentConfigurations);
        final CloudSearchEngine cloudSearchEngine = new CloudSearchEngine(documentConfigurationHolder);
        final CloudSearchClient mockCloudSearchClient = mock(CloudSearchClient.class);
        cloudSearchEngine.initialize(mockCloudSearchClient);

        // When
        cloudSearchEngine.update(document);

        // Then
        final ArgumentCaptor<BatchDocumentUpdateRequest> batchDocumentUpdateRequestArgumentCaptor = ArgumentCaptor
                .forClass(BatchDocumentUpdateRequest.class);
        verify(mockCloudSearchClient).batchDocumentUpdate(batchDocumentUpdateRequestArgumentCaptor.capture());
        final BatchDocumentUpdateRequest uploadDocumentBatchRequest = batchDocumentUpdateRequestArgumentCaptor
                .getValue();
        assertEquals(schemaName + "-" + namespace, uploadDocumentBatchRequest.getSearchDomain());
        assertEquals(1, uploadDocumentBatchRequest.getDocumentUpdates().size());
        final DocumentUpdate documentRequest = uploadDocumentBatchRequest.getDocumentUpdates().iterator().next();
        assertEquals(Type.ADD, documentRequest.getType());
        assertEquals(documentId, documentRequest.getId());
        assertEquals(properties.size(), documentRequest.getFields().size());
        for (final Field field : documentRequest.getFields()) {
            final PropertyDescriptor propertyDescriptor = properties.get(field.getName());
            assertEquals(propertyDescriptor.getReadMethod().invoke(document), field.getValue());
        }
    }

    @Test
    public void shouldDelete_withDocument() throws Exception {
        // Given
        final StubDocument document = randomStubDocument();
        final String documentId = document.getId();
        final DocumentConfigurationHolder documentConfigurationHolder = mock(DocumentConfigurationHolder.class);
        final String namespace = documentId;
        final DocumentConfiguration mockStubDocumentConfiguration = mock(DocumentConfiguration.class);
        doReturn(StubDocument.class).when(mockStubDocumentConfiguration).documentClass();
        when(mockStubDocumentConfiguration.namespace()).thenReturn(namespace);
        final Map<String, PropertyDescriptor> properties = getStubDocumentPropertyDescriptors();
        when(mockStubDocumentConfiguration.properties()).thenReturn(properties);
        final Collection<DocumentConfiguration> documentConfigurations = Arrays.asList(mockStubDocumentConfiguration);
        final String schemaName = randomString(10);
        when(documentConfigurationHolder.schemaName()).thenReturn(schemaName);
        when(documentConfigurationHolder.documentConfigurations()).thenReturn(documentConfigurations);
        final CloudSearchEngine cloudSearchEngine = new CloudSearchEngine(documentConfigurationHolder);
        final CloudSearchClient mockCloudSearchClient = mock(CloudSearchClient.class);
        cloudSearchEngine.initialize(mockCloudSearchClient);

        // When
        cloudSearchEngine.delete(document);

        // Then
        final ArgumentCaptor<BatchDocumentUpdateRequest> batchDocumentUpdateRequestArgumentCaptor = ArgumentCaptor
                .forClass(BatchDocumentUpdateRequest.class);
        verify(mockCloudSearchClient).batchDocumentUpdate(batchDocumentUpdateRequestArgumentCaptor.capture());
        final BatchDocumentUpdateRequest uploadDocumentBatchRequest = batchDocumentUpdateRequestArgumentCaptor
                .getValue();
        assertEquals(schemaName + "-" + namespace, uploadDocumentBatchRequest.getSearchDomain());
        assertEquals(1, uploadDocumentBatchRequest.getDocumentUpdates().size());
        final DocumentUpdate documentRequest = uploadDocumentBatchRequest.getDocumentUpdates().iterator().next();
        assertEquals(Type.DELETE, documentRequest.getType());
        assertEquals(documentId, documentRequest.getId());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldSearch_withQuery() throws Exception {
        // Given
        final StubDocument document = randomStubDocument();
        final String documentId = document.getId();
        final DocumentConfigurationHolder documentConfigurationHolder = mock(DocumentConfigurationHolder.class);
        final String namespace = documentId;
        final DocumentConfiguration mockStubDocumentConfiguration = mock(DocumentConfiguration.class);
        doReturn(StubDocument.class).when(mockStubDocumentConfiguration).documentClass();
        when(mockStubDocumentConfiguration.namespace()).thenReturn(namespace);
        final Map<String, PropertyDescriptor> properties = getStubDocumentPropertyDescriptors();
        when(mockStubDocumentConfiguration.properties()).thenReturn(properties);
        final Collection<DocumentConfiguration> documentConfigurations = Arrays.asList(mockStubDocumentConfiguration);
        final String schemaName = randomString(10);
        when(documentConfigurationHolder.schemaName()).thenReturn(schemaName);
        when(documentConfigurationHolder.documentConfigurations()).thenReturn(documentConfigurations);
        final Query query = mock(Query.class);
        final CloudSearchEngine cloudSearchEngine = new CloudSearchEngine(documentConfigurationHolder);
        final CloudSearchClient mockCloudSearchClient = mock(CloudSearchClient.class);

        final DocumentSearchResponse<Document> documents = mock(DocumentSearchResponse.class);

        final Integer start = Randoms.randomInt(100);
        final Integer size = Randoms.randomInt(100);

        when(
                mockCloudSearchClient.searchDocuments(any(Query.class), eq(start), eq(size), anyString(),
                        any(Class.class))).thenReturn(documents);
        cloudSearchEngine.initialize(mockCloudSearchClient);

        // When
        final DocumentSearchResponse<StubDocument> returnedDocuments = cloudSearchEngine.search(query, start, size,
                StubDocument.class);

        // Then
        assertNotNull(returnedDocuments);
        assertEquals(documents, returnedDocuments);
        verify(mockCloudSearchClient).searchDocuments(query, start, size, schemaName + "-" + namespace,
                StubDocument.class);
    }

    @Test(expected = UnsuccessfulSearchException.class)
    @SuppressWarnings("unchecked")
    public void shoulNotSearch_withQuery() throws Exception {
        // Given
        final StubDocument document = randomStubDocument();
        final String documentId = document.getId();
        final DocumentConfigurationHolder documentConfigurationHolder = mock(DocumentConfigurationHolder.class);
        final String namespace = documentId;
        final DocumentConfiguration mockStubDocumentConfiguration = mock(DocumentConfiguration.class);
        doReturn(StubDocument.class).when(mockStubDocumentConfiguration).documentClass();
        when(mockStubDocumentConfiguration.namespace()).thenReturn(namespace);
        final Map<String, PropertyDescriptor> properties = getStubDocumentPropertyDescriptors();
        when(mockStubDocumentConfiguration.properties()).thenReturn(properties);
        final Collection<DocumentConfiguration> documentConfigurations = Arrays.asList(mockStubDocumentConfiguration);
        final String schemaName = randomString(10);
        when(documentConfigurationHolder.schemaName()).thenReturn(schemaName);
        when(documentConfigurationHolder.documentConfigurations()).thenReturn(documentConfigurations);
        final Query query = mock(Query.class);
        final CloudSearchEngine cloudSearchEngine = new CloudSearchEngine(documentConfigurationHolder);
        final CloudSearchClient mockCloudSearchClient = mock(CloudSearchClient.class);

        final Integer start = Randoms.randomInt(100);
        final Integer size = Randoms.randomInt(100);

        when(
                mockCloudSearchClient.searchDocuments(any(Query.class), eq(start), eq(size), anyString(),
                        any(Class.class))).thenThrow(new AmazonServiceException(Randoms.randomString()));
        cloudSearchEngine.initialize(mockCloudSearchClient);

        // When
        cloudSearchEngine.search(query, start, size, StubDocument.class);

    }

    private StubDocument randomStubDocument() {
        final StubDocument stubDocument = new StubDocument();
        stubDocument.setId(randomString(10));
        return stubDocument;
    }

    private Map<String, PropertyDescriptor> getStubDocumentPropertyDescriptors() throws Exception {
        final Map<String, PropertyDescriptor> propertyDescriptors = new HashMap<>();
        final BeanInfo info = Introspector.getBeanInfo(StubDocument.class);
        for (final PropertyDescriptor propertyDescriptor : info.getPropertyDescriptors()) {
            if (!propertyDescriptor.getName().equals("class") && !propertyDescriptor.getName().equals("id")) {
                propertyDescriptors.put(propertyDescriptor.getName(), propertyDescriptor);
            }
        }
        return propertyDescriptors;
    }
}
