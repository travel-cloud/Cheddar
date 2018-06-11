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
package com.clicktravel.cheddar.infrastructure.persistence.filestore;

import java.util.List;

import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.exception.PersistenceResourceFailureException;

public interface FileStore {

    FileItem read(FilePath filePath) throws NonExistentItemException;

    /**
     * Write a {@link FileItem} to the given {@link FilePath}. The name of the file is taken from the given
     * {@link FileItem}.
     *
     * @param filePath {@link FilePath} to write the file to.
     * @param fileItem {@link FileItem} to write.
     */
    void write(FilePath filePath, FileItem fileItem);

    void delete(FilePath filePath) throws NonExistentItemException;

    /**
     * Lists the contents of a FileStore.
     *
     * @param directory The directory to list the contents of.
     * @param prefix The prefix to limit the results by.
     * @return A list of the contents of the FileStore.
     *
     * @throws PersistenceResourceFailureException If an error occurred listing the directory.
     */
    List<FilePath> list(String directory, String prefix) throws PersistenceResourceFailureException;
}
