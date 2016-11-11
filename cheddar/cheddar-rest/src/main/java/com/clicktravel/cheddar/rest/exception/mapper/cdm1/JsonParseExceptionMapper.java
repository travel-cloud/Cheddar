package com.clicktravel.cheddar.rest.exception.mapper.cdm1;

import static com.clicktravel.cheddar.rest.exception.mapper.cdm1.JsonProcessingExceptionMapperUtils.buildErrorResponse;

import javax.annotation.Priority;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.rest.media.MediaTypes;
import com.fasterxml.jackson.core.JsonParseException;

@Provider
@Produces(MediaTypes.CDM_V1_JSON)
@Priority(Integer.MAX_VALUE)
public class JsonParseExceptionMapper implements ExceptionMapper<JsonParseException> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Response toResponse(final JsonParseException exception) {
        if (logger.isDebugEnabled()) {
            logger.debug(exception.getMessage(), exception);
        }
        return Response.status(Response.Status.BAD_REQUEST).entity(buildErrorResponse(exception)).build();
    }

}
