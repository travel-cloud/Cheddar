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
package com.clicktravel.common.test.bean;

import static com.clicktravel.common.random.Randoms.randomInt;

public class InvalidHashCodeMethodBean {

    private String property1;

    private String property2;

    public String getProperty1() {
        return property1;
    }

    public void setProperty1(final String property1) {
        this.property1 = property1;
    }

    public String getProperty2() {
        return property2;
    }

    public void setProperty2(final String property2) {
        this.property2 = property2;
    }

    @Override
    public int hashCode() {
        return randomInt(10);
    }

}
