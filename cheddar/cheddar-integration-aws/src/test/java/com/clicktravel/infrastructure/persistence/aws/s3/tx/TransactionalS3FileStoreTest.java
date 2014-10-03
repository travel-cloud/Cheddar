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
package com.clicktravel.infrastructure.persistence.aws.s3.tx;

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
import com.clicktravel.cheddar.infrastructure.tx.NestedTransactionException;
import com.clicktravel.cheddar.infrastructure.tx.NonExistentTransactionException;
import com.clicktravel.common.random.Randoms;
import com.clicktravel.infrastructure.persistence.aws.s3.S3FileStore;

public class TransactionalS3FileStoreTest {

    private final S3FileStore mockS3FileStore = mock(S3FileStore.class);

    @Test
    public void shouldCreateTransactionalS3FileStore_withS3FileStore() {
        // Given
        final S3FileStore mockS3FileStore = mock(S3FileStore.class);

        // When
        final TransactionalS3FileStore transactionalS3FileStore = new TransactionalS3FileStore(mockS3FileStore);

        // Then
        assertNotNull(transactionalS3FileStore);
    }

    @Test
    public void shouldRead_withItemIdAndItemClass() throws Exception {
        // Given
        final FilePath filePath = mock(FilePath.class);
        final FileItem mockFileItem = mock(FileItem.class);
        when(mockS3FileStore.read(any(FilePath.class))).thenReturn(mockFileItem);
        final TransactionalS3FileStore transactionalS3FileStore = new TransactionalS3FileStore(mockS3FileStore);

        // When
        final FileItem returnedFileItem = transactionalS3FileStore.read(filePath);

        // Then
        verify(mockS3FileStore).read(filePath);
        assertEquals(mockFileItem, returnedFileItem);
    }

    @Test
    public void shouldBeginTransaction_withNoExistingTransaction() throws Exception {
        // Given
        final TransactionalS3FileStore transactionalS3FileStore = new TransactionalS3FileStore(mockS3FileStore);

        // When
        Exception actualException = null;
        try {
            transactionalS3FileStore.begin();
        } catch (final Exception e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
    }

    @Test
    public void shouldNotBeginTransaction_withExistingTransaction() throws Exception {
        // Given
        final TransactionalS3FileStore transactionalS3FileStore = new TransactionalS3FileStore(mockS3FileStore);
        transactionalS3FileStore.begin();

        // When
        NestedTransactionException actualException = null;
        try {
            transactionalS3FileStore.begin();
        } catch (final NestedTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotCommit_withNoExistingTransaction() throws Exception {
        // Given
        final TransactionalS3FileStore transactionalS3FileStore = new TransactionalS3FileStore(mockS3FileStore);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalS3FileStore.commit();
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldWrite_withExistingTransaction() throws Exception {
        // Given
        final TransactionalS3FileStore transactionalS3FileStore = new TransactionalS3FileStore(mockS3FileStore);
        transactionalS3FileStore.begin();
        final FilePath filePath = mock(FilePath.class);
        final FileItem fileItem = mock(FileItem.class);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalS3FileStore.write(filePath, fileItem);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
        verifyZeroInteractions(mockS3FileStore);
    }

    @Test
    public void shouldNotWrite_withNonExistingTransaction() throws Exception {
        // Given
        final TransactionalS3FileStore transactionalS3FileStore = new TransactionalS3FileStore(mockS3FileStore);
        final FilePath filePath = mock(FilePath.class);
        final FileItem fileItem = mock(FileItem.class);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalS3FileStore.write(filePath, fileItem);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCommitWrite_withExistingTransaction() throws Exception {
        // Given
        final TransactionalS3FileStore transactionalS3FileStore = new TransactionalS3FileStore(mockS3FileStore);
        transactionalS3FileStore.begin();
        final FilePath filePath = mock(FilePath.class);
        final FileItem fileItem = mock(FileItem.class);
        transactionalS3FileStore.write(filePath, fileItem);

        // When
        transactionalS3FileStore.commit();

        // Then
        verify(mockS3FileStore).write(filePath, fileItem);
    }

    @Test
    public void shouldDelete_withExistingTransaction() throws Exception {
        // Given
        final TransactionalS3FileStore transactionalS3FileStore = new TransactionalS3FileStore(mockS3FileStore);
        transactionalS3FileStore.begin();
        final FilePath filePath = mock(FilePath.class);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalS3FileStore.delete(filePath);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNull(actualException);
        verifyZeroInteractions(mockS3FileStore);
    }

    @Test
    public void shouldNotDelete_withNonExistingTransaction() throws Exception {
        // Given
        final TransactionalS3FileStore transactionalS3FileStore = new TransactionalS3FileStore(mockS3FileStore);
        final FilePath filePath = mock(FilePath.class);

        // When
        NonExistentTransactionException actualException = null;
        try {
            transactionalS3FileStore.delete(filePath);
        } catch (final NonExistentTransactionException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldCommitDelete_withExistingTransaction() throws Exception {
        // Given
        final TransactionalS3FileStore transactionalS3FileStore = new TransactionalS3FileStore(mockS3FileStore);
        transactionalS3FileStore.begin();
        final FilePath filePath = mock(FilePath.class);
        transactionalS3FileStore.delete(filePath);

        // When
        transactionalS3FileStore.commit();

        // Then
        verify(mockS3FileStore).delete(filePath);
    }

    @Test
    public void shouldList_withPrefix() throws Exception {

        // Given
        final TransactionalS3FileStore transactionalS3FileStore = new TransactionalS3FileStore(mockS3FileStore);
        final String directory = Randoms.randomString();
        final String prefix = Randoms.randomString();

        // When
        transactionalS3FileStore.list(directory, prefix);

        // Then
        verify(mockS3FileStore).list(directory, prefix);
    }

    @Test
    public void shouldList_withNoPrefix() throws Exception {

        // Given
        final TransactionalS3FileStore transactionalS3FileStore = new TransactionalS3FileStore(mockS3FileStore);
        final String directory = Randoms.randomString();

        // When
        transactionalS3FileStore.list(directory, null);

        // Then
        verify(mockS3FileStore).list(directory, null);
    }
}
