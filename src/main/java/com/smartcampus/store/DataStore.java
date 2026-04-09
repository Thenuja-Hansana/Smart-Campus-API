package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Application-wide singleton in-memory data store.
 *
 * All three resource collections are held in {@link ConcurrentHashMap}
 * instances,
 * which are thread-safe for individual read/write operations. This is required
 * because
 * JAX-RS creates a new resource-class instance per HTTP request (request-scoped
 * lifecycle), so instance fields inside resource classes are NOT shared.
 * Storing state
 * in this singleton ensures data persists across requests.
 *
 * The readings map is keyed by sensor ID and holds an ordered list of
 * {@link SensorReading} objects. Wrapping the per-sensor list with
 * {@code Collections.synchronizedList()} prevents race conditions when multiple
 * concurrent POST requests target the same sensor's readings.
 *
 * No external database is used.This fulfils the coursework
 * constraint requiring in-memory data structures only.
 *
 * 5COSC022W Client-Server Architectures Coursework (2025/26)
 *
 * @author Rajapaksaha Thusew nambi Thenuja Hansana (20231300)
 */
public class DataStore {

    // ------------------------------------------------------------------ //
    // Singleton wiring
    // ------------------------------------------------------------------ //

    private static final DataStore INSTANCE = new DataStore();

    /** Private constructor prevents external instantiation. */
    private DataStore() {
    }

    /**
     * Returns the single shared instance of this data store.
     *
     * @return the singleton {@link DataStore}
     */
    public static DataStore getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------ //
    // In-memory collections (ConcurrentHashMap — thread-safe)
    // ------------------------------------------------------------------ //

    /**
     * All registered campus Rooms, keyed by room ID.
     * Example key: {@code "LIB-301"}
     */
    public final Map<String, Room> rooms = new ConcurrentHashMap<>();

    /**
     * All registered Sensors, keyed by sensor ID.
     * Example key: {@code "TEMP-001"}
     */
    public final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    /**
     * Historical readings per sensor, keyed by sensor ID.
     * Each value is a synchronised ordered list of {@link SensorReading} objects.
     * A new entry is created automatically when the first reading is posted.
     */
    public final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    // ------------------------------------------------------------------ //
    // Convenience helpers
    // ------------------------------------------------------------------ //

    /**
     * Retrieves the readings list for the given sensor ID.
     * If no list exists yet (first ever reading for this sensor), a new
     * synchronised list is created, stored, and returned.
     *
     * @param sensorId the sensor whose readings list is requested
     * @return the thread-safe ordered list of readings for that sensor
     */
    public List<SensorReading> getOrCreateReadings(String sensorId) {
        return readings.computeIfAbsent(sensorId,
                id -> java.util.Collections.synchronizedList(new ArrayList<>()));
    }
}
