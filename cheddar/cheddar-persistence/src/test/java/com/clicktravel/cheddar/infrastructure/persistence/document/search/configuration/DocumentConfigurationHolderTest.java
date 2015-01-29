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
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

public class DocumentConfigurationHolderTest {

    @Test
    public void shouldCreateDocumentConfigurationHolder_withSchemaNameAndDocumentConfigurations() throws Exception {
        // Given
        final String schemaName = randomString(10);
        final Collection<DocumentConfiguration> documentConfigurations = Arrays
                .asList(mock(DocumentConfiguration.class), mock(DocumentConfiguration.class),
                        mock(DocumentConfiguration.class));

        // When
        final DocumentConfigurationHolder documentConfigurationHolder = new DocumentConfigurationHolder(schemaName,
                documentConfigurations);

        // Then
        assertNotNull(documentConfigurationHolder);
        assertEquals(schemaName, documentConfigurationHolder.schemaName());
        assertEquals(documentConfigurations, documentConfigurationHolder.documentConfigurations());
    }

    @Test
    public void shouldNotCreateDocumentConfigurationHolder_withEmptySchemaName() throws Exception {
        // Given
        final String schemaName = "";
        final Collection<DocumentConfiguration> documentConfigurations = Arrays
                .asList(mock(DocumentConfiguration.class), mock(DocumentConfiguration.class),
                        mock(DocumentConfiguration.class));

        // When
        IllegalArgumentException actualException = null;
        try {
            new DocumentConfigurationHolder(schemaName, documentConfigurations);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotCreateDocumentConfigurationHolder_withNullSchemaName() throws Exception {
        // Given
        final String schemaName = null;
        final Collection<DocumentConfiguration> documentConfigurations = Arrays
                .asList(mock(DocumentConfiguration.class), mock(DocumentConfiguration.class),
                        mock(DocumentConfiguration.class));

        // When
        IllegalArgumentException actualException = null;
        try {
            new DocumentConfigurationHolder(schemaName, documentConfigurations);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCreateDocumentConfigurationHolder_withEmptyDocumentConfigurations() throws Exception {
        // Given
        final String schemaName = randomString(10);
        final Collection<DocumentConfiguration> documentConfigurations = Arrays.asList();

        // When
        IllegalArgumentException actualException = null;
        try {
            new DocumentConfigurationHolder(schemaName, documentConfigurations);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCreateDocumentConfigurationHolder_withNullDocumentConfigurations() throws Exception {
        // Given
        final String schemaName = randomString(10);
        final Collection<DocumentConfiguration> documentConfigurations = null;

        // When
        IllegalArgumentException actualException = null;
        try {
            new DocumentConfigurationHolder(schemaName, documentConfigurations);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

}
