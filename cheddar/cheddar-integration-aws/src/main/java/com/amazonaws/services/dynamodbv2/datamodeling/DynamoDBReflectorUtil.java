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
package com.amazonaws.services.dynamodbv2.datamodeling;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.amazonaws.services.dynamodbv2.datamodeling.ArgumentMarshaller.*;
import com.amazonaws.services.dynamodbv2.datamodeling.unmarshallers.DynamoDBUnmarshallerUtil;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.util.DateUtils;

public class DynamoDBReflectorUtil {

    private final Map<Method, ArgumentUnmarshaller> argumentUnmarshallerCache = new HashMap<Method, ArgumentUnmarshaller>();
    private final Map<Method, ArgumentMarshaller> argumentMarshallerCache = new HashMap<Method, ArgumentMarshaller>();

    /**
     * Marshalls the custom value given into the proper return type.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> T getCustomMarshalledValue(final T toReturn, final Method getter, final AttributeValue value) {
        final DynamoDBMarshalling annotation = ReflectionUtils.getAnnotationFromGetterOrField(getter,
                DynamoDBMarshalling.class);
        final Class<? extends DynamoDBMarshaller<? extends Object>> marshallerClass = annotation.marshallerClass();

        DynamoDBMarshaller marshaller;
        try {
            marshaller = marshallerClass.newInstance();
        } catch (final InstantiationException e) {
            throw new DynamoDBMappingException("Couldn't instantiate marshaller of class " + marshallerClass, e);
        } catch (final IllegalAccessException e) {
            throw new DynamoDBMappingException("Couldn't instantiate marshaller of class " + marshallerClass, e);
        }

        return (T) marshaller.unmarshall(getter.getReturnType(), value.getS());
    }

    /**
     * Returns the argument unmarshaller used to unmarshall the getter / setter pair given.
     * <p>
     * Determining how to unmarshall a response, especially a numeric one, requires checking it against all supported
     * types. This is expensive, so we cache a lookup table of getter method to argument unmarhsaller which can be
     * reused.
     *
     * @param toReturn The typed domain object being unmarshalled for the client
     * @param getter The getter method being considered
     * @param setter The corresponding setter method being considered
     */
    <T> ArgumentUnmarshaller getArgumentUnmarshaller(final T toReturn, final Method getter, final Method setter,
            final S3ClientCache s3cc) {
        synchronized (argumentUnmarshallerCache) {
            ArgumentUnmarshaller unmarshaller = argumentUnmarshallerCache.get(getter);
            if (unmarshaller != null) {
                return unmarshaller;
            }
            final Class<?>[] parameterTypes = setter.getParameterTypes();
            final Class<?> paramType = parameterTypes[0];
            if (parameterTypes.length != 1) {
                throw new DynamoDBMappingException("Expected exactly one agument to " + setter);
            }

            if (isCustomMarshaller(getter)) {
                unmarshaller = DynamoDBUnmarshallerUtil.getCustomSUnmarshaller(toReturn, getter);
            } else {
                unmarshaller = computeArgumentUnmarshaller(toReturn, getter, setter, paramType, s3cc);
            }
            argumentUnmarshallerCache.put(getter, unmarshaller);
            return unmarshaller;
        }
    }

    /**
     * Returns whether or not this getter has a custom marshaller
     */
    private boolean isCustomMarshaller(final Method getter) {
        return ReflectionUtils.getterOrFieldHasAnnotation(getter, DynamoDBMarshalling.class);
    }

