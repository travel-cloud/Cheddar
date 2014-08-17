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

public class PrimitiveBean {

    private byte byteProperty;
    private short shortProperty;
    private int intProperty;
    private long longProperty;
    private float floatProperty;
    private double doubleProperty;
    private boolean booleanProperty;
    private char charProperty;

    public byte getByteProperty() {
        return byteProperty;
    }

    public void setByteProperty(final byte byteProperty) {
        this.byteProperty = byteProperty;
    }

    public short getShortProperty() {
        return shortProperty;
    }

    public void setShortProperty(final short shortProperty) {
        this.shortProperty = shortProperty;
    }

    public int getIntProperty() {
        return intProperty;
    }

    public void setIntProperty(final int intProperty) {
        this.intProperty = intProperty;
    }

    public long getLongProperty() {
        return longProperty;
    }

    public void setLongProperty(final long longProperty) {
        this.longProperty = longProperty;
    }

    public float getFloatProperty() {
        return floatProperty;
    }

    public void setFloatProperty(final float floatProperty) {
        this.floatProperty = floatProperty;
    }

    public double getDoubleProperty() {
        return doubleProperty;
    }

    public void setDoubleProperty(final double doubleProperty) {
        this.doubleProperty = doubleProperty;
    }

    public boolean isBooleanProperty() {
        return booleanProperty;
    }

    public void setBooleanProperty(final boolean booleanProperty) {
        this.booleanProperty = booleanProperty;
    }

    public char getCharProperty() {
        return charProperty;
    }

    public void setCharProperty(final char charProperty) {
        this.charProperty = charProperty;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (booleanProperty ? 1231 : 1237);
        result = prime * result + byteProperty;
        result = prime * result + charProperty;
        long temp;
        temp = Double.doubleToLongBits(doubleProperty);
        result = prime * result + (int) (temp ^ temp >>> 32);
        result = prime * result + Float.floatToIntBits(floatProperty);
        result = prime * result + intProperty;
        result = prime * result + (int) (longProperty ^ longProperty >>> 32);
        result = prime * result + shortProperty;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PrimitiveBean other = (PrimitiveBean) obj;
        if (booleanProperty != other.booleanProperty) {
            return false;
        }
        if (byteProperty != other.byteProperty) {
            return false;
        }
        if (charProperty != other.charProperty) {
            return false;
        }
        if (Double.doubleToLongBits(doubleProperty) != Double.doubleToLongBits(other.doubleProperty)) {
            return false;
        }
        if (Float.floatToIntBits(floatProperty) != Float.floatToIntBits(other.floatProperty)) {
            return false;
        }
        if (intProperty != other.intProperty) {
            return false;
        }
        if (longProperty != other.longProperty) {
            return false;
        }
        if (shortProperty != other.shortProperty) {
            return false;
        }
        return true;
    }

}
