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
package com.clicktravel.infrastructure.persistence.aws.cloudsearch;

import static com.clicktravel.common.random.Randoms.randomDateTime;
import static com.clicktravel.common.random.Randoms.randomDouble;
import static com.clicktravel.common.random.Randoms.randomInt;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.persistence.document.search.query.*;

public class StructuredQueryBuilderTest {

    final DateTimeFormatter formatter = ISODateTimeFormat.dateTime().withZoneUTC();

    @Test
    public void shouldBuildTermQuery_withStringTermQuery() {
        // Given
        final String fieldName = randomString();
        final String value = randomString();
        final Query query = new TermQuery(fieldName, value);
        final QueryBuilder builder = new QueryBuilder();

        // When
        final String result = builder.buildQuery(query);

        // Then
        final String expected = "(term field=" + fieldName + " '" + value + "')";
        assertEquals(expected, result);
    }

    @Test
    public void shouldBuildTermQuery_withIntegerTermQuery() {
        // Given
        final String fieldName = randomString();
        final Integer value = randomInt(1000);
        final Query query = new TermQuery(fieldName, value);
        final QueryBuilder builder = new QueryBuilder();

        // When
        final String result = builder.buildQuery(query);

        // Then
        final String expected = "(term field=" + fieldName + " " + value + ")";
        assertEquals(expected, result);
    }

    @Test
    public void shouldBuildTermQuery_withDoubleTermQuery() {
        // Given
        final String fieldName = randomString();
        final Double value = 100 * randomDouble();
        final Query query = new TermQuery(fieldName, value);
        final QueryBuilder builder = new QueryBuilder();

        // When
        final String result = builder.buildQuery(query);

        // Then
        final String expected = "(term field=" + fieldName + " " + value + ")";
        assertEquals(expected, result);
    }

    @Test
    public void shouldBuildTermQuery_withDateTimeTermQuery() {
        // Given
        final String fieldName = randomString();
        final DateTime value = randomDateTime();
        final Query query = new TermQuery(fieldName, value);
        final QueryBuilder builder = new QueryBuilder();

        // When
        final String result = builder.buildQuery(query);

        // Then
        final String expected = "(term field=" + fieldName + " '" + formatter.print(value) + "')";
        assertEquals(expected, result);
    }

    @Test
    public void shouldBuildRangeQuery_withStringInclusiveRangeQuery() {
        // Given
        final String fieldName = randomString();
        final String lowerBound = randomString();
        final String upperBound = randomString();
        final boolean lowerBoundInclusive = true;
        final boolean upperBoundInclusive = true;
        final Query query = new RangeQuery(fieldName, lowerBound, upperBound, lowerBoundInclusive, upperBoundInclusive);
        final QueryBuilder builder = new QueryBuilder();

        // When
        final String result = builder.buildQuery(query);

        // Then
        final String expected = "(range field=" + fieldName + " ['" + lowerBound + "','" + upperBound + "'])";
        assertEquals(expected, result);
    }

    @Test
    public void shouldBuildRangeQuery_withStringExclusiveRangeQuery() {
        // Given
        final String fieldName = randomString();
        final String lowerBound = randomString();
        final String upperBound = randomString();
        final boolean lowerBoundInclusive = false;
        final boolean upperBoundInclusive = false;
        final Query query = new RangeQuery(fieldName, lowerBound, upperBound, lowerBoundInclusive, upperBoundInclusive);
        final QueryBuilder builder = new QueryBuilder();

        // When
        final String result = builder.buildQuery(query);

        // Then
        final String expected = "(range field=" + fieldName + " {'" + lowerBound + "','" + upperBound + "'})";
        assertEquals(expected, result);
    }

    @Test
    public void shouldBuildRangeQuery_withIntegerHalfOpenRangeQuery() {
        // Given
        final String fieldName = randomString();
        final Integer lowerBound = randomInt(300) - 150;
        final Integer upperBound = null;
        final boolean lowerBoundInclusive = false;
        final boolean upperBoundInclusive = false;
        final Query query = new RangeQuery(fieldName, lowerBound, upperBound, lowerBoundInclusive, upperBoundInclusive);
        final QueryBuilder builder = new QueryBuilder();

        // When
        final String result = builder.buildQuery(query);

        // Then
        final String expected = "(range field=" + fieldName + " {" + lowerBound + ",})";
        assertEquals(expected, result);
    }

    @Test
    public void shouldBuildRangeQuery_withDateTimeMixedRangeQuery() {
        // Given
        final String fieldName = randomString();
        final DateTime lowerBound = randomDateTime();
        final DateTime upperBound = randomDateTime();
        final boolean lowerBoundInclusive = false;
        final boolean upperBoundInclusive = true;
        final Query query = new RangeQuery(fieldName, lowerBound, upperBound, lowerBoundInclusive, upperBoundInclusive);
        final QueryBuilder builder = new QueryBuilder();

        // When
        final String result = builder.buildQuery(query);

        // Then
        final String expected = "(range field=" + fieldName + " {'" + formatter.print(lowerBound) + "','"
                + formatter.print(upperBound) + "'])";
        assertEquals(expected, result);
    }

    @Test
    public void shouldBuildAndQuery_withAndQuery() {
        // Given
        final String field1Name = randomString();
        final String value1 = randomString();
        final String field2Name = randomString();
        final String value2 = randomString();
        final StructuredQuery termQuery1 = new TermQuery(field1Name, value1);
        final StructuredQuery termQuery2 = new TermQuery(field2Name, value2);
        final StructuredQuery andQuery = new AndQuery(Arrays.asList(termQuery1, termQuery2));
        final QueryBuilder builder = new QueryBuilder();

        // When
        final String result = builder.buildQuery(andQuery);

        // Then
        assertTrue(result.startsWith("(and"));
        assertTrue(result.endsWith(")"));
        assertTrue(result.matches(".*(term field=" + field1Name + " '" + value1 + "').*(term field=" + field2Name
                + " '" + value2 + "')*"));
    }

    @Test
    public void shouldBuildAndQuery_withOrQuery() {
        // Given
        final String field1Name = randomString();
        final String value1 = randomString();
        final String field2Name = randomString();
        final String value2 = randomString();
        final StructuredQuery termQuery1 = new TermQuery(field1Name, value1);
        final StructuredQuery termQuery2 = new TermQuery(field2Name, value2);
        final StructuredQuery orQuery = new OrQuery(Arrays.asList(termQuery1, termQuery2));
        final QueryBuilder builder = new QueryBuilder();

        // When
        final String result = builder.buildQuery(orQuery);

        // Then
        assertTrue(result.startsWith("(or"));
        assertTrue(result.endsWith(")"));
        assertTrue(result.matches(".*(term field=" + field1Name + " '" + value1 + "').*(term field=" + field2Name
                + " '" + value2 + "')*"));
    }
}
