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
package com.clicktravel.cheddar.infrastructure.persistence.inmemory.database;

import java.util.HashSet;
import java.util.Set;

import com.clicktravel.cheddar.infrastructure.persistence.database.Item;

public class StubWithRangeItem implements Item {

    private String id;
    private String supportingId;
    private String stringProperty;
    private boolean booleanProperty;
    private Set<String> stringSetProperty = new HashSet<>();
    private Long version;

    public String getSupportingId() {
        return supportingId;
    }

    public void setSupportingId(final String supportingId) {
        this.supportingId = supportingId;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public Long getVersion() {
        return version;
    }

    @Override
    public void setVersion(final Long version) {
        this.version = version;
    }

    public String getStringProperty() {
        return stringProperty;
    }

    public void setStringProperty(final String stringProperty) {
        this.stringProperty = stringProperty;
    }

    public Set<String> getStringSetProperty() {
        return stringSetProperty;
    }

    public void setStringSetProperty(final Set<String> stringSetProperty) {
        this.stringSetProperty = new HashSet<>(stringSetProperty);
    }

    public boolean isBooleanProperty() {
        return booleanProperty;
    }

    public void setBooleanProperty(final boolean booleanProperty) {
        this.booleanProperty = booleanProperty;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (booleanProperty ? 1231 : 1237);
        result = prime * result + (id == null ? 0 : id.hashCode());
        result = prime * result + (stringProperty == null ? 0 : stringProperty.hashCode());
        result = prime * result + (stringSetProperty == null ? 0 : stringSetProperty.hashCode());
        result = prime * result + (supportingId == null ? 0 : supportingId.hashCode());
        result = prime * result + (version == null ? 0 : version.hashCode());
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
        final StubWithRangeItem other = (StubWithRangeItem) obj;
        if (booleanProperty != other.booleanProperty) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (stringProperty == null) {
            if (other.stringProperty != null) {
                return false;
            }
        } else if (!stringProperty.equals(other.stringProperty)) {
            return false;
        }
        if (stringSetProperty == null) {
            if (other.stringSetProperty != null) {
                return false;
            }
        } else if (!stringSetProperty.equals(other.stringSetProperty)) {
            return false;
        }
        if (supportingId == null) {
            if (other.supportingId != null) {
                return false;
            }
        } else if (!supportingId.equals(other.supportingId)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

}