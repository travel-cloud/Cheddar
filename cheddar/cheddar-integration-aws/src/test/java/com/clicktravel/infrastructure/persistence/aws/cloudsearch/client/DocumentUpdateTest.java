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

import static com.clicktravel.common.random.Randoms.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import com.clicktravel.infrastructure.persistence.aws.cloudsearch.client.DocumentUpdate.Type;

public class DocumentUpdateTest {

    @Test
    public void shouldCreateDocumentUpdate_withTypeAndId() throws Exception {
        // Given
        final Type type = randomEnum(Type.class);
        final String id = randomString(10);

        // When
        final DocumentUpdate documentUpdate = new DocumentUpdate(type, id);

        // Then
        assertNotNull(documentUpdate);
        assertEquals(type, documentUpdate.getType());
        assertEquals(id, documentUpdate.getId());
    }

    @Test
    public void shouldFieldsId_withFields() throws Exception {
        // Given
        final Collection<Field> fields = Arrays.asList(randomField(), randomField(), randomField());
        final Type type = randomEnum(Type.class);
        final String id = randomString(10);

        final DocumentUpdate documentUpdate = new DocumentUpdate(type, id);

        // When
        final DocumentUpdate returnedDocumentUpdate = documentUpdate.withFields(fields);

        // Then
        assertNotNull(returnedDocumentUpdate);
        assertEquals(fields, returnedDocumentUpdate.getFields());
    }

    private Field randomField() {
        final Object value = Arrays.asList(randomString(), randomLocalDate(), randomLong(), randomBoolean(),
                randomBigDecimal(randomInt(1000), 3));
        final Field field = new Field(randomString(10), value);
        return field;
    }

}
