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
package com.clicktravel.infrastructure.persistence.inmemory.database;

import com.clicktravel.cheddar.infrastructure.persistence.database.Item;

public class StubWithGlobalSecondaryIndexItem implements Item {

    private String id;
    private String gsiHashProperty;
    private Integer gsiRangeProperty;
    private Long version;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getGsiHashProperty() {
        return gsiHashProperty;
    }

    public void setGsiHashProperty(final String gsiHashProperty) {
        this.gsiHashProperty = gsiHashProperty;
    }

    public Integer getGsiRangeProperty() {
        return gsiRangeProperty;
    }

    public void setGsiRangeProperty(final Integer gsiRangeProperty) {
        this.gsiRangeProperty = gsiRangeProperty;
    }

    @Override
    public Long getVersion() {
        return version;
    }

    @Override
    public void setVersion(final Long version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((gsiHashProperty == null) ? 0 : gsiHashProperty.hashCode());
        result = prime * result + ((gsiRangeProperty == null) ? 0 : gsiRangeProperty.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
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
        final StubWithGlobalSecondaryIndexItem other = (StubWithGlobalSecondaryIndexItem) obj;
        if (gsiHashProperty == null) {
            if (other.gsiHashProperty != null) {
                return false;
            }
        } else if (!gsiHashProperty.equals(other.gsiHashProperty)) {
            return false;
        }
        if (gsiRangeProperty == null) {
            if (other.gsiRangeProperty != null) {
                return false;
            }
        } else if (!gsiRangeProperty.equals(other.gsiRangeProperty)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
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