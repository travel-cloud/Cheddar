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
package com.clicktravel.common.utils;

import java.io.File;
import java.util.UUID;

public class FileUtils {

    private static final int TEMP_DIR_ATTEMPTS = 100;

    /**
     * TAKEN FROM com.google.common.io.Files
     * 
     * Atomically creates a new directory somewhere beneath the system's
     * temporary directory (as defined by the {@code java.io.tmpdir} system
     * property), and returns it as a java.io.File. The directory name will be part UUID and part attempt if it takes
     * more than one attempt.
     * 
     * <p>
     * Use this method instead of {@link File#createTempFile(String, String)} when you wish to create a directory, not a
     * regular file. A common pitfall is to call {@code createTempFile}, delete the file and create a directory in its
     * place, but this leads a race condition which can be exploited to create security vulnerabilities, especially when
     * executable files are to be written into the directory.
     * 
     * <p>
     * This method assumes that the temporary volume is writable, has free inodes and free blocks
     * 
     * @return the newly-created directory
     * @throws IllegalStateException if the directory could not be created
     */
    public static File createTempDir() {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        String baseName = UUID.randomUUID().toString();

        for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
            File tempDir = new File(baseDir, counter > 0 ? baseName + "-" + counter : baseName);

            if (tempDir.mkdir()) {
                return tempDir;
            }
        }
        throw new IllegalStateException("Failed to create directory within " + TEMP_DIR_ATTEMPTS + " attempts (tried "
                + baseName + "0 to " + baseName + (TEMP_DIR_ATTEMPTS - 1) + ')');
    }

    /**
     * Utilises the create temp directory method above to create a normal file inside a temp directory, this is slightly
     * better for most uses as it doesnt append extra chars to your filename for uniqueness
     * @param name of the file you want to create
     * @return file marked as delete on exit
     */
    public static File createTempFile(final String name) {
        if (name == null) {
            throw new NullPointerException("Please supply a file name");
        }

        File dir = createTempDir();
        File temp = new File(dir, name);
        temp.deleteOnExit();
        return temp;
    }
}
