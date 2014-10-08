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
package com.clicktravel.infrastructure.persistence.aws.cloudsearch;

import java.util.Collection;
import java.util.Map;

import org.joda.time.DateTime;

import com.clicktravel.cheddar.infrastructure.persistence.document.search.Document;

public class StubDocument implements Document {

    public enum MyEnum {
        VALUE1,
        VALUE2,
        VALUE3
    }

    private String id;
    private String stringProperty;
    private Collection<String> collectionProperty;
    private Map<String, String> mapProperty;
    private MyEnum myEnum;
    private DateTime dateTimeValue;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(final String id) {
        this.id = id;
    }

    public String getStringProperty() {
        return stringProperty;
    }

    public void setStringProperty(final String stringProperty) {
        this.stringProperty = stringProperty;
    }

    public Collection<String> getCollectionProperty() {
        return collectionProperty;
    }

    public void setCollectionProperty(final Collection<String> collectionProperty) {
        this.collectionProperty = collectionProperty;
    }

    public Map<String, String> getMapProperty() {
        return mapProperty;
    }

    public void setMapProperty(final Map<String, String> mapProperty) {
        this.mapProperty = mapProperty;
    }

    public MyEnum getMyEnum() {
        return myEnum;
    }

    public void setMyEnum(final MyEnum myEnum) {
        this.myEnum = myEnum;
    }

    public DateTime getDateTimeValue() {
        return dateTimeValue;
    }

    public void setDateTimeValue(final DateTime dateTimeValue) {
        this.dateTimeValue = dateTimeValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (collectionProperty == null ? 0 : collectionProperty.hashCode());
        result = prime * result + (dateTimeValue == null ? 0 : dateTimeValue.hashCode());
        result = prime * result + (id == null ? 0 : id.hashCode());
        result = prime * result + (mapProperty == null ? 0 : mapProperty.hashCode());
        result = prime * result + (myEnum == null ? 0 : myEnum.hashCode());
        result = prime * result + (stringProperty == null ? 0 : stringProperty.hashCode());
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
        final StubDocument other = (StubDocument) obj;
        if (collectionProperty == null) {
            if (other.collectionProperty != null) {
                return false;
            }
        } else if (!collectionProperty.equals(other.collectionProperty)) {
            return false;
        }
        if (dateTimeValue == null) {
            if (other.dateTimeValue != null) {
                return false;
            }
        } else if (!dateTimeValue.equals(other.dateTimeValue)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (mapProperty == null) {
            if (other.mapProperty != null) {
                return false;
            }
        } else if (!mapProperty.equals(other.mapProperty)) {
            return false;
        }
        if (myEnum != other.myEnum) {
            return false;
        }
        if (stringProperty == null) {
            if (other.stringProperty != null) {
                return false;
            }
        } else if (!stringProperty.equals(other.stringProperty)) {
            return false;
        }
        return true;
    }

}
