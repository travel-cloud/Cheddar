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
package com.clicktravel.infrastructure.persistence.inmemory.document.search.mock;

import com.clicktravel.cheddar.infrastructure.persistence.document.search.Document;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.options.SearchOptions;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.query.LuceneQuery;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.query.Query;

public class SearchParameter {

    private final Query query;
    private final Integer start;
    private final Integer size;
    private final Class<? extends Document> documentClass;
    private final SearchOptions searchOptions;

    public SearchParameter(final Query query, final Integer start, final Integer size,
            final Class<? extends Document> documentClass, final SearchOptions searchOptions) {
        this.query = query;
        this.start = start;
        this.size = size;
        this.documentClass = documentClass;
        this.searchOptions = searchOptions;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("SearchParameter [query=");
        builder.append(query);
        builder.append(", start=");
        builder.append(start);
        builder.append(", size=");
        builder.append(size);
        builder.append(", documentClass=");
        builder.append(documentClass);
        builder.append(", searchOptions=");
        builder.append(searchOptions);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((documentClass == null) ? 0 : documentClass.hashCode());
        result = prime * result + ((searchOptions == null) ? 0 : searchOptions.hashCode());
        result = prime * result + ((query == null) ? 0 : query.hashCode());
        result = prime * result + ((size == null) ? 0 : size.hashCode());
        result = prime * result + ((start == null) ? 0 : start.hashCode());
        return result;
    }

    // TODO change this back to default equals method when Query concrete equals method implemented in Cheddar
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
        final SearchParameter other = (SearchParameter) obj;
        if (documentClass == null) {
            if (other.documentClass != null) {
                return false;
            }
        } else if (!documentClass.equals(other.documentClass)) {
            return false;
        }
        if (searchOptions == null) {
            if (other.searchOptions != null) {
                return false;
            }
        } else if (!searchOptions.equals(other.searchOptions)) {
            return false;
        }
        if (query == null) {
            if (other.query != null) {
                return false;
            }
        } else {
            return checkQueryEquals(query, other.query);
        }
        if (size == null) {
            if (other.size != null) {
                return false;
            }
        } else if (!size.equals(other.size)) {
            return false;
        }
        if (start == null) {
            if (other.start != null) {
                return false;
            }
        } else if (!start.equals(other.start)) {
            return false;
        }
        return true;
    }

    // TODO temporary method to allow cloud search service tests to work with lucene queries
    private boolean checkQueryEquals(final Query query, final Query otherQuery) {
        if (query.queryType().equals(otherQuery.queryType())) {

            switch (query.queryType()) {
                case LUCENE:
                    final String queryString = ((LuceneQuery) query).getQuery();
                    final String otherQueryString = ((LuceneQuery) otherQuery).getQuery();
                    if (queryString.equals(otherQueryString)) {
                        return true;
                    }
                    break;
                default:
                    return false;
            }

        }
        return false;
    }

}