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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FileItem;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FilePath;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FileStore;
import com.clicktravel.infrastructure.inmemory.Resettable;
import com.clicktravel.infrastructure.persistence.inmemory.SerializedItem;

public class InMemoryFileStore implements FileStore, Resettable {

    private final Map<FilePath, SerializedItem> fileItems;

    public InMemoryFileStore() {
        fileItems = new ConcurrentHashMap<>();
    }

    @Override
    public FileItem read(final FilePath filePath) throws NonExistentItemException {
        final SerializedItem fileItem = fileItems.get(filePath);
        if (fileItem == null) {
            throw new NonExistentItemException("No FileItem for file path: " + filePath);
        }
        return fileItem.getEntity(FileItem.class);
    }

    @Override
    public void write(final FilePath filePath, final FileItem fileItem) {
        fileItems.put(filePath, new SerializedItem(fileItem));
    }

    @Override
    public void delete(final FilePath filePath) throws NonExistentItemException {
        fileItems.remove(filePath);
    }

    @Override
    public List<FilePath> list(final String directory, final String prefix) {
        final List<FilePath> matches = new ArrayList<>();
        for (final FilePath filePath : fileItems.keySet()) {
            if (filePath.directory().equals(directory) && filePath.filename().startsWith(prefix)) {
                matches.add(filePath);
            }
        }
        return matches;
    }

    @Override
    public void reset() {
        fileItems.clear();
    }
}
