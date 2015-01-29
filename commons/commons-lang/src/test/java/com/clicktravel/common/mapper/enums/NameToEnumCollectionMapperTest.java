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
package com.clicktravel.common.mapper.enums;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

public class NameToEnumCollectionMapperTest {

    private enum StubEnum {
        ENUM1,
        ENUM2,
        ENUM3
    }

    @Test
    public void shouldMapStringCollectionToEnum_withStubEnum() {
        // Given
        final Collection<String> enumNames = Arrays.asList("ENUM1", "ENUM2", "ENUM3");
        final NameToEnumCollectionMapper<StubEnum> collectionMapper = new NameToEnumCollectionMapper<StubEnum>(
                StubEnum.class);

        // When
        final Collection<StubEnum> enumSet = collectionMapper.map(enumNames);

        // Then
        assertEquals(enumNames.size(), enumSet.size());
        for (final StubEnum stubEnum : enumSet) {
            enumNames.contains(stubEnum.name());
        }
    }
}