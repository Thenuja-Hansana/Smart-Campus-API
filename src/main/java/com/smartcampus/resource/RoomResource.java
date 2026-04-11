package com.smartcampus.resource;

import com.smartcampus.exception.ErrorResponse;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * Part 2 — Room Management Resource.
 *
 * 
 * Manages the {@code /api/v1/rooms} collection endpoint and the individual
 * room sub-resource {@code /api/v1/rooms/{roomId}}.
 *
 * 
 * Endpoints provided:
 * 
 * {@code GET    /api/v1/rooms} — list all rooms
 * {@code POST   /api/v1/rooms} — create a new room
 * {@code GET    /api/v1/rooms/{roomId}} — get a single room by ID
 * {@code DELETE /api/v1/rooms/{roomId}} — decommission a room (fails if
 * sensors present)
 *
 * 
 * sensors assigned to it. Attempting to do so throws a
 * {@link RoomNotEmptyException}
 * which is mapped to HTTP 409 Conflict by
 * {@link com.smartcampus.exception.RoomNotEmptyExceptionMapper}.
 *
 * 
 * 5COSC022W Client-Server Architectures Coursework (2025/26)
 *
 * @author Rajapaksaha Thusew nambi Thenuja Hansana (20231300)
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    // ------------------------------------------------------------------ //
    // GET /api/v1/rooms
    // ------------------------------------------------------------------ //

    /**
     * Returns the full list of all registered rooms.
     *
     * <p>
     * Full room objects are returned (not just IDs) so that clients receive
     * all metadata (name, capacity, sensorIds) in a single round-trip, avoiding
     * the N+1 request problem that arises when only IDs are returned.
     *
     * @return HTTP 200 with a JSON array of {@link Room} objects
     */
    @GET
    public Response getAllRooms() {
        Collection<Room> rooms = new ArrayList<>(store.rooms.values());
        return Response.ok(rooms).build();
    }

    // ------------------------------------------------------------------ //
    // POST /api/v1/rooms
    // ------------------------------------------------------------------ //

    /**
     * Creates and registers a new Room.
     *
     * <p>
     * If the client omits the {@code id} field, a UUID is auto-generated.
     * Returns a {@code Location} header pointing to the new resource.
     *
     * @param room the room object deserialised from the JSON request body
     * @return HTTP 201 Created with the persisted {@link Room} and a Location
     *         header
     */
    @POST
    public Response createRoom(Room room) {
        if (room == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "INVALID_REQUEST", "Request body must be a valid Room JSON object."))
                    .build();
        }

        if (room.getName() == null || room.getName().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "VALIDATION_FAILED", "Room 'name' is required and cannot be empty."))
                    .build();
        }

        if (room.getCapacity() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "VALIDATION_FAILED", "Room 'capacity' must be greater than zero."))
                    .build();
        }

        // Auto-generate an ID if the client did not supply one
        if (room.getId() == null || room.getId().isBlank()) {
            room.setId(UUID.randomUUID().toString());
        }

        // Reject duplicate IDs
        if (store.rooms.containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(409, "DUPLICATE_RESOURCE",
                            "A room with id '" + room.getId() + "' already exists."))
                    .build();
        }

        store.rooms.put(room.getId(), room);

        URI location = UriBuilder.fromResource(RoomResource.class)
                .path(room.getId())
                .build();

        return Response.created(location).entity(room).build();
    }

    // ------------------------------------------------------------------ //
    // GET /api/v1/rooms/{roomId}
    // ------------------------------------------------------------------ //

    /**
     * Retrieves the metadata for a single room.
     *
     * @param roomId the unique room identifier from the URL path
     * @return HTTP 200 with the {@link Room}, or HTTP 404 if not found
     */
    @GET
    @Path("{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = store.rooms.get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(404, "ROOM_NOT_FOUND", "Room '" + roomId + "' not found."))
                    .build();
        }
        return Response.ok(room).build();
    }

    // ------------------------------------------------------------------ //
    // DELETE /api/v1/rooms/{roomId}
    // ------------------------------------------------------------------ //

    /**
     * Decommissions (permanently removes) a Room from the system.
     *
     * 
     * Safety logic:If the room's {@code sensorIds} list is
     * non-empty, the request is rejected with {@link RoomNotEmptyException}
     * (HTTP 409). The client must remove or reassign all sensors first.
     *
     * Idempotency:The first successful DELETE returns 204.
     * Subsequent calls for the same ID return 404 (room no longer exists). The
     * server state is consistent in both cases — the room does not exist after
     * the first call — so the operation is considered idempotent under HTTP
     * semantics even though the response code differs.
     *
     * @param roomId the unique room identifier from the URL path
     * @return HTTP 204 No Content on success, 404 if not found, 409 if sensors
     *         present
     */
    @DELETE
    @Path("{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.rooms.get(roomId);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(404, "ROOM_NOT_FOUND", "Room '" + roomId + "' not found."))
                    .build();
        }

        // Safety constraint: block deletion if any sensors are still assigned
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId, room.getSensorIds().size());
        }

        store.rooms.remove(roomId);
        return Response.noContent().build(); // 204
    }
}
