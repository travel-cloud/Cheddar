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

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import com.clicktravel.cheddar.infrastructure.persistence.filestore.FileItem;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FilePath;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FileStore;
import com.clicktravel.cheddar.infrastructure.tx.Transaction;

public class FileStoreTransaction implements Transaction {

    private final Queue<FileStoreAction> fileStoreActions;
    private final String transactionId;

    public FileStoreTransaction() {
        fileStoreActions = new LinkedList<>();
        transactionId = UUID.randomUUID().toString();
    }

    @Override
    public String transactionId() {
        return transactionId;
    }

    public void addWriteAction(final FilePath filePath, final FileItem fileItem) {
        fileStoreActions.add(new CreateAction(filePath, fileItem));
    }

    public void addDeleteAction(final FilePath filePath) {
        fileStoreActions.add(new DeleteAction(filePath));
    }

    public void applyActions(final FileStore fileStore) {
        while (!fileStoreActions.isEmpty()) {
            final FileStoreAction fileStoreAction = fileStoreActions.remove();
            fileStoreAction.apply(fileStore);
        }
    }

}
