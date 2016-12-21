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

import com.clicktravel.cheddar.infrastructure.persistence.database.query.AttributeQuery;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.CompoundAttributeQuery;

public class IndexNameBuilder {

    public static String build(final AttributeQuery attributeQuery) {
        final StringBuffer stringBuffer = buildStringBufferWithAttributeName(attributeQuery);

        if (CompoundAttributeQuery.class.isAssignableFrom(attributeQuery.getClass())) {
            final CompoundAttributeQuery compoundAttributeQuery = (CompoundAttributeQuery) attributeQuery;
            appendSupportingAttributeNameToStringBuffer(stringBuffer, compoundAttributeQuery);
        }

        stringBuffer.append("_idx");

        return stringBuffer.toString();
    }

    private static StringBuffer buildStringBufferWithAttributeName(final AttributeQuery attributeQuery) {
        final StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(attributeQuery.getAttributeName());

        return stringBuffer;
    }

    private static void appendSupportingAttributeNameToStringBuffer(final StringBuffer stringBuffer,
            final CompoundAttributeQuery compoundAttributeQuery) {
        stringBuffer.append("_");
        stringBuffer.append(compoundAttributeQuery.getSupportingAttributeName());
    }
}
