package com.smartcampus.exception;

/**
 * Thrown when a client attempts to POST a new reading to a Sensor whose
 * status is currently {@code "MAINTENANCE"}.
 *
 * A sensor in maintenance mode is physically disconnected and cannot accept
 * or validate incoming measurements. The operation is forbidden until the
 * sensor status is changed back to {@code "ACTIVE"}.
 *
 * Mapped to HTTP 403 Forbidden by {@link SensorUnavailableExceptionMapper}.
 *
 * 5COSC022W Client-Server Architectures Coursework (2025/26)
 *
 * @author Rajapaksaha Thusew nambi Thenuja Hansana (20231300)
 */
public class SensorUnavailableException extends RuntimeException {

    private final String sensorId;
    private final String status;

    /**
     * @param sensorId the ID of the sensor that rejected the reading
     * @param status   the sensor's current status (expected: "MAINTENANCE")
     */
    public SensorUnavailableException(String sensorId, String status) {
        super("Sensor '" + sensorId + "' is in '" + status + "' state and cannot accept new readings.");
        this.sensorId = sensorId;
        this.status = status;
    }

    public String getSensorId() {
        return sensorId;
    }

    public String getStatus() {
        return status;
    }
}
