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
package com.clicktravel.cheddar.rest.body.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.clicktravel.schema.canonical.data.model.v1.common.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
@Produces("text/plain")
public class ErrorResponseToTextPlainBodyWriter implements MessageBodyWriter<ErrorResponse> {

    private final ObjectMapper objectMapper;

    public ErrorResponseToTextPlainBodyWriter() {
        objectMapper = new ObjectMapper();
    }

    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
            final MediaType mediaType) {
        return type == ErrorResponse.class;
    }

    @Override
    public long getSize(final ErrorResponse t, final Class<?> type, final Type genericType,
            final Annotation[] annotations, final MediaType mediaType) {
        // deprecated by JAX-RS 2.0 and ignored by Jersey runtime
        return 0;
    }

    @Override
    public void writeTo(final ErrorResponse errorResponse, final Class<?> type, final Type genericType,
            final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders,
            final OutputStream entityStream) throws IOException, WebApplicationException {

        final Writer writer = new PrintWriter(entityStream);
        writer.write(objectMapper.writeValueAsString(errorResponse));
        writer.flush();
        writer.close();
    }
}
