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

import java.net.MalformedURLException;
import java.net.URL;

import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FileItem;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FilePath;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.InternetFileStore;

public class InMemoryInternetFileStore extends InMemoryFileStore implements InternetFileStore {

    @Override
    public URL publicUrlForFilePath(final FilePath filePath) throws NonExistentItemException {
        try {
            final FileItem fileItem = read(filePath);
            return new URL("http://" + filePath.directory() + "localhost/" + fileItem.filename());
        } catch (final MalformedURLException e) {
            throw new IllegalStateException("Invalid file path. directory:[" + filePath.directory() + "], filename:["
                    + filePath.filename() + "]");
        }
    }

    @Override
    public void write(final FilePath filePath, final FileItem fileItem, final String filename) {
        super.write(filePath, fileItem);
    }
}
