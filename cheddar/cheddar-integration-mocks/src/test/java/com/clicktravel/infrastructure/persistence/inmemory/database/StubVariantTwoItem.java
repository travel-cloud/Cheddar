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

public class StubVariantTwoItem extends StubParentItem {

    private String stringPropertyTwo;

    public StubVariantTwoItem() {
        stringPropertyTwo = randomString(10);
    }

    public String getStringPropertyTwo() {
        return stringPropertyTwo;
    }

    public void setStringPropertyTwo(final String stringPropertyTwo) {
        this.stringPropertyTwo = stringPropertyTwo;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (stringPropertyTwo == null ? 0 : stringPropertyTwo.hashCode());
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
        final StubVariantTwoItem other = (StubVariantTwoItem) obj;
        if (stringPropertyTwo == null) {
            if (other.stringPropertyTwo != null) {
                return false;
            }
        } else if (!stringPropertyTwo.equals(other.stringPropertyTwo)) {
            return false;
        }
        return true;
    }

}