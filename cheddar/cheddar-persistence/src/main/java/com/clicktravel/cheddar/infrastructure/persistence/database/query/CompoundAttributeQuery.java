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
package com.clicktravel.cheddar.infrastructure.persistence.database.query;

import com.clicktravel.cheddar.infrastructure.persistence.database.Item;
import com.clicktravel.common.validation.Check;

public class CompoundAttributeQuery extends AttributeQuery {

    private final String supportingAttributeName;
    private final Condition supportingCondition;

    public CompoundAttributeQuery(final String attributeName, final Condition condition,
            final String supportingAttributeName, final Condition supportingCondition) {
        super(attributeName, condition);
        Check.isNotEmptyOrNull("supportingAttributeName", supportingAttributeName);
        Check.isNotNull("supportingCondition", supportingCondition);
        this.supportingAttributeName = supportingAttributeName;
        this.supportingCondition = supportingCondition;
    }

    public String getSupportingAttributeName() {
        return supportingAttributeName;
    }

    public Condition getSupportingCondition() {
        return supportingCondition;
    }

    public <T extends Item> Class<?> getSupportingAttributeType(final Class<T> itemClass) {
        return getAttributeType(supportingAttributeName, itemClass);
    }

    @Override
    public String toString() {
        return "CompoundAttributeQuery [attributeName=" + getAttributeName() + ", condition=" + getCondition()
                + ", supportingAttributeName=" + getSupportingAttributeName() + ", supportingCondition="
                + getSupportingCondition() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((supportingAttributeName == null) ? 0 : supportingAttributeName.hashCode());
        result = prime * result + ((supportingCondition == null) ? 0 : supportingCondition.hashCode());
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
        final CompoundAttributeQuery other = (CompoundAttributeQuery) obj;
        if (supportingAttributeName == null) {
            if (other.supportingAttributeName != null) {
                return false;
            }
        } else if (!supportingAttributeName.equals(other.supportingAttributeName)) {
            return false;
        }
        if (supportingCondition == null) {
            if (other.supportingCondition != null) {
                return false;
            }
        } else if (!supportingCondition.equals(other.supportingCondition)) {
            return false;
        }
        return true;
    }

}
