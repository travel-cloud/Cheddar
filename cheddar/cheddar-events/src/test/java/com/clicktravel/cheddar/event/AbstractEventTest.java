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

import static com.clicktravel.common.random.Randoms.randomString;

import org.junit.Assert;
import org.junit.Test;

import com.clicktravel.common.random.Randoms;

public class AbstractEventTest {

    @Test
    public void shouldSerializeEvent_withOnlyType() {
        // Given
        final String type = Randoms.randomString(10);
        final String value = Randoms.randomString(10);
        final String expectedSerializedEvent = "{\"value\":\"" + value + "\"}";

        final AbstractEvent event = new AbstractEvent() {

            @Override
            public String type() {
                return type;
            }

            @SuppressWarnings("unused")
            public String getValue() {
                return value;
            }

        };

        // When
        final String serializedEvent = event.serialize();

        // Then
        Assert.assertEquals(expectedSerializedEvent, serializedEvent);
    }

    @Test
    public void shouldDeserializeEvent_withEventTypeAndStringProperty() {
        // Given
        final String value = Randoms.randomString(10);
        final String serializedString = "{\"value\":\"" + value + "\"}";
        final StubDomainEvent event = new StubDomainEvent();

        // When
        event.deserializeAndApply(serializedString);

        // Then
        Assert.assertEquals(value, event.getValue());
    }

    private static class StubDomainEvent extends AbstractEvent {

        private final String type = randomString(10);
        private String value;

        @Override
        public String type() {
            return type;
        }

        @SuppressWarnings("unused")
        public void setValue(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }

}
