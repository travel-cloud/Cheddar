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

import java.beans.PropertyDescriptor;

public class UniqueConstraint {

    private final String propertyName;
    private PropertyDescriptor propertyDescriptor;

    public UniqueConstraint(final String propertyName) {
        this.propertyName = propertyName;
    }

    public void setPropertyDescriptor(final PropertyDescriptor propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;

    }

    public String propertyName() {
        return propertyName;
    }

    public PropertyDescriptor propertyDescriptor() {
        checkInitialization();
        return propertyDescriptor;
    }

    private void checkInitialization() {
        if (propertyDescriptor == null) {
            throw new IllegalStateException("Unique Constraint not registered with ItemConfiguration");
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (propertyName == null ? 0 : propertyName.hashCode());
        result = prime * result + (propertyDescriptor == null ? 0 : propertyDescriptor.hashCode());
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
        final UniqueConstraint other = (UniqueConstraint) obj;
        if (propertyName == null) {
            if (other.propertyName != null) {
                return false;
            }
        } else if (!propertyName.equals(other.propertyName)) {
            return false;
        }
        if (propertyDescriptor == null) {
            if (other.propertyDescriptor != null) {
                return false;
            }
        } else if (!propertyDescriptor.equals(other.propertyDescriptor)) {
            return false;
        }
        return true;
    }

}
