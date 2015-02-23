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
package com.clicktravel.cheddar.infrastructure.messaging.inmemory;

import com.clicktravel.cheddar.event.AbstractEvent;

public class StubEvent extends AbstractEvent {

    public static final String type = "test.StubEvent";
    private String eventField;

    @Override
    public String type() {
        return type;
    }

    public String getEventField() {
        return eventField;
    }

    public void setEventField(final String eventField) {
        this.eventField = eventField;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((eventField == null) ? 0 : eventField.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StubEvent other = (StubEvent) obj;
        if (eventField == null) {
            if (other.eventField != null) {
                return false;
            }
        } else if (!eventField.equals(other.eventField)) {
            return false;
        }
        return true;
    }

}
