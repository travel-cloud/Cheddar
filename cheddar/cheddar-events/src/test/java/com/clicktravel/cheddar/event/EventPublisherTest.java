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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.clicktravel.common.random.Randoms;
import com.clicktravel.cheddar.event.Event;
import com.clicktravel.cheddar.event.EventPublisher;
import com.clicktravel.cheddar.infrastructure.messaging.Message;
import com.clicktravel.cheddar.infrastructure.messaging.MessagePublisher;

public class EventPublisherTest {

    @Test
    public void shouldPublishEvent_withEvent() {
        // Given
        final MessagePublisher messagePublisher = mock(MessagePublisher.class);
        final Event event = mock(Event.class);
        final String type = Randoms.randomString(5);
        when(event.type()).thenReturn(type);
        final String serialized = Randoms.randomString();
        when(event.serialize()).thenReturn(serialized);
        final EventPublisher<Event> eventPublisher = new EventPublisher<Event>(messagePublisher) {
        };

        // When
        eventPublisher.publishEvent(event);

        // Then
        final ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messagePublisher).publishMessage(messageArgumentCaptor.capture());
        assertEquals(type, messageArgumentCaptor.getValue().getType());
        assertEquals(serialized, messageArgumentCaptor.getValue().getPayload());
    }
}
