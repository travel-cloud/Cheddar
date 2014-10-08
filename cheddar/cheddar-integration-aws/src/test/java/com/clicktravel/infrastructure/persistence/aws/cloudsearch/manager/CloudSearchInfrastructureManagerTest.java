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
package com.clicktravel.infrastructure.persistence.aws.cloudsearch.manager;

import static com.clicktravel.common.random.Randoms.randomBoolean;
import static com.clicktravel.common.random.Randoms.randomEnum;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.cloudsearchv2.AmazonCloudSearch;
import com.amazonaws.services.cloudsearchv2.model.*;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.DocumentConfiguration;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.DocumentConfigurationHolder;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.IndexDefinition;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.IndexFieldType;
import com.clicktravel.common.mapper.CollectionMapper;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.CloudSearchEngine;

@SuppressWarnings("unchecked")
public class CloudSearchInfrastructureManagerTest {

    private AmazonCloudSearch cloudSearchClient = mock(AmazonCloudSearch.class);
    private AWSCredentials awsCredentials;
    private CollectionMapper<IndexDefinition, IndexField> indexCollectionMapper;

    @Before
    public void setup() {
        cloudSearchClient = mock(AmazonCloudSearch.class);
        awsCredentials = mock(AWSCredentials.class);
        indexCollectionMapper = mock(CollectionMapper.class);
    }

    @Test
    public void shouldCreateCloudSearchInfrastructureManager_withAmazonCloudSearchAndAwsCredentialsAndIndexCollectionMapperAndAwsAccountId() {
        // Given
        final AmazonCloudSearch cloudSearchClient = mock(AmazonCloudSearch.class);
        final AWSCredentials awsCredentials = mock(AWSCredentials.class);
        final CollectionMapper<IndexDefinition, IndexField> indexCollectionMapper = mock(CollectionMapper.class);

        // When
        final CloudSearchInfrastructureManager manager = new CloudSearchInfrastructureManager(cloudSearchClient,
                awsCredentials, indexCollectionMapper);

        // Then
        assertNotNull(manager);
    }

