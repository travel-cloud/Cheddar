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
package com.clicktravel.cheddar.system.event.publisher;

import com.clicktravel.cheddar.event.EventPublisher;
import com.clicktravel.cheddar.infrastructure.messaging.MessagePublisher;
import com.clicktravel.cheddar.system.event.SystemEvent;

public class SystemEventPublisher extends EventPublisher<SystemEvent> {

    private static SystemEventPublisher instance;

    public static void init(final MessagePublisher messagePublisher) {
        instance = new SystemEventPublisher(messagePublisher);
    }

    private SystemEventPublisher(final MessagePublisher messagePublisher) {
        super(messagePublisher);
    }

    public static SystemEventPublisher instance() {
        if (instance == null) {
            throw new IllegalStateException("SystemEventPublisher not initialized");
        }
        return instance;
    }

}
