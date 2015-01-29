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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

public class JodaDateTimeDeserializer extends StdScalarDeserializer<DateTime> {

    private static final long serialVersionUID = -6535122411722119917L;
    private static final DateTimeFormatter formatter = ISODateTimeFormat.dateTimeParser().withZoneUTC();

    protected JodaDateTimeDeserializer() {
        super(DateTime.class);
    }

    @Override
    public DateTime deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException,
            JsonProcessingException {
        if (jp.getCurrentToken() != JsonToken.VALUE_STRING) {
            throw ctxt.mappingException("Expected JSON string");
        }
        return formatter.parseDateTime(jp.getText());
    }

}
