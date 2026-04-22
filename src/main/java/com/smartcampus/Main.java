package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

/**
 * Entry point for the SmartCampus REST API.
 *
 * Starts an embedded Grizzly HTTP server and registers all JAX-RS resources
 * through classpath scanning of the {@code com.smartcampus} package.
 *
 * The versioned API root is available at:
 * http://localhost:8080/api/v1
 *
 * 5COSC022W Client-Server Architectures Coursework (2025/26)
 *
 * @author Rajapaksaha Thusew nambi Thenuja Hansana (20231300)
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /** Base URI for the Grizzly HTTP server (before the application path). */
    public static final String BASE_URI = "http://localhost:8080/api/v1/";

    /**
     * Builds and starts the Grizzly server using the SmartCampusApplication config.
     *
     * @return the running {@link HttpServer} instance
     */
    public static HttpServer startServer() {
        // SmartCampusApplication extends ResourceConfig and is annotated
        // with @ApplicationPath("/api/v1"), so all resources are mounted under
        // http://localhost:8080/api/v1/
        return GrizzlyHttpServerFactory.createHttpServer(
                URI.create(BASE_URI), new SmartCampusApplication());
    }

    /**
     * Application entry point.
     *
     * Uses a {@link CountDownLatch} + JVM shutdown hook to keep the server
     * running until Ctrl+C or SIGTERM is received. This is more robust than
     * {@code System.in.read()}, which exits immediately when stdin is not
     * connected (e.g., when the JAR is launched from batch scripts or CI jobs).
     *
     * @param args command-line arguments (unused)
     * @throws InterruptedException if the blocking latch is interrupted
     */
    public static void main(String[] args) throws InterruptedException {
        final HttpServer server = startServer();

        LOGGER.info("====================================================");
        LOGGER.info("  SmartCampus API started successfully");
        LOGGER.info("  Discovery : http://localhost:8080/api/v1");
        LOGGER.info("  Rooms     : http://localhost:8080/api/v1/rooms");
        LOGGER.info("  Sensors   : http://localhost:8080/api/v1/sensors");
        LOGGER.info("  Press Ctrl+C to stop.");
        LOGGER.info("====================================================");

        // Block indefinitely — the shutdown hook releases the latch on
        // Ctrl+C / SIGTERM so the server shuts down gracefully.
        final CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutdown signal received — stopping server...");
            server.shutdownNow();
            latch.countDown();
        }));

        latch.await();
        LOGGER.info("Server stopped.");
    }
}
