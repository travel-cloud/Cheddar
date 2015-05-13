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
package com.clicktravel.cheddar.server.http.filter.charset;

import static com.clicktravel.common.random.Randoms.randomString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;

@SuppressWarnings("unchecked")
public class CharsetResponseFilterTest {

    @Test
    public void shouldAddCharsetParameter_onFilter() throws Exception {
        // Given
        final ContainerRequestContext mockContainerRequestContext = mock(ContainerRequestContext.class);
        final ContainerResponseContext mockContainerResponseContext = mock(ContainerResponseContext.class);
        final MediaType mockMediaType = mock(MediaType.class);
        when(mockContainerResponseContext.getMediaType()).thenReturn(mockMediaType);
        final MediaType mockTypeWithCharset = mock(MediaType.class);
        when(mockMediaType.withCharset("utf-8")).thenReturn(mockTypeWithCharset);
        final HashMap<String, String> parameters = new HashMap<String, String>();
        when(mockMediaType.getParameters()).thenReturn(parameters);
        final MultivaluedMap<String, Object> mockHeadersMap = mock(MultivaluedMap.class);
        when(mockContainerResponseContext.getHeaders()).thenReturn(mockHeadersMap);
        final CharsetResponseFilter charsetResponseFilter = new CharsetResponseFilter();

        // When
        charsetResponseFilter.filter(mockContainerRequestContext, mockContainerResponseContext);

        // Then
        verify(mockHeadersMap).putSingle(HttpHeaders.CONTENT_TYPE, mockTypeWithCharset);
    }

    @Test
    public void shouldNotOverrideCharsetParameter_onFilter() throws Exception {
        // Given
        final ContainerRequestContext mockContainerRequestContext = mock(ContainerRequestContext.class);
        final ContainerResponseContext mockContainerResponseContext = mock(ContainerResponseContext.class);
        final MediaType mockMediaType = mock(MediaType.class);
        when(mockContainerResponseContext.getMediaType()).thenReturn(mockMediaType);
        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put(MediaType.CHARSET_PARAMETER, randomString(10));
        when(mockMediaType.getParameters()).thenReturn(parameters);
        final MultivaluedMap<String, Object> mockHeadersMap = mock(MultivaluedMap.class);
        when(mockContainerResponseContext.getHeaders()).thenReturn(mockHeadersMap);
        final CharsetResponseFilter charsetResponseFilter = new CharsetResponseFilter();

        // When
        charsetResponseFilter.filter(mockContainerRequestContext, mockContainerResponseContext);

        // Then
        verifyZeroInteractions(mockHeadersMap);
    }

    @Test
    public void shouldNotSetContentTypeOnResponseWithoutMediaType_onFilter() throws Exception {
        // Given
        final ContainerRequestContext mockContainerRequestContext = mock(ContainerRequestContext.class);
        final ContainerResponseContext mockContainerResponseContext = mock(ContainerResponseContext.class);
        when(mockContainerResponseContext.getMediaType()).thenReturn(null);
        final MultivaluedMap<String, Object> mockHeadersMap = mock(MultivaluedMap.class);
        when(mockContainerResponseContext.getHeaders()).thenReturn(mockHeadersMap);
        final CharsetResponseFilter charsetResponseFilter = new CharsetResponseFilter();

        // When
        charsetResponseFilter.filter(mockContainerRequestContext, mockContainerResponseContext);

        // Then
        verifyZeroInteractions(mockHeadersMap);
    }
}
