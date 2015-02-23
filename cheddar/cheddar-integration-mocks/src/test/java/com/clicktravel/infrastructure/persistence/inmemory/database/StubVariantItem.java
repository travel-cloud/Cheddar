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

import static com.clicktravel.common.random.Randoms.randomString;

public class StubVariantItem extends StubParentItem {

    private String stringProperty2;

    public StubVariantItem() {
        stringProperty2 = randomString(10);
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
        int result = super.hashCode();
        result = prime * result + (stringProperty2 == null ? 0 : stringProperty2.hashCode());
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
        final StubVariantItem other = (StubVariantItem) obj;
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