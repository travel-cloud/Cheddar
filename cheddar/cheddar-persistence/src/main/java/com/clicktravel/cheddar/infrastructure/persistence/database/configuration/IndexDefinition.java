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
package com.clicktravel.cheddar.infrastructure.persistence.database.configuration;

public class IndexDefinition {

    private final String propertyName;
    private Class<?> propertyType;

    public IndexDefinition(final String propertyName) {
        this.propertyName = propertyName;
    }

    public String propertyName() {
        return propertyName;
    }

    public Class<?> propertyType() {
        if (propertyType == null) {
            throw new IllegalStateException("Key not registered with ItemConfiguration");
        }
        return propertyType;
    }

    public void setPropertyType(final Class<?> propertyType) {
        this.propertyType = propertyType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (propertyName == null ? 0 : propertyName.hashCode());
        result = prime * result + (propertyType == null ? 0 : propertyType.hashCode());
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
        final IndexDefinition other = (IndexDefinition) obj;
        if (propertyName == null) {
            if (other.propertyName != null) {
                return false;
            }
        } else if (!propertyName.equals(other.propertyName)) {
            return false;
        }
        if (propertyType == null) {
            if (other.propertyType != null) {
                return false;
            }
        } else if (!propertyType.equals(other.propertyType)) {
            return false;
        }
        return true;
    }

}