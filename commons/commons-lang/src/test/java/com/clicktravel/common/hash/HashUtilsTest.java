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
package com.clicktravel.common.hash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.clicktravel.common.hash.HashUtils;

public class HashUtilsTest {

    private static final String NULL_ARG_SECRET_SHA = "bfc9bde19df9f91546b5b7bc97d5ede901c1976c6924c4a4268b105ebc54b4af09d2dc7b4505aaf7101ca7b3a5106d5d8a3586d6cdaf642700cd9b1d1dad5537";
    private static final String STRING_ARG_SECRET_SHA = "98df19e7c781618d048c1862cd25b04b9bfcf25ae8e6149b123bef71bfbfb1e1eee28f8b9d266fe82a77ed41051e6d3368e03798d169407501f6d3df006c781b";
    private static final String LIST_ARG_SECRET_SHA = "676e608899ca75912ea2154e566e50ccd9442d0197334a7b8d77cdc48dd2bc7ae7b3bd48b98cb776d073892a75ea4c90c3dc2b885dc4c40abb57aec4e6dc6c62";
    private static final String MIXED_ARG_SECRET_SHA = "603040cac95107d3a104d17dcb7da8f79834c4cfbbe2ff841cfc6c167bc11d8d1c2d125fffd11bb5256fa655abd0fb9c6006836bf279c11ca559e74bd3412d9d";

    private static final String NULL_ARG_SECRET_MD5 = "b99834bc19bbad24580b3adfa04fb947";
    private static final String STRING_ARG_SECRET_MD5 = "a119c68217b9c03becae7b1fe4d205dd";
    private static final String LIST_ARG_SECRET_MD5 = "7f8ee43ba269ac2cc8c50f51031d248e";
    private static final String MIXED_ARG_SECRET_MD5 = "3cab15dd668c23a10ae4ffff309b5d5b";

    @Test
    public void shouldGenerateShaHash_noArgs() {
        // Given

        // When
        final String hash = HashUtils.generateShaHash();

        // Then
        assertNotNull(hash);
        assertEquals(NULL_ARG_SECRET_SHA, hash);
    }

    @Test
    public void shouldGenerateShaHash_singleStringArg() {
        // Given
        final String arg1 = "test";

        // When
        final String hash = HashUtils.generateShaHash(arg1);

        // Then
        assertNotNull(hash);
        assertEquals(STRING_ARG_SECRET_SHA, hash);

    }

    @Test
    public void shouldGenerateShaHash_singleListArg() {
        // Given
        final List<String> arg1 = new ArrayList<String>(Arrays.asList("test1", "test2", "test3"));

        // When
        final String hash = HashUtils.generateShaHash(arg1);

        // Then
        assertNotNull(hash);
        assertEquals(LIST_ARG_SECRET_SHA, hash);

    }

    @Test
    public void shouldGenerateShaHash_mixedArg() {
        // Given
        final String arg1 = "test";
        final List<String> arg2 = new ArrayList<String>(Arrays.asList("test1", "test2", "test3"));

        // When
        final String hash = HashUtils.generateShaHash(arg1, arg2);

        // Then
        assertNotNull(hash);
        assertEquals(MIXED_ARG_SECRET_SHA, hash);

    }

    @Test
    public void shouldGenerateMd5Hash_noArgs() {
        // Given

        // When
        final String hash = HashUtils.generateMd5Hash();

        // Then
        assertNotNull(hash);
        assertEquals(NULL_ARG_SECRET_MD5, hash);
    }

    @Test
    public void shouldGenerateMd5Hash_singleStringArg() {
        // Given
        final String arg1 = "test";

        // When
        final String hash = HashUtils.generateMd5Hash(arg1);

        // Then
        assertNotNull(hash);
        assertEquals(STRING_ARG_SECRET_MD5, hash);

    }

    @Test
    public void shouldGenerateMd5Hash_singleListArg() {
        // Given
        final List<String> arg1 = new ArrayList<String>(Arrays.asList("test1", "test2", "test3"));

        // When
        final String hash = HashUtils.generateMd5Hash(arg1);

        // Then
        assertNotNull(hash);
        assertEquals(LIST_ARG_SECRET_MD5, hash);

    }

    @Test
    public void shouldGenerateMd5Hash_mixedArg() {
        // Given
        final String arg1 = "test";
        final List<String> arg2 = new ArrayList<String>(Arrays.asList("test1", "test2", "test3"));

        // When
        final String hash = HashUtils.generateMd5Hash(arg1, arg2);

        // Then
        assertNotNull(hash);
        assertEquals(MIXED_ARG_SECRET_MD5, hash);

    }

}
