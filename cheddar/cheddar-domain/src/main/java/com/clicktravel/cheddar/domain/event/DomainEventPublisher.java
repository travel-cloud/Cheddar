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
package com.clicktravel.cheddar.domain.event;

import com.clicktravel.cheddar.event.EventPublisher;
import com.clicktravel.cheddar.infrastructure.messaging.MessagePublisher;

public class DomainEventPublisher extends EventPublisher<DomainEvent> {

    private static DomainEventPublisher instance;

    public static void init(final MessagePublisher messagePublisher) {
        instance = new DomainEventPublisher(messagePublisher);
    }

    private DomainEventPublisher(final MessagePublisher messagePublisher) {
        super(messagePublisher);
    }

    public static DomainEventPublisher instance() {
        if (instance == null) {
            throw new IllegalStateException("DomainEventPublisher not initialized");
        }
        return instance;
    }

}
