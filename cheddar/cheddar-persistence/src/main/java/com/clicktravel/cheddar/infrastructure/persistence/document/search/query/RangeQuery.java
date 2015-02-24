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
package com.clicktravel.cheddar.infrastructure.persistence.document.search.query;

import org.joda.time.DateTime;

public class RangeQuery extends StructuredQuery {

    private final String fieldName;
    private final boolean lowerBoundInclusive;
    private final boolean upperBoundInclusive;
    private final Object lowerBound;
    private final Object upperBound;

    private RangeQuery(final String fieldName, final Object lowerBound, final Object upperBound,
            final boolean lowerBoundInclusive, final boolean upperBoundInclusive) {
        this.fieldName = fieldName;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.lowerBoundInclusive = lowerBoundInclusive;
        this.upperBoundInclusive = upperBoundInclusive;
    }

    public RangeQuery(final String fieldName, final String lowerBound, final String upperBound,
            final boolean lowerBoundInclusive, final boolean upperBoundInclusive) {
        this(fieldName, (Object) lowerBound, (Object) upperBound, lowerBoundInclusive, upperBoundInclusive);
    }

    public RangeQuery(final String fieldName, final Integer lowerBound, final Integer upperBound,
            final boolean lowerBoundInclusive, final boolean upperBoundInclusive) {
        this(fieldName, (Object) lowerBound, (Object) upperBound, lowerBoundInclusive, upperBoundInclusive);
    }

    public RangeQuery(final String fieldName, final Double lowerBound, final Double upperBound,
            final boolean lowerBoundInclusive, final boolean upperBoundInclusive) {
        this(fieldName, (Object) lowerBound, (Object) upperBound, lowerBoundInclusive, upperBoundInclusive);
    }

    public RangeQuery(final String fieldName, final DateTime lowerBound, final DateTime upperBound,
            final boolean lowerBoundInclusive, final boolean upperBoundInclusive) {
        this(fieldName, (Object) lowerBound, (Object) upperBound, lowerBoundInclusive, upperBoundInclusive);
    }

    public boolean isLowerBoundInclusive() {
        return lowerBoundInclusive;
    }

    public boolean isUpperBoundInclusive() {
        return upperBoundInclusive;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getLowerBound() {
        return lowerBound;
    }

    public Object getUpperBound() {
        return upperBound;
    }

    @Override
    public void accept(final QueryVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
        result = prime * result + ((lowerBound == null) ? 0 : lowerBound.hashCode());
        result = prime * result + (lowerBoundInclusive ? 1231 : 1237);
        result = prime * result + ((upperBound == null) ? 0 : upperBound.hashCode());
        result = prime * result + (upperBoundInclusive ? 1231 : 1237);
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
        final RangeQuery other = (RangeQuery) obj;
        if (fieldName == null) {
            if (other.fieldName != null) {
                return false;
            }
        } else if (!fieldName.equals(other.fieldName)) {
            return false;
        }
        if (lowerBound == null) {
            if (other.lowerBound != null) {
                return false;
            }
        } else if (!lowerBound.equals(other.lowerBound)) {
            return false;
        }
        if (lowerBoundInclusive != other.lowerBoundInclusive) {
            return false;
        }
        if (upperBound == null) {
            if (other.upperBound != null) {
                return false;
            }
        } else if (!upperBound.equals(other.upperBound)) {
            return false;
        }
        if (upperBoundInclusive != other.upperBoundInclusive) {
            return false;
        }
        return true;
    }

}
