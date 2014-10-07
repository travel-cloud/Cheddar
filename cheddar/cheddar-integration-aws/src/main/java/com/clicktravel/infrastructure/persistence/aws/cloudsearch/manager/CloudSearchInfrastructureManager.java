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

import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.cloudsearchv2.model.*;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.DocumentConfiguration;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.DocumentConfigurationHolder;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration.IndexDefinition;
import com.clicktravel.common.mapper.CollectionMapper;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.CloudSearchEngine;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.client.CloudSearchClient;

/**
 * Amazon Web Services CloudSearch Infrastructure Manager
 * 
 * The responsibility of implementing classes is to create tables
 */
public class CloudSearchInfrastructureManager {

    private final CloudSearchClient cloudSearchClient;
    private final Collection<CloudSearchEngine> cloudSearchEngines;
    private final CollectionMapper<IndexDefinition, IndexField> indexDefinitionToIndexFieldCollectionMapper;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public CloudSearchInfrastructureManager(final CloudSearchClient cloudSearchClient,
            final CollectionMapper<IndexDefinition, IndexField> indexDefinitionToIndexFieldCollectionMapper) {
        this.cloudSearchClient = cloudSearchClient;
        cloudSearchEngines = new ArrayList<>();
        this.indexDefinitionToIndexFieldCollectionMapper = indexDefinitionToIndexFieldCollectionMapper;
    }

    public void setCloudSearchEngines(final Collection<CloudSearchEngine> cloudSearchEngines) {
        if (cloudSearchEngines != null) {
            this.cloudSearchEngines.addAll(cloudSearchEngines);
        }
    }

    public void init() {
        final Map<String, Collection<IndexDefinition>> domainsToBeCreated = new HashMap<>();
        for (final CloudSearchEngine cloudSearchEngine : cloudSearchEngines) {
            final DocumentConfigurationHolder documentConfigurationHolder = cloudSearchEngine
                    .documentConfigurationHolder();
            for (final DocumentConfiguration documentConfiguration : documentConfigurationHolder
                    .documentConfigurations()) {
                final String domainName = documentConfigurationHolder.schemaName() + "-"
                        + documentConfiguration.namespace();
                domainsToBeCreated.put(domainName, documentConfiguration.indexDefinitions());
            }
        }

        for (final Entry<String, Collection<IndexDefinition>> entry : domainsToBeCreated.entrySet()) {
            createCloudSearchDomain(entry.getKey(), entry.getValue());
        }

        for (final CloudSearchEngine cloudSearchEngine : cloudSearchEngines) {
            cloudSearchEngine.initialize(cloudSearchClient);
        }

    }

    private void createCloudSearchDomain(final String domainName, final Collection<IndexDefinition> indexDefinitions) {
        if (cloudSearchClient.domains().contains(domainName)) {
            logger.debug("CloudSearch domain already exists: " + domainName);
        } else {
            final CreateDomainRequest createDomainRequest = new CreateDomainRequest().withDomainName(domainName);
            final CreateDomainResult createDomainResult = cloudSearchClient.createDomain(createDomainRequest);
            if (!createDomainResult.getDomainStatus().getCreated()) {
                throw new IllegalStateException(String.format("Domain with name %s failed to be created", domainName));
            }
            logger.debug("CloudSearch domain created: " + domainName);
            final Collection<IndexField> indexFields = indexDefinitionToIndexFieldCollectionMapper
                    .map(indexDefinitions);
            for (final IndexField indexField : indexFields) {
                final DefineIndexFieldRequest defineIndexFieldRequest = new DefineIndexFieldRequest().withDomainName(
                        domainName).withIndexField(indexField);
                final DefineIndexFieldResult defineIndexFieldResult = cloudSearchClient
                        .defineIndexField(defineIndexFieldRequest);
                if (OptionState.valueOf(defineIndexFieldResult.getIndexField().getStatus().getState()) != OptionState.RequiresIndexDocuments) {
                    throw new IllegalStateException(String.format(
                            "Index with name %s failed to be created in domain %s", indexField.getIndexFieldName(),
                            domainName));
                }
                logger.debug("CloudSearch index created: " + domainName + "->" + indexField.getIndexFieldName());
            }
            final IndexDocumentsRequest indexDocumentsRequest = new IndexDocumentsRequest().withDomainName(domainName);
            cloudSearchClient.indexDocuments(indexDocumentsRequest);
            logger.debug("CloudSearch index rebuilding started for domain : " + domainName);
        }
    }

}
