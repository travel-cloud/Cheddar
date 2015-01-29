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

import static com.clicktravel.common.random.Randoms.randomDateTime;
import static com.clicktravel.common.random.Randoms.randomId;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FileItem;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.FilePath;
import com.clicktravel.common.random.Randoms;

public class S3FileStoreTest {

    private final FilePath filePath = new FilePath("s3_stub_items", Randoms.randomId() + "_test");
    private final AmazonS3Client mockAmazonS3Client = mock(AmazonS3Client.class);
    private final FileItem mockFileItem = mock(FileItem.class);
    private String bucketSchema = null;
    private final DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();

    @Before
    public void setup() {
        bucketSchema = randomString(10);

    }

    @After
    public void tearDown() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void shouldNotWrite_whenNotInitialized() {
        // Given
        final S3FileStore s3FileStore = new S3FileStore(bucketSchema);

        // When
        IllegalStateException actualException = null;
        try {
            s3FileStore.write(filePath, mockFileItem);
        } catch (final IllegalStateException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldRead_whenNotInitialized() {
        // Given
        final S3FileStore s3FileStore = new S3FileStore(bucketSchema);

        // When
        IllegalStateException actualException = null;
        try {
            s3FileStore.read(filePath);
        } catch (final IllegalStateException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotDelete_whenNotInitialized() {
        // Given
        final S3FileStore s3FileStore = new S3FileStore(bucketSchema);

        // When
        IllegalStateException actualException = null;
        try {
            s3FileStore.delete(filePath);
        } catch (final IllegalStateException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldWriteFile_withStringContent() throws Exception {
        // Given
        final String key = "file-id" + randomId();
        final String fileContent = randomString(255);
        final FileItem fileItem = new FileItem(key, fileContent);
        final S3FileStore s3FileStore = new S3FileStore(bucketSchema);
        s3FileStore.initialize(mockAmazonS3Client);

        // When
        s3FileStore.write(filePath, fileItem);

        // Then
        final ArgumentCaptor<PutObjectRequest> putObjectRequestArgumentCaptor = ArgumentCaptor
                .forClass(PutObjectRequest.class);
        verify(mockAmazonS3Client).putObject(putObjectRequestArgumentCaptor.capture());
        assertEquals(bucketSchema + "-" + filePath.directory(), putObjectRequestArgumentCaptor.getValue()
                .getBucketName());
        assertEquals(filePath.filename(), putObjectRequestArgumentCaptor.getValue().getKey());
        final InputStream inputStream = putObjectRequestArgumentCaptor.getValue().getInputStream();
        assertEquals(fileContent, inputStreamToString(inputStream));
        assertEquals(2, putObjectRequestArgumentCaptor.getValue().getMetadata().getUserMetadata().size());
        assertEquals(fileItem.filename(), putObjectRequestArgumentCaptor.getValue().getMetadata().getUserMetadata()
                .get("filename"));
        assertEquals(formatter.print(fileItem.lastUpdatedTime()), putObjectRequestArgumentCaptor.getValue()
                .getMetadata().getUserMetadata().get("last-updated-time"));
    }

    @SuppressWarnings("resource")
    private String inputStreamToString(final InputStream inputStream) throws Exception {
        final Scanner s = new Scanner(inputStream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Test
    public void shouldReadFile() throws Exception {
        // Given
        final String filename = randomString(10);
        final DateTime lastUpdatedTime = randomDateTime();
        final DateTime expectedLastUpdatedTime = lastUpdatedTime.withZone(DateTimeZone.UTC).withMillisOfSecond(0);
        final S3FileStore s3FileStore = new S3FileStore(bucketSchema);
        s3FileStore.initialize(mockAmazonS3Client);
        final S3Object mockS3Object = mock(S3Object.class);
        final ObjectMetadata mockObjectMetadata = mock(ObjectMetadata.class);
        final Map<String, String> userMetadata = new HashMap<>();
        userMetadata.put("filename", filename);
        userMetadata.put("last-updated-time", formatter.print(lastUpdatedTime));
        when(mockAmazonS3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockS3Object);
        final S3ObjectInputStream mockInputStream = mock(S3ObjectInputStream.class);
        when(mockS3Object.getObjectContent()).thenReturn(mockInputStream);
        when(mockS3Object.getObjectMetadata()).thenReturn(mockObjectMetadata);
        when(mockObjectMetadata.getUserMetadata()).thenReturn(userMetadata);
        when(mockInputStream.read(any(byte[].class))).thenReturn(-1);

        // When
        final FileItem fileItem = s3FileStore.read(filePath);

        // Then
        final ArgumentCaptor<GetObjectRequest> getObjectRequestArgumentCaptor = ArgumentCaptor
                .forClass(GetObjectRequest.class);
        verify(mockAmazonS3Client).getObject(getObjectRequestArgumentCaptor.capture());
        assertEquals(bucketSchema + "-" + filePath.directory(), getObjectRequestArgumentCaptor.getValue()
                .getBucketName());
        assertEquals(filePath.filename(), getObjectRequestArgumentCaptor.getValue().getKey());
        assertEquals(filename, fileItem.filename());
        assertEquals(expectedLastUpdatedTime, fileItem.lastUpdatedTime());
    }

    @Test
    public void shouldDeleteFile() throws IOException {
        // Given
        final S3FileStore s3FileStore = new S3FileStore(bucketSchema);
        s3FileStore.initialize(mockAmazonS3Client);

        // When
        s3FileStore.delete(filePath);

        // Then
        verify(mockAmazonS3Client).deleteObject(bucketSchema + "-" + filePath.directory(), filePath.filename());
    }

    @Test
    public void shouldGetURL() throws IOException {
        // Given
        final S3FileStore s3FileStore = new S3FileStore(bucketSchema);
        s3FileStore.initialize(mockAmazonS3Client);
        final int randomMillis = Randoms.randomInt(1000);
        DateTimeUtils.setCurrentMillisFixed(randomMillis);

        // When
        s3FileStore.publicUrlForFilePath(filePath);

        // Then
        verify(mockAmazonS3Client).generatePresignedUrl(bucketSchema + "-" + filePath.directory(), filePath.filename(),
                new Date(3600000 + randomMillis), HttpMethod.GET);
    }

    @Test
    public void shouldList_withPrefix() {

        // Given
        final S3FileStore s3FileStore = new S3FileStore(bucketSchema);
        s3FileStore.initialize(mockAmazonS3Client);
        final String directory = randomString(10);
        final String prefix = randomString(10);

        final ObjectListing mockObjectListing = mock(ObjectListing.class);
        when(mockAmazonS3Client.listObjects(bucketSchema + "-" + directory, prefix)).thenReturn(mockObjectListing);
        final S3ObjectSummary mockS3ObjectSummary = mock(S3ObjectSummary.class);
        final ArrayList<S3ObjectSummary> s3objectSummaries = new ArrayList<S3ObjectSummary>();
        s3objectSummaries.add(mockS3ObjectSummary);
        when(mockObjectListing.getObjectSummaries()).thenReturn(s3objectSummaries);
        when(mockS3ObjectSummary.getBucketName()).thenReturn(randomString(10));
        when(mockS3ObjectSummary.getKey()).thenReturn(randomString(10));

        // When
        final List<FilePath> filePathList = s3FileStore.list(directory, prefix);

        // Then
        verify(mockAmazonS3Client).listObjects(bucketSchema + "-" + directory, prefix);
        assertNotNull(filePathList);
        assertTrue(filePathList.size() == 1);
    }

    @Test
    public void shouldList_withNoPrefix() {

        // Given
        final S3FileStore s3FileStore = new S3FileStore(bucketSchema);
        s3FileStore.initialize(mockAmazonS3Client);
        final String directory = randomString(10);

        final ObjectListing mockObjectListing = mock(ObjectListing.class);
        when(mockAmazonS3Client.listObjects(bucketSchema + "-" + directory, null)).thenReturn(mockObjectListing);
        final S3ObjectSummary mockS3ObjectSummary = mock(S3ObjectSummary.class);
        final ArrayList<S3ObjectSummary> s3objectSummaries = new ArrayList<S3ObjectSummary>();
        s3objectSummaries.add(mockS3ObjectSummary);
        when(mockObjectListing.getObjectSummaries()).thenReturn(s3objectSummaries);
        when(mockS3ObjectSummary.getBucketName()).thenReturn(randomString(10));
        when(mockS3ObjectSummary.getKey()).thenReturn(randomString(10));

        // When
        final List<FilePath> filePathList = s3FileStore.list(directory, null);

        // Then
        verify(mockAmazonS3Client).listObjects(bucketSchema + "-" + directory, null);
        assertNotNull(filePathList);
        assertTrue(filePathList.size() == 1);
    }
}
