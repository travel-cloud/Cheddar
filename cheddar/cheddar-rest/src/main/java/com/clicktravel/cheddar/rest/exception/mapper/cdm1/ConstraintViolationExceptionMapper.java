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

import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.cheddar.domain.model.exception.ConstraintViolationException;
import com.clicktravel.cheddar.rest.media.MediaTypes;
import com.clicktravel.schema.canonical.data.model.v1.common.Error;
import com.clicktravel.schema.canonical.data.model.v1.common.ErrorResponse;

@Provider
@Produces(MediaTypes.CDM_V1_JSON)
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Response toResponse(final ConstraintViolationException exception) {
        if (logger.isDebugEnabled()) {
            logger.debug(exception.getMessage());
        }
        final ErrorResponse errorResponse = new ErrorResponse();
        final Error error = new Error();
        error.setDescription(exception.getMessage());
        errorResponse.getErrors().add(error);
        return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
    }

}
