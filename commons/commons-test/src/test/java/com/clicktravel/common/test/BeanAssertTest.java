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

import org.junit.Assert;
import org.junit.Test;

import com.clicktravel.common.test.BeanAssert;
import com.clicktravel.common.test.bean.*;

public class BeanAssertTest {

    @Test
    public void shouldAssertValid_withValidBeanClass() {
        // Given
        final Class<?> beanClass = Bean.class;

        // When
        Exception notExpectedException = null;
        try {
            BeanAssert.assertValid(beanClass);
        } catch (final Exception e) {
            notExpectedException = e;
        }

        // Then
        Assert.assertNull(notExpectedException);
    }

    @Test
    public void shouldAssertValid_withValidBeanWithPrimitivesClass() {
        // Given
        final Class<?> beanClass = PrimitiveBean.class;

        // When
        Exception notExpectedException = null;
        try {
            BeanAssert.assertValid(beanClass);
        } catch (final Exception e) {
            notExpectedException = e;
        }

        // Then
        Assert.assertNull(notExpectedException);
    }

    @Test
    public void shouldNotAssertValid_withoutNoArgConstrcutor() {
        // Given
        final Class<?> beanClass = BadConstructorBean.class;

        // When
        AssertionError expectedException = null;
        try {
            BeanAssert.assertValid(beanClass);
        } catch (final AssertionError e) {
            expectedException = e;
        }

        // Then
        Assert.assertNotNull(expectedException);

    }

    @Test
    public void shouldNotAssertValid_withInvalidGetterSetterPairsMethod() {
        // Given
        final Class<?> beanClass = InvalidGettersAndSettersPairsBean.class;

        // When
        AssertionError expectedException = null;
        try {
            BeanAssert.assertValid(beanClass);
        } catch (final AssertionError e) {
            expectedException = e;
        }

        // Then
        Assert.assertNotNull(expectedException);
    }

    @Test
    public void shouldNotAssertValid_withInvalidGetterSetterBehaviour() {
        // Given
        final Class<?> beanClass = InvalidGettersAndSettersBean.class;

        // When
        AssertionError expectedException = null;
        try {
            BeanAssert.assertValid(beanClass);
        } catch (final AssertionError e) {
            expectedException = e;
        }

        // Then
        Assert.assertNotNull(expectedException);
    }

    @Test
    public void shouldNotAssertValid_withProtectedGetterSetters() {
        // Given
        final Class<?> beanClass = ProtectedGettersAndSettersBean.class;

        // When
        AssertionError expectedException = null;
        try {
            BeanAssert.assertValid(beanClass);
        } catch (final AssertionError e) {
            expectedException = e;
        }

        // Then
        Assert.assertNotNull(expectedException);
    }

    @Test
    public void shouldNotAssertValid_withIncorrectEqualsMethod() {
        // Given
        final Class<?> beanClass = InvalidEqualsMethodBean.class;

        // When
        AssertionError expectedException = null;
        try {
            BeanAssert.assertValid(beanClass);
        } catch (final AssertionError e) {
            expectedException = e;
        }

        // Then
        Assert.assertNotNull(expectedException);
    }

    @Test
    public void shouldNotAssertValid_withIncorrectHashCodeMethod() {
        // Given
        final Class<?> beanClass = InvalidHashCodeMethodBean.class;

        // When
        AssertionError expectedException = null;
        try {
            BeanAssert.assertValid(beanClass);
        } catch (final AssertionError e) {
            expectedException = e;
        }

        // Then
        Assert.assertNotNull(expectedException);
    }

}
