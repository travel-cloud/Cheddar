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
package com.clicktravel.infrastructure.persistence.aws.s3.integration;

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.clicktravel.common.random.Randoms;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FileItem;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FilePath;
import com.clicktravel.infrastructure.integration.aws.AwsIntegration;
import com.clicktravel.infrastructure.persistence.aws.s3.S3FileStore;

@Category({ AwsIntegration.class })
public class S3FileStoreIntegrationTest {

    private static final String BUCKET_SCHEMA = "unittest";
    private static final String BUCKET_NAME = "s3store-items";
    private AmazonS3 amazonS3Client;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void before() {
        amazonS3Client = new AmazonS3Client(new BasicAWSCredentials(AwsIntegration.getAccessKeyId(),
                AwsIntegration.getSecretKeyId()));

        // create bucket if not present
        final String fullBucketName = BUCKET_SCHEMA + "." + BUCKET_NAME;
        if (!amazonS3Client.doesBucketExist(fullBucketName)) {
            amazonS3Client.createBucket(fullBucketName);
        }
    }

    @Test
    public void shouldCreateAndReadFileItem() throws IOException {
        // Given
        final S3FileStore s3FileStore = new S3FileStore(BUCKET_SCHEMA);
        s3FileStore.initialize(amazonS3Client);
        final FileItem fileItemS3 = new FileItem(randomString(10), randomString(255));
        final FilePath filePath = new FilePath(BUCKET_NAME, Randoms.randomId() + "-test");
        s3FileStore.write(filePath, fileItemS3);

        // When
        final FileItem retrievedFileItemS3 = s3FileStore.read(filePath);

        // Then
        assertEquals(fileItemS3.filename(), retrievedFileItemS3.filename());
        assertEquals(new String(fileItemS3.getBytes()), new String(retrievedFileItemS3.getBytes()));
    }

    @Test
    public void shouldCreateAndDeleteStringItem() throws IOException {
        // Given
        final S3FileStore s3FileStore = new S3FileStore(BUCKET_SCHEMA);
        s3FileStore.initialize(amazonS3Client);
        final File file = folder.newFile();
        final FileItem fileItemS3 = new FileItem(randomString(10), file);
        final FilePath filePath = new FilePath(BUCKET_NAME, Randoms.randomId() + "-test");
        s3FileStore.write(filePath, fileItemS3);

        // When
        NonExistentItemException actualDeleteException = null;
        try {
            s3FileStore.delete(filePath);
        } catch (final NonExistentItemException e) {
            actualDeleteException = e;
            e.printStackTrace();
        }
        // Then
        assertNull(actualDeleteException);

        // When
        NonExistentItemException actualReadException = null;
        try {
            s3FileStore.read(filePath);
        } catch (final NonExistentItemException e) {
            actualReadException = e;
        }
        // Then
        assertNotNull(actualReadException);
    }

}