    /**
     * Note this method is synchronized on {@link #argumentUnmarshallerCache} while being executed.
     */
    private <T> ArgumentUnmarshaller computeArgumentUnmarshaller(final T toReturn, final Method getter,
            final Method setter, Class<?> paramType, final S3ClientCache s3cc) {
        ArgumentUnmarshaller unmarshaller = null;
        // If we're dealing with a collection, we need to get the
        // underlying type out of it
        final boolean isCollection = Set.class.isAssignableFrom(paramType);
        if (isCollection) {
            final Type genericType = setter.getGenericParameterTypes()[0];
            if (genericType instanceof ParameterizedType) {
                if (((ParameterizedType) genericType).getActualTypeArguments()[0].toString().equals("byte[]")) {
                    paramType = byte[].class;
                } else {
                    paramType = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
                }
            }
        } else if (Collection.class.isAssignableFrom(paramType)) {
            throw new DynamoDBMappingException("Only java.util.Set collection types are permitted for "
                    + DynamoDBAttribute.class);
        }

        if (double.class.isAssignableFrom(paramType) || Double.class.isAssignableFrom(paramType)) {
            if (isCollection) {
                unmarshaller = DynamoDBUnmarshallerUtil.getDoubleNSUnmarshaller();
            } else {
                unmarshaller = DynamoDBUnmarshallerUtil.getDoubleNUnmarshaller();
            }
        } else if (BigDecimal.class.isAssignableFrom(paramType)) {
            if (isCollection) {
                unmarshaller = DynamoDBUnmarshallerUtil.getBigDecimalNSUnmarshaller();
            } else {
                unmarshaller = DynamoDBUnmarshallerUtil.getBigDecimalNUnmarshaller();
            }
        } else if (BigInteger.class.isAssignableFrom(paramType)) {
            if (isCollection) {
                unmarshaller = DynamoDBUnmarshallerUtil.getBigIntegerNSUnmarshaller();
            } else {
                unmarshaller = DynamoDBUnmarshallerUtil.getBigIntegerNUnmarshaller();
            }
        } else if (int.class.isAssignableFrom(paramType) || Integer.class.isAssignableFrom(paramType)) {
            if (isCollection) {
                unmarshaller = DynamoDBUnmarshallerUtil.getIntegerNSUnmarshaller();
            } else {
                unmarshaller = DynamoDBUnmarshallerUtil.getIntegerNUnmarshaller();
            }
        } else if (float.class.isAssignableFrom(paramType) || Float.class.isAssignableFrom(paramType)) {
            if (isCollection) {
                unmarshaller = DynamoDBUnmarshallerUtil.getFloatNSUnmarshaller();
            } else {
                unmarshaller = DynamoDBUnmarshallerUtil.getFloatNUnmarshaller();
            }
        } else if (byte.class.isAssignableFrom(paramType) || Byte.class.isAssignableFrom(paramType)) {
            if (isCollection) {
                unmarshaller = DynamoDBUnmarshallerUtil.getByteNSUnmarshaller();
            } else {
                unmarshaller = DynamoDBUnmarshallerUtil.getByteNUnmarshaller();
            }
        } else if (long.class.isAssignableFrom(paramType) || Long.class.isAssignableFrom(paramType)) {
            if (isCollection) {
                unmarshaller = DynamoDBUnmarshallerUtil.getLongNSUnmarshaller();
            } else {
                unmarshaller = DynamoDBUnmarshallerUtil.getLongNUnmarshaller();
            }
        } else if (short.class.isAssignableFrom(paramType) || Short.class.isAssignableFrom(paramType)) {
            if (isCollection) {
                unmarshaller = DynamoDBUnmarshallerUtil.getShortNSUnmarshaller();
            } else {
                unmarshaller = DynamoDBUnmarshallerUtil.getShortNUnmarshaller();
            }
        } else if (boolean.class.isAssignableFrom(paramType) || Boolean.class.isAssignableFrom(paramType)) {
            if (isCollection) {
                unmarshaller = DynamoDBUnmarshallerUtil.getBooleanNSUnmarshaller();
            } else {
                unmarshaller = DynamoDBUnmarshallerUtil.getBooleanNUnmarshaller();
            }
        } else if (Date.class.isAssignableFrom(paramType)) {
            if (isCollection) {
                unmarshaller = DynamoDBUnmarshallerUtil.getDateSSUnmarshaller();
            } else {
                unmarshaller = DynamoDBUnmarshallerUtil.getDateSUnmarshaller();
            }
        } else if (Calendar.class.isAssignableFrom(paramType)) {
            if (isCollection) {
                unmarshaller = DynamoDBUnmarshallerUtil.getCalendarSSUnmarshaller();
            } else {
                unmarshaller = DynamoDBUnmarshallerUtil.getCalendarSUnmarshaller();
            }
        } else if (DateTime.class.isAssignableFrom(paramType)) {
            if (isCollection) {
                unmarshaller = DynamoDBUnmarshallerUtil.getDateTimeSSUnmarshaller();
            } else {
                unmarshaller = DynamoDBUnmarshallerUtil.getDateTimeSUnmarshaller();
            }
        } else if (ByteBuffer.class.isAssignableFrom(paramType)) {
            if (isCollection) {
                unmarshaller = DynamoDBUnmarshallerUtil.getByteBufferBSUnmarshaller();
            } else {
                unmarshaller = DynamoDBUnmarshallerUtil.getByteBufferBUnmarshaller();
            }
        } else if (byte[].class.isAssignableFrom(paramType)) {
            if (isCollection) {
                unmarshaller = DynamoDBUnmarshallerUtil.getByteArrayBSUnmarshaller();
            } else {
                unmarshaller = DynamoDBUnmarshallerUtil.getByteArrayBUnmarshaller();
            }
        } else {
            unmarshaller = defaultArgumentUnmarshaller(paramType, isCollection, s3cc);
        }
        return unmarshaller;
    }

