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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

import org.joda.time.DateTime;

public class FileItem {

    private final String filename;
    private final byte[] contents;
    private final DateTime lastUpdatedTime;

    /**
     * Creates a file with given name, contents and last updated time
     * @param filename Name of the file
     * @param contents Contents of file as a byte array
     * @param lastUpdatedTime DateTime of last update of the file
     */
    public FileItem(final String filename, final byte[] contents, final DateTime lastUpdatedTime) {
        this.filename = filename;
        this.contents = contents;
        this.lastUpdatedTime = lastUpdatedTime == null ? DateTime.now() : lastUpdatedTime;
    }

    /**
     * Creates a file with given name and contents, with last updated time set to now
     * @param filename Name of the file
     * @param contents Contents of file as a byte array
     */
    public FileItem(final String filename, final byte[] contents) {
        this(filename, contents, null);
    }

    /**
     * Creates a file with given name and contents as a UTF-8 encoded string, with last updated time set to now
     * @param filename Name of the file
     * @param contents Content of file as a string
     */
    public FileItem(final String filename, final String contents) {
        this(filename, contents.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates a file with given name and contents taken from an InputStream
     * @param filename Name of the file
     * @param inputStream Source of file content. This stream is not closed by this method
     * @param lastUpdatedTime DateTime of last update of the file
     * @throws IOException
     */
    public FileItem(final String filename, final InputStream inputStream, final DateTime lastUpdatedTime)
            throws IOException {
        this(filename, toByteArray(inputStream), lastUpdatedTime);
    }

    /**
     * Creates a file with given name and contents taken from an InputStream, with last updated time set to now
     * @param filename Name of the file
     * @param inputStream Source of file content. This stream is not closed by this method
     * @throws IOException
     */
    public FileItem(final String filename, final InputStream inputStream) throws IOException {
        this(filename, inputStream, null);
    }

    /**
     * Creates a uniquely named file with contents as a UTF-8 encoded string
     * @param contents Content of file as a string
     */
    public FileItem(final String contents) {
        this(UUID.randomUUID().toString(), contents);
    }

    /**
     * Creates a file with given name and contents copied from an existing file on the standard file system
     * @param filename Name of the file to create
     * @param file Existing file with contents to be copied
     * @throws IOException
     */
    public FileItem(final String filename, final File file) throws IOException {
        this(filename, new FileInputStream(file));
    }

    private static byte[] toByteArray(final InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null");
        }
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int read = -1;
        final byte[] buf = new byte[1024 * 50];
        while ((read = inputStream.read(buf)) != -1) {
            bout.write(buf, 0, read);
        }
        return bout.toByteArray();
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

    /**
     * @return the contents of the bytes[] as a string, assumes the bytes are UTF-8 encoded
     */
    public String getContentsAsString() {
        return new String(getBytes(), StandardCharsets.UTF_8);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(contents);
        result = prime * result + ((filename == null) ? 0 : filename.hashCode());
        result = prime * result + ((lastUpdatedTime == null) ? 0 : lastUpdatedTime.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileItem other = (FileItem) obj;
        if (!Arrays.equals(contents, other.contents)) {
            return false;
        }
        if (filename == null) {
            if (other.filename != null) {
                return false;
            }
        } else if (!filename.equals(other.filename)) {
            return false;
        }
        if (lastUpdatedTime == null) {
            if (other.lastUpdatedTime != null) {
                return false;
            }
        } else if (!lastUpdatedTime.equals(other.lastUpdatedTime)) {
            return false;
        }
        return true;
    }

}
