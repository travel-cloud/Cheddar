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

import static com.clicktravel.common.random.Randoms.randomEnumSet;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Set;

import org.junit.Test;

public class EnumToNameCollectionMapperTest {

    private enum StubEnum {
        ENUM1,
        ENUM2,
        ENUM3
    }

    @Test
    public void shouldMapEnumCollectionToString_withStubEnum() {
        // Given
        final Set<StubEnum> enumSet = randomEnumSet(StubEnum.class);
        final EnumToNameCollectionMapper<StubEnum> collectionMapper = new EnumToNameCollectionMapperStub();

        // When
        final Collection<String> enumNames = collectionMapper.map(enumSet);

        // Then
        assertEquals(StubEnum.class, collectionMapper.getEnumClass());
        assertEquals(enumSet.size(), enumNames.size());
        for (final StubEnum stubEnum : enumSet) {
            enumNames.contains(stubEnum.name());
        }
    }

    private class EnumToNameCollectionMapperStub extends EnumToNameCollectionMapper<StubEnum> {

        @Override
        public Class<StubEnum> getEnumClass() {
            return StubEnum.class;
        }
    }

}