    /**
     * Note this method is synchronized on {@link #argumentUnmarshallerCache} while being executed.
     * @param paramType the parameter type or the element type if the parameter is a collection
     * @param isCollection true if the parameter is a collection; false otherwise.
     * @return the default unmarshaller
     */
    private ArgumentUnmarshaller defaultArgumentUnmarshaller(final Class<?> paramType, final boolean isCollection,
            final S3ClientCache s3cc) {
        if (S3Link.class.isAssignableFrom(paramType)) {
            if (isCollection) {
                throw new DynamoDBMappingException("Collection types are not permitted for " + S3Link.class);
            } else {
                return DynamoDBUnmarshallerUtil.getS3LinkSUnmarshaller(s3cc);
            }
        } else {
            if (!String.class.isAssignableFrom(paramType)) {
                throw new DynamoDBMappingException("Expected a String, but was " + paramType);
            } else {
                if (isCollection) {
                    return DynamoDBUnmarshallerUtil.getStringSSUnmarshaller();
                } else {
                    return DynamoDBUnmarshallerUtil.getStringSUnmarshaller();
                }
            }
        }
    }

    /**
     * Returns a marshaller that knows how to provide an AttributeValue for the result of the getter given.
     */
    ArgumentMarshaller getArgumentMarshaller(final Method getter) {
        synchronized (argumentMarshallerCache) {
            ArgumentMarshaller marshaller = argumentMarshallerCache.get(getter);
            if (marshaller != null) {
                return marshaller;
            }
            if (isCustomMarshaller(getter)) {
                // Custom marshaller always returns String attribute value.
                marshaller = new StringAttributeMarshaller() {
                    @Override
                    public AttributeValue marshall(final Object obj) {
                        return getCustomerMarshallerAttributeValue(getter, obj);
                    }
                };
            } else {
                marshaller = computeArgumentMarshaller(getter);
            }
            argumentMarshallerCache.put(getter, marshaller);
            return marshaller;
        }
    }

