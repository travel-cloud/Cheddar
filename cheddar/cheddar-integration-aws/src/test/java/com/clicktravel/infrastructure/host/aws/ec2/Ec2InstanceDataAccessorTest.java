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
package com.clicktravel.infrastructure.host.aws.ec2;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import com.clicktravel.common.http.client.HttpClient;
import com.clicktravel.common.random.Randoms;

public class Ec2InstanceDataAccessorTest {

    HttpClient mockHttpClient;

    @Before
    public void setUp() {
        mockHttpClient = mock(HttpClient.class);
    }

    @Test
    public void shouldReturnInstanceId_onGetInstanceId() {
        // Given
        final String instanceId = Randoms.randomString();
        final Response mockResponse = mock(Response.class);
        when(mockHttpClient.get("/meta-data/instance-id")).thenReturn(mockResponse);
        when(mockResponse.readEntity(String.class)).thenReturn(instanceId);
        final Ec2InstanceDataAccessor ec2InstanceDataAccessor = new Ec2InstanceDataAccessor(mockHttpClient);

        // When
        final String actualInstanceId = ec2InstanceDataAccessor.getInstanceId();

        // Then
        assertEquals(instanceId, actualInstanceId);
    }
}
