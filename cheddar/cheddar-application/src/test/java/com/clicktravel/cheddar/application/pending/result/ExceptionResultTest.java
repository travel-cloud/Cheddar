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
package com.clicktravel.cheddar.application.pending.result;

import static org.junit.Assert.assertSame;

import org.junit.Test;

public class ExceptionResultTest {

    @Test
    public void shouldThrowException_onGetValue() {
        // Given
        final Exception exception = new Exception();
        final ExceptionResult exceptionResult = new ExceptionResult(exception);

        // When
        Exception thrownException = null;
        try {
            exceptionResult.getValue();
        } catch (final Exception e) {
            thrownException = e;
        }

        // Then
        assertSame(exception, thrownException);
    }
}
