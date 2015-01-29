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

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashSet;

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

}
