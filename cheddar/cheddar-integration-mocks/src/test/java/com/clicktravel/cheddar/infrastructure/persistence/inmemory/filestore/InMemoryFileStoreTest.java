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
package com.clicktravel.cheddar.infrastructure.persistence.inmemory.filestore;

import static com.clicktravel.cheddar.infrastructure.persistence.inmemory.filestore.RandomFileStoreHelper.randomFileItem;
import static com.clicktravel.cheddar.infrastructure.persistence.inmemory.filestore.RandomFileStoreHelper.randomFilePath;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FileItem;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FilePath;

public class InMemoryFileStoreTest {

    @Test
    public void shouldReadAndWrite_toAndfromFilePath() throws Exception {
        // Given
        final FilePath filePath = randomFilePath();
        final FileItem expectedFileItem = randomFileItem();

        final InMemoryFileStore fileStore = new InMemoryFileStore();
        fileStore.write(filePath, expectedFileItem);

        // When
        final FileItem fileItem = fileStore.read(filePath);

        // Then
        assertEquals(expectedFileItem, fileItem);
    }

    @Test
    public void shouldDelete_fromFilePath() throws Exception {
        // Given
        final FilePath filePath = randomFilePath();
        final FileItem expectedFileItem = randomFileItem();

        final InMemoryFileStore fileStore = new InMemoryFileStore();
        fileStore.write(filePath, expectedFileItem);

        // When
        fileStore.delete(filePath);

        // Then
        NonExistentItemException actualException = null;
        try {
            fileStore.read(filePath);
        } catch (final NonExistentItemException e) {
            actualException = e;
        }
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotDelete_nonExistentItemFromFilePath() throws Exception {
        // Given
        final FilePath filePath = randomFilePath();

        final InMemoryFileStore fileStore = new InMemoryFileStore();

        // When
        NonExistentItemException actualException = null;
        try {
            fileStore.read(filePath);
        } catch (final NonExistentItemException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldList_withDirectoryAndPrefix() throws Exception {
        // Given
        final String directory = randomString(10);
        final String prefix = randomString(10);
        final FilePath filePath1 = new FilePath(directory, prefix + randomString());
        final FileItem expectedFileItem1 = randomFileItem();
        final FilePath filePath2 = new FilePath(directory, prefix + randomString());
        final FileItem expectedFileItem2 = randomFileItem();
        final FilePath filePath3 = new FilePath(directory, prefix + randomString());
        final FileItem expectedFileItem3 = randomFileItem();

        final InMemoryFileStore fileStore = new InMemoryFileStore();
        fileStore.write(filePath1, expectedFileItem1);
        fileStore.write(filePath2, expectedFileItem2);
        fileStore.write(filePath3, expectedFileItem3);

        // When
        final List<FilePath> filePaths = fileStore.list(directory, prefix);

        // Then
        assertNotNull(filePaths);
        assertThat(filePaths.size(), is(3));
        assertTrue(filePaths.containsAll(Arrays.asList(filePath1, filePath2, filePath3)));
    }

    @Test
    public void shouldList_withDirectoryAndMismatchingPrefix() throws Exception {
        // Given
        final String directory = randomString(10);
        final String prefix = randomString(10);
        final FilePath filePath1 = new FilePath(directory, prefix + randomString());
        final FileItem expectedFileItem1 = randomFileItem();
        final FilePath filePath2 = new FilePath(directory, prefix + randomString());
        final FileItem expectedFileItem2 = randomFileItem();
        final FilePath filePath3NoMatch = new FilePath(directory, randomString());
        final FileItem expectedFileItem3 = randomFileItem();

        final InMemoryFileStore fileStore = new InMemoryFileStore();
        fileStore.write(filePath1, expectedFileItem1);
        fileStore.write(filePath2, expectedFileItem2);
        fileStore.write(filePath3NoMatch, expectedFileItem3);

        // When
        final List<FilePath> filePaths = fileStore.list(directory, prefix);

        // Then
        assertNotNull(filePaths);
        assertThat(filePaths.size(), is(2));
        assertTrue(filePaths.containsAll(Arrays.asList(filePath1, filePath2)));
    }

    @Test
    public void shouldList_withNoMatchingDirectoryOrPrefix() throws Exception {
        // Given
        final String directory = randomString(10);
        final String prefix = randomString(10);
        final FilePath filePath1 = randomFilePath();
        final FileItem expectedFileItem1 = randomFileItem();
        final FilePath filePath2 = randomFilePath();
        final FileItem expectedFileItem2 = randomFileItem();
        final FilePath filePath3NoMatch = randomFilePath();
        final FileItem expectedFileItem3 = randomFileItem();

        final InMemoryFileStore fileStore = new InMemoryFileStore();
        fileStore.write(filePath1, expectedFileItem1);
        fileStore.write(filePath2, expectedFileItem2);
        fileStore.write(filePath3NoMatch, expectedFileItem3);

        // When
        final List<FilePath> filePaths = fileStore.list(directory, prefix);

        // Then
        assertNotNull(filePaths);
        assertThat(filePaths.size(), is(0));
    }

}
