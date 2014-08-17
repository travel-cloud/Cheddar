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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    private enum HashType {

        MD5("MD5"),
        SHA_512("SHA-512");

        private final String value;

        private HashType(final String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

    }

    private static final String SEPARATOR = "|";

    public static String generateMd5Hash(final Object... args) {
        return generateHash(generateParameterKey(args), HashType.MD5);
    }

    public static String generateShaHash(final Object... args) {
        return generateHash(generateParameterKey(args), HashType.SHA_512);
    }

    private static String generateParameterKey(final Object[] args) {
        final StringBuilder sb = new StringBuilder();
        sb.append(SEPARATOR);
        for (final Object arg : args) {
            sb.append(arg);
            sb.append(SEPARATOR);
        }
        return sb.toString();
    }

    private static String generateHash(final String input, final HashType hashType) {
        try {
            final MessageDigest md = MessageDigest.getInstance(hashType.value());
            md.update(input.getBytes());
            final byte byteData[] = md.digest();
            final StringBuilder sb = new StringBuilder();
            for (final byte element : byteData) {
                final String hex = Integer.toHexString(0xff & element);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

}
