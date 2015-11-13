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
import java.util.Collection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.joda.JodaModule;

public class JsonDocumentUpdateMarshaller {

    private static final JsonDocumentUpdateMarshaller INSTANCE = new JsonDocumentUpdateMarshaller();

    private final ObjectMapper mapper;

    public static final String marshall(final Collection<DocumentUpdate> documentUpdates) {
        return INSTANCE.marshallDocumentUpdates(documentUpdates);
    }

    private JsonDocumentUpdateMarshaller() {
        mapper = new ObjectMapper();
        final SimpleModule module = new SimpleModule();
        module.addSerializer(Boolean.class, new BooleanLiteralSerializer());
        mapper.registerModule(module);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.registerModule(new JodaModule());
        mapper.setPropertyNamingStrategy(new LowerCasePropertyNamingStrategy());
    }

    private String marshallDocumentUpdates(final Collection<DocumentUpdate> documentUpdates) {
        try {
            final ArrayNode documentUpdateCollectionNode = mapper.createArrayNode();
            for (final DocumentUpdate documentUpdate : documentUpdates) {
                documentUpdateCollectionNode.add(createJsonNode(documentUpdate));
            }
            return mapper.writeValueAsString(documentUpdateCollectionNode);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private ObjectNode createJsonNode(final DocumentUpdate document) throws IOException {
        final ObjectNode documentUpdateNode = mapper.createObjectNode();
        documentUpdateNode.put("id", document.getId());
        documentUpdateNode.put("type", document.getType().name().toLowerCase());
        final ObjectNode fields = mapper.createObjectNode();
        for (final Field field : document.getFields()) {
            if (field.getValue() != null) {
                final String fieldValueStr = mapper.writeValueAsString(field.getValue());
                final JsonNode fieldValueJsonNode = mapper.readTree(fieldValueStr);
                fields.put(field.getName().toLowerCase(), fieldValueJsonNode);
            }
        }
        if (fields.size() > 0) {
            documentUpdateNode.put("fields", fields);
        }
        return documentUpdateNode;
    }

}
