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
package com.clicktravel.cheddar.infrastructure.persistence.database;

public class ItemId {

    private final String value;
    private final String supportingValue;

    public ItemId(final String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value must not be null");
        }
        this.value = value;
        supportingValue = null;
    }

    public ItemId(final String value, final String supportingValue) {
        if (value == null || supportingValue == null) {
            throw new IllegalArgumentException("Values must not be null");
        }
        this.value = value;
        this.supportingValue = supportingValue;
    }

    public String value() {
        return value;
    }

    public String supportingValue() {
        if (supportingValue == null) {
            throw new IllegalStateException("Supporting value not supported");
        }
        return supportingValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (supportingValue == null ? 0 : supportingValue.hashCode());
        result = prime * result + (value == null ? 0 : value.hashCode());
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
        final ItemId other = (ItemId) obj;
        if (supportingValue == null) {
            if (other.supportingValue != null) {
                return false;
            }
        } else if (!supportingValue.equals(other.supportingValue)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (supportingValue == null) {
            return "ItemId [value=" + value + "]";
        } else {
            return "ItemId [value=" + value + ", supportingValue=" + supportingValue + "]";
        }
    }

}
