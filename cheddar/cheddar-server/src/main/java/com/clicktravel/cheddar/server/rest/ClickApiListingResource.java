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

import io.swagger.config.FilterFactory;
import io.swagger.config.Scanner;
import io.swagger.config.ScannerFactory;
import io.swagger.config.SwaggerConfig;
import io.swagger.core.filter.SpecFilter;
import io.swagger.core.filter.SwaggerSpecFilter;
import io.swagger.jaxrs.Reader;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.config.JaxrsScanner;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.models.Swagger;
import io.swagger.util.Yaml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Click travels own version of the class com.wordnik.swagger.jaxrs.listing.ApiListingResource as that depends on a
 * servlet deployment which our apps currently do not support (i tried to setup and failed to load the resources). This
 * version just changes the fetching of the swagger class from the servlet found at @Context (null) to the scanner
 * (BeanConfig).
 * 
 * THIS MAY NOT BE NEEDED NOW WE HAVE MOVED TO THE NEW VERSION OF THE LIBARARY!
 * 
 * @author james
 * 
 */

@Path("/")
public class ClickApiListingResource {

    Logger LOGGER = LoggerFactory.getLogger(ApiListingResource.class);

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
                    // Another reference to serlet which will always be null
                    // final SwaggerConfig configurator = (SwaggerConfig) context.getAttribute("reader");
                    final SwaggerConfig configurator = null;
                    if (configurator != null) {
                        LOGGER.debug("configuring swagger with " + configurator);
                        configurator.configure(swagger);
                    } else {
                        LOGGER.debug("no configurator");
                    }
                }
                // Commented out as swagger is already being mutated and referenced in the scannerFactory
                // context.setAttribute("swagger", swagger);
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
                swagger = f.filter(swagger, filterImpl, getQueryParams(uriInfo.getQueryParameters()),
                        getCookies(headers), getHeaders(headers));
            }
            return Response.ok().entity(swagger).build();
        } else {
            return Response.status(404).build();
        }
    }

    @GET
    @Produces("application/yaml")
    @Path("/swagger.yaml")
    public Response getListingYaml(@Context final Application app, @Context final ServletConfig sc,
            @Context final HttpHeaders headers, @Context final UriInfo uriInfo) {
        Swagger swagger = getSwagger();
        if (!initialized) {
            swagger = scan(app, sc);
        }
        try {
            if (swagger != null) {
                final SwaggerSpecFilter filterImpl = FilterFactory.getFilter();
                LOGGER.debug("using filter " + filterImpl);
                if (filterImpl != null) {
                    final SpecFilter f = new SpecFilter();
                    swagger = f.filter(swagger, filterImpl, getQueryParams(uriInfo.getQueryParameters()),
                            getCookies(headers), getHeaders(headers));
                }

                final String yaml = Yaml.mapper().writeValueAsString(swagger);
                final String[] parts = yaml.split("\n");
                final StringBuilder b = new StringBuilder();
                for (final String part : parts) {
                    final int pos = part.indexOf("!<");
                    final int endPos = part.indexOf(">");
                    b.append(part);
                    b.append("\n");
                }
                return Response.ok().entity(b.toString()).type("text/plain").build();
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return Response.status(404).build();
    }

    protected Map<String, List<String>> getQueryParams(final MultivaluedMap<String, String> params) {
        final Map<String, List<String>> output = new HashMap<String, List<String>>();
        if (params != null) {
            for (final String key : params.keySet()) {
                final List<String> values = params.get(key);
                output.put(key, values);
            }
        }
        return output;
    }

    protected Map<String, String> getCookies(final HttpHeaders headers) {
        final Map<String, String> output = new HashMap<String, String>();
        if (headers != null) {
            for (final String key : headers.getCookies().keySet()) {
                final Cookie cookie = headers.getCookies().get(key);
                output.put(key, cookie.getValue());
            }
        }
        return output;
    }

    protected Map<String, List<String>> getHeaders(final HttpHeaders headers) {
        final Map<String, List<String>> output = new HashMap<String, List<String>>();
        if (headers != null) {
            for (final String key : headers.getRequestHeaders().keySet()) {
                final List<String> values = headers.getRequestHeaders().get(key);
                output.put(key, values);
            }
        }
        return output;
    }

    /**
     * Method added to obtain the swagger configuration from the BeanConfig scanner found in the ScannerFactory
     * singleton as click implementation will always use this method of configuring Swagger.
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
