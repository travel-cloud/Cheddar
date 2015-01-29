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
package com.clicktravel.cheddar.server.http.filter.status;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;

import org.junit.Test;

import com.clicktravel.cheddar.server.application.status.RestAdapterStatusHolder;

public class RestAdapterStatusResponseFilterTest {

    @Test
    public void shouldInvokeProcessingFinished_onFilter() throws Exception {
        // Given
        final RestAdapterStatusHolder mockRestAdapterStatusHolder = mock(RestAdapterStatusHolder.class);
        final RestAdapterStatusResponseFilter restAdapterStatusResponseFilter = new RestAdapterStatusResponseFilter(
                mockRestAdapterStatusHolder);
        final ContainerRequestContext mockRequestContext = mock(ContainerRequestContext.class);
        final ContainerResponseContext mockResponseContext = mock(ContainerResponseContext.class);

        // When
        restAdapterStatusResponseFilter.filter(mockRequestContext, mockResponseContext);

        // Then
        verify(mockRestAdapterStatusHolder).requestProcessingFinished();
    }

}
