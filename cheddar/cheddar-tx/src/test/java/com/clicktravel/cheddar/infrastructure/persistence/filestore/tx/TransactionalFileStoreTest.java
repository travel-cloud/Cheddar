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
package com.clicktravel.cheddar.infrastructure.persistence.filestore.tx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.persistence.filestore.FileItem;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FilePath;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FileStore;
import com.clicktravel.cheddar.infrastructure.tx.NestedTransactionException;
import com.clicktravel.cheddar.infrastructure.tx.NonExistentTransactionException;
import com.clicktravel.common.random.Randoms;

public class TransactionalFileStoreTest {

    private final FileStore mockFileStore = mock(FileStore.class);

    @Test
    public void shouldCreateTransactionalFileStore_withFileStore() {
        // Given
        final FileStore mockFileStore = mock(FileStore.class);

        // When
        final TransactionalFileStore transactionalFileStore = new TransactionalFileStore(mockFileStore);

        // Then
        assertNotNull(transactionalFileStore);
    }

    @Test
    public void shouldRead_withItemIdAndItemClass() throws Exception {
        // Given
        final FilePath filePath = mock(FilePath.class);
        final FileItem mockFileItem = mock(FileItem.class);
        when(mockFileStore.read(any(FilePath.class))).thenReturn(mockFileItem);
        final TransactionalFileStore transactionalFileStore = new TransactionalFileStore(mockFileStore);

        // When
        final FileItem returnedFileItem = transactionalFileStore.read(filePath);

        // Then
        verify(mockFileStore).read(filePath);
        assertEquals(mockFileItem, returnedFileItem);
    }

    @Test
    public void shouldBeginTransaction_withNoExistingTransaction() throws Exception {
        // Given
        final TransactionalFileStore transactionalFileStore = new TransactionalFileStore(mockFileStore);

        // When
        Exception actualException = null;
        try {
            transactionalFileStore.begin();
        } catch (final Exception e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
    }

    @Test
    public void shouldNotBeginTransaction_withExistingTransaction() throws Exception {
        // Given
        final TransactionalFileStore transactionalFileStore = new TransactionalFileStore(mockFileStore);
        transactionalFileStore.begin();

        // When
        NestedTransactionException actualException = null;
        try {
            transactionalFileStore.begin();
        } catch (final NestedTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotCommit_withNoExistingTransaction() throws Exception {
        // Given
        final TransactionalFileStore transactionalFileStore = new TransactionalFileStore(mockFileStore);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalFileStore.commit();
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldWrite_withExistingTransaction() throws Exception {
        // Given
        final TransactionalFileStore transactionalFileStore = new TransactionalFileStore(mockFileStore);
        transactionalFileStore.begin();
        final FilePath filePath = mock(FilePath.class);
        final FileItem fileItem = mock(FileItem.class);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalFileStore.write(filePath, fileItem);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
        verifyZeroInteractions(mockFileStore);
    }

    @Test
    public void shouldNotWrite_withNonExistingTransaction() throws Exception {
        // Given
        final TransactionalFileStore transactionalFileStore = new TransactionalFileStore(mockFileStore);
        final FilePath filePath = mock(FilePath.class);
        final FileItem fileItem = mock(FileItem.class);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalFileStore.write(filePath, fileItem);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCommitWrite_withExistingTransaction() throws Exception {
        // Given
        final TransactionalFileStore transactionalFileStore = new TransactionalFileStore(mockFileStore);
        transactionalFileStore.begin();
        final FilePath filePath = mock(FilePath.class);
        final FileItem fileItem = mock(FileItem.class);
        transactionalFileStore.write(filePath, fileItem);

        // When
        transactionalFileStore.commit();

        // Then
        verify(mockFileStore).write(filePath, fileItem);
    }

    @Test
    public void shouldDelete_withExistingTransaction() throws Exception {
        // Given
        final TransactionalFileStore transactionalFileStore = new TransactionalFileStore(mockFileStore);
        transactionalFileStore.begin();
        final FilePath filePath = mock(FilePath.class);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalFileStore.delete(filePath);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
        verifyZeroInteractions(mockFileStore);
    }

    @Test
    public void shouldNotDelete_withNonExistingTransaction() throws Exception {
        // Given
        final TransactionalFileStore transactionalFileStore = new TransactionalFileStore(mockFileStore);
        final FilePath filePath = mock(FilePath.class);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalFileStore.delete(filePath);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCommitDelete_withExistingTransaction() throws Exception {
        // Given
        final TransactionalFileStore transactionalFileStore = new TransactionalFileStore(mockFileStore);
        transactionalFileStore.begin();
        final FilePath filePath = mock(FilePath.class);
        transactionalFileStore.delete(filePath);

        // When
        transactionalFileStore.commit();

        // Then
        verify(mockFileStore).delete(filePath);
    }

    @Test
    public void shouldList_withPrefix() throws Exception {

        // Given
        final TransactionalFileStore transactionalFileStore = new TransactionalFileStore(mockFileStore);
        final String directory = Randoms.randomString();
        final String prefix = Randoms.randomString();

        // When
        transactionalFileStore.list(directory, prefix);

        // Then
        verify(mockFileStore).list(directory, prefix);
    }

    @Test
    public void shouldList_withNoPrefix() throws Exception {

        // Given
        final TransactionalFileStore transactionalFileStore = new TransactionalFileStore(mockFileStore);
        final String directory = Randoms.randomString();

        // When
        transactionalFileStore.list(directory, null);

        // Then
        verify(mockFileStore).list(directory, null);
    }
}
