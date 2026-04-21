package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * JAX-RS filter that provides observability by logging every incoming request
 * and every outgoing response.
 *
 * Implements both {@link ContainerRequestFilter} and
 * {@link ContainerResponseFilter} in a single class so that request and
 * response logging is handled by a single, cohesive cross-cutting concern.
 *
 * Using a filter rather than inserting {@code Logger.info()} calls in every
 * resource method enforces the DRY (Don't Repeat Yourself) principle and
 * guarantees that <em>all</em> requests are logged — including those rejected
 * early (e.g., 415 Unsupported Media Type) before a resource method is invoked.
 *
 * Logging output:
 * 
 * [REQUEST] POST http://localhost:8080/api/v1/sensors
 * [RESPONSE] 201
 * 
 *
 * 5COSC022W Client-Server Architectures Coursework (2025/26)
 *
 * @author Rajapaksaha Thusew nambi Thenuja Hansana (20231300)
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    /**
     * Logs the HTTP method and full request URI for every incoming request.
     *
     * @param requestContext context object exposing method, URI, headers, etc.
     * @throws IOException never thrown — declared by the interface contract
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info(String.format("[REQUEST]  %-7s %s",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri()));
    }

    /**
     * Logs the final HTTP status code for every outgoing response.
     *
     * @param requestContext  the original request context (for correlation if
     *                        needed)
     * @param responseContext context exposing the status code, headers, and entity
     * @throws IOException never thrown — declared by the interface contract
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
            ContainerResponseContext responseContext) throws IOException {
        LOGGER.info(String.format("[RESPONSE] %d  ← %s %s",
                responseContext.getStatus(),
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri()));
    }
}
