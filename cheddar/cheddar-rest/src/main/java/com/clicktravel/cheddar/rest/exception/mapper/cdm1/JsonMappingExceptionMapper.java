package com.clicktravel.cheddar.rest.exception.mapper.cdm1;

import static com.clicktravel.cheddar.rest.exception.mapper.cdm1.JsonProcessingExceptionMapperUtils.buildErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonMappingException;

public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Response toResponse(final JsonMappingException exception) {
        if (logger.isDebugEnabled()) {
            logger.debug(exception.getMessage(), exception);
        }
        return Response.status(Response.Status.BAD_REQUEST).entity(buildErrorResponse(exception)).build();
    }

}
