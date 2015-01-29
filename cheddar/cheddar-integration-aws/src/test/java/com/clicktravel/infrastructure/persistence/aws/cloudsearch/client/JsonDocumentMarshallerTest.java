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
package com.clicktravel.infrastructure.persistence.aws.cloudsearch.client;

import static com.clicktravel.common.random.Randoms.randomDateTime;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import com.clicktravel.infrastructure.persistence.aws.cloudsearch.client.DocumentUpdate.Type;

public class JsonDocumentMarshallerTest {

    @Test
    public void shouldMarshallDocumentUpdateCollection_withDocumentUpdateCollection() throws Exception {
        // Given
        final String documentId1 = randomString(10);
        final DocumentUpdate documentUpdate1 = new DocumentUpdate(Type.ADD, documentId1);
        final String fieldName1a = randomString(10);
        final String fieldValue1a = randomString();
        final Field field1a = new Field(fieldName1a, fieldValue1a);
        final String fieldName1b = randomString(10);
        final String fieldValue1b = randomString();
        final Field field1b = new Field(fieldName1b, fieldValue1b);
        final Collection<Field> fields1 = Arrays.asList(field1a, field1b);
        documentUpdate1.withFields(fields1);
        final String documentId2 = randomString(10);
        final DocumentUpdate documentUpdate2 = new DocumentUpdate(Type.ADD, documentId2);
        final String fieldName2a = randomString(10);
        final DateTime fieldValue2a = randomDateTime();
        final Field field2a = new Field(fieldName2a, fieldValue2a);
        final String fieldName2b = randomString(10);
        final String fieldValue2b = randomString();
        final Field field2b = new Field(fieldName2b, fieldValue2b);
        final Collection<Field> fields2 = Arrays.asList(field2a, field2b);
        documentUpdate2.withFields(fields2);
        final String documentId3 = randomString(10);
        final DocumentUpdate documentUpdate3 = new DocumentUpdate(Type.DELETE, documentId3);

        final Collection<DocumentUpdate> documentUpdates = Arrays.asList(documentUpdate1, documentUpdate2,
                documentUpdate3);
        final String expectedJsonString = "[{\"id\":\"" + documentId1 + "\",\"type\":\"add\",\"fields\":{\""
                + fieldName1a.toLowerCase() + "\":\"" + fieldValue1a + "\",\"" + fieldName1b.toLowerCase() + "\":\""
                + fieldValue1b + "\"}},{\"id\":\"" + documentId2 + "\",\"type\":\"add\",\"fields\":{\""
                + fieldName2a.toLowerCase() + "\":\"" + ISODateTimeFormat.dateTime().withZoneUTC().print(fieldValue2a)
                + "\",\"" + fieldName2b.toLowerCase() + "\":\"" + fieldValue2b + "\"}},{\"id\":\"" + documentId3
                + "\",\"type\":\"delete\"}]";

        // When
        final String jsonString = JsonDocumentUpdateMarshaller.marshall(documentUpdates);

        // Then
        assertEquals(expectedJsonString, jsonString);
    }

    @Test
    public void shouldMarshallDocumentUpdateCollection_withDocumentUpdateCollectionAndNullFields() throws Exception {
        // Given
        final String documentId = randomString(10);
        final DocumentUpdate documentUpdate = new DocumentUpdate(Type.ADD, documentId);
        final String fieldName1 = randomString(10);
        final String fieldValue1 = randomString();
        final Field field1 = new Field(fieldName1, fieldValue1);
        final String fieldName2 = randomString(10);
        final String fieldValue2 = null;
        final Field field2 = new Field(fieldName2, fieldValue2);
        final Collection<Field> fields = Arrays.asList(field1, field2);
        documentUpdate.withFields(fields);

        final Collection<DocumentUpdate> documentUpdates = Arrays.asList(documentUpdate);
        final String expectedJsonString = "[{\"id\":\"" + documentId + "\",\"type\":\"add\",\"fields\":{\""
                + fieldName1.toLowerCase() + "\":\"" + fieldValue1 + "\"}}]";

        // When
        final String jsonString = JsonDocumentUpdateMarshaller.marshall(documentUpdates);

        // Then
        assertEquals(expectedJsonString, jsonString);
    }

}
