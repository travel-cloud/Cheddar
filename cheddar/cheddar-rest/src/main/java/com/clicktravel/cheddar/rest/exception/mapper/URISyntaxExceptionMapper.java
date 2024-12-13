package com.clicktravel.cheddar.rest.exception.mapper;

import java.net.URISyntaxException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class URISyntaxExceptionMapper implements ExceptionMapper<URISyntaxException> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Response toResponse(final URISyntaxException exception) {
        if (logger.isDebugEnabled()) {
            logger.debug(exception.getMessage());
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }
}
