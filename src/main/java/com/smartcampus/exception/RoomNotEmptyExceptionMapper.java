package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * JAX-RS ExceptionMapper for {@link RoomNotEmptyException}.
 *
 * Intercepts the exception and converts it into an HTTP 409 Conflict response
 * with a structured JSON body. This prevents any raw Java exception details
 * from
 * leaking to the client.
 *
 * 5COSC022W Client-Server Architectures Coursework (2025/26)
 *
 * @author Rajapaksaha Thusew nambi Thenuja Hansana (20231300)
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        ErrorResponse body = new ErrorResponse(
                Response.Status.CONFLICT.getStatusCode(),
                "ROOM_NOT_EMPTY",
                "Cannot delete room '" + ex.getRoomId() + "': it still has "
                        + ex.getSensorCount() + " sensor(s) assigned to it. "
                        + "Please remove or reassign all sensors before decommissioning this room.");
        return Response
                .status(Response.Status.CONFLICT) // 409
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