    /**
     * Returns an attribute value for the getter method with a custom marshaller. Directly returns null when the custom
     * marshaller returns a null String.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private AttributeValue getCustomerMarshallerAttributeValue(final Method getter, final Object getterReturnResult) {
        final DynamoDBMarshalling annotation = ReflectionUtils.getAnnotationFromGetterOrField(getter,
                DynamoDBMarshalling.class);
        final Class<? extends DynamoDBMarshaller<? extends Object>> marshallerClass = annotation.marshallerClass();

        DynamoDBMarshaller marshaller;
        try {
            marshaller = marshallerClass.newInstance();
        } catch (final InstantiationException e) {
            throw new DynamoDBMappingException("Failed to instantiate custom marshaller for class " + marshallerClass,
                    e);
        } catch (final IllegalAccessException e) {
            throw new DynamoDBMappingException("Failed to instantiate custom marshaller for class " + marshallerClass,
                    e);
        }
        final String stringValue = marshaller.marshall(getterReturnResult);

        if (stringValue == null) {
            return null;
        } else {
            return new AttributeValue().withS(stringValue);
        }
    }

    /**
     * Note this method is synchronized on {@link #argumentMarshallerCache} while being executed.
     */
    private ArgumentMarshaller computeArgumentMarshaller(final Method getter) {
        ArgumentMarshaller marshaller;
        Class<?> returnType = getter.getReturnType();
        if (Set.class.isAssignableFrom(returnType)) {
            final Type genericType = getter.getGenericReturnType();
            if (genericType instanceof ParameterizedType) {
                if (((ParameterizedType) genericType).getActualTypeArguments()[0].toString().equals("byte[]")) {
                    returnType = byte[].class;
                } else {
                    returnType = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
                }
            }

            if (Date.class.isAssignableFrom(returnType)) {
                marshaller = new StringSetAttributeMarshaller() {

                    @Override
                    public AttributeValue marshall(final Object obj) {
                        final List<String> timestamps = new LinkedList<String>();
                        for (final Object o : (Set<?>) obj) {
                            timestamps.add(DateUtils.formatISO8601Date((Date) o));
                        }
                        return new AttributeValue().withSS(timestamps);
                    }
                };
            } else if (Calendar.class.isAssignableFrom(returnType)) {
                marshaller = new StringSetAttributeMarshaller() {

                    @Override
                    public AttributeValue marshall(final Object obj) {
                        final List<String> timestamps = new LinkedList<String>();
                        for (final Object o : (Set<?>) obj) {
                            timestamps.add(DateUtils.formatISO8601Date(((Calendar) o).getTime()));
                        }
                        return new AttributeValue().withSS(timestamps);
                    }
                };
            } else if (DateTime.class.isAssignableFrom(returnType)) {
                marshaller = new StringSetAttributeMarshaller() {

                    private final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime();

                    @Override
                    public AttributeValue marshall(final Object obj) {
                        final List<String> timestamps = new LinkedList<String>();
                        for (final Object o : (Set<?>) obj) {
                            timestamps.add(dateTimeFormatter.print((DateTime) o));
                        }
                        return new AttributeValue().withSS(timestamps);
                    }
                };
            } else if (boolean.class.isAssignableFrom(returnType) || Boolean.class.isAssignableFrom(returnType)) {
                marshaller = new NumberSetAttributeMarshaller() {

                    @Override
                    public AttributeValue marshall(final Object obj) {
                        final List<String> booleanAttributes = new ArrayList<String>();
                        for (final Object b : (Set<?>) obj) {
                            if (b == null || !(Boolean) b) {
                                booleanAttributes.add("0");
                            } else {
                                booleanAttributes.add("1");
                            }
                        }
                        return new AttributeValue().withNS(booleanAttributes);
                    }
                };
            } else if (returnType.isPrimitive() || Number.class.isAssignableFrom(returnType)) {
                marshaller = new NumberSetAttributeMarshaller() {

                    @Override
                    public AttributeValue marshall(final Object obj) {
                        final List<String> attributes = new ArrayList<String>();
                        for (final Object o : (Set<?>) obj) {
                            attributes.add(String.valueOf(o));
                        }
                        return new AttributeValue().withNS(attributes);
                    }
                };
            } else if (ByteBuffer.class.isAssignableFrom(returnType)) {
                marshaller = new BinarySetAttributeMarshaller() {

                    @Override
                    public AttributeValue marshall(final Object obj) {
                        final List<ByteBuffer> attributes = new ArrayList<ByteBuffer>();
                        for (final Object o : (Set<?>) obj) {
                            attributes.add((ByteBuffer) o);
                        }
                        return new AttributeValue().withBS(attributes);
                    }
                };
            } else if (byte[].class.isAssignableFrom(returnType)) {
                marshaller = new BinarySetAttributeMarshaller() {

                    @Override
                    public AttributeValue marshall(final Object obj) {
                        final List<ByteBuffer> attributes = new ArrayList<ByteBuffer>();
                        for (final Object o : (Set<?>) obj) {
                            attributes.add(ByteBuffer.wrap((byte[]) o));
                        }
                        return new AttributeValue().withBS(attributes);
                    }
                };
            } else {
                // subclass may extend the behavior by overriding the
                // defaultCollectionArgumentMarshaller method
                marshaller = defaultCollectionArgumentMarshaller(returnType);
            }
        } else if (Collection.class.isAssignableFrom(returnType)) {
            throw new DynamoDBMappingException("Non-set collections aren't supported: "
                    + (getter.getDeclaringClass() + "." + getter.getName()));
        } else { // Non-set return type
            if (Date.class.isAssignableFrom(returnType)) {
                marshaller = new StringAttributeMarshaller() {

                    @Override
                    public AttributeValue marshall(final Object obj) {
                        return new AttributeValue().withS(DateUtils.formatISO8601Date((Date) obj));
                    }
                };
            } else if (Calendar.class.isAssignableFrom(returnType)) {
                marshaller = new StringAttributeMarshaller() {

                    @Override
                    public AttributeValue marshall(final Object obj) {
                        return new AttributeValue().withS(DateUtils.formatISO8601Date(((Calendar) obj).getTime()));
                    }
                };
            } else if (DateTime.class.isAssignableFrom(returnType)) {
                marshaller = new StringAttributeMarshaller() {

                    private final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime();

                    @Override
                    public AttributeValue marshall(final Object obj) {
                        return new AttributeValue().withS(dateTimeFormatter.print((DateTime) obj));
                    }
                };
            } else if (boolean.class.isAssignableFrom(returnType) || Boolean.class.isAssignableFrom(returnType)) {
                marshaller = new NumberAttributeMarshaller() {

                    @Override
                    public AttributeValue marshall(final Object obj) {
                        if (obj == null || !(Boolean) obj) {
                            return new AttributeValue().withN("0");
                        } else {
                            return new AttributeValue().withN("1");
                        }
                    }
                };
            } else if (returnType.isPrimitive() || Number.class.isAssignableFrom(returnType)) {
                marshaller = new NumberAttributeMarshaller() {

                    @Override
                    public AttributeValue marshall(final Object obj) {
                        return new AttributeValue().withN(String.valueOf(obj));
                    }
                };
            } else if (returnType == String.class) {
                marshaller = new StringAttributeMarshaller() {

                    @Override
                    public AttributeValue marshall(final Object obj) {
                        if (((String) obj).length() == 0) {
                            return null;
                        }
                        return new AttributeValue().withS(String.valueOf(obj));
                    }
                };
            } else if (returnType == ByteBuffer.class) {
                marshaller = new BinaryAttributeMarshaller() {

                    @Override
                    public AttributeValue marshall(final Object obj) {
                        return new AttributeValue().withB((ByteBuffer) obj);
                    }
                };
            } else if (returnType == byte[].class) {
                marshaller = new BinaryAttributeMarshaller() {

                    @Override
                    public AttributeValue marshall(final Object obj) {
                        return new AttributeValue().withB(ByteBuffer.wrap((byte[]) obj));
                    }
                };
            } else {
                marshaller = defaultArgumentMarshaller(returnType, getter);
            }
        }
        return marshaller;
    }

