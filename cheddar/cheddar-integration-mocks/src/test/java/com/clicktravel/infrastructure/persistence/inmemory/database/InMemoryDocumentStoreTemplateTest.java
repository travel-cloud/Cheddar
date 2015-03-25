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
package com.clicktravel.infrastructure.persistence.inmemory.database;

import static com.clicktravel.common.random.Randoms.randomId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.clicktravel.cheddar.infrastructure.persistence.database.ItemId;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.DatabaseSchemaHolder;
import com.clicktravel.cheddar.infrastructure.persistence.database.configuration.ItemConfiguration;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.AttributeQuery;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Condition;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Operators;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Query;
import com.clicktravel.common.random.Randoms;

public class InMemoryDocumentStoreTemplateTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private static final String STRING_PROPERTY = "stringProperty";
    private DatabaseSchemaHolder databaseSchemaHolder;

    @Before
    public void init() throws Exception {
        final Collection<ItemConfiguration> itemConfigurations = new ArrayList<>();

        final ItemConfiguration stubDocumentConfiguration = new ItemConfiguration(StubDocument.class,
                InMemoryDbDataGenerator.STUB_ITEM_TABLE_NAME);

        itemConfigurations.add(stubDocumentConfiguration);

        databaseSchemaHolder = new DatabaseSchemaHolder(InMemoryDbDataGenerator.UNIT_TEST_SCHEMA_NAME,
                itemConfigurations);
    }

    @Test
    public void shouldCreateDocument() {
        // GIVEN
        final InMemoryDocumentStoreTemplate docStoreTemplate = new InMemoryDocumentStoreTemplate(databaseSchemaHolder);
        final StubDocument stubDocument = new StubDocument();
        stubDocument.setId(randomId());

        // WHEN
        final StubDocument item = docStoreTemplate.createDocument(stubDocument);

        // THEN
        assertEquals(stubDocument, item);
    }

    @Test
    public void shouldReadDocument() {
        // GIVEN
        final InMemoryDocumentStoreTemplate docStoreTemplate = new InMemoryDocumentStoreTemplate(databaseSchemaHolder);
        final StubDocument stubDocument = new StubDocument();
        stubDocument.setId(randomId());
        docStoreTemplate.create(stubDocument);
        final ItemId itemId = new ItemId(stubDocument.getId());

        // WHEN
        final StubDocument item = docStoreTemplate.readDocument(itemId, StubDocument.class);

        // THEN
        assertEquals(stubDocument, item);
    }

    @Test
    public void shouldUpdateDocument() {
        // GIVEN
        final InMemoryDocumentStoreTemplate docStoreTemplate = new InMemoryDocumentStoreTemplate(databaseSchemaHolder);
        final StubDocument stubDocument = new StubDocument();
        stubDocument.setId(randomId());
        docStoreTemplate.create(stubDocument);
        final Long newVersion = stubDocument.getVersion() + 1;

        // WHEN
        final StubDocument item = docStoreTemplate.updateDocument(stubDocument);

        // THEN
        assertEquals(stubDocument, item);
        assertEquals(newVersion, item.getVersion());
    }

    @Test
    public void shouldDeleteDocument() {
        // GIVEN
        final InMemoryDocumentStoreTemplate docStoreTemplate = new InMemoryDocumentStoreTemplate(databaseSchemaHolder);
        final StubDocument stubDocument = new StubDocument();
        stubDocument.setId(randomId());
        docStoreTemplate.create(stubDocument);
        final ItemId itemId = new ItemId(stubDocument.getId());

        // WHEN
        docStoreTemplate.deleteDocument(stubDocument);

        // THEN
        exception.expect(NonExistentItemException.class);
        docStoreTemplate.readDocument(itemId, StubDocument.class);
    }

    @Test
    public void shouldFetchNotNullDocument() {
        // GIVEN
        final InMemoryDocumentStoreTemplate docStoreTemplate = new InMemoryDocumentStoreTemplate(databaseSchemaHolder);
        final StubDocument stubDocument = new StubDocument();
        stubDocument.setId(randomId());
        stubDocument.setStringProperty(Randoms.randomString());
        docStoreTemplate.create(stubDocument);

        final Query query = new AttributeQuery(STRING_PROPERTY, new Condition(Operators.NOT_NULL));

        // WHEN
        final Collection<StubDocument> itemResults = docStoreTemplate.fetchDocuments(query, StubDocument.class);

        // THEN
        assertNotNull(itemResults);
        assertEquals(1, itemResults.size());
        assertEquals(stubDocument, itemResults.iterator().next());
    }

    @Test
    public void shouldFetchTenNotNullDocuments() {
        // GIVEN
        final InMemoryDocumentStoreTemplate docStoreTemplate = new InMemoryDocumentStoreTemplate(databaseSchemaHolder);
        for (int i = 0; i < 10; i++) {
            final StubDocument stubDocument = new StubDocument();
            stubDocument.setId(randomId());
            stubDocument.setStringProperty(Randoms.randomString());
            docStoreTemplate.create(stubDocument);
        }
        final Query query = new AttributeQuery(STRING_PROPERTY, new Condition(Operators.NOT_NULL));

        // WHEN
        final Collection<StubDocument> itemResults = docStoreTemplate.fetchDocuments(query, StubDocument.class);

        // THEN
        assertNotNull(itemResults);
        assertEquals(10, itemResults.size());
    }

    @Test
    public void shouldFetchFiveNotNullDocuments() {
        // GIVEN
        final InMemoryDocumentStoreTemplate docStoreTemplate = new InMemoryDocumentStoreTemplate(databaseSchemaHolder);
        for (int i = 0; i < 10; i++) {
            final StubDocument stubDocument = new StubDocument();
            stubDocument.setId(randomId());
            if (i % 2 == 0) {
                stubDocument.setStringProperty(Randoms.randomString());
            }
            docStoreTemplate.create(stubDocument);
        }
        final Query query = new AttributeQuery(STRING_PROPERTY, new Condition(Operators.NOT_NULL));

        // WHEN
        final Collection<StubDocument> itemResults = docStoreTemplate.fetchDocuments(query, StubDocument.class);

        // THEN
        assertNotNull(itemResults);
        assertEquals(5, itemResults.size());
    }

    @Test
    public void shouldFetchNullDocument() {
        // GIVEN
        final InMemoryDocumentStoreTemplate docStoreTemplate = new InMemoryDocumentStoreTemplate(databaseSchemaHolder);
        final StubDocument stubDocument = new StubDocument();
        stubDocument.setId(randomId());
        docStoreTemplate.create(stubDocument);

        final Query query = new AttributeQuery(STRING_PROPERTY, new Condition(Operators.NULL));

        // WHEN
        final Collection<StubDocument> itemResults = docStoreTemplate.fetchDocuments(query, StubDocument.class);

        // THEN
        assertNotNull(itemResults);
        assertEquals(1, itemResults.size());
        assertEquals(stubDocument, itemResults.iterator().next());
    }

    @Test
    public void shouldFetchTenNullDocuments() {
        // GIVEN
        final InMemoryDocumentStoreTemplate docStoreTemplate = new InMemoryDocumentStoreTemplate(databaseSchemaHolder);
        for (int i = 0; i < 10; i++) {
            final StubDocument stubDocument = new StubDocument();
            stubDocument.setId(randomId());
            docStoreTemplate.create(stubDocument);
        }
        final Query query = new AttributeQuery(STRING_PROPERTY, new Condition(Operators.NULL));

        // WHEN
        final Collection<StubDocument> itemResults = docStoreTemplate.fetchDocuments(query, StubDocument.class);

        // THEN
        assertNotNull(itemResults);
        assertEquals(10, itemResults.size());
    }

    @Test
    public void shouldFetchFiveNullDocuments() {
        // GIVEN
        final InMemoryDocumentStoreTemplate docStoreTemplate = new InMemoryDocumentStoreTemplate(databaseSchemaHolder);
        for (int i = 0; i < 10; i++) {
            final StubDocument stubDocument = new StubDocument();
            stubDocument.setId(randomId());
            if (i % 2 == 0) {
                stubDocument.setStringProperty(Randoms.randomString());
            }
            docStoreTemplate.create(stubDocument);
        }
        final Query query = new AttributeQuery(STRING_PROPERTY, new Condition(Operators.NULL));

        // WHEN
        final Collection<StubDocument> itemResults = docStoreTemplate.fetchDocuments(query, StubDocument.class);

        // THEN
        assertNotNull(itemResults);
        assertEquals(5, itemResults.size());
    }

    @Test
    public void shouldFetchUniqueDocument() {
        // GIVEN
        final InMemoryDocumentStoreTemplate docStoreTemplate = new InMemoryDocumentStoreTemplate(databaseSchemaHolder);
        String propertyValue = "";
        final int randomIndex = Randoms.randomInt(10);
        for (int i = 0; i < 10; i++) {
            final StubDocument stubDocument = new StubDocument();
            stubDocument.setId(randomId());
            stubDocument.setStringProperty(Randoms.randomString());
            docStoreTemplate.create(stubDocument);
            if (randomIndex == i) {
                propertyValue = stubDocument.getStringProperty();
            }
        }
        final Query query = new AttributeQuery(STRING_PROPERTY, new Condition(Operators.EQUALS, propertyValue));

        // WHEN
        final StubDocument itemResult = docStoreTemplate.fetchUniqueDocument(query, StubDocument.class);

        // THEN
        assertNotNull(itemResult);
    }
}
