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

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.clicktravel.common.random.Randoms;

public class OperatorsTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldThrowExceptionWhenNotNullOperatorCompareStrings() {
        // GIVEN
        final ComparisonOperator notNullOperator = Operators.NOT_NULL;
        exception.expect(UnsupportedOperationException.class);

        // WHEN
        notNullOperator.compare(Randoms.randomString(), Randoms.randomString());

        // THEN
    }

    @Test
    public void shouldThrowExceptionWhenNotNullOperatorCompareCollection() {
        // GIVEN
        final ComparisonOperator notNullOperator = Operators.NOT_NULL;
        exception.expect(UnsupportedOperationException.class);

        // WHEN
        notNullOperator.compare(Collections.singletonList(Randoms.randomString()),
                Collections.singletonList(Randoms.randomString()));

        // THEN
    }

    @Test
    public void shouldReturnToString() {
        // GIVEN
        final ComparisonOperator notNullOperator = Operators.NOT_NULL;

        // WHEN
        final String representation = notNullOperator.toString();

        // THEN
        assertEquals("ComparisonOperator.NOT_NULL", representation);
    }
}
