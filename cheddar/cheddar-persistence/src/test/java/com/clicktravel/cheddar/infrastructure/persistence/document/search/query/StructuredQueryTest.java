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

import static com.clicktravel.common.random.Randoms.randomBoolean;
import static com.clicktravel.common.random.Randoms.randomDateTime;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;

import org.joda.time.DateTime;
import org.junit.Test;

import com.clicktravel.common.random.Randoms;

public class StructuredQueryTest {

    @Test
    public void shouldBeStructuredQuery() {
        final RangeQuery range = new RangeQuery(Randoms.randomString(), Randoms.randomDateTime(),
                Randoms.randomDateTime(), Randoms.randomBoolean(), Randoms.randomBoolean());
        assertEquals(QueryType.STRUCTURED, range.queryType());
        final TermQuery term = new TermQuery(Randoms.randomString(), Randoms.randomString());
        assertEquals(QueryType.STRUCTURED, term.queryType());

        final Collection<StructuredQuery> queries = new HashSet<StructuredQuery>();
        queries.add(range);
        queries.add(term);
        assertEquals(QueryType.STRUCTURED, new AndQuery(queries).queryType());
        assertEquals(QueryType.STRUCTURED, new OrQuery(queries).queryType());
    }

    @Test
    public void shouldBeEqualRangeQuerys_withSameValues() {
        // Given
        final String fieldName = randomString();
        final DateTime lowerBound = randomDateTime();
        final DateTime upperBound = randomDateTime();
        final boolean lowerBoundInclusive = randomBoolean();
        final boolean upperBoundInclusive = randomBoolean();
        final RangeQuery rangeQuery = new RangeQuery(fieldName, lowerBound, upperBound, lowerBoundInclusive,
                upperBoundInclusive);
        final RangeQuery otherRangeQuery = new RangeQuery(fieldName, lowerBound, upperBound, lowerBoundInclusive,
                upperBoundInclusive);

        // When
        final boolean equals = rangeQuery.equals(otherRangeQuery);

        // Then
        assertTrue(equals);
    }

    @Test
    public void shouldBeEqualTermQuerys_withSameValues() {
        // Given
        final String fieldName = randomString();
        final String value = randomString();
        final TermQuery termQuery = new TermQuery(fieldName, value);
        final TermQuery otherTermQuery = new TermQuery(fieldName, value);

        // When
        final boolean equals = termQuery.equals(otherTermQuery);

        // Then
        assertTrue(equals);
    }

}
