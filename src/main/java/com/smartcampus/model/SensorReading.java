package com.smartcampus.model;

/**
 * POJO representing a single historical measurement event for a Sensor.
 *
 * Readings are stored as an ordered list per sensor in the
 * {@link com.smartcampus.store.DataStore}. Every successfully persisted reading
 * also updates the parent sensor's {@code currentValue} field to maintain
 * cross-entity data consistency.
 *
 * The {@link #id} is always auto-generated as a UUID by the service layer;
 * any client-supplied ID is ignored. The {@link #timestamp} defaults to the
 * current epoch-millisecond time if the client omits it.
 *
 * 5COSC022W Client-Server Architectures Coursework (2025/26)
 *
 * @author Rajapaksaha Thusew nambi Thenuja Hansana (20231300)
 */
public class SensorReading {

    /** Auto-generated UUID that uniquely identifies this reading event. */
    private String id;

    /**
     * Unix epoch time (milliseconds) when the hardware produced this reading.
     * If not supplied by the client it is set to
     * {@code System.currentTimeMillis()}.
     */
    private long timestamp;

    /** The raw metric value captured by the sensor hardware. */
    private double value;

    // ------------------------------------------------------------------ //
    // Constructors
    // ------------------------------------------------------------------ //

    /** No-arg constructor required for Jackson deserialisation. */
    public SensorReading() {
    }

    /**
     * Full constructor used by the service to persist a new reading.
     *
     * @param id        auto-generated UUID
     * @param timestamp epoch-millisecond capture time
     * @param value     raw measurement value
     */
    public SensorReading(String id, long timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
