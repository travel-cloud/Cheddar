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
package com.clicktravel.cheddar.application.pending.result;

public class TestValue {

    private final String internalStringValue;
    private final int internalIntValue;

    public TestValue(final String internalStringValue, final int internalIntValue) {
        this.internalStringValue = internalStringValue;
        this.internalIntValue = internalIntValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + internalIntValue;
        result = prime * result + ((internalStringValue == null) ? 0 : internalStringValue.hashCode());
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
        final TestValue other = (TestValue) obj;
        if (internalIntValue != other.internalIntValue) {
            return false;
        }
        if (internalStringValue == null) {
            if (other.internalStringValue != null) {
                return false;
            }
        } else if (!internalStringValue.equals(other.internalStringValue)) {
            return false;
        }
        return true;
    }

}
