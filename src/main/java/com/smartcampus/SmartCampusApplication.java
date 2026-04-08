package com.smartcampus;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * JAX-RS Application configuration.
 *
 * 
 * Extends {@link ResourceConfig} (which itself extends
 * {@link javax.ws.rs.core.Application})
 * to satisfy the coursework requirement of implementing a subclass of
 * {@code javax.ws.rs.core.Application}.
 *
 * 
 * The {@code @ApplicationPath("/api/v1")} annotation establishes the versioned
 * entry point for all API endpoints. Jersey will automatically discover all
 * {@code @Provider} and {@code @Path} annotated classes in the
 * {@code com.smartcampus}
 * package hierarchy via the {@code packages()} call in the constructor.
 *
 * 
 * 5COSC022W Client-Server Architectures Coursework (2025/26)
 *
 * @author Rajapaksaha Thusew nambi Thenuja Hansana (20231300)
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {

    /**
     * Configures the application by enabling package scanning.
     * Jersey will find all resources, providers, and filters automatically.
     */
    public SmartCampusApplication() {
        // Explicitly register all resource classes, providers, and filters.
        // This is more robust than package scanning when running from an Uber-JAR.
        register(com.smartcampus.resource.DiscoveryResource.class);
        register(com.smartcampus.resource.RoomResource.class);
        register(com.smartcampus.resource.SensorResource.class);
        register(com.smartcampus.resource.SensorReadingResource.class);

        // Exception Mappers
        register(com.smartcampus.exception.RoomNotEmptyExceptionMapper.class);
        register(com.smartcampus.exception.LinkedResourceNotFoundExceptionMapper.class);
        register(com.smartcampus.exception.SensorUnavailableExceptionMapper.class);
        register(com.smartcampus.exception.GlobalExceptionMapper.class);

        // Filters
        register(com.smartcampus.filter.LoggingFilter.class);

        // JSON support
        register(org.glassfish.jersey.jackson.JacksonFeature.class);
    }
}
