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

import static com.clicktravel.common.random.Randoms.randomEnum;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudsearchdomain.AmazonCloudSearchDomain;
import com.amazonaws.services.cloudsearchdomain.model.*;
import com.amazonaws.services.cloudsearchv2.AmazonCloudSearch;
import com.amazonaws.services.cloudsearchv2.model.DescribeDomainsRequest;
import com.amazonaws.services.cloudsearchv2.model.DescribeDomainsResult;
import com.amazonaws.services.cloudsearchv2.model.DomainStatus;
import com.amazonaws.services.cloudsearchv2.model.ServiceEndpoint;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.Document;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.DocumentSearchResponse;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.DocumentConfiguration;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.DocumentConfigurationHolder;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.options.SearchOptions;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.query.Query;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.query.QueryType;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.sort.SortOrder;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.sort.SortingOption;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.sort.SortingOption.Direction;
import com.clicktravel.cheddar.infrastructure.persistence.exception.PersistenceResourceFailureException;
import com.clicktravel.common.random.Randoms;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.client.DocumentUpdate;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.client.DocumentUpdate.Type;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.client.Field;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.client.JsonDocumentUpdateMarshaller;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AmazonCloudSearchDomainClientBuilder.class, JsonDocumentUpdateMarshaller.class })
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CloudSearchEngineTest {

    @Test
    public void shouldNotCreateCloudSearchEngine_withNullDocumentConfigurationHolder() throws Exception {
        // Given
        final DocumentConfigurationHolder documentConfigurationHolder = null;
        final AmazonCloudSearch client = mock(AmazonCloudSearch.class);

        // When
        IllegalArgumentException actualException = null;
        try {
            new CloudSearchEngine(documentConfigurationHolder, client);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldInitializeCloudSearchEngine_withDocumentConfigurationHolderAndClient() throws Exception {
        // Given
        final AmazonCloudSearch mockAmazonCloudSearch = mock(AmazonCloudSearch.class);

        final String namespace = randomString(10);
        final DocumentConfiguration mockStubDocumentConfiguration = mock(DocumentConfiguration.class);
        final Map<String, PropertyDescriptor> properties = getStubDocumentPropertyDescriptors();
        final Collection<DocumentConfiguration> documentConfigurations = Arrays.asList(mockStubDocumentConfiguration);
        final String schemaName = randomString(10);
        final DocumentConfigurationHolder documentConfigurationHolder = new DocumentConfigurationHolder(schemaName,
                documentConfigurations);
        final DescribeDomainsRequest describeDomainsRequest = new DescribeDomainsRequest()
                .withDomainNames(Arrays.asList(schemaName + "-" + namespace));
        final DescribeDomainsResult describeDomainsResult = new DescribeDomainsResult().withDomainStatusList();
        doReturn(StubDocument.class).when(mockStubDocumentConfiguration).documentClass();
        when(mockStubDocumentConfiguration.namespace()).thenReturn(namespace);
        when(mockStubDocumentConfiguration.properties()).thenReturn(properties);
        when(mockAmazonCloudSearch.describeDomains(describeDomainsRequest)).thenReturn(describeDomainsResult);

        // When
        Exception actualException = null;
        try {
            new CloudSearchEngine(documentConfigurationHolder, mockAmazonCloudSearch);
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
        final String namespace = randomString(10);
        final DocumentConfiguration mockStubDocumentConfiguration = mock(DocumentConfiguration.class);
        final Map<String, PropertyDescriptor> properties = getStubDocumentPropertyDescriptors();
        final Collection<DocumentConfiguration> documentConfigurations = Arrays.asList(mockStubDocumentConfiguration);
        final String schemaName = randomString(10);
        final DocumentConfigurationHolder documentConfigurationHolder = new DocumentConfigurationHolder(schemaName,
                documentConfigurations);
        final AmazonCloudSearch mockAmazonCloudSearch = mock(AmazonCloudSearch.class);
        final byte[] jsonBytes = randomString().getBytes(Charset.forName("UTF-8"));
        final String domainName = schemaName + "-" + namespace;
        final DescribeDomainsRequest describeDomainsRequest = new DescribeDomainsRequest()
                .withDomainNames(Arrays.asList(domainName));
        final String documentServiceEndpoint = randomString();
        final DescribeDomainsResult describeDomainsResult = getDescribeDomainsResult(domainName,
                documentServiceEndpoint, randomString());
        final AmazonCloudSearchDomain mockDocumentServiceClient = mock(AmazonCloudSearchDomain.class);
        mockStatic(JsonDocumentUpdateMarshaller.class);
        when(JsonDocumentUpdateMarshaller.marshall(anyCollection())).thenReturn(new String(jsonBytes));
        doReturn(StubDocument.class).when(mockStubDocumentConfiguration).documentClass();
        when(mockStubDocumentConfiguration.namespace()).thenReturn(namespace);
        when(mockStubDocumentConfiguration.properties()).thenReturn(properties);
        when(mockAmazonCloudSearch.describeDomains(describeDomainsRequest)).thenReturn(describeDomainsResult);
        mockStatic(AmazonCloudSearchDomainClientBuilder.class);
        when(AmazonCloudSearchDomainClientBuilder.build(documentServiceEndpoint)).thenReturn(mockDocumentServiceClient);

        final CloudSearchEngine cloudSearchEngine = new CloudSearchEngine(documentConfigurationHolder,
                mockAmazonCloudSearch);

        // When
        cloudSearchEngine.update(document);

        // Then
        final ArgumentCaptor<Collection> documentUpdateCollectionCaptor = ArgumentCaptor.forClass(Collection.class);
        final ArgumentCaptor<UploadDocumentsRequest> uploadDocumentsRequestCaptor = ArgumentCaptor
                .forClass(UploadDocumentsRequest.class);
        PowerMockito.verifyStatic();
        JsonDocumentUpdateMarshaller.marshall(documentUpdateCollectionCaptor.capture());
        verify(mockDocumentServiceClient).uploadDocuments(uploadDocumentsRequestCaptor.capture());
        final DocumentUpdate documentUpdate = (DocumentUpdate) documentUpdateCollectionCaptor.getValue().iterator()
                .next();
        assertEquals(document.getId(), documentUpdate.getId());
        assertEquals(Type.ADD, documentUpdate.getType());
        for (final Field field : documentUpdate.getFields()) {
            if (field.getName().equals("stringProperty")) {
                assertEquals(document.getStringProperty(), field.getValue());
            }
        }
        final UploadDocumentsRequest uploadDocumentsRequest = uploadDocumentsRequestCaptor.getValue();
        assertInputStreamEquals(jsonBytes, uploadDocumentsRequest.getDocuments());
    }

    @Test
    public void shouldUpdate_withDocumentThatHasAttributesWithWhitespace() throws Exception {
        // Given
        final StubDocument document = new StubDocument();
        final String whitespaceCharset = " \t\n\f\r";
        final String propertyValue = randomString(10);
        final String namespace = randomString(10);
        final DocumentConfiguration mockStubDocumentConfiguration = mock(DocumentConfiguration.class);
        final Map<String, PropertyDescriptor> properties = getStubDocumentPropertyDescriptors();
        final Collection<DocumentConfiguration> documentConfigurations = Arrays.asList(mockStubDocumentConfiguration);
        final String schemaName = randomString(10);
        final DocumentConfigurationHolder documentConfigurationHolder = new DocumentConfigurationHolder(schemaName,
                documentConfigurations);
        final AmazonCloudSearch mockAmazonCloudSearch = mock(AmazonCloudSearch.class);
        final byte[] jsonBytes = randomString().getBytes(Charset.forName("UTF-8"));
        final String domainName = schemaName + "-" + namespace;
        final DescribeDomainsRequest describeDomainsRequest = new DescribeDomainsRequest()
                .withDomainNames(Arrays.asList(domainName));
        final String documentServiceEndpoint = randomString();
        final DescribeDomainsResult describeDomainsResult = getDescribeDomainsResult(domainName,
                documentServiceEndpoint, randomString());
        final AmazonCloudSearchDomain mockDocumentServiceClient = mock(AmazonCloudSearchDomain.class);
        mockStatic(JsonDocumentUpdateMarshaller.class);
        when(JsonDocumentUpdateMarshaller.marshall(anyCollection())).thenReturn(new String(jsonBytes));
        doReturn(StubDocument.class).when(mockStubDocumentConfiguration).documentClass();
        when(mockStubDocumentConfiguration.namespace()).thenReturn(namespace);
        when(mockStubDocumentConfiguration.properties()).thenReturn(properties);
        when(mockAmazonCloudSearch.describeDomains(describeDomainsRequest)).thenReturn(describeDomainsResult);
        mockStatic(AmazonCloudSearchDomainClientBuilder.class);
        when(AmazonCloudSearchDomainClientBuilder.build(documentServiceEndpoint)).thenReturn(mockDocumentServiceClient);

        document.setId(randomString(10));
        document.setStringProperty(whitespaceCharset.charAt(Randoms.randomInt(whitespaceCharset.length()))
                + propertyValue + Randoms.randomInt(whitespaceCharset.length()));

        final CloudSearchEngine cloudSearchEngine = new CloudSearchEngine(documentConfigurationHolder,
                mockAmazonCloudSearch);

        // When
        cloudSearchEngine.update(document);

        // Then
        final ArgumentCaptor<Collection> documentUpdateCollectionCaptor = ArgumentCaptor.forClass(Collection.class);
        final ArgumentCaptor<UploadDocumentsRequest> uploadDocumentsRequestCaptor = ArgumentCaptor
                .forClass(UploadDocumentsRequest.class);
        PowerMockito.verifyStatic();
        JsonDocumentUpdateMarshaller.marshall(documentUpdateCollectionCaptor.capture());
        verify(mockDocumentServiceClient).uploadDocuments(uploadDocumentsRequestCaptor.capture());
        final DocumentUpdate documentUpdate = (DocumentUpdate) documentUpdateCollectionCaptor.getValue().iterator()
                .next();
        assertEquals(document.getId(), documentUpdate.getId());
        assertEquals(Type.ADD, documentUpdate.getType());
        for (final Field field : documentUpdate.getFields()) {
            if (field.getName().equals("stringProperty")) {
                assertEquals(propertyValue, field.getValue());
            }
        }
        final UploadDocumentsRequest uploadDocumentsRequest = uploadDocumentsRequestCaptor.getValue();
        assertInputStreamEquals(jsonBytes, uploadDocumentsRequest.getDocuments());
    }

    @Test
    public void shouldUpdate_withDocuments() throws Exception {
        // Given
        final int numberOfDocumentsToUpdate = Randoms.randomIntInRange(1, 3000);
        final Collection<StubDocument> documents = randomCollectionOfStubDocuments(numberOfDocumentsToUpdate);
        final String namespace = randomString(10);
        final DocumentConfiguration mockStubDocumentConfiguration = mock(DocumentConfiguration.class);
        final Map<String, PropertyDescriptor> properties = getStubDocumentPropertyDescriptors();
        final Collection<DocumentConfiguration> documentConfigurations = Arrays.asList(mockStubDocumentConfiguration);
        final String schemaName = randomString(10);
        final DocumentConfigurationHolder documentConfigurationHolder = new DocumentConfigurationHolder(schemaName,
                documentConfigurations);
        final AmazonCloudSearch mockAmazonCloudSearch = mock(AmazonCloudSearch.class);
        final byte[] jsonBytes = randomString().getBytes(Charset.forName("UTF-8"));
        final String domainName = schemaName + "-" + namespace;
        final DescribeDomainsRequest describeDomainsRequest = new DescribeDomainsRequest()
                .withDomainNames(Arrays.asList(domainName));
        final String documentServiceEndpoint = randomString();
        final DescribeDomainsResult describeDomainsResult = getDescribeDomainsResult(domainName,
                documentServiceEndpoint, randomString());
        final AmazonCloudSearchDomain mockDocumentServiceClient = mock(AmazonCloudSearchDomain.class);
        mockStatic(JsonDocumentUpdateMarshaller.class);
        when(JsonDocumentUpdateMarshaller.marshall(anyCollection())).thenReturn(new String(jsonBytes));
        doReturn(StubDocument.class).when(mockStubDocumentConfiguration).documentClass();
        when(mockStubDocumentConfiguration.namespace()).thenReturn(namespace);
        when(mockStubDocumentConfiguration.properties()).thenReturn(properties);
        when(mockAmazonCloudSearch.describeDomains(describeDomainsRequest)).thenReturn(describeDomainsResult);
        mockStatic(AmazonCloudSearchDomainClientBuilder.class);
        when(AmazonCloudSearchDomainClientBuilder.build(documentServiceEndpoint)).thenReturn(mockDocumentServiceClient);

        final CloudSearchEngine cloudSearchEngine = new CloudSearchEngine(documentConfigurationHolder,
                mockAmazonCloudSearch);

        // When
        cloudSearchEngine.update(documents);

        // Then
        final ArgumentCaptor<Collection> documentUpdateCollectionCaptor = ArgumentCaptor.forClass(Collection.class);
        final ArgumentCaptor<UploadDocumentsRequest> uploadDocumentsRequestCaptor = ArgumentCaptor
                .forClass(UploadDocumentsRequest.class);
        final int numberOfBatches = (numberOfDocumentsToUpdate / 1000) + 1;
        PowerMockito.verifyStatic(times(numberOfBatches));
        JsonDocumentUpdateMarshaller.marshall(documentUpdateCollectionCaptor.capture());
        verify(mockDocumentServiceClient, times(numberOfBatches))
                .uploadDocuments(uploadDocumentsRequestCaptor.capture());

        for (final Collection<DocumentUpdate> documentUpdates : documentUpdateCollectionCaptor.getAllValues()) {
            assertTrue(documentUpdates.size() <= 1000);
            for (final DocumentUpdate documentUpdate : documentUpdates) {
                assertEquals(Type.ADD, documentUpdate.getType());
                for (final Field field : documentUpdate.getFields()) {
                    if (field.getName().equals("stringProperty")) {
                        assertTrue(documents.stream().anyMatch(d -> d.getStringProperty().equals(field.getValue())));
                    }
                }
            }
        }

        for (final UploadDocumentsRequest uploadDocumentsRequest : uploadDocumentsRequestCaptor.getAllValues()) {
            assertInputStreamEquals(jsonBytes, uploadDocumentsRequest.getDocuments());
        }
    }

    @Test
    public void shouldNotUpdate_withDocumentsNotInstancesOfTheSameClass() throws Exception {
        // Given
        final Collection<Document> documents = Arrays.asList(mock(Document.class), randomStubDocument());
        final String namespace = randomString(10);
        final DocumentConfiguration mockStubDocumentConfiguration = mock(DocumentConfiguration.class);
        final Map<String, PropertyDescriptor> properties = getStubDocumentPropertyDescriptors();
        final Collection<DocumentConfiguration> documentConfigurations = Arrays.asList(mockStubDocumentConfiguration);
        final String schemaName = randomString(10);
        final DocumentConfigurationHolder documentConfigurationHolder = new DocumentConfigurationHolder(schemaName,
                documentConfigurations);
        final AmazonCloudSearch mockAmazonCloudSearch = mock(AmazonCloudSearch.class);
        final byte[] jsonBytes = randomString().getBytes(Charset.forName("UTF-8"));
        final String domainName = schemaName + "-" + namespace;
        final DescribeDomainsRequest describeDomainsRequest = new DescribeDomainsRequest()
                .withDomainNames(Arrays.asList(domainName));
        final String documentServiceEndpoint = randomString();
        final DescribeDomainsResult describeDomainsResult = getDescribeDomainsResult(domainName,
                documentServiceEndpoint, randomString());
        final AmazonCloudSearchDomain mockDocumentServiceClient = mock(AmazonCloudSearchDomain.class);
        mockStatic(JsonDocumentUpdateMarshaller.class);
        when(JsonDocumentUpdateMarshaller.marshall(anyCollection())).thenReturn(new String(jsonBytes));
        doReturn(StubDocument.class).when(mockStubDocumentConfiguration).documentClass();
        when(mockStubDocumentConfiguration.namespace()).thenReturn(namespace);
        when(mockStubDocumentConfiguration.properties()).thenReturn(properties);
        when(mockAmazonCloudSearch.describeDomains(describeDomainsRequest)).thenReturn(describeDomainsResult);
        mockStatic(AmazonCloudSearchDomainClientBuilder.class);
        when(AmazonCloudSearchDomainClientBuilder.build(documentServiceEndpoint)).thenReturn(mockDocumentServiceClient);

        final CloudSearchEngine cloudSearchEngine = new CloudSearchEngine(documentConfigurationHolder,
                mockAmazonCloudSearch);

        // When
        IllegalArgumentException thrownException = null;
        try {
            cloudSearchEngine.update(documents);
        } catch (final IllegalArgumentException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    private DescribeDomainsResult getDescribeDomainsResult(final String domainName,
            final String documentServiceEndpoint, final String searchServiceEndpoint) {
        return new DescribeDomainsResult()
                .withDomainStatusList(new DomainStatus().withDomainName(domainName).withCreated(true).withDeleted(false)
                        .withDocService(new ServiceEndpoint().withEndpoint(documentServiceEndpoint))
                        .withSearchService(new ServiceEndpoint().withEndpoint(searchServiceEndpoint)));
    }

    @Test
    public void shouldDelete_withDocument() throws Exception {
        // Given
        final StubDocument document = randomStubDocument();
        final String namespace = randomString(10);
        final DocumentConfiguration mockStubDocumentConfiguration = mock(DocumentConfiguration.class);
        final Map<String, PropertyDescriptor> properties = getStubDocumentPropertyDescriptors();
        final Collection<DocumentConfiguration> documentConfigurations = Arrays.asList(mockStubDocumentConfiguration);
        final String schemaName = randomString(10);
        final DocumentConfigurationHolder documentConfigurationHolder = new DocumentConfigurationHolder(schemaName,
                documentConfigurations);
        final AmazonCloudSearch mockAmazonCloudSearch = mock(AmazonCloudSearch.class);
        final byte[] jsonBytes = randomString().getBytes(Charset.forName("UTF-8"));
        final String domainName = schemaName + "-" + namespace;
        final DescribeDomainsRequest describeDomainsRequest = new DescribeDomainsRequest()
                .withDomainNames(Arrays.asList(domainName));
        final String documentServiceEndpoint = randomString();
        final DescribeDomainsResult describeDomainsResult = getDescribeDomainsResult(domainName,
                documentServiceEndpoint, randomString());
        final AmazonCloudSearchDomain mockDocumentServiceClient = mock(AmazonCloudSearchDomain.class);
        mockStatic(JsonDocumentUpdateMarshaller.class);
        when(JsonDocumentUpdateMarshaller.marshall(anyCollection())).thenReturn(new String(jsonBytes));
        doReturn(StubDocument.class).when(mockStubDocumentConfiguration).documentClass();
        when(mockStubDocumentConfiguration.namespace()).thenReturn(namespace);
        when(mockStubDocumentConfiguration.properties()).thenReturn(properties);
        when(mockAmazonCloudSearch.describeDomains(describeDomainsRequest)).thenReturn(describeDomainsResult);
        mockStatic(AmazonCloudSearchDomainClientBuilder.class);
        when(AmazonCloudSearchDomainClientBuilder.build(documentServiceEndpoint)).thenReturn(mockDocumentServiceClient);

        final CloudSearchEngine cloudSearchEngine = new CloudSearchEngine(documentConfigurationHolder,
                mockAmazonCloudSearch);

        // When
        cloudSearchEngine.delete(document);

        // Then
        final ArgumentCaptor<Collection> documentUpdateCollectionCaptor = ArgumentCaptor.forClass(Collection.class);
        final ArgumentCaptor<UploadDocumentsRequest> uploadDocumentsRequestCaptor = ArgumentCaptor
                .forClass(UploadDocumentsRequest.class);
        PowerMockito.verifyStatic();
        JsonDocumentUpdateMarshaller.marshall(documentUpdateCollectionCaptor.capture());
        verify(mockDocumentServiceClient).uploadDocuments(uploadDocumentsRequestCaptor.capture());
        final DocumentUpdate documentUpdate = (DocumentUpdate) documentUpdateCollectionCaptor.getValue().iterator()
                .next();
        assertEquals(document.getId(), documentUpdate.getId());
        assertEquals(Type.DELETE, documentUpdate.getType());
        final UploadDocumentsRequest uploadDocumentsRequest = uploadDocumentsRequestCaptor.getValue();
        assertInputStreamEquals(jsonBytes, uploadDocumentsRequest.getDocuments());
    }

    @Test
    public void shouldNotDelete_withDocumentsNotInstancesOfTheSameClass() throws Exception {
        // Given
        final Collection<Document> documents = Arrays.asList(mock(Document.class), randomStubDocument());
        final String namespace = randomString(10);
        final DocumentConfiguration mockStubDocumentConfiguration = mock(DocumentConfiguration.class);
        final Map<String, PropertyDescriptor> properties = getStubDocumentPropertyDescriptors();
        final Collection<DocumentConfiguration> documentConfigurations = Arrays.asList(mockStubDocumentConfiguration);
        final String schemaName = randomString(10);
        final DocumentConfigurationHolder documentConfigurationHolder = new DocumentConfigurationHolder(schemaName,
                documentConfigurations);
        final AmazonCloudSearch mockAmazonCloudSearch = mock(AmazonCloudSearch.class);
        final byte[] jsonBytes = randomString().getBytes(Charset.forName("UTF-8"));
        final String domainName = schemaName + "-" + namespace;
        final DescribeDomainsRequest describeDomainsRequest = new DescribeDomainsRequest()
                .withDomainNames(Arrays.asList(domainName));
        final String documentServiceEndpoint = randomString();
        final DescribeDomainsResult describeDomainsResult = getDescribeDomainsResult(domainName,
                documentServiceEndpoint, randomString());
        final AmazonCloudSearchDomain mockDocumentServiceClient = mock(AmazonCloudSearchDomain.class);
        mockStatic(JsonDocumentUpdateMarshaller.class);
        when(JsonDocumentUpdateMarshaller.marshall(anyCollection())).thenReturn(new String(jsonBytes));
        doReturn(StubDocument.class).when(mockStubDocumentConfiguration).documentClass();
        when(mockStubDocumentConfiguration.namespace()).thenReturn(namespace);
        when(mockStubDocumentConfiguration.properties()).thenReturn(properties);
        when(mockAmazonCloudSearch.describeDomains(describeDomainsRequest)).thenReturn(describeDomainsResult);
        mockStatic(AmazonCloudSearchDomainClientBuilder.class);
        when(AmazonCloudSearchDomainClientBuilder.build(documentServiceEndpoint)).thenReturn(mockDocumentServiceClient);

        final CloudSearchEngine cloudSearchEngine = new CloudSearchEngine(documentConfigurationHolder,
                mockAmazonCloudSearch);

        // When
        IllegalArgumentException thrownException = null;
        try {
            cloudSearchEngine.delete(documents);
        } catch (final IllegalArgumentException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldDelete_withDocuments() throws Exception {
        // Given
        final Collection<StubDocument> documents = randomCollectionOfStubDocuments(Randoms.randomIntInRange(1, 10));
        final String namespace = randomString(10);
        final DocumentConfiguration mockStubDocumentConfiguration = mock(DocumentConfiguration.class);
        final Map<String, PropertyDescriptor> properties = getStubDocumentPropertyDescriptors();
        final Collection<DocumentConfiguration> documentConfigurations = Arrays.asList(mockStubDocumentConfiguration);
        final String schemaName = randomString(10);
        final DocumentConfigurationHolder documentConfigurationHolder = new DocumentConfigurationHolder(schemaName,
                documentConfigurations);
        final AmazonCloudSearch mockAmazonCloudSearch = mock(AmazonCloudSearch.class);
        final byte[] jsonBytes = randomString().getBytes(Charset.forName("UTF-8"));
        final String domainName = schemaName + "-" + namespace;
        final DescribeDomainsRequest describeDomainsRequest = new DescribeDomainsRequest()
                .withDomainNames(Arrays.asList(domainName));
        final String documentServiceEndpoint = randomString();
        final DescribeDomainsResult describeDomainsResult = getDescribeDomainsResult(domainName,
                documentServiceEndpoint, randomString());
        final AmazonCloudSearchDomain mockDocumentServiceClient = mock(AmazonCloudSearchDomain.class);
        mockStatic(JsonDocumentUpdateMarshaller.class);
        when(JsonDocumentUpdateMarshaller.marshall(anyCollection())).thenReturn(new String(jsonBytes));
        doReturn(StubDocument.class).when(mockStubDocumentConfiguration).documentClass();
        when(mockStubDocumentConfiguration.namespace()).thenReturn(namespace);
        when(mockStubDocumentConfiguration.properties()).thenReturn(properties);
        when(mockAmazonCloudSearch.describeDomains(describeDomainsRequest)).thenReturn(describeDomainsResult);
        mockStatic(AmazonCloudSearchDomainClientBuilder.class);
        when(AmazonCloudSearchDomainClientBuilder.build(documentServiceEndpoint)).thenReturn(mockDocumentServiceClient);

        final CloudSearchEngine cloudSearchEngine = new CloudSearchEngine(documentConfigurationHolder,
                mockAmazonCloudSearch);

        // When
        cloudSearchEngine.delete(documents);

        // Then
        final ArgumentCaptor<Collection> documentUpdateCollectionCaptor = ArgumentCaptor.forClass(Collection.class);
        final ArgumentCaptor<UploadDocumentsRequest> uploadDocumentsRequestCaptor = ArgumentCaptor
                .forClass(UploadDocumentsRequest.class);
        PowerMockito.verifyStatic();
        JsonDocumentUpdateMarshaller.marshall(documentUpdateCollectionCaptor.capture());
        verify(mockDocumentServiceClient).uploadDocuments(uploadDocumentsRequestCaptor.capture());
        final Collection<DocumentUpdate> documentUpdates = documentUpdateCollectionCaptor.getValue();
        assertEquals(documents.size(), documentUpdates.size());
        for (final DocumentUpdate documentUpdate : documentUpdates) {
            assertEquals(Type.DELETE, documentUpdate.getType());
            assertTrue(documents.stream().anyMatch(d -> d.getId().equals(documentUpdate.getId())));
        }
        final UploadDocumentsRequest uploadDocumentsRequest = uploadDocumentsRequestCaptor.getValue();
        assertInputStreamEquals(jsonBytes, uploadDocumentsRequest.getDocuments());
    }

    @Test
    public void shouldSearch_withQuery() throws Exception {
        // Given
        final StubDocument document = randomStubDocument();
        final String documentId = document.getId();
        final DocumentConfigurationHolder documentConfigurationHolder = mock(DocumentConfigurationHolder.class);
        final String namespace = documentId;
        final DocumentConfiguration mockStubDocumentConfiguration = mock(DocumentConfiguration.class);
        final Map<String, PropertyDescriptor> properties = getStubDocumentPropertyDescriptors();
        final Collection<DocumentConfiguration> documentConfigurations = Arrays.asList(mockStubDocumentConfiguration);
        final String schemaName = randomString(10);
        final Query query = mock(Query.class);
        final QueryType queryType = randomEnum(QueryType.class);
        final AmazonCloudSearchDomain mockCloudSearchClient = mock(AmazonCloudSearchDomain.class);
        final Integer start = Randoms.randomInt(100);
        final Integer size = Randoms.randomInt(100);
        final String searchServiceEndpoint = randomString();
        final String domainName = schemaName + "-" + namespace;
        final DescribeDomainsResult describeDomainsResult = getDescribeDomainsResult(domainName, randomString(),
                searchServiceEndpoint);
        final AmazonCloudSearch mockAmazonCloudSearch = mock(AmazonCloudSearch.class);
        final DescribeDomainsRequest describeDomainsRequest = new DescribeDomainsRequest()
                .withDomainNames(Arrays.asList(domainName));
        final SearchRequest searchRequest = buildSearchRequest(queryType, query);
        final SearchResult searchResult = new SearchResult().withHits(getExpectedHits(document));
        mockStatic(AmazonCloudSearchDomainClientBuilder.class);
        when(AmazonCloudSearchDomainClientBuilder.build(searchServiceEndpoint)).thenReturn(mockCloudSearchClient);
        doReturn(StubDocument.class).when(mockStubDocumentConfiguration).documentClass();
        when(mockStubDocumentConfiguration.properties()).thenReturn(properties);
        when(mockStubDocumentConfiguration.namespace()).thenReturn(namespace);
        when(mockAmazonCloudSearch.describeDomains(describeDomainsRequest)).thenReturn(describeDomainsResult);
        when(documentConfigurationHolder.schemaName()).thenReturn(schemaName);
        when(documentConfigurationHolder.documentConfigurations()).thenReturn(documentConfigurations);
        when(query.queryType()).thenReturn(queryType);
        searchRequest.withStart((long) start);
        searchRequest.withSize((long) size);
        when(mockCloudSearchClient.search(searchRequest)).thenReturn(searchResult);

        final CloudSearchEngine cloudSearchEngine = new CloudSearchEngine(documentConfigurationHolder,
                mockAmazonCloudSearch);

        // When
        final DocumentSearchResponse<StubDocument> returnedDocuments = cloudSearchEngine.search(query, start, size,
                StubDocument.class);

        // Then
        assertNotNull(returnedDocuments);
        assertEquals(document, returnedDocuments.getHits().get(0));
    }

    @Test
    public void shouldSearch_withExpression() throws Exception {
        // Given
        final StubDocument document = randomStubDocument();
        final String documentId = document.getId();
        final DocumentConfigurationHolder documentConfigurationHolder = mock(DocumentConfigurationHolder.class);
        final String namespace = documentId;
        final DocumentConfiguration mockStubDocumentConfiguration = mock(DocumentConfiguration.class);
        final Map<String, PropertyDescriptor> properties = getStubDocumentPropertyDescriptors();
        final Collection<DocumentConfiguration> documentConfigurations = Arrays.asList(mockStubDocumentConfiguration);
        final String schemaName = randomString(10);
        final Query query = mock(Query.class);
        final QueryType queryType = randomEnum(QueryType.class);
        final AmazonCloudSearchDomain mockCloudSearchClient = mock(AmazonCloudSearchDomain.class);
        final Integer start = Randoms.randomInt(100);
        final Integer size = Randoms.randomInt(100);
        final String searchServiceEndpoint = randomString();
        final String domainName = schemaName + "-" + namespace;
        final DescribeDomainsResult describeDomainsResult = getDescribeDomainsResult(domainName, randomString(),
                searchServiceEndpoint);
        final AmazonCloudSearch mockAmazonCloudSearch = mock(AmazonCloudSearch.class);
        final DescribeDomainsRequest describeDomainsRequest = new DescribeDomainsRequest()
                .withDomainNames(Arrays.asList(domainName));
        final SearchResult searchResult = new SearchResult().withHits(getExpectedHits(document));
        mockStatic(AmazonCloudSearchDomainClientBuilder.class);
        when(AmazonCloudSearchDomainClientBuilder.build(searchServiceEndpoint)).thenReturn(mockCloudSearchClient);
        doReturn(StubDocument.class).when(mockStubDocumentConfiguration).documentClass();
        when(mockStubDocumentConfiguration.properties()).thenReturn(properties);
        when(mockStubDocumentConfiguration.namespace()).thenReturn(namespace);
        when(mockAmazonCloudSearch.describeDomains(describeDomainsRequest)).thenReturn(describeDomainsResult);
        when(documentConfigurationHolder.schemaName()).thenReturn(schemaName);
        when(documentConfigurationHolder.documentConfigurations()).thenReturn(documentConfigurations);
        when(query.queryType()).thenReturn(queryType);
        when(mockCloudSearchClient.search(any(SearchRequest.class))).thenReturn(searchResult);

        final CloudSearchEngine cloudSearchEngine = new CloudSearchEngine(documentConfigurationHolder,
                mockAmazonCloudSearch);

        final Map<String, String> expressions = new HashMap<String, String>();
        expressions.put("key", "expression");

        final SearchOptions options = new SearchOptions().withExpressions(expressions);
        // When
        cloudSearchEngine.search(query, start, size, StubDocument.class, options);

        // Then
        final ArgumentCaptor<SearchRequest> searchRequestCaptor = ArgumentCaptor.forClass(SearchRequest.class);

        verify(mockCloudSearchClient).search(searchRequestCaptor.capture());

        final SearchRequest request = searchRequestCaptor.getValue();
        assertEquals(request.getExpr(), "{\"key\":\"expression\"}");
    }

    @Test
    public void shouldSearch_withSortInQuery() throws Exception {
        // Given
        final StubDocument document = randomStubDocument();
        final String documentId = document.getId();
        final DocumentConfigurationHolder documentConfigurationHolder = mock(DocumentConfigurationHolder.class);
        final String namespace = documentId;
        final DocumentConfiguration mockStubDocumentConfiguration = mock(DocumentConfiguration.class);
        final Map<String, PropertyDescriptor> properties = getStubDocumentPropertyDescriptors();
        final Collection<DocumentConfiguration> documentConfigurations = Arrays.asList(mockStubDocumentConfiguration);
        final String schemaName = randomString(10);
        final Query query = mock(Query.class);
        final QueryType queryType = Randoms.randomEnum(QueryType.class);
        final AmazonCloudSearchDomain mockCloudSearchClient = mock(AmazonCloudSearchDomain.class);
        final Integer start = Randoms.randomInt(100);
        final Integer size = Randoms.randomInt(100);
        final String searchServiceEndpoint = randomString();
        final String domainName = schemaName + "-" + namespace;
        final DescribeDomainsResult describeDomainsResult = getDescribeDomainsResult(domainName, randomString(),
                searchServiceEndpoint);
        final AmazonCloudSearch mockAmazonCloudSearch = mock(AmazonCloudSearch.class);
        final DescribeDomainsRequest describeDomainsRequest = new DescribeDomainsRequest()
                .withDomainNames(Arrays.asList(domainName));
        final SearchRequest searchRequest = buildSearchRequest(queryType, query);
        final SearchResult searchResult = new SearchResult().withHits(getExpectedHits(document));
        final SortOrder sortOrder = new SortOrder();
        sortOrder.addSortingOption(new SortingOption(Randoms.randomString(), Direction.DESCENDING));
        sortOrder.addSortingOption(new SortingOption(Randoms.randomString(), Direction.ASCENDING));

        mockStatic(AmazonCloudSearchDomainClientBuilder.class);
        when(AmazonCloudSearchDomainClientBuilder.build(searchServiceEndpoint)).thenReturn(mockCloudSearchClient);
        doReturn(StubDocument.class).when(mockStubDocumentConfiguration).documentClass();
        when(mockStubDocumentConfiguration.properties()).thenReturn(properties);
        when(mockStubDocumentConfiguration.namespace()).thenReturn(namespace);
        when(mockAmazonCloudSearch.describeDomains(describeDomainsRequest)).thenReturn(describeDomainsResult);
        when(documentConfigurationHolder.schemaName()).thenReturn(schemaName);
        when(documentConfigurationHolder.documentConfigurations()).thenReturn(documentConfigurations);
        when(query.queryType()).thenReturn(queryType);

        searchRequest.withStart((long) start);
        searchRequest.withSize((long) size);
        final StringBuilder sort = new StringBuilder();
        String direction = null;
        int count = 0;
        for (final SortingOption sortingOption : sortOrder.sortingOptions()) {
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
            if (count < sortOrder.sortingOptions().size()) {
                sort.append(", ");
            }
        }
        searchRequest.setSort(sort.toString());
        when(mockCloudSearchClient.search(searchRequest)).thenReturn(searchResult);

        final CloudSearchEngine cloudSearchEngine = new CloudSearchEngine(documentConfigurationHolder,
                mockAmazonCloudSearch);

        // When
        final DocumentSearchResponse<StubDocument> returnedDocuments = cloudSearchEngine.search(query, start, size,
                StubDocument.class, new SearchOptions().withSortOrder(sortOrder));

        // Then
        assertNotNull(returnedDocuments);
        assertEquals(document, returnedDocuments.getHits().get(0));
    }

    @Test
    public void shouldSearch_withDefaultSortInQuery() throws Exception {
        // Given
        final StubDocument document = randomStubDocument();
        final String documentId = document.getId();
        final DocumentConfigurationHolder documentConfigurationHolder = mock(DocumentConfigurationHolder.class);
        final String namespace = documentId;
        final DocumentConfiguration mockStubDocumentConfiguration = mock(DocumentConfiguration.class);
        final Map<String, PropertyDescriptor> properties = getStubDocumentPropertyDescriptors();
        final Collection<DocumentConfiguration> documentConfigurations = Arrays.asList(mockStubDocumentConfiguration);
        final String schemaName = randomString(10);
        final Query query = mock(Query.class);
        final QueryType queryType = Randoms.randomEnum(QueryType.class);
        final AmazonCloudSearchDomain mockCloudSearchClient = mock(AmazonCloudSearchDomain.class);
        final Integer start = Randoms.randomInt(100);
        final Integer size = Randoms.randomInt(100);
        final String searchServiceEndpoint = randomString();
        final String domainName = schemaName + "-" + namespace;
        final DescribeDomainsResult describeDomainsResult = getDescribeDomainsResult(domainName, randomString(),
                searchServiceEndpoint);
        final AmazonCloudSearch mockAmazonCloudSearch = mock(AmazonCloudSearch.class);
        final DescribeDomainsRequest describeDomainsRequest = new DescribeDomainsRequest()
                .withDomainNames(Arrays.asList(domainName));
        final SearchRequest searchRequest = buildSearchRequest(queryType, query);
        final SearchResult searchResult = new SearchResult().withHits(getExpectedHits(document));

        mockStatic(AmazonCloudSearchDomainClientBuilder.class);
        when(AmazonCloudSearchDomainClientBuilder.build(searchServiceEndpoint)).thenReturn(mockCloudSearchClient);
        doReturn(StubDocument.class).when(mockStubDocumentConfiguration).documentClass();
        when(mockStubDocumentConfiguration.properties()).thenReturn(properties);
        when(mockStubDocumentConfiguration.namespace()).thenReturn(namespace);
        when(mockAmazonCloudSearch.describeDomains(describeDomainsRequest)).thenReturn(describeDomainsResult);
        when(documentConfigurationHolder.schemaName()).thenReturn(schemaName);
        when(documentConfigurationHolder.documentConfigurations()).thenReturn(documentConfigurations);
        when(query.queryType()).thenReturn(queryType);

        searchRequest.withStart((long) start);
        searchRequest.withSize((long) size);

        when(mockCloudSearchClient.search(searchRequest)).thenReturn(searchResult);

        final CloudSearchEngine cloudSearchEngine = new CloudSearchEngine(documentConfigurationHolder,
                mockAmazonCloudSearch);

        // When
        final DocumentSearchResponse<StubDocument> returnedDocuments = cloudSearchEngine.search(query, start, size,
                StubDocument.class, SearchOptions.DEFAULT);

        // Then
        assertNotNull(returnedDocuments);
        assertEquals(document, returnedDocuments.getHits().get(0));
    }

    @Test
    public void shouldNotSearch_withNullSearchOptions() throws Exception {
        // Given
        final StubDocument document = randomStubDocument();
        final String documentId = document.getId();
        final DocumentConfigurationHolder documentConfigurationHolder = mock(DocumentConfigurationHolder.class);
        final String namespace = documentId;
        final DocumentConfiguration mockStubDocumentConfiguration = mock(DocumentConfiguration.class);
        final Map<String, PropertyDescriptor> properties = getStubDocumentPropertyDescriptors();
        final Collection<DocumentConfiguration> documentConfigurations = Arrays.asList(mockStubDocumentConfiguration);
        final String schemaName = randomString(10);
        final AmazonCloudSearchDomain mockCloudSearchClient = mock(AmazonCloudSearchDomain.class);
        final String searchServiceEndpoint = randomString();
        final String domainName = schemaName + "-" + namespace;
        final DescribeDomainsResult describeDomainsResult = getDescribeDomainsResult(domainName, randomString(),
                searchServiceEndpoint);
        final AmazonCloudSearch mockAmazonCloudSearch = mock(AmazonCloudSearch.class);
        final DescribeDomainsRequest describeDomainsRequest = new DescribeDomainsRequest()
                .withDomainNames(Arrays.asList(domainName));
        mockStatic(AmazonCloudSearchDomainClientBuilder.class);
        when(AmazonCloudSearchDomainClientBuilder.build(searchServiceEndpoint)).thenReturn(mockCloudSearchClient);
        doReturn(StubDocument.class).when(mockStubDocumentConfiguration).documentClass();
        when(mockStubDocumentConfiguration.properties()).thenReturn(properties);
        when(mockStubDocumentConfiguration.namespace()).thenReturn(namespace);
        when(mockAmazonCloudSearch.describeDomains(describeDomainsRequest)).thenReturn(describeDomainsResult);
        when(documentConfigurationHolder.schemaName()).thenReturn(schemaName);
        when(documentConfigurationHolder.documentConfigurations()).thenReturn(documentConfigurations);

        final CloudSearchEngine cloudSearchEngine = new CloudSearchEngine(documentConfigurationHolder,
                mockAmazonCloudSearch);

        // When
        IllegalArgumentException actualException = null;
        try {
            cloudSearchEngine.search(mock(Query.class), Randoms.randomInt(100), Randoms.randomInt(100),
                    StubDocument.class, null);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertEquals("SearchOptions cannot be null", actualException.getMessage());
    }

    private Hits getExpectedHits(final StubDocument document) {
        final Hits hits = new Hits();
        final String cursor = randomString();
        hits.withCursor(cursor);
        final Hit hit = new Hit();
        hit.withId(document.getId());
        final Map<String, List<String>> fields = new HashMap<>();
        fields.put("stringproperty", Arrays.asList(document.getStringProperty()));
        hit.withFields(fields);
        hits.withHit(hit);
        hits.withFound(1l);
        return hits;
    }

    private SearchRequest buildSearchRequest(final QueryType queryType, final Query query) {
        final String queryString = new QueryBuilder().buildQuery(query);
        final SearchRequest searchRequest = new SearchRequest();
        final String queryParser = queryType.name().toLowerCase();
        searchRequest.withQueryParser(queryParser);
        searchRequest.withQuery(queryString);
        return searchRequest;
    }

    @Test
    public void shoulNotSearch_withQuery() throws Exception {
        // Given
        final StubDocument document = randomStubDocument();
        final String documentId = document.getId();
        final DocumentConfigurationHolder documentConfigurationHolder = mock(DocumentConfigurationHolder.class);
        final String namespace = documentId;
        final DocumentConfiguration mockStubDocumentConfiguration = mock(DocumentConfiguration.class);
        final Map<String, PropertyDescriptor> properties = getStubDocumentPropertyDescriptors();
        final Collection<DocumentConfiguration> documentConfigurations = Arrays.asList(mockStubDocumentConfiguration);
        final String schemaName = randomString(10);
        final Query query = mock(Query.class);
        final QueryType queryType = randomEnum(QueryType.class);
        final AmazonCloudSearchDomain mockCloudSearchClient = mock(AmazonCloudSearchDomain.class);
        final Integer start = Randoms.randomInt(100);
        final Integer size = Randoms.randomInt(100);
        final String searchServiceEndpoint = randomString();
        final String domainName = schemaName + "-" + namespace;
        final DescribeDomainsResult describeDomainsResult = getDescribeDomainsResult(domainName, randomString(),
                searchServiceEndpoint);
        final AmazonCloudSearch mockAmazonCloudSearch = mock(AmazonCloudSearch.class);
        final DescribeDomainsRequest describeDomainsRequest = new DescribeDomainsRequest()
                .withDomainNames(Arrays.asList(domainName));
        final SearchRequest searchRequest = buildSearchRequest(queryType, query);
        mockStatic(AmazonCloudSearchDomainClientBuilder.class);
        when(AmazonCloudSearchDomainClientBuilder.build(searchServiceEndpoint)).thenReturn(mockCloudSearchClient);
        doReturn(StubDocument.class).when(mockStubDocumentConfiguration).documentClass();
        when(mockStubDocumentConfiguration.properties()).thenReturn(properties);
        when(mockStubDocumentConfiguration.namespace()).thenReturn(namespace);
        when(mockAmazonCloudSearch.describeDomains(describeDomainsRequest)).thenReturn(describeDomainsResult);
        when(documentConfigurationHolder.schemaName()).thenReturn(schemaName);
        when(documentConfigurationHolder.documentConfigurations()).thenReturn(documentConfigurations);
        when(query.queryType()).thenReturn(queryType);
        searchRequest.withStart((long) start);
        searchRequest.withSize((long) size);
        when(mockCloudSearchClient.search(searchRequest)).thenThrow(AmazonServiceException.class);

        final CloudSearchEngine cloudSearchEngine = new CloudSearchEngine(documentConfigurationHolder,
                mockAmazonCloudSearch);

        // When
        PersistenceResourceFailureException actualException = null;
        try {
            cloudSearchEngine.search(query, start, size, StubDocument.class);
        } catch (final PersistenceResourceFailureException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    private Collection<StubDocument> randomCollectionOfStubDocuments(final int numberOfDocuments) {
        final Collection<StubDocument> documents = new ArrayList<>();
        for (int i = 0; i < numberOfDocuments; i++) {
            documents.add(randomStubDocument());
        }
        return documents;
    }

    private StubDocument randomStubDocument() {
        final StubDocument stubDocument = new StubDocument();
        stubDocument.setId(randomString(10));
        stubDocument.setStringProperty(randomString(10));
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

    private void assertInputStreamEquals(final byte[] bytes, final InputStream actual) throws Exception {
        final String expectedString = new String(bytes);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = actual.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        assertEquals(expectedString, new String(baos.toByteArray()));
    }

}
