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

public class ProtectedGettersAndSettersBean {

    private String stringProperty1;

    private String stringProperty2;

    public String getStringProperty1() {
        return stringProperty1;
    }

    protected void setStringProperty1(final String stringProperty1) {
        this.stringProperty1 = stringProperty1;
    }

    protected String getStringProperty2() {
        return stringProperty2;
    }

    public void setStringProperty2(final String stringProperty2) {
        this.stringProperty2 = stringProperty2;
    }

}