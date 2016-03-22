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

import static com.clicktravel.common.random.Randoms.randomInt;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.persistence.document.search.Document;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.DocumentSearchResponse;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.options.SearchOptions;
import com.clicktravel.cheddar.infrastructure.persistence.document.search.query.Query;
import com.clicktravel.infrastructure.persistence.inmemory.document.search.StubDocument;
import com.clicktravel.infrastructure.persistence.inmemory.document.search.mock.exception.SearchExpectationException;
import com.clicktravel.infrastructure.persistence.inmemory.document.search.mock.exception.UnexpectedSearchException;

public class InMemoryMockDocumentSearchEngineTest {

    @Test
    public void shouldUpdateDocument_withDocument() throws Exception {
        // Given
        final Document document = mock(Document.class);
        final InMemoryMockDocumentSearchEngine documentSearchEngine = new InMemoryMockDocumentSearchEngine();

        // When
        documentSearchEngine.update(document);

        // Then
        assertTrue(documentSearchEngine.contains(document));
    }

    @Test
    public void shouldDeleteDocument_withDocument() throws Exception {
        // Given
        final Document document = mock(Document.class);
        final InMemoryMockDocumentSearchEngine documentSearchEngine = new InMemoryMockDocumentSearchEngine();
        documentSearchEngine.update(document);
        assertTrue(documentSearchEngine.contains(document));

        // When
        documentSearchEngine.delete(document);

        // Then
        assertFalse(documentSearchEngine.contains(document));
    }
    
    @Test
    public void shouldDelete_withDocuments() throws Exception {
        // Given
    	final Collection<Document> documents = Arrays.asList(mock(Document.class), mock(Document.class), mock(Document.class));
        final InMemoryMockDocumentSearchEngine documentSearchEngine = new InMemoryMockDocumentSearchEngine();
        for(final Document document : documents){
        	documentSearchEngine.update(document);
        	assertTrue(documentSearchEngine.contains(document));
        }

        // When
        documentSearchEngine.delete(documents);

        // Then
        for(final Document document : documents){
        	assertFalse(documentSearchEngine.contains(document));
        }
    }

    @Test
    public void shouldVerifyContains_withSingleDocument() throws Exception {
        // Given
        final Document document = mock(Document.class);
        final InMemoryMockDocumentSearchEngine documentSearchEngine = new InMemoryMockDocumentSearchEngine();
        documentSearchEngine.update(document);

        // When
        final boolean containsDocuments = documentSearchEngine.contains(document);

        // Then
        assertTrue(containsDocuments);
    }

    @Test
    public void shouldVerifyContainsExactly_withMultipleDocuments() throws Exception {
        // Given
        final Document document1 = mock(Document.class);
        final Document document2 = mock(Document.class);
        final Document document3 = mock(Document.class);
        final InMemoryMockDocumentSearchEngine documentSearchEngine = new InMemoryMockDocumentSearchEngine();
        documentSearchEngine.update(document1);
        documentSearchEngine.update(document2);
        documentSearchEngine.update(document3);

        // When
        final boolean containsDocuments = documentSearchEngine.containsExactly(Arrays.asList(document1, document2,
                document3));

        // Then
        assertTrue(containsDocuments);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldStubQueryBehaviour_withMatchingQuery() throws Exception {
        // Given
        final Query query = mock(Query.class);
        final Integer start = randomInt(Integer.MAX_VALUE);
        final Integer size = randomInt(Integer.MAX_VALUE);
        final Class<StubDocument> documentClass = StubDocument.class;
        final SearchOptions searchOptions = mock(SearchOptions.class);
        final InMemoryMockDocumentSearchEngine documentSearchEngine = new InMemoryMockDocumentSearchEngine();
        final DocumentSearchResponse<StubDocument> expectedDocumentSearchResponse = mock(DocumentSearchResponse.class);

        // When
        documentSearchEngine.when(query, start, size, documentClass, searchOptions).thenReturn(
                expectedDocumentSearchResponse);
        final DocumentSearchResponse<StubDocument> documentSearchResponse = documentSearchEngine.search(query, start,
                size, documentClass, searchOptions);

        // Then
        assertThat(documentSearchResponse, is(expectedDocumentSearchResponse));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotStubQueryBehaviour_withNonMatchingQuery() throws Exception {
        // Given
        final Query query = mock(Query.class);
        final Integer start = randomInt(Integer.MAX_VALUE);
        final Integer size = randomInt(Integer.MAX_VALUE);
        final Class<StubDocument> documentClass = StubDocument.class;
        final SearchOptions searchOptions = mock(SearchOptions.class);
        final InMemoryMockDocumentSearchEngine documentSearchEngine = new InMemoryMockDocumentSearchEngine();
        final DocumentSearchResponse<StubDocument> expectedDocumentSearchResponse = mock(DocumentSearchResponse.class);

        // When
        documentSearchEngine.when(query, start, size, documentClass, searchOptions).thenReturn(
                expectedDocumentSearchResponse);
        UnexpectedSearchException actualException = null;
        try {
            documentSearchEngine.search(mock(Query.class), start, size, documentClass, searchOptions);
        } catch (final UnexpectedSearchException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotStubQueryBehaviour_withMatchingQueryButOngoingStubbing() throws Exception {
        // Given
        final Query query = mock(Query.class);
        final Integer start = randomInt(Integer.MAX_VALUE);
        final Integer size = randomInt(Integer.MAX_VALUE);
        final Class<StubDocument> documentClass = StubDocument.class;
        final SearchOptions searchOptions = mock(SearchOptions.class);
        final InMemoryMockDocumentSearchEngine documentSearchEngine = new InMemoryMockDocumentSearchEngine();

        // When
        documentSearchEngine.when(query, start, size, documentClass, searchOptions);
        SearchExpectationException actualException = null;
        try {
            documentSearchEngine.search(query, start, size, documentClass, searchOptions);
        } catch (final SearchExpectationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

}
