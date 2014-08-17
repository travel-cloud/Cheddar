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
import static com.clicktravel.common.random.Randoms.randomDouble;
import static com.clicktravel.common.random.Randoms.randomInt;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.persistence.document.search.DocumentSearchResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonDocumentSearchResponseUnmarshallerTest {

    @Test
    public void shouldUnmarshall_givenJson() throws Exception {
        // Given
        final String cursor = randomString();
        final int start = randomInt(100);
        final int found = randomInt(100);
        final String id0 = randomString();
        final String stringValue = randomString();
        final int intValue = randomInt(100);
        final double doubleValue = randomDouble();
        final DateTime dateTimeValue = randomDateTime();
        final List<String> listString = Arrays.asList(randomString(), randomString());
        final List<Integer> listInteger = Arrays.asList(randomInt(100), randomInt(100));
        final List<Double> listDouble = Arrays.asList(randomDouble(), randomDouble());
        final List<DateTime> listDateTime = Arrays.asList(randomDateTime(), randomDateTime());
        final DateTimeFormatter formatter = ISODateTimeFormat.dateTime().withZoneUTC();

        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode root = mapper.createObjectNode();
        final ObjectNode hitsNode = mapper.createObjectNode();
        root.set("hits", hitsNode);
        hitsNode.put("cursor", cursor);
        hitsNode.put("start", start);
        hitsNode.put("found", found);
        final ArrayNode hitNode = mapper.createArrayNode();
        hitsNode.put("hit", hitNode);
        final ObjectNode hitElement0Node = mapper.createObjectNode();
        hitNode.add(hitElement0Node);
        hitElement0Node.put("id", id0);
        final ObjectNode fields0Node = mapper.createObjectNode();
        hitElement0Node.put("fields", fields0Node);

        fields0Node.put("stringValue", stringValue);
        fields0Node.put("intValue", intValue);
        fields0Node.put("doubleValue", doubleValue);
        fields0Node.put("dateTimeValue", formatter.print(dateTimeValue));

        final ArrayNode listStringNode = mapper.createArrayNode();
        fields0Node.put("listString", listStringNode);
        listStringNode.add(listString.get(0));
        listStringNode.add(listString.get(1));

        final ArrayNode listIntegerNode = mapper.createArrayNode();
        fields0Node.put("listInteger", listIntegerNode);
        listIntegerNode.add(listInteger.get(0));
        listIntegerNode.add(listInteger.get(1));

        final ArrayNode listDoubleNode = mapper.createArrayNode();
        fields0Node.put("listDouble", listDoubleNode);
        listDoubleNode.add(listDouble.get(0));
        listDoubleNode.add(listDouble.get(1));

        final ArrayNode listDateTimeNode = mapper.createArrayNode();
        fields0Node.put("listDateTime", listDateTimeNode);
        listDateTimeNode.add(formatter.print(listDateTime.get(0)));
        listDateTimeNode.add(formatter.print(listDateTime.get(1)));

        final String json = mapper.writeValueAsString(root);

        final JsonDocumentSearchResponseUnmarshaller<StubDocument> unmarshaller = new JsonDocumentSearchResponseUnmarshaller<StubDocument>(
                StubDocument.class);

        // When
        final DocumentSearchResponse<StubDocument> documentSearchResponse = unmarshaller.unmarshall(json);

        // Then
        assertNotNull(documentSearchResponse);
        assertEquals(cursor, documentSearchResponse.getCursor());
        assertEquals(1, documentSearchResponse.getCount());
        assertEquals(found, documentSearchResponse.getTotalCount());
        final List<StubDocument> hits = documentSearchResponse.getHits();
        assertNotNull(hits);
        assertEquals(1, hits.size());
        final StubDocument hitDoc = hits.get(0);
        assertNotNull(hitDoc);
        assertEquals(stringValue, hitDoc.getStringValue());
        assertEquals(intValue, hitDoc.getIntValue());
        assertEquals(doubleValue, hitDoc.getDoubleValue(), 0.0);
        assertEquals(dateTimeValue.withZone(DateTimeZone.UTC), hitDoc.getDateTimeValue());
        assertEquals(listString, hitDoc.getListString());
        assertEquals(listInteger, hitDoc.getListInteger());
        assertEquals(listDouble, hitDoc.getListDouble());
        assertEquals(listDateTime.get(0).withZone(DateTimeZone.UTC), hitDoc.getListDateTime().get(0));
        assertEquals(listDateTime.get(1).withZone(DateTimeZone.UTC), hitDoc.getListDateTime().get(1));
    }

}
