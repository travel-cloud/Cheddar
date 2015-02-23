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
package com.clicktravel.cheddar.infrastructure.persistence.inmemory;

import com.thoughtworks.xstream.XStream;

public class SerializedItem {

    private static final XStream xstream = new XStream();

    private final String serializedEntity;

    public SerializedItem(final Object entity) {
        serializedEntity = xstream.toXML(entity);
    }

    @SuppressWarnings("unchecked")
    public <T> T getEntity(final Class<T> clazz) {
        return (T) xstream.fromXML(serializedEntity);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((serializedEntity == null) ? 0 : serializedEntity.hashCode());
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
        final SerializedItem other = (SerializedItem) obj;
        if (serializedEntity == null) {
            if (other.serializedEntity != null) {
                return false;
            }
        } else if (!serializedEntity.equals(other.serializedEntity)) {
            return false;
        }
        return true;
    }

}
