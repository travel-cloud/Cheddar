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
package com.clicktravel.common.http.application;

import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

@SuppressWarnings("serial")
public class DecimalStringSerializer extends StdScalarSerializer<BigDecimal> {

    public DecimalStringSerializer() {
        super(BigDecimal.class);
    }

    public DecimalStringSerializer(final Class<BigDecimal> t) {
        super(t);
    }

    @Override
    public void serialize(final BigDecimal value, final JsonGenerator gen, final SerializerProvider provider)
            throws IOException {
        gen.writeString(value.toPlainString());
    }

}
