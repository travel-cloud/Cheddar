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
package com.clicktravel.cheddar.infrastructure.persistence.inmemory.document.search.mock;

import java.util.ArrayList;
import java.util.Collection;

import com.clicktravel.cheddar.infrastructure.inmemory.Resettable;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.Document;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.DocumentSearchResponse;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.options.SearchOptions;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.query.Query;
import com.clicktravel.cheddar.infrastructure.persistence.inmemory.document.search.InMemoryDocumentSearchEngine;
import com.clicktravel.cheddar.infrastructure.persistence.inmemory.document.search.mock.exception.SearchExpectationException;
import com.clicktravel.cheddar.infrastructure.persistence.inmemory.document.search.mock.exception.UnexpectedSearchException;

public class InMemoryMockDocumentSearchEngine extends InMemoryDocumentSearchEngine implements Resettable {

    private final Collection<SearchExpectation> searchExpectations;

    public InMemoryMockDocumentSearchEngine() {
        searchExpectations = new ArrayList<>();
    }

    public boolean contains(final Document document) {
        return allDocuments.contains(document);
    }

    /**
     * Returns true if the DocumentSearchEngine contains exactly all of the specified documents
     * 
     * @param documents
     * @return
     */
    public boolean containsExactly(final Collection<Document> documents) {
        return allDocuments.containsAll(documents) && allDocuments.size() == documents.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Document> DocumentSearchResponse<T> search(final Query query, final Integer start,
            final Integer size, final Class<T> documentClass, final SearchOptions searchOptions) {
        final SearchParameter searchParameter = new SearchParameter(query, start, size, documentClass, searchOptions);
        for (final SearchExpectation searchExpectation : searchExpectations) {
            if (searchExpectation.searchParameter().equals(searchParameter)) {
                final DocumentSearchResponse<? extends Document> documentSearchResponse = searchExpectation
                        .documentSearchResponse();
                if (documentSearchResponse == null) {
                    throw new SearchExpectationException();
                }
                return (DocumentSearchResponse<T>) documentSearchResponse;
            }
        }
        throw new UnexpectedSearchException(searchParameter);
    }

    public SearchExpectation when(final Query query, final Integer start, final Integer size,
            final Class<? extends Document> documentClass, final SearchOptions searchOptions) {
        final SearchExpectation searchExpectation = new SearchExpectation(new SearchParameter(query, start, size,
                documentClass, searchOptions));
        searchExpectations.add(searchExpectation);
        return searchExpectation;
    }

    @Override
    public void reset() {
        allDocuments.clear();
        searchExpectations.clear();
    }

}
