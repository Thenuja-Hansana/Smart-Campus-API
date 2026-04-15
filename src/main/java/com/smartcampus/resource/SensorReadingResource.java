package com.smartcampus.resource;

import com.smartcampus.exception.ErrorResponse;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

/**
 * Part 4 — Readings Sub-Resource (Historical Data Management).
 *
 * This class is instantiated by the Sub-Resource Locator in
 * {@link SensorResource#getReadingsResource(String)} and handles all HTTP
 * operations on the path {@code /api/v1/sensors/{sensorId}/readings}.
 *
 * It carries <strong>no</strong> class-level {@code @Path} annotation because
 * path resolution is already handled by the locator method. JAX-RS injects the
 * correct path context at runtime.
 *
 * Endpoints provided:
 * 
 * {@code GET  /api/v1/sensors/{sensorId}/readings} — fetch all historical
 * readings
 * {@code POST /api/v1/sensors/{sensorId}/readings} — append a new reading
 * 
 *
 * State constraint (Part 5.3) If the parent sensor's status is
 * {@code "MAINTENANCE"}, a POST to its readings is rejected with
 * {@link SensorUnavailableException} → HTTP 403 Forbidden.
 *
 * Consistency side effect: Every successful POST immediately
 * updates the parent {@link Sensor#setCurrentValue(double)} so the sensor's
 * most-recent-value field is always up to date (Part 4.2 requirement).
 *
 * 5COSC022W Client-Server Architectures Coursework (2025/26)
 *
 * @author Rajapaksaha Thusew nambi Thenuja Hansana (20231300)
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    /**
     * Constructor invoked by the sub-resource locator in {@link SensorResource}.
     *
     * @param sensorId the ID of the sensor this sub-resource instance is scoped to
     */
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // ------------------------------------------------------------------ //
    // GET /api/v1/sensors/{sensorId}/readings
    // ------------------------------------------------------------------ //

    /**
     * Returns all historical readings recorded for this sensor, in insertion order.
     *
     * @return HTTP 200 with a JSON array of {@link SensorReading} objects,
     *         or HTTP 404 if the sensor does not exist
     */
    @GET
    public Response getAllReadings() {
        Sensor sensor = store.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(404, "SENSOR_NOT_FOUND", "Sensor '" + sensorId + "' not found."))
                    .build();
        }

        List<SensorReading> readings = store.getOrCreateReadings(sensorId);
        return Response.ok(readings).build();
    }

    // ------------------------------------------------------------------ //
    // POST /api/v1/sensors/{sensorId}/readings
    // ------------------------------------------------------------------ //

    /**
     * Appends a new measurement reading to this sensor's historical log.
     *
     * Three key behaviours are enforced:
     * 
     * UUID auto-generation:the reading's {@code id} field
     * is always replaced with a freshly generated {@link UUID}, regardless of
     * any client-supplied value.
     * Timestamp default: if the client omits {@code timestamp},
     * it is set to {@code System.currentTimeMillis()} (epoch ms).
     * currentValue sync: after persisting the reading, the
     * parent {@link Sensor#setCurrentValue(double)} is updated immediately,
     * ensuring cross-entity data consistency across the API.
     *
     * State constraint: if the parent sensor's {@code status}
     * is {@code "MAINTENANCE"}, a {@link SensorUnavailableException} is thrown,
     * which maps to HTTP 403 Forbidden.
     *
     * @param reading the reading deserialised from the JSON request body
     * @return HTTP 201 Created with the persisted {@link SensorReading},
     *         HTTP 404 if the sensor does not exist,
     *         HTTP 403 if the sensor is in MAINTENANCE state
     */
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.sensors.get(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(404, "SENSOR_NOT_FOUND", "Sensor '" + sensorId + "' not found."))
                    .build();
        }

        // ── State constraint: block readings from MAINTENANCE sensors ────
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus()); // → 403
        }

        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "INVALID_REQUEST",
                            "Request body must be a valid SensorReading JSON object."))
                    .build();
        }

        // ── Always auto-generate the reading ID (UUID) ───────────────────
        reading.setId(UUID.randomUUID().toString());

        // ── Default timestamp to now if client omitted it ────────────────
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Persist the reading
        store.getOrCreateReadings(sensorId).add(reading);

        // ── Consistency side effect: update sensor's currentValue ─────────
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
