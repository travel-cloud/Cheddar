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

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.services.cloudsearchv2.model.*;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.DocumentConfiguration;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.DocumentConfigurationHolder;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.IndexDefinition;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.IndexFieldType;
import com.clicktravel.common.mapper.CollectionMapper;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.CloudSearchEngine;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.client.CloudSearchClient;

@SuppressWarnings("unchecked")
public class CloudSearchInfrastructureManagerTest {

    @Test
    public void shouldCreateCloudSearchInfrastructureManager_withCloudSearchClientAndIndexCollectionMapperAndAwsAccountId() {
        // Given
        final CloudSearchClient cloudSearchClient = mock(CloudSearchClient.class);
        final CollectionMapper<IndexDefinition, IndexField> indexCollectionMapper = mock(CollectionMapper.class);
        final String awsAccountId = randomString(10);

        // When
        final CloudSearchInfrastructureManager manager = new CloudSearchInfrastructureManager(cloudSearchClient,
                indexCollectionMapper, awsAccountId);

        // Then
        assertNotNull(manager);
    }

    @Test
    public void shouldInit_withCloudSearchEngines() throws Exception {
        // Given
        final CloudSearchEngine mockCloudSearchEngine = mock(CloudSearchEngine.class);
        final CollectionMapper<IndexDefinition, IndexField> indexCollectionMapper = mock(CollectionMapper.class);
        final String awsAccountId = randomString(10);
        final Collection<CloudSearchEngine> cloudSearchEngines = Arrays.asList(mockCloudSearchEngine);
        final DocumentConfigurationHolder mockDocumentConfigurationHolder = mock(DocumentConfigurationHolder.class);
        final String schemaName = randomString(10);
        when(mockDocumentConfigurationHolder.schemaName()).thenReturn(schemaName);
        final DocumentConfiguration mockDocumentConfiguration = mock(DocumentConfiguration.class);
        final IndexDefinition mockIndexDefinition = randomIndexDefinition();
        final Collection<IndexDefinition> indexDefinitions = Arrays.asList(mockIndexDefinition);
        final String namespace = randomString(10);
        final String domainName = schemaName + "-" + namespace;
        when(mockDocumentConfiguration.namespace()).thenReturn(namespace);
        when(mockDocumentConfiguration.indexDefinitions()).thenReturn(indexDefinitions);
        final Collection<DocumentConfiguration> mockDocumentConfigurations = Arrays.asList(mockDocumentConfiguration);
        when(mockDocumentConfigurationHolder.documentConfigurations()).thenReturn(mockDocumentConfigurations);
        when(mockCloudSearchEngine.documentConfigurationHolder()).thenReturn(mockDocumentConfigurationHolder);
        final CloudSearchClient cloudSearchClient = mock(CloudSearchClient.class);
        final CreateDomainResult mockCreateDomainResponse = mock(CreateDomainResult.class);
        final DomainStatus mockDomainStatus = mock(DomainStatus.class);
        when(mockDomainStatus.getCreated()).thenReturn(true);
        when(mockCreateDomainResponse.getDomainStatus()).thenReturn(mockDomainStatus);
        when(cloudSearchClient.createDomain(any(CreateDomainRequest.class))).thenReturn(mockCreateDomainResponse);
        final CloudSearchInfrastructureManager manager = new CloudSearchInfrastructureManager(cloudSearchClient,
                indexCollectionMapper, awsAccountId);
        final IndexField mockIndexField = mock(IndexField.class);
        final Collection<IndexField> indexFields = Arrays.asList(mockIndexField);
        when(indexCollectionMapper.map(anyCollection())).thenReturn(indexFields);
        manager.setCloudSearchEngines(cloudSearchEngines);
        final DefineIndexFieldResult mockDefineIndexFieldResult = mock(DefineIndexFieldResult.class);
        final IndexFieldStatus mockIndexFieldStatus = mock(IndexFieldStatus.class);
        final OptionStatus mockStatus = mock(OptionStatus.class);
        when(mockStatus.getState()).thenReturn("RequiresIndexDocuments");
        when(mockIndexFieldStatus.getStatus()).thenReturn(mockStatus);
        when(mockDefineIndexFieldResult.getIndexField()).thenReturn(mockIndexFieldStatus);
        when(cloudSearchClient.defineIndexField(any(DefineIndexFieldRequest.class))).thenReturn(
                mockDefineIndexFieldResult);

        // When
        manager.init();

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
        final ArgumentCaptor<IndexDocumentsRequest> indexDocumentsRequestArgumentCaptor = ArgumentCaptor
                .forClass(IndexDocumentsRequest.class);
        final ArgumentCaptor<UpdateServiceAccessPoliciesRequest> updateServiceAccessPoliciesRequestArgumentCaptor = ArgumentCaptor
                .forClass(UpdateServiceAccessPoliciesRequest.class);
        verify(cloudSearchClient).updateServiceAccessPolicies(
                updateServiceAccessPoliciesRequestArgumentCaptor.capture());
        assertEquals(domainName, updateServiceAccessPoliciesRequestArgumentCaptor.getValue().getDomainName());
        assertEquals(policyJson(awsAccountId, domainName), updateServiceAccessPoliciesRequestArgumentCaptor.getValue()
                .getAccessPolicies());
        verify(cloudSearchClient).indexDocuments(indexDocumentsRequestArgumentCaptor.capture());
        assertEquals(domainName, indexDocumentsRequestArgumentCaptor.getValue().getDomainName());
        verify(mockCloudSearchEngine).initialize(cloudSearchClient);
    }

