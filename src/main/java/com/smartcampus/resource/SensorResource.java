package com.smartcampus.resource;

import com.smartcampus.exception.ErrorResponse;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Part 3 — Sensor Operations & Linking.
 * Part 4 — Sub-Resource Locator for Readings.
 *
 * Manages the {@code /api/v1/sensors} collection and individual sensor
 * resources.
 * Also delegates responsibility for the readings sub-resource via the
 * Sub-Resource
 * Locator pattern.
 *
 * Endpoints provided:
 * 
 * {@code GET  /api/v1/sensors} — list all sensors (optional ?type= filter)
 * {@code POST /api/v1/sensors} — register a new sensor (validates roomId)
 * {@code GET  /api/v1/sensors/{sensorId}} — get a single sensor
 * {@code /api/v1/sensors/{sensorId}/readings} — delegated to
 * SensorReadingResource
 * 
 *
 * Integrity constraint (Part 3.1): When registering a sensor, the
 * {@code roomId} field in the request body must reference an existing Room. If
 * not,
 * a {@link LinkedResourceNotFoundException} is thrown, mapping to HTTP 422.
 *
 * 5COSC022W Client-Server Architectures Coursework (2025/26)
 *
 * @author Rajapaksaha Thusew nambi Thenuja Hansana (20231300)
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    // ------------------------------------------------------------------ //
    // GET /api/v1/sensors (optionally filtered by ?type=)
    // ------------------------------------------------------------------ //

    /**
     * Returns all sensors, optionally filtered by type.
     *
     * The {@code type} query parameter is optional. When absent, all sensors
     * are returned. When present (e.g., {@code ?type=CO2}), only sensors whose
     * {@code type} field matches (case-insensitive) are included in the response.
     *
     * Using {@code @QueryParam} for filtering is preferred over embedding the
     * type in the URL path (e.g., {@code /sensors/type/CO2}) because query
     * parameters are designed for optional, combinable search criteria and do not
     * imply a hierarchical resource structure.
     *
     * @param type optional sensor type filter (e.g., "CO2", "Temperature")
     * @return HTTP 200 with a JSON array of matching {@link Sensor} objects
     */
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        Collection<Sensor> all = store.sensors.values();

        if (type == null || type.isBlank()) {
            return Response.ok(new ArrayList<>(all)).build();
        }

        // Filter case-insensitively so "co2", "CO2", and "Co2" all work
        Collection<Sensor> filtered = all.stream()
                .filter(s -> type.equalsIgnoreCase(s.getType()))
                .collect(Collectors.toList());

        return Response.ok(filtered).build();
    }

    // ------------------------------------------------------------------ //
    // POST /api/v1/sensors
    // ------------------------------------------------------------------ //

    /**
     * Registers a new Sensor and links it to an existing Room.
     *
     * Data integrity check: The {@code roomId} field in the
     * request body is validated against the DataStore. If the referenced Room does
     * not exist, a {@link LinkedResourceNotFoundException} is thrown, which is
     * mapped to HTTP 422 Unprocessable Entity.
     *
     * On success, the new sensor's ID is added to the parent Room's
     * {@code sensorIds} list to keep bidirectional linkage consistent.
     *
     * @Consumes(APPLICATION_JSON): If a client sends a request
     * with {@code Content-Type: text/plain} or another non-JSON type, JAX-RS
     * short-circuits with HTTP 415 Unsupported Media Type before this method is
     * even invoked.
     *
     * @param sensor the sensor object deserialised from the JSON request body
     * @return HTTP 201 Created with the persisted {@link Sensor} and a Location
     *         header
     */
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "INVALID_REQUEST",
                            "Request body must be a valid Sensor JSON object."))
                    .build();
        }

        // ── Integrity check: verify the referenced room exists ──────────
        String roomId = sensor.getRoomId();
        if (roomId == null || roomId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "VALIDATION_FAILED", "The 'roomId' field is required."))
                    .build();
        }

        Room room = store.rooms.get(roomId);
        if (room == null) {
            throw new LinkedResourceNotFoundException(roomId); // → 422
        }

        // Auto-generate an ID if the client did not provide one
        if (sensor.getId() == null || sensor.getId().isBlank()) {
            sensor.setId(UUID.randomUUID().toString());
        }

        // Reject duplicate IDs
        if (store.sensors.containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(409, "DUPLICATE_RESOURCE",
                            "A sensor with id '" + sensor.getId() + "' already exists."))
                    .build();
        }

        // Default status to ACTIVE if not supplied
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        } else {
            String status = sensor.getStatus().toUpperCase();
            if (!status.equals("ACTIVE") && !status.equals("MAINTENANCE") && !status.equals("OFFLINE")) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse(400, "VALIDATION_FAILED",
                                "Status must be ACTIVE, MAINTENANCE, or OFFLINE."))
                        .build();
            }
            sensor.setStatus(status);
        }

        store.sensors.put(sensor.getId(), sensor);

        // ── Link sensor to the parent room ──────────────────────────────
        room.getSensorIds().add(sensor.getId());

        URI location = UriBuilder.fromResource(SensorResource.class)
                .path(sensor.getId())
                .build();

        return Response.created(location).entity(sensor).build();
    }

    // ------------------------------------------------------------------ //
    // GET /api/v1/sensors/{sensorId}
    // ------------------------------------------------------------------ //

    /**
     * Retrieves a single sensor by its unique ID.
     *
     * @param sensorId the sensor identifier from the URL path
     * @return HTTP 200 with the {@link Sensor}, or HTTP 404 if not found
     */
    @GET
    @Path("{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(404, "SENSOR_NOT_FOUND", "Sensor '" + sensorId + "' not found."))
                    .build();
        }
        return Response.ok(sensor).build();
    }

    // ------------------------------------------------------------------ //
    // DELETE /api/v1/sensors/{sensorId}
    // ------------------------------------------------------------------ //

    /**
     * Deletes a sensor and removes its ID from the parent room.
     *
     * @param sensorId the sensor identifier
     * @return HTTP 204 No Content
     */
    @DELETE
    @Path("{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(404, "SENSOR_NOT_FOUND", "Sensor '" + sensorId + "' not found."))
                    .build();
        }

        // Clean up reverse reference in parent Room
        Room room = store.rooms.get(sensor.getRoomId());
        if (room != null) {
            room.getSensorIds().remove(sensorId);
        }

        store.sensors.remove(sensorId);
        store.readings.remove(sensorId); // Also clean up historical data
        return Response.noContent().build();
    }

    // ------------------------------------------------------------------ //
    // Sub-Resource Locator — /api/v1/sensors/{sensorId}/readings
    // ------------------------------------------------------------------ //

    /**
     * Sub-Resource Locator delegating control to {@link SensorReadingResource}.
     *
     * This method carries <strong>no</strong> HTTP method annotation
     * (no {@code @GET}, {@code @POST}, etc.), which is what makes it a
     * <em>locator</em> rather than a handler. JAX-RS uses the returned object
     * to resolve the appropriate method for the actual HTTP verb at runtime.
     *
     * Architectural benefit: {@link SensorReadingResource} has a single
     * responsibility — managing readings for exactly one sensor — keeping both
     * classes focused and independently maintainable.
     *
     * @param sensorId the sensor whose readings sub-resource is being accessed
     * @return a {@link SensorReadingResource} instance scoped to this sensor
     */
    @Path("{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
