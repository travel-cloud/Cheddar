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
package com.clicktravel.infrastructure.persistence.aws.dynamodb.manager;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import jersey.repackaged.com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.DatabaseSchemaHolder;
import com.clicktravel.common.random.Randoms;
import com.clicktravel.infrastructure.persistence.aws.dynamodb.DynamoDocumentStoreTemplate;

public class DynamoDocumentStoreTemplateInfrastructureManagerTest {

    private final AmazonDynamoDB amazonDynamoDbClient = mock(AmazonDynamoDB.class);
    private DynamoDocumentStoreTemplateInfrastructureManager manager;

    @Before
    public void init() {
        manager = new DynamoDocumentStoreTemplateInfrastructureManager(amazonDynamoDbClient,
                Randoms.randomInt(Integer.MAX_VALUE), Randoms.randomInt(Integer.MAX_VALUE));
    }

    @Test
    public void shouldSetDynamoDocumentStoreTemplates() {
        // GIVEN
        final List<DynamoDocumentStoreTemplate> templateList = Lists.newArrayList();
        for (int i = 0; i < Randoms.randomInt(20); i++) {
            templateList.add(mock(DynamoDocumentStoreTemplate.class));
        }

        // WHEN
        manager.setDynamoDocumentStoreTemplates(templateList);

        // THEN

    }

    @Test
    public void shouldInitDynamoDocumentStoreTemplates() {
        // GIVEN
        final List<DynamoDocumentStoreTemplate> templateList = Lists.newArrayList();
        final int listSize = Randoms.randomInt(20);
        for (int i = 0; i < listSize; i++) {
            final DynamoDocumentStoreTemplate tmpMock = mock(DynamoDocumentStoreTemplate.class);
            when(tmpMock.databaseSchemaHolder()).thenReturn(mock(DatabaseSchemaHolder.class));
            templateList.add(tmpMock);
        }
        manager.setDynamoDocumentStoreTemplates(templateList);

        // WHEN
        manager.init();

        // THEN
        for (int i = 0; i < listSize; i++) {
            verify(templateList.get(i)).initialize(eq(amazonDynamoDbClient));
        }

    }
}
