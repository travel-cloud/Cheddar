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
package com.clicktravel.infrastructure.persistence.aws.s3;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FileItem;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FilePath;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FileStore;

public class S3FileStore implements FileStore {

    private static final String USER_METADATA_FILENAME = "filename";
    private static final String USER_METADATA_LAST_UPDATED_TIME = "last-updated-time";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String bucketSchema;
    private boolean initialized;
    protected AmazonS3 amazonS3Client;
    private final DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();

    private final Collection<String> missingItemErrorCodes = Arrays.asList("NoSuchBucket", "NoSuchKey");

    public S3FileStore(final String bucketSchema) {
        this.bucketSchema = bucketSchema;
        initialized = false;
    }

    public void initialize(final AmazonS3 amazonS3Client) {
        this.amazonS3Client = amazonS3Client;
        initialized = true;
        logger.info("S3FileStore initialized.");
    }

    private void checkInitialization() {
        if (!initialized) {
            throw new IllegalStateException("S3FileStore not initialized.");
        }
    }

    @Override
    public FileItem read(final FilePath filePath) throws NonExistentItemException {
        checkInitialization();
        final GetObjectRequest getObjectRequest = new GetObjectRequest(bucketNameForFilePath(filePath),
                filePath.filename());
        try {
            final S3Object s3Object = amazonS3Client.getObject(getObjectRequest);
            final String filename = s3Object.getObjectMetadata().getUserMetadata().get(USER_METADATA_FILENAME);
            final String lastUpdatedTimeStr = s3Object.getObjectMetadata().getUserMetadata()
                    .get(USER_METADATA_LAST_UPDATED_TIME);
            DateTime lastUpdatedTime = null;
            if (lastUpdatedTimeStr != null) {
                try {
                    lastUpdatedTime = formatter.parseDateTime(lastUpdatedTimeStr);
                } catch (final Exception e) {
                    logger.warn(e.getMessage(), e);
                }
            }
            try {

                final FileItem fileItem = new FileItem(filename, s3Object.getObjectContent(), lastUpdatedTime);
                return fileItem;
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        } catch (final AmazonS3Exception e) {
            if (missingItemErrorCodes.contains(e.getErrorCode())) {
                throw new NonExistentItemException("Item does not exist" + filePath.directory() + "->"
                        + filePath.filename());
            }
            throw e;
        }
    }

    @Override
    public void write(final FilePath filePath, final FileItem fileItem) {
        checkInitialization();

        final ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata(USER_METADATA_FILENAME, fileItem.filename());
        metadata.addUserMetadata(USER_METADATA_LAST_UPDATED_TIME, formatter.print(fileItem.lastUpdatedTime()));
        metadata.setContentLength(fileItem.getBytes().length);
        final InputStream is = new ByteArrayInputStream(fileItem.getBytes());
        final String bucketName = bucketNameForFilePath(filePath);
        if (!amazonS3Client.doesBucketExist(bucketName)) {
            createBucket(bucketName);
        }
        final PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, filePath.filename(), is, metadata);
        amazonS3Client.putObject(putObjectRequest);
    }

    @Override
    public void delete(final FilePath filePath) {
        checkInitialization();
        try {
            amazonS3Client.deleteObject(bucketNameForFilePath(filePath), filePath.filename());
        } catch (final AmazonS3Exception e) {
            if (missingItemErrorCodes.contains(e.getErrorCode())) {
                throw new NonExistentItemException("Item does not exist" + filePath.directory() + "->"
                        + filePath.filename());
            }
            throw e;
        }
    }

    private String bucketNameForFilePath(final FilePath filePath) {
        return bucketSchema + "-" + filePath.directory();
    }

    private void createBucket(final String bucketName) {
        amazonS3Client.createBucket(bucketName);
    }

    @Override
    public URL publicUrlForFilePath(final FilePath filePath) throws NonExistentItemException {
        return amazonS3Client.generatePresignedUrl(bucketNameForFilePath(filePath), filePath.filename(), new DateTime()
                .plusHours(1).toDate(), HttpMethod.GET);
    }

}
