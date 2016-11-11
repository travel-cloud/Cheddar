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