    private Object policyJson(final String awsAccountId, final String domainName) {
        return "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Sid\":\"search_only\",\"Action\":\"cloudsearch:*\",\"Condition\":{\"IpAddress\":{\"aws:SourceIp\":[\"0.0.0.0/0\"]}},\"Principal\":{\"AWS\":[\"*\"]}}]}";
    }

    @Test
    public void shouldNotInit_withDomainCreationFailed() throws Exception {
        // Given
        final CloudSearchEngine mockCloudSearchEngine = mock(CloudSearchEngine.class);
        final CollectionMapper<IndexDefinition, IndexField> indexCollectionMapper = mock(CollectionMapper.class);
        final String awsAccountId = randomString(10);
        final Collection<CloudSearchEngine> cloudSearchEngines = Arrays.asList(mockCloudSearchEngine);
        final DocumentConfigurationHolder mockDocumentConfigurationHolder = mock(DocumentConfigurationHolder.class);
        final String schemaName = randomString(10);
        when(mockDocumentConfigurationHolder.schemaName()).thenReturn(schemaName);
        final DocumentConfiguration mockDocumentConfiguration = mock(DocumentConfiguration.class);
        final Collection<IndexDefinition> indexDefinitions = mock(Collection.class);
        final String namespace = randomString(10);
        final String domainName = schemaName + "-" + namespace;
        when(mockDocumentConfiguration.namespace()).thenReturn(namespace);
        when(mockDocumentConfiguration.indexDefinitions()).thenReturn(indexDefinitions);
        final Collection<DocumentConfiguration> mockDocumentConfigurations = Arrays.asList(mockDocumentConfiguration);
        when(mockDocumentConfigurationHolder.documentConfigurations()).thenReturn(mockDocumentConfigurations);
        when(mockCloudSearchEngine.documentConfigurationHolder()).thenReturn(mockDocumentConfigurationHolder);
        final CloudSearchClient cloudSearchClient = mock(CloudSearchClient.class);
        final CreateDomainResult mockCreateDomainResponse = mock(CreateDomainResult.class);
        final DomainStatus mockDomainStatus = mock(DomainStatus.class);
        when(mockDomainStatus.getCreated()).thenReturn(false);
        when(mockCreateDomainResponse.getDomainStatus()).thenReturn(mockDomainStatus);
        when(cloudSearchClient.createDomain(any(CreateDomainRequest.class))).thenReturn(mockCreateDomainResponse);
        final CloudSearchInfrastructureManager manager = new CloudSearchInfrastructureManager(cloudSearchClient,
                indexCollectionMapper, awsAccountId);
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
        final CreateDomainRequest createDomainRequest = createDomainRequestArgumentCaptor.getValue();
        assertEquals(domainName, createDomainRequest.getDomainName());
        verifyZeroInteractions(indexCollectionMapper);
        verify(cloudSearchClient, never()).defineIndexField(any(DefineIndexFieldRequest.class));
        verify(cloudSearchClient, never()).indexDocuments(any(IndexDocumentsRequest.class));
        verify(cloudSearchClient, never()).updateServiceAccessPolicies(any(UpdateServiceAccessPoliciesRequest.class));
        verify(mockCloudSearchEngine, never()).initialize(any(CloudSearchClient.class));
        assertNotNull(actualException);

    }

