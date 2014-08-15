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
package com.clicktravel.common.test.bean;

public class Bean {

    private String stringProperty1;
    private String stringProperty2;

    public String getStringProperty1() {
        return stringProperty1;
    }

    public void setStringProperty1(final String stringProperty1) {
        this.stringProperty1 = stringProperty1;
    }

    public String getStringProperty2() {
        return stringProperty2;
    }

    public void setStringProperty2(final String stringProperty2) {
        this.stringProperty2 = stringProperty2;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (stringProperty1 == null ? 0 : stringProperty1.hashCode());
        result = prime * result + (stringProperty2 == null ? 0 : stringProperty2.hashCode());
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
        final Bean other = (Bean) obj;
        if (stringProperty1 == null) {
            if (other.stringProperty1 != null) {
                return false;
            }
        } else if (!stringProperty1.equals(other.stringProperty1)) {
            return false;
        }
        if (stringProperty2 == null) {
            if (other.stringProperty2 != null) {
                return false;
            }
        } else if (!stringProperty2.equals(other.stringProperty2)) {
            return false;
        }
        return true;
    }

}