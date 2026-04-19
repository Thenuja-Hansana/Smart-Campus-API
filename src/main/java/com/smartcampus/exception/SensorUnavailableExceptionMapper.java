package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * JAX-RS ExceptionMapper for {@link SensorUnavailableException}.
 *
 * Returns HTTP 403 Forbidden when a client attempts to POST a new reading
 * to a Sensor marked as {@code "MAINTENANCE"}.
 *
 * 403 is used here because the client is making a legitimate, syntactically
 * correct request, but the current state of the sensor forbids the
 * operation — the sensor is physically offline for maintenance.
 *
 * 5COSC022W Client-Server Architectures Coursework (2025/26)
 *
 * @author Rajapaksaha Thusew nambi Thenuja Hansana (20231300)
 */
@Provider
public class SensorUnavailableExceptionMapper
                implements ExceptionMapper<SensorUnavailableException> {

        @Override
        public Response toResponse(SensorUnavailableException ex) {
                ErrorResponse body = new ErrorResponse(
                                Response.Status.FORBIDDEN.getStatusCode(),
                                "SENSOR_UNAVAILABLE",
                                "Sensor '" + ex.getSensorId() + "' is currently in '"
                                                + ex.getStatus() + "' state and cannot accept new readings. "
                                                + "Update the sensor status to 'ACTIVE' before submitting readings.");
                return Response
                                .status(Response.Status.FORBIDDEN) // 403
                                .type(MediaType.APPLICATION_JSON)
                                .entity(body)
                                .build();
        }
}
