package com.smartcampus.exception;

/**
 * JSON error-response body returned by all custom ExceptionMappers.
 *
 * Serialised by Jackson into a consistent structure so that every error
 * response the API produces looks identical, regardless of the root cause.
 * 
 * {
 * "status": 409,
 * "error": "ROOM_NOT_EMPTY",
 * "message": "Cannot delete room 'LIB-301': it still has 2 sensor(s) assigned."
 * }
 *
 * 5COSC022W Client-Server Architectures Coursework (2025/26)
 *
 * @author Rajapaksaha Thusew nambi Thenuja Hansana (20231300)
 */
public class ErrorResponse {

    private int status;
    private String error;
    private String message;

    /** No-arg constructor required for Jackson. */
    public ErrorResponse() {
    }

    /**
     * Creates a fully populated error response.
     *
     * @param status  HTTP status code
     * @param error   machine-readable error code
     * @param message human-readable explanation
     */
    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String msg) {
        this.message = msg;
    }
}