    @Test
    public void shouldInit_withCloudSearchEngines() throws Exception {
        // Given
        final CloudSearchEngine mockCloudSearchEngine = mock(CloudSearchEngine.class);
        final Collection<CloudSearchEngine> cloudSearchEngines = Arrays.asList(mockCloudSearchEngine);
        final String schemaName = randomString(10);
        final DocumentConfiguration mockDocumentConfiguration = mock(DocumentConfiguration.class);
        final IndexDefinition mockIndexDefinition = randomIndexDefinition();
        final Collection<IndexDefinition> indexDefinitions = Arrays.asList(mockIndexDefinition);
        final String namespace = randomString(10);
        final String domainName = schemaName + "-" + namespace;
        final Collection<DocumentConfiguration> documentConfigurations = Arrays.asList(mockDocumentConfiguration);
        final DocumentConfigurationHolder documentConfigurationHolder = new DocumentConfigurationHolder(schemaName,
                documentConfigurations);
        final CreateDomainResult mockCreateDomainResponse = mock(CreateDomainResult.class);
        final DomainStatus mockDomainStatus = mock(DomainStatus.class);
        final IndexField mockIndexField = mock(IndexField.class);
        final Collection<IndexField> indexFields = Arrays.asList(mockIndexField);
        final DefineIndexFieldResult mockDefineIndexFieldResult = mock(DefineIndexFieldResult.class);
        final IndexFieldStatus mockIndexFieldStatus = mock(IndexFieldStatus.class);
        final OptionStatus mockStatus = mock(OptionStatus.class);
        when(mockDocumentConfiguration.namespace()).thenReturn(namespace);
        when(mockDocumentConfiguration.indexDefinitions()).thenReturn(indexDefinitions);
        when(mockCloudSearchEngine.documentConfigurationHolder()).thenReturn(documentConfigurationHolder);
        when(mockDomainStatus.getCreated()).thenReturn(true);
        when(mockCreateDomainResponse.getDomainStatus()).thenReturn(mockDomainStatus);
        when(cloudSearchClient.describeDomains(describeDomainsRequestWithDomainNames(Arrays.asList(domainName))))
                .thenReturn(getDescribeDomainsResultWithNoDomains());
        when(cloudSearchClient.createDomain(any(CreateDomainRequest.class))).thenReturn(mockCreateDomainResponse);
        when(indexCollectionMapper.map(anyCollection())).thenReturn(indexFields);
        when(mockStatus.getState()).thenReturn("RequiresIndexDocuments");
        when(mockIndexFieldStatus.getStatus()).thenReturn(mockStatus);
        when(mockDefineIndexFieldResult.getIndexField()).thenReturn(mockIndexFieldStatus);
        when(cloudSearchClient.defineIndexField(any(DefineIndexFieldRequest.class))).thenReturn(
                mockDefineIndexFieldResult);

        final CloudSearchInfrastructureManager manager = new CloudSearchInfrastructureManager(cloudSearchClient,
                awsCredentials, indexCollectionMapper);
        manager.setCloudSearchEngines(cloudSearchEngines);

        // When
        manager.init();

        // Then
        final ArgumentCaptor<CreateDomainRequest> createDomainRequestArgumentCaptor = ArgumentCaptor
                .forClass(CreateDomainRequest.class);
        verify(cloudSearchClient).createDomain(createDomainRequestArgumentCaptor.capture());
        final CreateDomainRequest createDomainRequest = createDomainRequestArgumentCaptor.getValue();
        verify(indexCollectionMapper).map(indexDefinitions);
        final ArgumentCaptor<DefineIndexFieldRequest> defineIndexFieldRequestArgumentCaptor = ArgumentCaptor
                .forClass(DefineIndexFieldRequest.class);
        verify(cloudSearchClient).defineIndexField(defineIndexFieldRequestArgumentCaptor.capture());
        final ArgumentCaptor<IndexDocumentsRequest> indexDocumentsRequestArgumentCaptor = ArgumentCaptor
                .forClass(IndexDocumentsRequest.class);
        verify(cloudSearchClient).indexDocuments(indexDocumentsRequestArgumentCaptor.capture());
        verify(mockCloudSearchEngine).initialize(cloudSearchClient, awsCredentials);
        assertEquals(domainName, defineIndexFieldRequestArgumentCaptor.getValue().getDomainName());
        assertEquals(domainName, createDomainRequest.getDomainName());
        assertEquals(domainName, indexDocumentsRequestArgumentCaptor.getValue().getDomainName());
    }

    private DescribeDomainsRequest describeDomainsRequestWithDomainNames(final Collection<String> domainNames) {
        final DescribeDomainsRequest describeDomainsRequest = new DescribeDomainsRequest().withDomainNames(domainNames);
        return describeDomainsRequest;
    }

    private DescribeDomainsResult getDescribeDomainsResultWithNoDomains() {
        final DescribeDomainsResult describeDomainsResult = new DescribeDomainsResult();
        describeDomainsResult.withDomainStatusList();
        return describeDomainsResult;
    }

