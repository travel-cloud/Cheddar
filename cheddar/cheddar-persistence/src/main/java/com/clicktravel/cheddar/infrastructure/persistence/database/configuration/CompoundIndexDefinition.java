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

public class CompoundIndexDefinition extends IndexDefinition implements CompoundKeyDefinition {

    private final String supportingPropertyName;
    private Class<?> supportingPropertyType;

    public CompoundIndexDefinition(final String property, final String supportingPropertyName) {
        super(property);
        this.supportingPropertyName = supportingPropertyName;
    }

    @Override
    public String supportingPropertyName() {
        return supportingPropertyName;
    }

    @Override
    public Class<?> supportingPropertyType() {
        if (supportingPropertyType == null) {
            throw new IllegalStateException("Index not registered with ItemConfiguration");
        }
        return supportingPropertyType;
    }

    public void setSupportingPropertyType(final Class<?> supportingPropertyType) {
        this.supportingPropertyType = supportingPropertyType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (supportingPropertyName == null ? 0 : supportingPropertyName.hashCode());
        result = prime * result + (supportingPropertyType == null ? 0 : supportingPropertyType.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CompoundIndexDefinition other = (CompoundIndexDefinition) obj;
        if (supportingPropertyName == null) {
            if (other.supportingPropertyName != null) {
                return false;
            }
        } else if (!supportingPropertyName.equals(other.supportingPropertyName)) {
            return false;
        }
        if (supportingPropertyType == null) {
            if (other.supportingPropertyType != null) {
                return false;
            }
        } else if (!supportingPropertyType.equals(other.supportingPropertyType)) {
            return false;
        }
        return true;
    }

}
