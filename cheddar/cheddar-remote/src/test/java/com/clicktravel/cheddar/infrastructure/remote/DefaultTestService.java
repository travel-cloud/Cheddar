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
package com.clicktravel.cheddar.infrastructure.remote;

import com.clicktravel.cheddar.remote.FailImmediatelyOnException;

public class DefaultTestService implements TestService {

    private boolean method1Called;
    private boolean method2Called;
    private String[] method2ReturnValue;
    private RuntimeException exceptionToThrow;

    @Override
    public void method1(final String parameter1, final int parameter2) {
        method1Called = true;
        if (exceptionToThrow != null) {
            throw exceptionToThrow;
        }
    }

    @FailImmediatelyOnException({ TestBaseException.class })
    @Override
    public String[] method2(final TestObject testObject) {
        method2Called = true;
        if (exceptionToThrow != null) {
            throw exceptionToThrow;
        } else {
            return method2ReturnValue;
        }
    }

    public void setMethod2ReturnValue(final String[] method2ReturnValue) {
        this.method2ReturnValue = method2ReturnValue;
    }

    public boolean isMethod1Called() {
        return method1Called;
    }

    public boolean isMethod2Called() {
        return method2Called;
    }

    public void setExceptionToThrow(final RuntimeException exceptionToThrow) {
        this.exceptionToThrow = exceptionToThrow;
    }

}
