package com.smartcampus.exception;

/**
 * Thrown when a client attempts to register a new Sensor referencing a
 * {@code roomId} that does not exist in the DataStore.
 *
 * The request payload is syntactically valid JSON, but the referenced
 * resource (the Room) cannot be found — making this a semantic validation
 * failure rather than a simple 404 "endpoint not found" error.
 *
 * Mapped to HTTP 422 Unprocessable Entity by
 * {@link LinkedResourceNotFoundExceptionMapper}.
 *
 * 5COSC022W Client-Server Architectures Coursework (2025/26)
 *
 * @author Rajapaksaha Thusew nambi Thenuja Hansana (20231300)
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    private final String roomId;

    /**
     * @param roomId the referenced room ID that was not found in the DataStore
     */
    public LinkedResourceNotFoundException(String roomId) {
        super("roomId '" + roomId + "' does not reference any known Room.");
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }
}