    @Test
    public void shouldNotInit_withDomainCreationFailed() throws Exception {
        // Given
        final CloudSearchEngine mockCloudSearchEngine = mock(CloudSearchEngine.class);
        final Collection<CloudSearchEngine> cloudSearchEngines = Arrays.asList(mockCloudSearchEngine);
        final String schemaName = randomString(10);
        final DocumentConfiguration mockDocumentConfiguration = mock(DocumentConfiguration.class);
        final Collection<IndexDefinition> indexDefinitions = mock(Collection.class);
        final String namespace = randomString(10);
        final String domainName = schemaName + "-" + namespace;
        final Collection<DocumentConfiguration> documentConfigurations = Arrays.asList(mockDocumentConfiguration);
        final DocumentConfigurationHolder documentConfigurationHolder = new DocumentConfigurationHolder(schemaName,
                documentConfigurations);

        final CreateDomainResult mockCreateDomainResponse = mock(CreateDomainResult.class);
        final DomainStatus mockDomainStatus = mock(DomainStatus.class);
        when(mockDocumentConfiguration.namespace()).thenReturn(namespace);
        when(mockDocumentConfiguration.indexDefinitions()).thenReturn(indexDefinitions);
        when(mockCloudSearchEngine.documentConfigurationHolder()).thenReturn(documentConfigurationHolder);
        when(mockDomainStatus.getCreated()).thenReturn(false);
        when(mockCreateDomainResponse.getDomainStatus()).thenReturn(mockDomainStatus);
        when(cloudSearchClient.describeDomains(describeDomainsRequestWithDomainNames(Arrays.asList(domainName))))
                .thenReturn(getDescribeDomainsResultWithNoDomains());
        when(cloudSearchClient.createDomain(any(CreateDomainRequest.class))).thenReturn(mockCreateDomainResponse);

        final CloudSearchInfrastructureManager manager = new CloudSearchInfrastructureManager(cloudSearchClient,
                awsCredentials, indexCollectionMapper);
        manager.setCloudSearchEngines(cloudSearchEngines);

        // When
        IllegalStateException actualException = null;
        try {
            manager.init();
        } catch (final IllegalStateException e) {
            actualException = e;
        }

        // Then
        final ArgumentCaptor<CreateDomainRequest> createDomainRequestArgumentCaptor = ArgumentCaptor
                .forClass(CreateDomainRequest.class);
        verify(cloudSearchClient).createDomain(createDomainRequestArgumentCaptor.capture());
        verifyZeroInteractions(indexCollectionMapper);
        verify(cloudSearchClient, never()).defineIndexField(any(DefineIndexFieldRequest.class));
        verify(cloudSearchClient, never()).indexDocuments(any(IndexDocumentsRequest.class));
        verify(cloudSearchClient, never()).updateServiceAccessPolicies(any(UpdateServiceAccessPoliciesRequest.class));
        verify(mockCloudSearchEngine, never()).initialize(any(AmazonCloudSearch.class), any(AWSCredentials.class));
        final CreateDomainRequest createDomainRequest = createDomainRequestArgumentCaptor.getValue();
        assertEquals(domainName, createDomainRequest.getDomainName());
        assertNotNull(actualException);

    }

