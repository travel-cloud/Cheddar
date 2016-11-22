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

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LoggingUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingUtils.class);
    private static final Gson MAPPER;

    static {
        MAPPER = new GsonBuilder().addSerializationExclusionStrategy(new ExclusionStrategy() {

            @Override
            public boolean shouldSkipField(final FieldAttributes f) {
                return "password".equalsIgnoreCase(f.getName());
            }

            @Override
            public boolean shouldSkipClass(final Class<?> clazz) {
                return false;
            }
        }).create();
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
                LOGGER.debug(template, tableName, MAPPER.toJson(item));
            }
        } catch (final Exception e) {
            LOGGER.warn("Error during logging the interaction with DynamoDb: ", e);
        }
    }
}
