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
package com.amazonaws.services.dynamodbv2.datamodeling.unmarshallers;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.*;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBReflectorUtil;
import com.amazonaws.services.dynamodbv2.datamodeling.S3ClientCache;
import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.util.DateUtils;

public final class DynamoDBUnmarshallerUtil {

    public static <T> SUnmarshaller getCustomSUnmarshaller(final T toReturn, final Method getter) {
        return new SUnmarshaller() {
            @Override
            public Object unmarshall(final AttributeValue value) {
                return DynamoDBReflectorUtil.getCustomMarshalledValue(toReturn, getter, value);
            }
        };
    }

    public static NSUnmarshaller getDoubleNSUnmarshaller() {
        return new NSUnmarshaller() {
            @Override
            public Object unmarshall(final AttributeValue value) {
                final Set<Double> argument = new HashSet<Double>();
                for (final String s : value.getNS()) {
                    argument.add(Double.parseDouble(s));
                }
                return argument;
            }
        };
    }

    public static NUnmarshaller getDoubleNUnmarshaller() {
        return new NUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) {
                return Double.parseDouble(value.getN());
            }
        };
    }

    public static NSUnmarshaller getBigDecimalNSUnmarshaller() {
        return new NSUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) {
                final Set<BigDecimal> argument = new HashSet<BigDecimal>();
                for (final String s : value.getNS()) {
                    argument.add(new BigDecimal(s));
                }
                return argument;
            }
        };
    }

    public static NUnmarshaller getBigDecimalNUnmarshaller() {
        return new NUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) {
                return new BigDecimal(value.getN());
            }
        };
    }

    public static NSUnmarshaller getBigIntegerNSUnmarshaller() {
        return new NSUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) {
                final Set<BigInteger> argument = new HashSet<BigInteger>();
                for (final String s : value.getNS()) {
                    argument.add(new BigInteger(s));
                }
                return argument;
            }
        };
    }

    public static NUnmarshaller getBigIntegerNUnmarshaller() {
        return new NUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) {
                return new BigInteger(value.getN());
            }
        };
    }

    public static NSUnmarshaller getIntegerNSUnmarshaller() {
        return new NSUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) {
                final Set<Integer> argument = new HashSet<Integer>();
                for (final String s : value.getNS()) {
                    argument.add(Integer.parseInt(s));
                }
                return argument;
            }
        };
    }

    public static NUnmarshaller getIntegerNUnmarshaller() {
        return new NUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) {
                return Integer.parseInt(value.getN());
            }
        };
    }

    public static NSUnmarshaller getFloatNSUnmarshaller() {
        return new NSUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) {
                final Set<Float> argument = new HashSet<Float>();
                for (final String s : value.getNS()) {
                    argument.add(Float.parseFloat(s));
                }
                return argument;
            }
        };
    }

    public static NUnmarshaller getFloatNUnmarshaller() {
        return new NUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) {
                return Float.parseFloat(value.getN());
            }
        };
    }

    public static NSUnmarshaller getByteNSUnmarshaller() {
        return new NSUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) {
                final Set<Byte> argument = new HashSet<Byte>();
                for (final String s : value.getNS()) {
                    argument.add(Byte.parseByte(s));
                }
                return argument;
            }
        };
    }

    public static NUnmarshaller getByteNUnmarshaller() {
        return new NUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) {
                return Byte.parseByte(value.getN());
            }
        };
    }

    public static NSUnmarshaller getLongNSUnmarshaller() {
        return new NSUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) {
                final Set<Long> argument = new HashSet<Long>();
                for (final String s : value.getNS()) {
                    argument.add(Long.parseLong(s));
                }
                return argument;
            }
        };
    }

    public static NUnmarshaller getLongNUnmarshaller() {
        return new NUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) {
                return Long.parseLong(value.getN());
            }
        };
    }

    public static NSUnmarshaller getShortNSUnmarshaller() {
        return new NSUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) {
                final Set<Short> argument = new HashSet<Short>();
                for (final String s : value.getNS()) {
                    argument.add(Short.parseShort(s));
                }
                return argument;
            }
        };
    }

    public static NUnmarshaller getShortNUnmarshaller() {
        return new NUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) {
                return Short.parseShort(value.getN());
            }
        };
    }

    public static NSUnmarshaller getBooleanNSUnmarshaller() {
        return new NSUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) {
                final Set<Boolean> argument = new HashSet<Boolean>();
                for (final String s : value.getNS()) {
                    argument.add(parseBoolean(s));
                }
                return argument;
            }
        };
    }

    public static NUnmarshaller getBooleanNUnmarshaller() {
        return new NUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) {
                return parseBoolean(value.getN());
            }
        };
    }

    public static SSUnmarshaller getDateSSUnmarshaller() {
        return new SSUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) throws ParseException {
                final Set<Date> argument = new HashSet<Date>();
                for (final String s : value.getSS()) {
                    argument.add(DateUtils.parseISO8601Date(s));
                }
                return argument;
            }
        };
    }

    public static SUnmarshaller getDateSUnmarshaller() {
        return new SUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) throws ParseException {
                return DateUtils.parseISO8601Date(value.getS());
            }
        };
    }

    public static SSUnmarshaller getCalendarSSUnmarshaller() {
        return new SSUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) throws ParseException {
                final Set<Calendar> argument = new HashSet<Calendar>();
                for (final String s : value.getSS()) {
                    final Calendar cal = GregorianCalendar.getInstance();
                    cal.setTime(DateUtils.parseISO8601Date(s));
                    argument.add(cal);
                }
                return argument;
            }
        };
    }

    public static SUnmarshaller getCalendarSUnmarshaller() {
        return new SUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) throws ParseException {
                final Calendar cal = GregorianCalendar.getInstance();
                cal.setTime(DateUtils.parseISO8601Date(value.getS()));
                return cal;
            }
        };
    }

    public static BSUnmarshaller getByteBufferBSUnmarshaller() {
        return new BSUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) throws ParseException {
                final Set<ByteBuffer> argument = new HashSet<ByteBuffer>();
                for (final ByteBuffer b : value.getBS()) {
                    argument.add(b);
                }
                return argument;
            }
        };
    }

    public static BUnmarshaller getByteBufferBUnmarshaller() {
        return new BUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) throws ParseException {
                return value.getB();
            }
        };
    }

    public static BSUnmarshaller getByteArrayBSUnmarshaller() {
        return new BSUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) throws ParseException {
                final Set<byte[]> argument = new HashSet<byte[]>();
                for (final ByteBuffer b : value.getBS()) {
                    byte[] bytes = null;
                    if (b.hasArray()) {
                        bytes = b.array();
                    } else {
                        bytes = new byte[b.limit()];
                        b.get(bytes, 0, bytes.length);
                    }
                    argument.add(bytes);
                }
                return argument;
            }
        };
    }

    public static BUnmarshaller getByteArrayBUnmarshaller() {
        return new BUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) throws ParseException {
                final ByteBuffer byteBuffer = value.getB();
                byte[] bytes = null;
                if (byteBuffer.hasArray()) {
                    bytes = byteBuffer.array();
                } else {
                    bytes = new byte[byteBuffer.limit()];
                    byteBuffer.get(bytes, 0, bytes.length);
                }
                return bytes;
            }
        };
    }

    public static SUnmarshaller getS3LinkSUnmarshaller(final S3ClientCache s3cc) {
        return new SUnmarshaller() {
            @Override
            public Object unmarshall(final AttributeValue value) {
                if (s3cc == null) {
                    throw new IllegalStateException("Mapper must be constructed with S3 AWS Credentials to load S3Link");
                }
                // value should never be null
                final String json = value.getS();
                return S3Link.fromJson(s3cc, json);
            }
        };
    }

    public static SSUnmarshaller getStringSSUnmarshaller() {
        return new SSUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) {
                final Set<String> argument = new HashSet<String>();
                for (final String s : value.getSS()) {
                    argument.add(s);
                }
                return argument;
            }
        };
    }

    public static SUnmarshaller getStringSUnmarshaller() {
        return new SUnmarshaller() {

            @Override
            public Object unmarshall(final AttributeValue value) {
                return value.getS();
            }
        };

    }

    /**
     * Attempts to parse the string given as a boolean and return its value. Throws an exception if the value is
     * anything other than 0 or 1.
     */
    private static boolean parseBoolean(final String s) {
        if ("1".equals(s)) {
            return true;
        } else if ("0".equals(s)) {
            return false;
        } else {
            throw new IllegalArgumentException("Expected 1 or 0 for boolean value, was " + s);
        }
    }

}
