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

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("unchecked")
public class JsonLogTest {

    private Logger logger;
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        logger = mock(Logger.class);
        mapper = new ObjectMapper();
    }

    private Map<String, Object> randomMessage() {
        final Map<String, Object> message = new HashMap<>();
        message.put(randomString(), randomString());
        return message;
    }

    private void assertExpectedJson(final Map<String, Object> expectedMap, final String actualJson) throws Exception {
        final Map<String, Object> actualMap = mapper.readValue(actualJson, Map.class);
        assertEquals(expectedMap, actualMap);
    }

    @Test
    public void shouldLogTraceJson_withLoggerAndMessage() throws Exception {
        // Given
        final Map<String, Object> message = randomMessage();

        // When
        JsonLog.trace(logger, message);

        // Then
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger).trace(captor.capture());
        assertExpectedJson(message, captor.getValue());
    }

    @Test
    public void shouldLogDebugJson_withLoggerAndMessage() throws Exception {
        // Given
        final Map<String, Object> message = randomMessage();

        // When
        JsonLog.debug(logger, message);

        // Then
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger).debug(captor.capture());
        assertExpectedJson(message, captor.getValue());
    }

    @Test
    public void shouldLogInfoJson_withLoggerAndMessage() throws Exception {
        // Given
        final Map<String, Object> message = randomMessage();

        // When
        JsonLog.info(logger, message);

        // Then
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger).info(captor.capture());
        assertExpectedJson(message, captor.getValue());
    }

    @Test
    public void shouldLogWarnJson_withLoggerAndMessage() throws Exception {
        // Given
        final Map<String, Object> message = randomMessage();

        // When
        JsonLog.warn(logger, message);

        // Then
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger).warn(captor.capture());
        assertExpectedJson(message, captor.getValue());
    }

    @Test
    public void shouldLogErrorJson_withLoggerAndMessage() throws Exception {
        // Given
        final Map<String, Object> message = randomMessage();

        // When
        JsonLog.error(logger, message);

        // Then
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger).error(captor.capture());
        assertExpectedJson(message, captor.getValue());
    }

}
