package com.smartcampus.model;

/**
 * POJO representing a hardware Sensor deployed in a campus Room.
 *
 * Each Sensor belongs to exactly one Room (via {@link #roomId}) and
 * maintains its most recent measurement in {@link #currentValue}. That field
 * is automatically updated whenever a new {@link SensorReading} is posted to
 * the sensor's readings sub-resource.
 *
 * Valid status values: {@code "ACTIVE"}, {@code "MAINTENANCE"},
 * {@code "OFFLINE"}
 *
 * 5COSC022W Client-Server Architectures Coursework (2025/26)
 *
 * @author Rajapaksaha Thusew nambi Thenuja Hansana (20231300)
 */
public class Sensor {

    /** Unique identifier (e.g., "TEMP-001"). */
    private String id;

    /** Category of measurement (e.g., "Temperature", "CO2", "Occupancy"). */
    private String type;

    /**
     * Current operational state of the sensor.
     * Must be one of: {@code ACTIVE}, {@code MAINTENANCE}, {@code OFFLINE}.
     */
    private String status;

    /**
     * The most recent measurement value recorded by this sensor.
     * Updated automatically on every successful POST to /readings.
     */
    private double currentValue;

    /** Foreign key linking this sensor to its parent Room. */
    private String roomId;

    // ------------------------------------------------------------------ //
    // Constructors
    // ------------------------------------------------------------------ //

    /** No-arg constructor required for Jackson deserialisation. */
    public Sensor() {
    }

    /**
     * Full constructor for programmatic creation.
     *
     * @param id           unique sensor ID
     * @param type         sensor category
     * @param status       operational state
     * @param currentValue latest reading value
     * @param roomId       ID of the room this sensor is deployed in
     */
    public Sensor(String id, String type, String status, double currentValue, String roomId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.currentValue = currentValue;
        this.roomId = roomId;
    }

    // ------------------------------------------------------------------ //
    // Getters & Setters
    // ------------------------------------------------------------------ //

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double v) {
        this.currentValue = v;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
