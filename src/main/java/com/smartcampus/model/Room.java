package com.smartcampus.model;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO representing a physical Room in the SmartCampus system.
 *
 * A Room is the top-level resource. It holds references (by ID) to all
 * Sensors that are deployed within it. This "foreign-key" list is used to
 * enforce the safety constraint that a Room cannot be deleted while it still
 * has active sensors assigned to it.
 *
 * 5COSC022W Client-Server Architectures Coursework (2025/26)
 *
 * @author Rajapaksaha Thusew nambi Thenuja Hansana (20231300)
 */
public class Room {

    /** Unique identifier for this room (e.g., "LIB-301"). */
    private String id;

    /** Human-readable display name (e.g., "Library Quiet Study"). */
    private String name;

    /** Maximum occupancy for health and safety compliance. */
    private int capacity;

    /**
     * Ordered list of sensor IDs currently deployed in this room.
     * Kept in sync by SensorResource on sensor creation/deletion.
     */
    private List<String> sensorIds = new ArrayList<>();

    // ------------------------------------------------------------------ //
    // Constructors
    // ------------------------------------------------------------------ //

    /** No-arg constructor required for Jackson deserialisation. */
    public Room() {
    }

    /**
     * Convenience constructor used when the client provides all fields.
     *
     * @param id       unique room identifier
     * @param name     display name
     * @param capacity maximum occupancy
     */
    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<String> getSensorIds() {
        return sensorIds;
    }

    public void setSensorIds(List<String> sensorIds) {
        this.sensorIds = sensorIds;
    }
}
