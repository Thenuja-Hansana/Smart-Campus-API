package com.smartcampus.exception;

/**
 * Thrown when a client attempts to DELETE a Room that still has one or more
 * Sensors assigned to it.
 *
 * This enforces the business rule that prevents data orphans: a Room must be
 * empty (i.e., all sensors removed or re-assigned) before it can be
 * decommissioned.
 *
 * Mapped to HTTP 409 Conflict by {@link RoomNotEmptyExceptionMapper}.
 *
 * 5COSC022W Client-Server Architectures Coursework (2025/26)
 *
 * @author Rajapaksaha Thusew nambi Thenuja Hansana (20231300)
 */
public class RoomNotEmptyException extends RuntimeException {

    private final String roomId;
    private final int sensorCount;

    /**
     * @param roomId      the ID of the room that the client attempted to delete
     * @param sensorCount the number of sensors still assigned to that room
     */
    public RoomNotEmptyException(String roomId, int sensorCount) {
        super("Room '" + roomId + "' still has " + sensorCount + " sensor(s) assigned.");
        this.roomId = roomId;
        this.sensorCount = sensorCount;
    }

    public String getRoomId() {
        return roomId;
    }

    public int getSensorCount() {
        return sensorCount;
    }
}
