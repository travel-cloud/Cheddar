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

import java.io.*;
import java.util.UUID;

import org.joda.time.DateTime;

public class FileItem {

    private final String filename;

    private final DateTime lastUpdatedTime;

    private final byte[] contents;

    public FileItem(final String filename, final String contents) {
        this.filename = filename;
        this.contents = contents.getBytes();
        lastUpdatedTime = DateTime.now();
    }

    public FileItem(final String filename, final InputStream inputStream, final DateTime lastUpdatedTime)
            throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null");
        }
        this.filename = filename;
        if (lastUpdatedTime == null) {
            this.lastUpdatedTime = DateTime.now();
        } else {
            this.lastUpdatedTime = lastUpdatedTime;
        }
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int read = -1;
        final byte[] buf = new byte[1024 * 50];
        try {
            while ((read = inputStream.read(buf)) != -1) {
                bout.write(buf, 0, read);
            }
            contents = bout.toByteArray();
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public FileItem(final String contents) {
        this(UUID.randomUUID().toString(), contents);
    }

    public FileItem(final String filename, final File file) throws IOException {
        this(filename, new FileInputStream(file), DateTime.now());
    }

    public String filename() {
        return filename;
    }

    public DateTime lastUpdatedTime() {
        return lastUpdatedTime;
    }

    public byte[] getBytes() {
        return contents;
    }

}
