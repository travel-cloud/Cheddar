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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.clicktravel.cheddar.infrastructure.persistence.document.search.Document;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.DocumentSearchResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonDocumentSearchResponseUnmarshaller<T extends Document> {

    private final Class<T> documentClass;
    private final ObjectMapper mapper;

    public JsonDocumentSearchResponseUnmarshaller(final Class<T> documentClass) {
        this.documentClass = documentClass;
        mapper = new ObjectMapper();
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(DateTime.class, new JodaDateTimeDeserializer());
        mapper.registerModule(module);
    }

    public DocumentSearchResponse<T> unmarshall(final String json) {
        try {
            final ObjectNode root = mapper.readValue(json, ObjectNode.class);
            final JsonNode hitsNode = root.get("hits");
            if (hitsNode == null) {
                throw new IllegalStateException("No hits found in search response");
            }
            final String cursor = hitsNode.path("cursor").asText();
            final JsonNode hitNode = hitsNode.get("hit");
            final List<T> documents = new ArrayList<>();
            final int found = hitsNode.path("found").asInt();

            for (final JsonNode hitElementNode : hitNode) {
                final String docId = hitElementNode.get("id").textValue();
                final JsonNode fieldsNode = hitElementNode.get("fields");
                final String documentString = mapper.writeValueAsString(fieldsNode);
                final T document = mapper.readValue(documentString, documentClass);
                document.setId(docId);
                documents.add(document);
            }

            return new DocumentSearchResponse<T>(found, cursor, documents);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
