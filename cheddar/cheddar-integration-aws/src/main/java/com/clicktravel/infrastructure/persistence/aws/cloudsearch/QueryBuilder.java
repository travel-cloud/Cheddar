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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.clicktravel.cheddar.infrastructure.persistence.document.search.query.*;

/**
 * Class Responsible for constructing cloudsearch query strings from Query.class
 *
 */
public class QueryBuilder implements QueryVisitor {

    private final DateTimeFormatter formatter = ISODateTimeFormat.dateTime().withZoneUTC();
    private Writer writer;

    @Override
    public void visit(final TermQuery termQuery) {
        write("(term field=");
        write(termQuery.getFieldName());
        write(' ');
        writeValue(termQuery.getValue());
        write(')');
    }

    @Override
    public void visit(final RangeQuery rangeQuery) {
        write("(range field=");
        write(rangeQuery.getFieldName());
        write(' ');
        write(rangeQuery.isLowerBoundInclusive() ? '[' : '{');
        writeValue(rangeQuery.getLowerBound());
        write(',');
        writeValue(rangeQuery.getUpperBound());
        write(rangeQuery.isUpperBoundInclusive() ? ']' : '}');
        write(')');
    }

    private void writeValue(final Object value) {
        if (value == null) {
            return;
        }

        final boolean isQuoted = value instanceof String || value instanceof DateTime;

        if (isQuoted) {
            write('\'');
        }

        if (value instanceof DateTime) {
            try {
                formatter.printTo(writer, (DateTime) value);
            } catch (final IOException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            write(value.toString());
        }

        if (isQuoted) {
            write('\'');
        }
    }

    @Override
    public void visit(final AndQuery andQuery) {
        writeBooleanQuery(andQuery.getQueries(), "and");
    }

    @Override
    public void visit(final OrQuery orQuery) {
        writeBooleanQuery(orQuery.getQueries(), "or");
    }

    private void writeBooleanQuery(final List<StructuredQuery> subqueries, final String operator) {
        write('(');
        write(operator);
        for (final StructuredQuery query : subqueries) {
            write(' ');
            query.accept(this);
        }
        write(')');
    }

    public String buildQuery(final Query query) {
        writer = new StringWriter();
        query.accept(this);
        return writer.toString();
    }

    private void write(final int c) {
        try {
            writer.write(c);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void write(final String s) {
        try {
            writer.write(s);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void visit(final SimpleQuery simple) {
        write(simple.getQuery());
    }

    @Override
    public void visit(final LuceneQuery luceneQuery) {
        write(luceneQuery.getQuery());
    }
}
