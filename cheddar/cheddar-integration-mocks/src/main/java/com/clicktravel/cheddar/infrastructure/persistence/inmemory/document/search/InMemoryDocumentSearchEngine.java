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
package com.clicktravel.cheddar.infrastructure.persistence.inmemory.document.search;

import java.util.ArrayList;
import java.util.Collection;

import com.clicktravel.cheddar.infrastructure.persistence.document.search.Document;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.DocumentSearchEngine;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.DocumentSearchResponse;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.options.SearchOptions;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.query.Query;

public class InMemoryDocumentSearchEngine implements DocumentSearchEngine {

    protected final Collection<Document> allDocuments;

    public InMemoryDocumentSearchEngine() {
        allDocuments = new ArrayList<>();
    }

    @Override
    public void update(final Document document) {
        allDocuments.add(document);
    }

    @Override
    public void delete(final Document document) {
        allDocuments.remove(document);
    }

    @Override
    public <T extends Document> DocumentSearchResponse<T> search(final Query query, final Integer start,
            final Integer size, final Class<T> documentClass, final SearchOptions searchOptions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Document> DocumentSearchResponse<T> search(final Query query, final Integer start,
            final Integer size, final Class<T> documentClass) {
        return search(query, start, size, documentClass, SearchOptions.DEFAULT);
    }

}
