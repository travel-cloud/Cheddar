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
package com.clicktravel.common.logging;

import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility for logging messages as JSON rather than plain strings.
 *
 * Example usage:
 *
 * <pre>
 * {@code
 * private final Logger log = LoggerFactory.getLogger(getClass());
 *
 * ... {
 *
 *     Map<String,Object> message = new HashMap<>();
 *     Map<String,Object> userDetail = new HashMap<>();
 *
 *     userDetail.put("id", "9182-abf");
 *     userDetail.put("firstName", "Bob");
 *     message.put("eventType", "logInSuccess");
 *     message.put("user", userDetail);
 *
 *     JsonLog.info(logger, message);
 * }}
 * </pre>
 */
public class JsonLog {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static String asJson(final Object message) {
        try {
            return mapper.writeValueAsString(message);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException("Could not serialise log message to JSON", e);
        }
    }

    /**
     * Log a message as JSON at TRACE level
     * @param logger Logger to write message to
     * @param message Message to be serialised to JSON and written to log
     */
    public static void trace(final Logger logger, final Object message) {
        logger.trace(asJson(message));
    }

    /**
     * Log a message as JSON at DEBUG level
     * @param logger Logger to write message to
     * @param message Message to be serialised to JSON and written to log
     */
    public static void debug(final Logger logger, final Object message) {
        logger.debug(asJson(message));
    }

    /**
     * Log a message as JSON at INFO level
     * @param logger Logger to write message to
     * @param message Message to be serialised to JSON and written to log
     */
    public static void info(final Logger logger, final Object message) {
        logger.info(asJson(message));
    }

    /**
     * Log a message as JSON at WARN level
     * @param logger Logger to write message to
     * @param message Message to be serialised to JSON and written to log
     */
    public static void warn(final Logger logger, final Object message) {
        logger.warn(asJson(message));
    }

    /**
     * Log a message as JSON at ERROR level
     * @param logger Logger to write message to
     * @param message Message to be serialised to JSON and written to log
     */
    public static void error(final Logger logger, final Object message) {
        logger.error(asJson(message));
    }

}
