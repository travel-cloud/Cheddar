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
package com.clicktravel.infrastructure.persistence.aws.cloudsearch.client;

import java.util.List;

import org.joda.time.DateTime;

import com.clicktravel.cheddar.infrastructure.persistence.document.search.Document;

public class StubDocument implements Document {

    private String id;
    private String stringValue;
    private int intValue;
    private double doubleValue;
    private DateTime dateTimeValue;
    private List<String> listString;
    private List<Integer> listInteger;
    private List<Double> listDouble;
    private List<DateTime> listDateTime;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(final String id) {
        this.id = id;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(final String stringValue) {
        this.stringValue = stringValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(final int intValue) {
        this.intValue = intValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(final double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public DateTime getDateTimeValue() {
        return dateTimeValue;
    }

    public void setDateTimeValue(final DateTime dateTimeValue) {
        this.dateTimeValue = dateTimeValue;
    }

    public List<Integer> getListInteger() {
        return listInteger;
    }

    public void setListInteger(final List<Integer> listInteger) {
        this.listInteger = listInteger;
    }

    public List<Double> getListDouble() {
        return listDouble;
    }

    public void setListDouble(final List<Double> listDouble) {
        this.listDouble = listDouble;
    }

    public List<DateTime> getListDateTime() {
        return listDateTime;
    }

    public void setListDateTime(final List<DateTime> listDateTime) {
        this.listDateTime = listDateTime;
    }

    public List<String> getListString() {
        return listString;
    }

    public void setListString(final List<String> listString) {
        this.listString = listString;
    }

}
