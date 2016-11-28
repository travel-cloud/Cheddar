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
package com.clicktravel.infrastructure.persistence.aws.dynamodb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;

public class LoggingUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingUtils.class);
    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        MAPPER.registerModule(new JodaModule());
    }

    public static void logWriteItemToDatabase(final String tableName, final Object item) {
        logDebug("DB-WRITE [{}] [{}]", tableName, item);
    }

    public static void logReadItemFromDatabase(final String tableName, final Object item) {
        logDebug("DB-READ [{}] [{}]", tableName, item);
    }

    private static void logDebug(final String template, final String tableName, final Object item) {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(template, tableName, MAPPER.writeValueAsString(item));
            }
        } catch (final Exception e) {
            LOGGER.warn("Error during logging the interaction with DynamoDb: ", e);
        }
    }
}
