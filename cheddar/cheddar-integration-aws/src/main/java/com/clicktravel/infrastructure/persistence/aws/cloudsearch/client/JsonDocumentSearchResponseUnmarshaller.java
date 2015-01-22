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
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.clicktravel.cheddar.infrastructure.persistence.document.search.Document;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JsonDocumentSearchResponseUnmarshaller {

    private final ObjectMapper mapper;

    public JsonDocumentSearchResponseUnmarshaller() {
        mapper = new ObjectMapper();
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(DateTime.class, new JodaDateTimeDeserializer());
        mapper.registerModule(module);
        mapper.configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED, true);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    public <T extends Document> T unmarshall(final Map<String, List<String>> fields, final Class<T> documentClass) {
        try {
            final String fieldJson = mapper.writeValueAsString(fields);
            final JsonNode fieldsNode = mapper.readValue(fieldJson, JsonNode.class);
            final String documentString = mapper.writeValueAsString(fieldsNode);
            final T document = mapper.readValue(documentString, documentClass);
            return document;
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
