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
package com.clicktravel.common.random;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import org.joda.time.*;

public class Randoms {

    private static final String CHARSET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int MIN_RANDOM_STRING_LENGTH = 20;
    private static final int MAX_RANDOM_STRING_LENGTH = 40;
    private static final long DAY_MILLIS = 24 * 60 * 60 * 1000;

    /**
     * Returns a random string value of specified length
     * @param length Length of returned string
     * @return A string of random alphanumeric characters, possibly upper and lower case
     */
    public static String randomString(final int length) {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            sb.append(randomChar());
        }
        return sb.toString();
    }

    /**
     * Return a random character
     * @return
     */
    public static char randomChar() {
        final int pos = newRandom().nextInt(CHARSET.length());
        return CHARSET.charAt(pos);
    }

    /**
     * @return a random non-empty string value up to MAX_RANDOM_STRING_LENGTH characters in length
     */
    public static String randomString() {
        return randomString(MIN_RANDOM_STRING_LENGTH + randomInt(MAX_RANDOM_STRING_LENGTH - MIN_RANDOM_STRING_LENGTH));
    }

    /**
     * @return A random valid e-mail address. Sufficiently random to be treated as unique.
     */
    public static String randomEmailAddress() {
        final StringBuffer sb = new StringBuffer();
        sb.append(randomString(10));
        sb.append("@");
        sb.append(randomString(10));
        sb.append(".com");
        return sb.toString();
    }

    /**
     * @return A random valid phone number. Sufficiently random to be treated as unique.
     */
    public static String randomPhoneNumber() {
        final StringBuffer sb = new StringBuffer();
        if (randomBoolean()) {
            sb.append("+");
            sb.append(randomInt(100));
            sb.append(" ");
        }
        if (randomBoolean()) {
            sb.append(randomInt(1000));
            sb.append(" ");
            sb.append(randomInt(1000));
            sb.append(" ");
        }
        sb.append(randomInt(1000));
        sb.append(" ");
        sb.append(randomInt(1000));
        return sb.toString();
    }

    /**
     * @return A random date & time with a random time offset, between approximately year 1970 and year 6429. The time
     *         offset is between -12:00 and +12:00 and is rounded to a 15 minute division.
     */
    public static DateTime randomDateTime() {
        final long timeMillis = (randomLong() & 0x0000_7fff_ffff_ffffL) + DAY_MILLIS; // prevent underflow
        final int offsetTotalMinutes = 15 * (randomInt(96) - 48);
        final DateTimeZone dateTimeZone = DateTimeZone.forOffsetMillis(1000 * 60 * offsetTotalMinutes);
        return new DateTime(timeMillis, dateTimeZone);
    }

    /**
     * @return A random local date & time, between approximately year 1970 and year 6429.
     */
    public static LocalDateTime randomLocalDateTime() {
        return new LocalDateTime(randomDateTime());
    }

    /**
     * @return A random local date, between approximately year 1970 and year 6429.
     */
    public static LocalDate randomLocalDate() {
        return new LocalDate(randomDateTime());
    }

    /**
     * @return A random local time.
     */
    public static LocalTime randomLocalTime() {
        return new LocalTime(randomDateTime());
    }

    /**
     * @param enumClass Enumeration class
     * @return A random member of the specified enumeration
     */
    public static <E extends Enum<E>> E randomEnum(final Class<E> enumClass) {
        final E[] enumConstants = enumClass.getEnumConstants();
        final int randomIndex = randomInt(enumConstants.length);
        return enumConstants[randomIndex];
    }

    /**
     * Returns a set of random members of the specified enumeration. Each member of the enum has equal chance of being
     * included or not. There will always be at least one.
     * 
     * @param enumClass Enumeration class
     * @return Random enum set
     */
    public static <E extends Enum<E>> Set<E> randomEnumSet(final Class<E> enumClass) {
        final Set<E> randomEnumCollection = randomSubset(new HashSet<E>(Arrays.asList(enumClass.getEnumConstants())));
        randomEnumCollection.add(randomEnum(enumClass));
        return randomEnumCollection;
    }

    /**
     * Returns a subset of a specified original set. The original set is not modified by this method.
     * @param set Original set
     * @return A random subset (may be the empty set) of the specified original set. Each member of the original set has
     *         equal chance of being included or not in the returned subset.
     */
    public static <T> Set<T> randomSubset(final Set<T> set) {
        final Set<T> subset = new HashSet<>();
        for (final T member : set) {
            if (randomBoolean()) {
                subset.add(member);
            }
        }
        return subset;
    }

    /**
     * @param max Exclusive maximum value of returned value
     * @return A random integer value between 0 (inclusive) and specified maximum (exclusive)
     */
    public static int randomInt(final int max) {
        if (max == 0) {
            return 0;
        }
        return newRandom().nextInt(max);
    }

    /**
     * @param max Exclusive maximum value of returned value
     * @return A random floating point number between 0 and 1
     */
    public static float randomFloat() {
        return newRandom().nextFloat();
    }

    /**
     * @param max Exclusive maximum value of returned value
     * @return A random double-point precision point number between 0 and 1
     */
    public static double randomDouble() {
        return newRandom().nextDouble();
    }

    /**
     * @return A random long value. Sufficiently random to be treated as unique.
     */
    public static long randomLong() {
        return newRandom().nextLong();
    }

    /**
     * @return A random boolean value
     */
    public static boolean randomBoolean() {
        return newRandom().nextBoolean();
    }

    /**
     * @param maxValue - int value that represents the exclusive maximum for this random
     * @param precision - the precision of the BigDecimal
     * @return A random BigDecimal between 0 and the max value with precision
     */

    public static BigDecimal randomBigDecimal(final int maxValue, final int precision) {
        final BigInteger maxiumValue = BigInteger.valueOf(maxValue).multiply(BigInteger.TEN.pow(precision));
        return BigDecimal.valueOf(Randoms.randomInt(maxiumValue.intValue()), precision);
    }

    /**
     * @param maxValue - int value that represents the exclusive maximum for this random
     * @param precision - the precision of the BigDecimal
     * @return A random positive BigDecimal between the first positive value and the max value with precision
     */

    public static BigDecimal randomPositiveBigDecimal(final int maxValue, final int precision) {
        final BigInteger maxiumValue = BigInteger.valueOf(maxValue).multiply(BigInteger.TEN.pow(precision))
                .subtract(BigInteger.ONE);
        return BigDecimal.valueOf(Randoms.randomInt(maxiumValue.intValue()), precision).add(
                BigDecimal.valueOf(BigInteger.ONE.intValue(), precision));
    }

    /**
     * @return A random id string. Sufficiently random to be treated as unique.
     */
    public static String randomId() {
        return UUID.randomUUID().toString();
    }

    private static Random newRandom() {
        final UUID uuid = UUID.randomUUID();
        return new Random(uuid.getMostSignificantBits() * uuid.getLeastSignificantBits());
    }

}
