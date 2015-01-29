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
package com.amazonaws.services.dynamodbv2.datamodeling;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Collection;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.clicktravel.cheddar.infrastructure.persistence.database.Item;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MappingJsonFactory;

/**
 * Uses DynamoDB's underlying marshalling functionality provided by DynamoDbMapper
 */
public class DynamoDbPropertyMarshaller {

    private static final JsonFactory jsonFactory = new MappingJsonFactory();

    public static <T extends Item> void setValue(final T item, final PropertyDescriptor propertyDescriptor,
            final AttributeValue attributeValue) {
        if (attributeValue != null) {
            final DynamoDBReflector reflector = new DynamoDBReflector();
            final Method writeMethod = propertyDescriptor.getWriteMethod();
            Object argument = null;
            if (writeMethod != null) {
                try {
                    final ArgumentUnmarshaller unmarshaller = reflector.getArgumentUnmarshaller(item,
                            propertyDescriptor.getReadMethod(), writeMethod, null);
                    argument = unmarshaller.unmarshall(attributeValue);
                } catch (final DynamoDBMappingException | ParseException mappingException) {
                    try {
                        final JsonParser jsonParser = jsonFactory.createJsonParser(new StringReader(attributeValue
                                .getS()));
                        argument = jsonParser.readValueAs(writeMethod.getParameterTypes()[0]);
                    } catch (final Exception e) {
                        throw new IllegalStateException("Could not parse attribute value: " + attributeValue, e);
                    }
                }
                try {
                    writeMethod.invoke(item, argument);
                } catch (final Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    public static <T extends Item> AttributeValue getValue(final T item, final PropertyDescriptor propertyDescriptor) {
        final Method readMethod = propertyDescriptor.getReadMethod();
        Object propertyValue;
        try {
            propertyValue = readMethod.invoke(item);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
        if (propertyValue == null) {
            return null;
        }
        if (propertyValue instanceof Collection && ((Collection<?>) propertyValue).isEmpty()) {
            return null;
        }
        final DynamoDBReflector reflector = new DynamoDBReflector();
        try {
            final ArgumentMarshaller marshaller = reflector.getArgumentMarshaller(readMethod);
            return marshaller.marshall(propertyValue);
        } catch (final DynamoDBMappingException e) {
            try {
                final StringWriter output = new StringWriter();
                final JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(output);
                jsonGenerator.writeObject(propertyValue);
                return new AttributeValue(output.toString());
            } catch (final IOException ioException) {
                throw new IllegalStateException(ioException);
            }
        }
    }

    public static ScalarAttributeType getAttributeType(final Class<?> propertyClass) {
        if (propertyClass == ByteBuffer.class || propertyClass == byte[].class) {
            return ScalarAttributeType.B;
        } else if (char.class.isAssignableFrom(propertyClass)) {
            return ScalarAttributeType.S;
        } else if (propertyClass.isPrimitive() || Number.class.isAssignableFrom(propertyClass)
                || Boolean.class.isAssignableFrom(propertyClass)) {
            return ScalarAttributeType.N;
        } else {
            return ScalarAttributeType.S;
        }
    }
}
