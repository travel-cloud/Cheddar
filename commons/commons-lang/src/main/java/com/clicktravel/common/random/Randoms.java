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
import java.util.regex.Pattern;

import org.joda.time.*;

public class Randoms {

    private static final String CHARSET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int MIN_RANDOM_STRING_LENGTH = 20;
    private static final int MAX_RANDOM_STRING_LENGTH = 40;
    private static final long DAY_MILLIS = 24 * 60 * 60 * 1000;
    private static final Pattern CARD_PREFIX_PATTERN = Pattern.compile("\\d+");

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
     * Returns a random enum present in the given enum Set.
     *
     * @param enumSet Set of enumerations to pick from.
     * @return Random enum from the given set.
     */
    public static <E extends Enum<E>> E randomEnumInSet(final Set<E> enumSet) {
        final int size = enumSet.size();
        final int positionToSelect = randomIntInRange(0, size);

        return enumSet.stream().skip((positionToSelect)).findFirst().get();
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
        return BigDecimal.valueOf(Randoms.randomInt(maxiumValue.intValue()), precision)
                .add(BigDecimal.valueOf(BigInteger.ONE.intValue(), precision));
    }

    /**
     * Return an int in the range <code>from</code> to <code>to</code> inclusive of from but exclusive of to e.g.
     * randomIntInRange(1,20) could return an int from 1 to 19 inclusive. Negative numbers are also supported by this
     * method e.g. randomIntInRange(-10,10) could return an int from -10 to 9 inclusive
     *
     * @param from the start of the range (inclusive)
     * @param to the end of the range (exclusive)
     *
     * @return a random int
     */
    public static int randomIntInRange(final int from, final int to) {
        int returnValue;
        if (from > to) {
            throw new IllegalArgumentException(String.format("From (%d) must be less than to (%d)", from, to));
        } else { // from <= to
            returnValue = from + Randoms.randomInt(to - from);
        }
        return returnValue;
    }

    /**
     * Generates a random valid credit card number.
     *
     * Code taken from a public Gist created by Josef Galea https://gist.github.com/josefeg/5781824
     *
     * @param prefix, used to identify the bank that is issuing the credit card, this should be a positive number
     *
     * @return A randomly generated, valid, credit card number.
     */
    public static String randomCreditCardNumber(final String prefix) {

        if (!CARD_PREFIX_PATTERN.matcher(prefix).matches()) {
            throw new IllegalArgumentException(String.format("Invalid card prefix: %s", prefix));
        }

        final int length = randomIntInRange(13, 19);
        // The number of random digits that we need to generate is equal to the
        // total length of the card number minus the start digits given by the
        // user, minus the check digit at the end.
        final int randomNumberLength = length - (prefix.length() + 1);

        final StringBuffer buffer = new StringBuffer(prefix);
        for (int i = 0; i < randomNumberLength; i++) {
            final int digit = Randoms.randomInt(10);
            buffer.append(digit);
        }

        // Do the Luhn algorithm to generate the check digit.
        final int checkDigit = getCreditCardCheckDigit(buffer.toString());
        buffer.append(checkDigit);

        return buffer.toString();
    }

    /**
     * Generates the check digit required to make the given credit card number valid (i.e. pass the Luhn check)
     *
     * Code taken from a public Gist created by Josef Galea https://gist.github.com/josefeg/5781824
     *
     * @param number The credit card number for which to generate the check digit.
     * @return The check digit required to make the given credit card number valid.
     */
    static int getCreditCardCheckDigit(final String number) {

        // Get the sum of all the digits, however we need to replace the value
        // of every other digit with the same digit multiplied by 2. If this
        // multiplication yields a number greater than 9, then add the two
        // digits together to get a single digit number.
        //
        // The digits we need to replace will be those in an even position for
        // card numbers whose length is an even number, or those is an odd
        // position for card numbers whose length is an odd number. This is
        // because the Luhn algorithm reverses the card number, and doubles
        // every other number starting from the second number from the last
        // position.
        int sum = 0;
        final int remainder = (number.length() + 1) % 2;
        for (int i = 0; i < number.length(); i++) {

            // Get the digit at the current position.
            int digit = Integer.parseInt(number.substring(i, (i + 1)));

            if ((i % 2) == remainder) {
                digit = digit * 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
        }

        // The check digit is the number required to make the sum a multiple of
        // 10.
        final int mod = sum % 10;
        final int checkDigit = ((mod == 0) ? 0 : 10 - mod);

        return checkDigit;
    }

    /**
     * @return A random id string. Sufficiently random to be treated as unique.
     */
    public static String randomId() {
        return UUID.randomUUID().toString();
    }

    /**
     * @param array
     * @return An item from the given array
     */
    public static <T> T randomItem(final T[] array) {
        return array[Randoms.randomInt(array.length)];
    }

    /**
     * @param collection
     * @return An item from the given collection
     */
    public static <T> T randomItem(final Collection<T> collection) {
        @SuppressWarnings("unchecked")
        final T[] collectionAsArray = (T[]) collection.toArray();
        return Randoms.randomItem(collectionAsArray);
    }

    private static Random newRandom() {
        final UUID uuid = UUID.randomUUID();
        return new Random(uuid.getMostSignificantBits() * uuid.getLeastSignificantBits());
    }

}
