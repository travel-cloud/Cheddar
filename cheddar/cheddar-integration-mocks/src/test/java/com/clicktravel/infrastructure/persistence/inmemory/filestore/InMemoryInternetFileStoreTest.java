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
package com.clicktravel.infrastructure.persistence.inmemory.filestore;

import static com.clicktravel.infrastructure.persistence.inmemory.filestore.RandomFileStoreHelper.randomFileItem;
import static com.clicktravel.infrastructure.persistence.inmemory.filestore.RandomFileStoreHelper.randomFilePath;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.net.URL;

import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FileItem;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FilePath;

public class InMemoryInternetFileStoreTest {

    @Test
    public void shouldGeneratePublicUrl_withFilePath() throws Exception {
        // Given
        final FilePath filePath = randomFilePath();
        final FileItem expectedFileItem = randomFileItem();

        final InMemoryInternetFileStore fileStore = new InMemoryInternetFileStore();
        fileStore.write(filePath, expectedFileItem);

        // When
        final URL publicUrl = fileStore.publicUrlForFilePath(filePath);

        // Then
        assertThat(publicUrl, is(new URL("http://" + filePath.directory() + "localhost/" + filePath.filename())));
    }

    @Test
    public void shouldNotGeneratePublicUrl_withIncorrectFilePath() throws Exception {
        // Given
        final FilePath filePath = randomFilePath();

        final InMemoryInternetFileStore fileStore = new InMemoryInternetFileStore();

        // When
        NonExistentItemException actualException = null;
        try {
            fileStore.publicUrlForFilePath(filePath);
        } catch (final NonExistentItemException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

}