    @Test
    public void shouldNotInit_withIndexDefinitionFailed() throws Exception {
        // Given
        final CloudSearchEngine mockCloudSearchEngine = mock(CloudSearchEngine.class);
        final CollectionMapper<IndexDefinition, IndexField> indexCollectionMapper = mock(CollectionMapper.class);
        final String awsAccountId = randomString(10);
        final Collection<CloudSearchEngine> cloudSearchEngines = Arrays.asList(mockCloudSearchEngine);
        final DocumentConfigurationHolder mockDocumentConfigurationHolder = mock(DocumentConfigurationHolder.class);
        final String schemaName = randomString(10);
        when(mockDocumentConfigurationHolder.schemaName()).thenReturn(schemaName);
        final DocumentConfiguration mockDocumentConfiguration = mock(DocumentConfiguration.class);
        final IndexDefinition mockIndexDefinition = randomIndexDefinition();
        final Collection<IndexDefinition> indexDefinitions = Arrays.asList(mockIndexDefinition);
        final String namespace = randomString(10);
        final String domainName = schemaName + "-" + namespace;
        when(mockDocumentConfiguration.namespace()).thenReturn(namespace);
        when(mockDocumentConfiguration.indexDefinitions()).thenReturn(indexDefinitions);
        final Collection<DocumentConfiguration> mockDocumentConfigurations = Arrays.asList(mockDocumentConfiguration);
        when(mockDocumentConfigurationHolder.documentConfigurations()).thenReturn(mockDocumentConfigurations);
        when(mockCloudSearchEngine.documentConfigurationHolder()).thenReturn(mockDocumentConfigurationHolder);
        final CloudSearchClient cloudSearchClient = mock(CloudSearchClient.class);
        final CreateDomainResult mockCreateDomainResponse = mock(CreateDomainResult.class);
        final DomainStatus mockDomainStatus = mock(DomainStatus.class);
        when(mockDomainStatus.getCreated()).thenReturn(true);
        when(mockCreateDomainResponse.getDomainStatus()).thenReturn(mockDomainStatus);
        when(cloudSearchClient.createDomain(any(CreateDomainRequest.class))).thenReturn(mockCreateDomainResponse);
        final CloudSearchInfrastructureManager manager = new CloudSearchInfrastructureManager(cloudSearchClient,
                indexCollectionMapper, awsAccountId);
        final IndexField mockIndexField = mock(IndexField.class);
        final Collection<IndexField> indexFields = Arrays.asList(mockIndexField);
        when(indexCollectionMapper.map(anyCollection())).thenReturn(indexFields);
        manager.setCloudSearchEngines(cloudSearchEngines);
        final DefineIndexFieldResult mockDefineIndexFieldResult = mock(DefineIndexFieldResult.class);
        final IndexFieldStatus mockIndexFieldStatus = mock(IndexFieldStatus.class);
        final OptionStatus mockStatus = mock(OptionStatus.class);
        when(mockStatus.getState()).thenReturn(randomNonRequiresIndexingOptionState().toString());
        when(mockIndexFieldStatus.getStatus()).thenReturn(mockStatus);
        verify(cloudSearchClient, never()).updateServiceAccessPolicies(any(UpdateServiceAccessPoliciesRequest.class));
        when(mockDefineIndexFieldResult.getIndexField()).thenReturn(mockIndexFieldStatus);
        when(cloudSearchClient.defineIndexField(any(DefineIndexFieldRequest.class))).thenReturn(
                mockDefineIndexFieldResult);

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
        verify(mockCloudSearchEngine, never()).initialize(any(CloudSearchClient.class));
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
