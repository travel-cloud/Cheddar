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
package com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration;

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.persistence.document.search.Document;

public class DocumentConfigurationTest {

    @Test
    public void shouldCreateDocumentConfiguration_withNamespaceAndDocumentClass() throws Exception {
        // Given
        final String namespace = randomString(10);
        final Class<? extends Document> documentClass = StubDocument.class;

        // When
        final DocumentConfiguration documentConfiguration = new DocumentConfiguration(documentClass, namespace);

        // Then
        assertNotNull(documentConfiguration);
        assertEquals(namespace, documentConfiguration.namespace());
        assertEquals(documentClass, documentConfiguration.documentClass());
        assertEquals(4, documentConfiguration.properties().size());
        assertNotNull(documentConfiguration.properties().get("stringProperty"));
    }

    @Test
    public void shouldNotCreateDocumentConfiguration_withNullDocumentClass() throws Exception {
        // Given
        final String namespace = randomString(10);
        final Class<? extends Document> documentClass = null;

        // When
        IllegalArgumentException actualException = null;
        try {
            new DocumentConfiguration(documentClass, namespace);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotCreateDocumentConfiguration_withEmptyNamespace() throws Exception {
        // Given
        final String namespace = "";
        final Class<? extends Document> documentClass = StubDocument.class;

        // When
        IllegalArgumentException actualException = null;
        try {
            new DocumentConfiguration(documentClass, namespace);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotCreateDocumentConfiguration_withNullNamespace() throws Exception {
        // Given
        final String namespace = null;
        final Class<? extends Document> documentClass = StubDocument.class;

        // When
        IllegalArgumentException actualException = null;
        try {
            new DocumentConfiguration(documentClass, namespace);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldRegisterIndexes_withIndexDefinitions() throws Exception {
        // Given
        final Collection<IndexDefinition> indexDefinitions = Arrays.asList(new IndexDefinition("stringProperty",
                IndexFieldType.LITERAL));
        final String namespace = randomString(10);
        final Class<? extends Document> documentClass = StubDocument.class;
        final DocumentConfiguration documentConfiguration = new DocumentConfiguration(documentClass, namespace);

        // When
        documentConfiguration.registerIndexes(indexDefinitions);

        // Then
        assertEquals(indexDefinitions, documentConfiguration.indexDefinitions());
    }

    @Test
    public void shouldNotRegisterIndexes_withIncorrectIndexDefinitions() throws Exception {
        // Given
        final Collection<IndexDefinition> indexDefinitions = Arrays.asList(new IndexDefinition(randomString(10),
                IndexFieldType.INT));
        final String namespace = randomString(10);
        final Class<? extends Document> documentClass = StubDocument.class;
        final DocumentConfiguration documentConfiguration = new DocumentConfiguration(documentClass, namespace);

        // When
        IllegalStateException actualException = null;
        try {
            documentConfiguration.registerIndexes(indexDefinitions);
        } catch (final IllegalStateException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

}
