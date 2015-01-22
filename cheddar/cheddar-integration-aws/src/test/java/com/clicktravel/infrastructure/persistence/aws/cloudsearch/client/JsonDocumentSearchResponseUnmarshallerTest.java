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
import static com.clicktravel.common.random.Randoms.randomEnum;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import com.clicktravel.infrastructure.persistence.aws.cloudsearch.StubDocument;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.StubDocument.MyEnum;

public class JsonDocumentSearchResponseUnmarshallerTest {

    @Test
    public void shouldCreateJsonDocumentSearchResponseUnmarshaller() {
        // Given

        // When
        final JsonDocumentSearchResponseUnmarshaller jsonDocumentSearchResponseUnmarshaller = new JsonDocumentSearchResponseUnmarshaller();

        // Then
        assertNotNull(jsonDocumentSearchResponseUnmarshaller);
    }

    @Test
    public void shouldUnmarshall_withFieldsAndDocumentClass() {
        // Given
        final Map<String, List<String>> fields = new HashMap<>();
        final String stringProperty = randomString(10);
        fields.put("stringproperty", Arrays.asList(stringProperty));
        final String[] stringListValues = new String[3];
        stringListValues[0] = randomString(10);
        stringListValues[1] = randomString(10);
        stringListValues[2] = randomString(10);
        fields.put("collectionproperty", Arrays.asList(stringListValues[0], stringListValues[1], stringListValues[2]));
        final MyEnum myEnum = randomEnum(MyEnum.class);
        fields.put("myenum", Arrays.asList(myEnum.name()));
        final DateTime dateTimeValue = randomDateTime();
        fields.put("datetimevalue", Arrays.asList(ISODateTimeFormat.dateTime().withZoneUTC().print(dateTimeValue)));
        final JsonDocumentSearchResponseUnmarshaller jsonDocumentSearchResponseUnmarshaller = new JsonDocumentSearchResponseUnmarshaller();

        // When
        final StubDocument document = jsonDocumentSearchResponseUnmarshaller.unmarshall(fields, StubDocument.class);

        // Then
        assertNotNull(document);
        assertEquals(stringProperty, document.getStringProperty());
        final List<String> collectionProperty = (List<String>) document.getCollectionProperty();
        for (int i = 0; i < collectionProperty.size(); i++) {
            assertEquals(stringListValues[i], collectionProperty.get(i));
        }
        assertEquals(myEnum, document.getMyEnum());
        assertEquals(dateTimeValue.getMillis(), document.getDateTimeValue().getMillis());
    }
}
