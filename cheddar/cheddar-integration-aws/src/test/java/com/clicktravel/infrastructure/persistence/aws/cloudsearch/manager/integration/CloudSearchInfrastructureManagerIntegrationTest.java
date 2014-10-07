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
package com.clicktravel.infrastructure.persistence.aws.cloudsearch.manager.integration;

import static com.clicktravel.common.random.Randoms.randomString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import jersey.repackaged.com.google.common.collect.Sets;

import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudsearchv2.model.*;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.DocumentConfiguration;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.DocumentConfigurationHolder;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.IndexDefinition;
import com.clicktravel.common.mapper.CollectionMapper;
import com.clicktravel.infrastructure.integration.aws.AwsIntegration;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.CloudSearchEngine;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.StubDocument;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.client.CloudSearchClient;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.manager.CloudSearchInfrastructureManager;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.manager.IndexDefinitionToIndexFieldCollectionMapper;

@Category(AwsIntegration.class)
public class CloudSearchInfrastructureManagerIntegrationTest {

    private final CloudSearchClient cloudSearchClient = new CloudSearchClient(new BasicAWSCredentials(
            AwsIntegration.getAccessKeyId(), AwsIntegration.getSecretKeyId()));
    private String domainName;

    @After
    public void tearDown() {
        final DeleteDomainRequest deleteDomainRequest = new DeleteDomainRequest().withDomainName(domainName);
        final DeleteDomainResult deleteDomainResults = cloudSearchClient.deleteDomain(deleteDomainRequest);
        assertTrue(deleteDomainResults.getDomainStatus().isDeleted());
        domainName = null;
    }

    @Test
    public void shouldCreateADomainAndIndexes_withOneCloudSearchEngineAndTwoIndexes() {
        // Given
        final String nameSpace = randomString(10).toLowerCase();
        final Class<StubDocument> documentClazz = StubDocument.class;
        final DocumentConfiguration documentConfiguration = new DocumentConfiguration(documentClazz, nameSpace);
        final String textIndexDefinitionName = "stringProperty";
        final com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.IndexFieldType textIndexFieldType = com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.IndexFieldType.TEXT;
        final IndexDefinition textIndexDefinition = new IndexDefinition(textIndexDefinitionName, textIndexFieldType);
        final String doubleArrayIndexDefinitionName = "collectionProperty";
        final com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.IndexFieldType doubleArrayIndexFieldType = com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.IndexFieldType.DOUBLE_ARRAY;
        final IndexDefinition doubleArrayDefinition = new IndexDefinition(doubleArrayIndexDefinitionName,
                doubleArrayIndexFieldType, true, true, false);
        final Collection<IndexDefinition> indexDefinitionCollection = Sets.newHashSet(textIndexDefinition,
                doubleArrayDefinition);
        documentConfiguration.registerIndexes(indexDefinitionCollection);
        final Collection<DocumentConfiguration> documentConfigurationCollection = Sets
                .newHashSet(documentConfiguration);
        final String schemaName = randomString(10).toLowerCase().replaceAll("[0-9]", "");
        final DocumentConfigurationHolder documentConfigurationHolder = new DocumentConfigurationHolder(schemaName,
                documentConfigurationCollection);
        final CloudSearchEngine cloudSearchEngine = new CloudSearchEngine(documentConfigurationHolder);
        final String awsCloudSearchEndpiont = AwsIntegration.getCloudSearchEndpoint();
        cloudSearchClient.setEndpoint(awsCloudSearchEndpiont);
        cloudSearchClient.initialize();
        final CollectionMapper<IndexDefinition, IndexField> indexDefinitionToIndexFieldCollectionMaper = new IndexDefinitionToIndexFieldCollectionMapper();
        final CloudSearchInfrastructureManager cloudSearchInfrastructureManager = new CloudSearchInfrastructureManager(
                cloudSearchClient, indexDefinitionToIndexFieldCollectionMaper);
        final Collection<CloudSearchEngine> cloudSearchEngineCollection = Sets.newHashSet(cloudSearchEngine);
        cloudSearchInfrastructureManager.setCloudSearchEngines(cloudSearchEngineCollection);
        domainName = schemaName + "-" + nameSpace;

        // When
        cloudSearchInfrastructureManager.init();

        // Then
        assertTrue(domainExists(domainName));
        assertTrue(indexDefinitionsExist(domainName, indexDefinitionCollection));
    }

    private boolean domainExists(final String domainName) {
        final DescribeDomainsRequest describeDomainsRequest = new DescribeDomainsRequest().withDomainNames(domainName);
        final DescribeDomainsResult describeDomainsResult = cloudSearchClient.describeDomains(describeDomainsRequest);
        assertNotNull(describeDomainsResult);
        DomainStatus significantDomainStatus = new DomainStatus();
        for (final DomainStatus domainStatus : describeDomainsResult.getDomainStatusList()) {
            if (domainStatus.getDomainName().equals(domainName)) {
                significantDomainStatus = domainStatus;
                break;
            }
        }
        assertNotNull(significantDomainStatus);
        assertTrue(significantDomainStatus.isCreated());
        return true;
    }

    private boolean indexDefinitionsExist(final String domainName,
            final Collection<IndexDefinition> indexDefinitionCollection) {
        final DescribeIndexFieldsRequest describeIndexFieldsRequest = new DescribeIndexFieldsRequest()
                .withDomainName(domainName);
        final Collection<String> indexFeildNames = new ArrayList<>();
        for (final IndexDefinition indexDefinition : indexDefinitionCollection) {
            indexFeildNames.add(indexDefinition.getName().toLowerCase());
        }
        describeIndexFieldsRequest.setFieldNames(indexFeildNames);
        final DescribeIndexFieldsResult describeIndexFieldsResult = cloudSearchClient
                .describeIndexFields(describeIndexFieldsRequest);
        assertNotNull(describeIndexFieldsResult);
        assertThat(describeIndexFieldsResult.getIndexFields().size(), Is.is(indexDefinitionCollection.size()));
        for (final IndexFieldStatus indexFieldStatus : describeIndexFieldsResult.getIndexFields()) {
            final OptionState fieldIndexOptionSate = OptionState.valueOf(indexFieldStatus.getStatus().getState());
            assertThat(fieldIndexOptionSate, is(OptionState.Processing));
        }
        return true;
    }

}
