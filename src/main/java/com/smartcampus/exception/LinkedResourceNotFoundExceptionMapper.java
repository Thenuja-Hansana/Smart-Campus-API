package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * JAX-RS ExceptionMapper for {@link LinkedResourceNotFoundException}.
 *
 * Returns HTTP 422 Unprocessable Entity when a sensor is created referencing
 * a {@code roomId} that does not correspond to any registered Room.
 *
 * 422 is semantically more precise than 404 here: the endpoint URL is valid,
 * and the JSON payload is well-formed, but the data inside the
 * payload is broken — a semantic validation failure rather than a missing
 * resource.
 *
 * 5COSC022W Client-Server Architectures Coursework (2025/26)
 *
 * @author Rajapaksaha Thusew nambi Thenuja Hansana (20231300)
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper
                implements ExceptionMapper<LinkedResourceNotFoundException> {

        /** HTTP 422 status code (not in pre-Java 11 Response.Status enum). */
        private static final int UNPROCESSABLE_ENTITY = 422;

        @Override
        public Response toResponse(LinkedResourceNotFoundException ex) {
                ErrorResponse body = new ErrorResponse(
                                UNPROCESSABLE_ENTITY,
                                "LINKED_RESOURCE_NOT_FOUND",
                                "The supplied roomId '" + ex.getRoomId() + "' does not correspond to any "
                                                + "registered Room. Please create the Room first before assigning a Sensor to it.");
                return Response
                                .status(UNPROCESSABLE_ENTITY) // 422
                                .type(MediaType.APPLICATION_JSON)
                                .entity(body)
                                .build();
        }
}
