package com.clicktravel.cheddar.server.http.filter.logging;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Priority(Priorities.USER)
public class LoggingResponseFilter implements ContainerResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingResponseFilter.class);

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) {
        if (LOGGER.isDebugEnabled()) {
            try {
                LOGGER.debug("REST-RESP [{}] [{}] [{}] [{}]", requestContext.getUriInfo().getPath(),
                        extractHeadersFromResponse(responseContext), responseContext.getStatus(),
                        responseContext.getEntity());
            } catch (final Exception e) {
                LOGGER.warn("Error during logging response: ", e);
            }
        }
    }

    private String extractHeadersFromResponse(final ContainerResponseContext responseContext) {
        final String locationHeader = responseContext.getHeaderString("Location");
        return locationHeader != null ? locationHeader : "";
    }

}
