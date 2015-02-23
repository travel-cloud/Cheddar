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

import static com.clicktravel.common.random.Randoms.randomString;

import com.clicktravel.cheddar.infrastructure.persistence.filestore.FileItem;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FilePath;

public class RandomFileStoreHelper {

    public static FilePath randomFilePath() {
        return new FilePath(randomString(), randomString());
    }

    public static FileItem randomFileItem() {
        return new FileItem(randomString());
    }

}
