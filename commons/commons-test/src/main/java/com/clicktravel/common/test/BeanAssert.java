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
package com.clicktravel.common.test;

import static com.clicktravel.common.random.Randoms.*;
import static org.mockito.Mockito.mock;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

import org.mockito.internal.util.collections.Sets;

import com.clicktravel.common.random.Randoms;

/**
 * Allows the testing of simple beans: no arg constructor and getter/setter pairs
 *
 * Ensures that equals and hash code are implemented properly. Only works for sub-set of property types defined in
 * SUPPORTED_PROPERTY_TYPES
 */
public class BeanAssert {

    public static void assertValid(final Class<?> beanClass) {
        assertValidNoArgConstructor(beanClass);
        assertValidPublicGetterSetterPairs(beanClass);
        assertValidPublicGetterSetterBehaviour(beanClass);
        assertValidEqualsAndHashCodeMethod(beanClass);
    }

    private static <T> void assertValidNoArgConstructor(final Class<T> beanClass) {
        try {
            beanClass.newInstance();
        } catch (final Exception e) {
            throw new AssertionError(e);
        }
    }

    private static <T> void assertValidPublicGetterSetterPairs(final Class<T> beanClass) {
        for (final PropertyDescriptor propertyDescriptor : getPropertyDescriptors(beanClass)) {
            final Method writeMethod = propertyDescriptor.getWriteMethod();
            final Method readMethod = propertyDescriptor.getReadMethod();
            if (writeMethod == null || readMethod == null) {
                throw new AssertionError("Invalid getter/setter pair for property: "
                        + propertyDescriptor.getDisplayName());
            }
            if (writeMethod.isAccessible()) {
                throw new AssertionError("Setter is not public");
            }
            if (readMethod.isAccessible()) {
                throw new AssertionError("Getter is not public");
            }
        }
    }

    private static <T> void assertValidPublicGetterSetterBehaviour(final Class<T> beanClass) {
        for (final PropertyDescriptor propertyDescriptor : getPropertyDescriptors(beanClass)) {

            final Method writeMethod = propertyDescriptor.getWriteMethod();
            final Method readMethod = propertyDescriptor.getReadMethod();
            final Class<?> propertyType = propertyDescriptor.getPropertyType();
            final Object propertyValue = getPropertyValue(propertyType);
            Object readPropertyValue = null;
            try {
                final T bean = beanClass.newInstance();
                writeMethod.invoke(bean, propertyValue);
                readPropertyValue = readMethod.invoke(bean);
            } catch (final Exception e) {
                throw new AssertionError(e);
            }
            if (!propertyValue.equals(readPropertyValue)) {
                throw new AssertionError("Read value is not same as the set value for property: "
                        + propertyDescriptor.getName());
            }

        }
    }

    private static Object getPropertyValue(final Class<?> propertyClass) {
        final Map<Class<?>, Object> supportedPropertyTypes = new HashMap<>();

        supportedPropertyTypes.put(Byte.class, (byte) randomInt(Byte.MAX_VALUE));
        supportedPropertyTypes.put(Short.class, (short) randomInt(Short.MAX_VALUE));
        supportedPropertyTypes.put(Integer.class, randomInt(Integer.MAX_VALUE));
        supportedPropertyTypes.put(Long.class, randomLong());
        supportedPropertyTypes.put(Float.class, randomFloat());
        supportedPropertyTypes.put(Double.class, randomDouble());
        supportedPropertyTypes.put(Boolean.class, randomBoolean());
        supportedPropertyTypes.put(Character.class, randomChar());
        supportedPropertyTypes.put(String.class, randomString(1000));
        supportedPropertyTypes.put(Date.class, new Date(randomDateTime().getMillis()));
        supportedPropertyTypes.put(Set.class, Sets.newSet(randomLong(), randomLong(), randomLong()));
        supportedPropertyTypes.put(BigDecimal.class, Randoms.randomBigDecimal(10000, randomInt(5)));

        if (supportedPropertyTypes.containsKey(propertyClass)) {
            return supportedPropertyTypes.get(propertyClass);
        }
        if (propertyClass.isPrimitive()) {
            if (byte.class.equals(propertyClass)) {
                return supportedPropertyTypes.get(Byte.class);
            } else if (short.class.equals(propertyClass)) {
                return supportedPropertyTypes.get(Short.class);
            } else if (int.class.equals(propertyClass)) {
                return supportedPropertyTypes.get(Integer.class);
            } else if (long.class.equals(propertyClass)) {
                return supportedPropertyTypes.get(Long.class);
            } else if (float.class.equals(propertyClass)) {
                return supportedPropertyTypes.get(Float.class);
            } else if (double.class.equals(propertyClass)) {
                return supportedPropertyTypes.get(Double.class);
            } else if (boolean.class.equals(propertyClass)) {
                return supportedPropertyTypes.get(Boolean.class);
            } else if (char.class.equals(propertyClass)) {
                return supportedPropertyTypes.get(Character.class);
            }
        }

        try {
            return mock(propertyClass);
        } catch (final Exception e) {
            throw new IllegalStateException("Cannot create random property for type: " + propertyClass, e);
        }

    }

    private static <T> void assertValidEqualsAndHashCodeMethod(final Class<T> beanClass) {
        T bean1 = null;
        T bean2 = null;
        try {
            bean1 = beanClass.newInstance();
            bean2 = beanClass.newInstance();
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
        for (final PropertyDescriptor propertyDescriptor : getPropertyDescriptors(beanClass)) {
            final Method writeMethod = propertyDescriptor.getWriteMethod();
            final Object startingPropertyValue = getPropertyValue(propertyDescriptor.getPropertyType());
            try {
                writeMethod.invoke(bean1, startingPropertyValue);
                writeMethod.invoke(bean2, startingPropertyValue);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
            Object newPropertyValue;
            do {
                newPropertyValue = getPropertyValue(propertyDescriptor.getPropertyType());
            } while (newPropertyValue.equals(startingPropertyValue));

            // Throw error if they are still equal after changing one
            try {
                writeMethod.invoke(bean1, newPropertyValue);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
            if (bean1.equals(bean2) || bean2.equals(bean1)) {
                throw new AssertionError("Equals method not properly implemented");
            }
            // Throw error if there are not now equal
            try {
                writeMethod.invoke(bean2, newPropertyValue);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
            if ((bean1.equals(bean2) && bean2.equals(bean1)) == false) {
                throw new AssertionError("Equals method not properly implemented (reverse)");
            }
            if (bean1.hashCode() != bean2.hashCode()) {
                throw new AssertionError("Hashcode method not properly implemented");
            }

        }

    }

    private static <T> Collection<PropertyDescriptor> getPropertyDescriptors(final Class<T> beanClass) {
        final Collection<PropertyDescriptor> propertyDescriptors = new HashSet<>();
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(beanClass);

        } catch (final Exception e) {
            throw new IllegalStateException("Cannot get BeanInfo");
        }
        for (final PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
            if (propertyDescriptor.getName().equals("class")) {
                continue;
            }
            propertyDescriptors.add(propertyDescriptor);
        }
        return propertyDescriptors;
    }
}