    @Test
    public void shouldNotInit_withIndexDefinitionFailed() throws Exception {
        // Given
        final CloudSearchEngine mockCloudSearchEngine = mock(CloudSearchEngine.class);
        final Collection<CloudSearchEngine> cloudSearchEngines = Arrays.asList(mockCloudSearchEngine);
        final String schemaName = randomString(10);
        final DocumentConfiguration mockDocumentConfiguration = mock(DocumentConfiguration.class);
        final IndexDefinition mockIndexDefinition = randomIndexDefinition();
        final Collection<IndexDefinition> indexDefinitions = Arrays.asList(mockIndexDefinition);
        final String namespace = randomString(10);
        final String domainName = schemaName + "-" + namespace;
        final Collection<DocumentConfiguration> documentConfigurations = Arrays.asList(mockDocumentConfiguration);
        final DocumentConfigurationHolder documentConfigurationHolder = new DocumentConfigurationHolder(schemaName,
                documentConfigurations);
        final CreateDomainResult mockCreateDomainResponse = mock(CreateDomainResult.class);
        final DomainStatus mockDomainStatus = mock(DomainStatus.class);
        final IndexField mockIndexField = mock(IndexField.class);
        final Collection<IndexField> indexFields = Arrays.asList(mockIndexField);
        final DefineIndexFieldResult mockDefineIndexFieldResult = mock(DefineIndexFieldResult.class);
        final IndexFieldStatus mockIndexFieldStatus = mock(IndexFieldStatus.class);
        final OptionStatus mockStatus = mock(OptionStatus.class);
        when(mockDocumentConfiguration.namespace()).thenReturn(namespace);
        when(mockDocumentConfiguration.indexDefinitions()).thenReturn(indexDefinitions);
        when(mockCloudSearchEngine.documentConfigurationHolder()).thenReturn(documentConfigurationHolder);
        when(mockDomainStatus.getCreated()).thenReturn(true);
        when(mockCreateDomainResponse.getDomainStatus()).thenReturn(mockDomainStatus);
        when(cloudSearchClient.describeDomains(describeDomainsRequestWithDomainNames(Arrays.asList(domainName))))
                .thenReturn(getDescribeDomainsResultWithNoDomains());
        when(cloudSearchClient.createDomain(any(CreateDomainRequest.class))).thenReturn(mockCreateDomainResponse);
        when(indexCollectionMapper.map(anyCollection())).thenReturn(indexFields);
        when(mockStatus.getState()).thenReturn(randomNonRequiresIndexingOptionState().toString());
        when(mockIndexFieldStatus.getStatus()).thenReturn(mockStatus);
        when(mockDefineIndexFieldResult.getIndexField()).thenReturn(mockIndexFieldStatus);
        when(cloudSearchClient.defineIndexField(any(DefineIndexFieldRequest.class))).thenReturn(
                mockDefineIndexFieldResult);
        final CloudSearchInfrastructureManager manager = new CloudSearchInfrastructureManager(cloudSearchClient,
                awsCredentials, indexCollectionMapper);
        manager.setCloudSearchEngines(cloudSearchEngines);

        // When
        IllegalStateException actualException = null;
        try {
            manager.init();
        } catch (final IllegalStateException e) {
            actualException = e;
            e.printStackTrace();
        }

        // Then
        final ArgumentCaptor<CreateDomainRequest> createDomainRequestArgumentCaptor = ArgumentCaptor
                .forClass(CreateDomainRequest.class);
        verify(cloudSearchClient).createDomain(createDomainRequestArgumentCaptor.capture());
        final CreateDomainRequest createDomainRequest = createDomainRequestArgumentCaptor.getValue();
        assertEquals(domainName, createDomainRequest.getDomainName());
        verify(indexCollectionMapper).map(indexDefinitions);
        final ArgumentCaptor<DefineIndexFieldRequest> defineIndexFieldRequestArgumentCaptor = ArgumentCaptor
                .forClass(DefineIndexFieldRequest.class);
        verify(cloudSearchClient).defineIndexField(defineIndexFieldRequestArgumentCaptor.capture());
        assertEquals(domainName, defineIndexFieldRequestArgumentCaptor.getValue().getDomainName());
        verify(cloudSearchClient, never()).updateServiceAccessPolicies(any(UpdateServiceAccessPoliciesRequest.class));
        verify(mockCloudSearchEngine, never()).initialize(any(AmazonCloudSearch.class), any(AWSCredentials.class));
        assertNotNull(actualException);
    }

    private OptionState randomNonRequiresIndexingOptionState() {
        OptionState optionState;
        do {
            optionState = randomEnum(OptionState.class);
        } while (optionState == OptionState.RequiresIndexDocuments);
        return optionState;
    }

    private IndexDefinition randomIndexDefinition() {
        final IndexFieldType inputFieldType = randomEnum(IndexFieldType.class);
        final boolean searcheable = inputFieldType == IndexFieldType.TEXT
                || inputFieldType == IndexFieldType.TEXT_ARRAY ? true : randomBoolean();
        final boolean sortable = inputFieldType.isArray() ? false : randomBoolean();
        final IndexDefinition indexDefinition = new IndexDefinition(randomString(10), inputFieldType, searcheable,
                randomBoolean(), sortable);
        return indexDefinition;
    }
}
