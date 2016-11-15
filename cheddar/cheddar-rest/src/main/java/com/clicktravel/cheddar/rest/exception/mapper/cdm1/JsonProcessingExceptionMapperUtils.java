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
package com.clicktravel.cheddar.rest.exception.mapper.cdm1;

import com.clicktravel.schema.canonical.data.model.v1.common.Error;
import com.clicktravel.schema.canonical.data.model.v1.common.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

public class JsonProcessingExceptionMapperUtils {

    public static ErrorResponse buildErrorResponse(final JsonProcessingException exception) {
        final ErrorResponse errorResponse = new ErrorResponse();
        final Error error = new Error();
        error.setDescription("An error occurred during processing the provided JSON content.");
        errorResponse.getErrors().add(error);
        return errorResponse;
    }
}
