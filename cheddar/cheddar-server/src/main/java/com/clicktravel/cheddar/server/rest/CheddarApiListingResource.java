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

package com.clicktravel.cheddar.server.rest;

import java.util.Set;

import javax.servlet.ServletConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.config.FilterFactory;
import io.swagger.config.Scanner;
import io.swagger.config.ScannerFactory;
import io.swagger.config.SwaggerConfig;
import io.swagger.core.filter.SpecFilter;
import io.swagger.core.filter.SwaggerSpecFilter;
import io.swagger.jaxrs.Reader;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.config.JaxrsScanner;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.models.Swagger;

/**
 * Cheddar's own version of the class com.wordnik.swagger.jaxrs.listing.ApiListingResource that removes the dependacy on
 * a servlet deployment which cheddar currently do not support. This version changes the fetching of the swagger object
 * to the scanner (BeanConfig) object instead of the servlet context.
 */

@Path("/")
public class CheddarApiListingResource {

    Logger LOGGER = LoggerFactory.getLogger(CheddarApiListingResource.class);

    static boolean initialized = false;

    protected synchronized Swagger scan(final Application app, final ServletConfig sc) {
        Swagger swagger = null;
        final Scanner scanner = ScannerFactory.getScanner();
        LOGGER.debug("using scanner " + scanner);

        if (scanner != null) {
            SwaggerSerializers.setPrettyPrint(scanner.getPrettyPrint());

            swagger = getSwagger();

            Set<Class<?>> classes = null;
            if (scanner instanceof JaxrsScanner) {
                final JaxrsScanner jaxrsScanner = (JaxrsScanner) scanner;
                classes = jaxrsScanner.classesFromContext(app, sc);
            } else {
                classes = scanner.classes();
            }
            if (classes != null) {
                final Reader reader = new Reader(swagger);
                swagger = reader.read(classes);
                if (scanner instanceof SwaggerConfig) {
                    swagger = ((SwaggerConfig) scanner).configure(swagger);
                } else {
                    LOGGER.debug("no configurator");
                }
            }
        }
        initialized = true;
        return swagger;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/swagger.json")
    public Response getListingJson(@Context final Application app, @Context final ServletConfig sc,
            @Context final HttpHeaders headers, @Context final UriInfo uriInfo) {
        Swagger swagger = getSwagger();
        if (!initialized) {
            swagger = scan(app, sc);
        }
        if (swagger != null) {
            final SwaggerSpecFilter filterImpl = FilterFactory.getFilter();
            if (filterImpl != null) {
                final SpecFilter f = new SpecFilter();
                swagger = f.filter(swagger, filterImpl, null, null, null);
            }
            return Response.ok().entity(swagger).build();
        } else {
            return Response.status(404).build();
        }
    }

    /**
     * Method added to obtain the swagger configuration from the BeanConfig scanner found in the ScannerFactory
     * singleton as cheddar implementations will always use this method of configuring Swagger.
     *
     * @return swagger configuration
     */

    private Swagger getSwagger() {
        final Scanner scanner = ScannerFactory.getScanner();
        if (scanner instanceof BeanConfig) {
            return ((BeanConfig) scanner).getSwagger();
        }
        return null;
    }
}
