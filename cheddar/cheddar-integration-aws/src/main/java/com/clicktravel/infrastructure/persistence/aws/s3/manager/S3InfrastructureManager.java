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
package com.clicktravel.infrastructure.persistence.aws.s3.manager;

import java.util.Collection;
import java.util.HashSet;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.s3.AmazonS3;
import com.clicktravel.infrastructure.persistence.aws.s3.S3FileStore;

public class S3InfrastructureManager {

    private final AmazonS3 amazonS3Client;

    private final Collection<S3FileStore> s3FileStores = new HashSet<>();

    @Autowired
    public S3InfrastructureManager(final AmazonS3 amazonS3Client) {
        this.amazonS3Client = amazonS3Client;
    }

    @Autowired(required = false)
    public void setS3FileStores(final Collection<S3FileStore> s3FileStores) {
        if (s3FileStores != null) {
            this.s3FileStores.addAll(s3FileStores);
        }
    }

    @PostConstruct
    public void init() {
        for (final S3FileStore s3FileStore : s3FileStores) {
            s3FileStore.initialize(amazonS3Client);
        }
    }

}