    /**
     * Note this method is synchronized on {@link #argumentMarshallerCache} while being executed.
     * @param returnElementType the element of the return type which is known to be a collection
     * @return the default argument marshaller for a collection
     */
    private ArgumentMarshaller defaultCollectionArgumentMarshaller(final Class<?> returnElementType) {
        if (S3Link.class.isAssignableFrom(returnElementType)) {
            throw new DynamoDBMappingException("Collection types not permitted for " + S3Link.class);
        } else {
            return new StringSetAttributeMarshaller() {
                @Override
                public AttributeValue marshall(final Object obj) {
                    final List<String> attributes = new ArrayList<String>();
                    for (final Object o : (Set<?>) obj) {
                        attributes.add(String.valueOf(o));
                    }
                    return new AttributeValue().withSS(attributes);
                }
            };
        }
    }

    /**
     * Note this method is synchronized on {@link #argumentMarshallerCache} while being executed.
     * @param returnType the return type
     * @return the default argument marshaller
     */
    private ArgumentMarshaller defaultArgumentMarshaller(final Class<?> returnType, final Method getter) {
        if (returnType == S3Link.class) {
            return new StringAttributeMarshaller() {
                @Override
                public AttributeValue marshall(final Object obj) {
                    final S3Link s3link = (S3Link) obj;
                    if (s3link.getBucketName() == null || s3link.getKey() == null) {
                        // insufficient S3 resource specification
                        return null;
                    }
                    final String json = s3link.toJson();
                    return new AttributeValue().withS(json);
                }
            };
        } else {
            throw new DynamoDBMappingException("Unsupported type: " + returnType + " for " + getter);
        }
    }
}
