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
package com.clicktravel.common.mapper;

import static com.clicktravel.common.random.Randoms.randomInt;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

public class CollectionMapperTest {

    @Test
    public void shouldMap_withCollection() throws Exception {
        // Given
        final StubCollectionMapper collectionMapper = new StubCollectionMapper();
        final int itemCount = randomInt(10);
        final Object[] mockInputItems = new Object[itemCount];
        final Object[] mockOutputItems = new Object[itemCount];
        final Mapper<Object, Object> actualMapper = collectionMapper.mapper();
        final Collection<Object> inputCollection = new ArrayList<>();
        final Collection<Object> expectedOutputCollection = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            mockInputItems[i] = mock(Object.class);
            mockOutputItems[i] = mock(Object.class);
            when(actualMapper.map(mockInputItems[i])).thenReturn(mockOutputItems[i]);
            inputCollection.add(mockInputItems[i]);
            expectedOutputCollection.add(mockOutputItems[i]);
        }

        // When
        final Collection<Object> outputCollection = collectionMapper.map(inputCollection);

        // Then
        assertThat(outputCollection, is(expectedOutputCollection));
    }
}
