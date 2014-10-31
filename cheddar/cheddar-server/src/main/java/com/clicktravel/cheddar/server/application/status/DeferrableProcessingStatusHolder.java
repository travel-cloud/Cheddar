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
package com.clicktravel.cheddar.server.application.status;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.clicktravel.cheddar.infrastructure.messaging.MessageListener;

@Component
public class DeferrableProcessingStatusHolder {

    private final Set<MessageListener> messageListenersForDeferrableProcessing;

    @Autowired
    public DeferrableProcessingStatusHolder(final Collection<MessageListener> messageListeners,
            final MessageListener eventMessageListener, final MessageListener highPriorityEventMessageListener,
            final MessageListener systemEventMessageListener) {
        messageListenersForDeferrableProcessing = new HashSet<>(messageListeners);
        messageListeners.remove(eventMessageListener);
        messageListeners.remove(highPriorityEventMessageListener);
        messageListeners.remove(systemEventMessageListener);
    }

    public boolean isDeferrableProcessing() {
        for (final MessageListener messageListener : messageListenersForDeferrableProcessing) {
            if (!messageListener.hasTerminated()) {
                return true;
            }
        }
        return false;
    }
}
