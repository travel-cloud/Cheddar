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
package com.clicktravel.infrastructure.messaging.inmemory;

import java.util.*;

import com.clicktravel.cheddar.event.AbstractEvent;
import com.clicktravel.cheddar.event.Event;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;

/**
 * Methods that verify that certain messages have been sent or events published to in-memory mocks. These methods can be
 * used to verify in-memory messages sent to a {@link InMemoryMessageSender} or event messages published using a
 * {@link InMemoryMessagePublisher}
 */
public class InMemoryMessageVerifier {

    /**
     * Verifies that a message of a specified type has been sent using the given {@link InMemoryMessageSender}
     * @param inMemoryMessageSender {@link InMemoryMessageSender} to check
     * @param messageType Type of message
     * @return {@code true} if message of the specified type was sent
     */
    public static boolean messageTypeSent(final InMemoryMessageSender<TypedMessage> inMemoryMessageSender,
            final String messageType) {
        return containsMessageOfType(inMemoryMessageSender.getSentMessages(), messageType);
    }

    /**
     * Verifies that an event of a specified class has been published using the given {@link InMemoryMessagePublisher}
     * @param inMemoryMessagePublisher {@link InMemoryMessagePublisher} to check
     * @param eventClass Class of event
     * @return {@code true} if event of specified class has been published
     */
    public static boolean eventTypePublished(final InMemoryMessagePublisher<TypedMessage> inMemoryMessagePublisher,
            final Class<? extends Event> eventClass) {
        try {
            final String messageType = eventClass.newInstance().type();
            return containsMessageOfType(inMemoryMessagePublisher.getPublishedMessages(), messageType);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to instantiate prototype event to verify publication");
        }
    }

    /**
     * Verifies that an event equals to the one specified has been published using the given
     * {@link InMemoryMessagePublisher}
     * @param inMemoryMessagePublisher {@link InMemoryMessagePublisher} to check
     * @param event Event to match
     * @return {@code true} if matching event has been published
     */
    public static boolean eventPublished(final InMemoryMessagePublisher<TypedMessage> inMemoryMessagePublisher,
            final Event event) {
        return publishedEvents(inMemoryMessagePublisher, event.getClass()).contains(event);
    }

    private static boolean containsMessageOfType(final Collection<TypedMessage> messages, final String messageType) {
        return messages.stream().anyMatch(m -> m.getType().equals(messageType));
    }

    /**
     * Returns a filtered list of all events published using the given {@link InMemoryMessagePublisher} of the specified
     * event types. This enables testing of published events by means other than complete equality testing.
     * @param inMemoryMessagePublisher {@link InMemoryMessagePublisher} to get published events for
     * @param eventClasses Event classes to filter returned list
     * @return Filtered list of events that have been published
     */
    @SafeVarargs
    public static List<Event> publishedEvents(final InMemoryMessagePublisher<TypedMessage> inMemoryMessagePublisher,
            final Class<? extends Event>... eventClasses) {
        final Map<String, Class<? extends Event>> typeToEventClass = new HashMap<>();
        for (final Class<? extends Event> eventClass : eventClasses) {
            try {
                typeToEventClass.put(eventClass.newInstance().type(), eventClass);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException("Unable to instatiate prototype event " + eventClass.getName());
            }
        }
        final List<Event> events = new LinkedList<>();
        for (final TypedMessage typedMessage : inMemoryMessagePublisher.getPublishedMessages()) {
            final Class<? extends Event> eventClass = typeToEventClass.get(typedMessage.getType());
            if (eventClass != null) {
                events.add(AbstractEvent.newEvent(eventClass, typedMessage.getPayload()));
            }
        }
        return events;
    }

}
