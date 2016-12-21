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
package com.clicktravel.infrastructure.persistence.aws.dynamodb;

import java.util.Set;

import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.clicktravel.cheddar.infrastructure.persistence.database.Item;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.AttributeQuery;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.CompoundAttributeQuery;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Operators;
import com.clicktravel.cheddar.infrastructure.persistence.exception.PersistenceResourceFailureException;

public class QuerySpecBuilder {

    public static <T extends Item> QuerySpec build(final AttributeQuery attributeQuery, final Class<T> itemClass) {
        if (CompoundAttributeQuery.class.isAssignableFrom(attributeQuery.getClass())) {
            final CompoundAttributeQuery compoundAttributeQuery = (CompoundAttributeQuery) attributeQuery;
            return buildWithHashAndRangeKey(compoundAttributeQuery, itemClass);
        }

        return buildWithHashKey(attributeQuery);
    }

    private static QuerySpec buildWithHashKey(final AttributeQuery attributeQuery) {
        return new QuerySpec().withHashKey(attributeQuery.getAttributeName(),
                attributeQuery.getCondition().getValues().iterator().next());
    }

    private static <T extends Item> QuerySpec buildWithHashAndRangeKey(
            final CompoundAttributeQuery compoundAttributeQuery, final Class<T> itemClass) {
        final Set<String> supportingConditionValues = compoundAttributeQuery.getSupportingCondition().getValues();
        validateSupportingConditionValues(supportingConditionValues);

        final QuerySpec querySpec = buildWithHashKey(compoundAttributeQuery);
        addRangeKeyConditionToQuerySpec(querySpec, compoundAttributeQuery, itemClass);

        return querySpec;
    }

    private static <T extends Item> void addRangeKeyConditionToQuerySpec(final QuerySpec querySpec,
            final CompoundAttributeQuery compoundAttributeQuery, final Class<T> itemClass) {
        final String supportingConditionStringValue = compoundAttributeQuery.getSupportingCondition().getValues().iterator()
                .next();
        final Operators comparisonOperator = compoundAttributeQuery.getSupportingCondition().getComparisonOperator();
        final Class<?> supportingAttributeType = compoundAttributeQuery.getSupportingAttributeType(itemClass);

        try {
            final Object supportingConditionValue = supportingAttributeType.getConstructor(String.class).newInstance(supportingConditionStringValue);

            final RangeKeyCondition rangeKeyCondition = RangeKeyConditionBuilder
                    .build(compoundAttributeQuery.getSupportingAttributeName(), supportingConditionValue, comparisonOperator);
            querySpec.withRangeKeyCondition(rangeKeyCondition);
        } catch (final Exception e) {
            throw new PersistenceResourceFailureException(
                    String.format("Could not add range key condition for query: %s on item %s.", compoundAttributeQuery,
                            itemClass.getSimpleName()),
                    e);
        }
    }

    private static void validateSupportingConditionValues(final Set<String> supportingConditionValues) {
        if (supportingConditionValues.size() != 1) {
            throw new InvalidConditionValuesException(String.format(
                    "Only 1 supporting condition value allowed for a CompoundAttributeQuery.  %s supplied.",
                    supportingConditionValues.size()));
        }
    }
}
