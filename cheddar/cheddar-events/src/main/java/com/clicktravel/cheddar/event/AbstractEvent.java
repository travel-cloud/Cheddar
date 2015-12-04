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
package com.clicktravel.cheddar.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;

public abstract class AbstractEvent implements Event {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static Logger logger = LoggerFactory.getLogger(AbstractEvent.class);

    static {
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.registerModule(new JodaModule());
    }

    @Override
    public abstract String type();

    public static <T extends Event> T newEvent(final Class<T> eventClass, final String serializedEvent) {
        logger.debug("Serialized event: " + serializedEvent);
        try {
            final T event = eventClass.newInstance();
            event.deserializeAndApply(serializedEvent);
            return event;
        } catch (final Exception e) {
            throw new IllegalStateException("Cannot build event from serialized form: [" + serializedEvent + "]", e);
        }
    }

    @Override
    public final String serialize() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (final Exception e) {
            throw new IllegalStateException("Could not serialize event: [" + this + "]", e);
        }
    }

    @Override
    public final void deserializeAndApply(final String serializedString) {
        try {
            final Event event = MAPPER.readValue(serializedString, getClass());
            BeanUtils.copyProperties(event, this);
        } catch (final Exception e) {
            throw new IllegalStateException("Could not serialize " + getClass() + " : [" + serializedString + "]", e);
        }
    }

}
