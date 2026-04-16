package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global "safety-net" ExceptionMapper that catches any {@link Throwable}
 * handled by a more specific mapper.
 *
 * This mapper ensures the API is "leak-proof": no raw Java stack trace or
 * internal server details are ever returned to an external client. Instead,
 * every unexpected error is translated into a generic HTTP 500 Internal Server
 * Error response with a sanitised JSON body.
 *
 * 5COSC022W Client-Server Architectures Coursework (2025/26)
 *
 * @author Rajapaksaha Thusew nambi Thenuja Hansana (20231300)
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        // Log full details server-side only — never sent to the client
        LOGGER.log(Level.SEVERE, "Unhandled exception caught by GlobalExceptionMapper: " + ex.getMessage(), ex);

        ErrorResponse body = new ErrorResponse(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please contact the system administrator.");
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR) // 500
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
