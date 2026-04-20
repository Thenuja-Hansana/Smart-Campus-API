package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Part 1 — The "Discovery" Endpoint.
 *
 * Provides a root discovery document at {@code GET /api/v1}. The response
 * includes API versioning information, an administrative contact, and a map of
 * primary resource collection URIs — implementing a basic form of HATEOAS
 * (Hypermedia as the Engine of Application State).
 *
 * This allows API clients to navigate the entire resource hierarchy starting
 * from a single well-known entry point, without relying on hard-coded URLs
 * embedded in client code.
 *
 * 5COSC022W Client-Server Architectures Coursework (2025/26)
 *
 * @author Rajapaksaha Thusew nambi Thenuja Hansana (20231300)
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    /**
     * Returns the API discovery document.
     *
     * <p>
     * Sample response:
     * 
     * <pre>
     * {
     *   "apiName":     "SmartCampus Sensor & Room Management API",
     *   "version":     "1.0",
     *   "description": "RESTful API for managing campus rooms and IoT sensors.",
     *   "author":      "Rajapaksaha Thusew nambi Thenuja Hansana (20231300)",
     *   "contact":     "admin@smartcampus.ac.uk",
     *   "resources": {
     *     "rooms":   "/api/v1/rooms",
     *     "sensors": "/api/v1/sensors"
     *   }
     * }
     * </pre>
     *
     * @param uriInfo injected context to build absolute URIs dynamically
     * @return HTTP 200 with the discovery JSON object
     */
    @GET
    public Response discover(@Context UriInfo uriInfo) {
        Map<String, Object> resources = new LinkedHashMap<>();
        resources.put("rooms", uriInfo.getBaseUriBuilder().path(RoomResource.class).build().toString());
        resources.put("sensors", uriInfo.getBaseUriBuilder().path(SensorResource.class).build().toString());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("apiName", "SmartCampus Sensor & Room Management API");
        body.put("version", "1.0");
        body.put("description", "RESTful API for managing campus rooms and IoT sensors.");
        body.put("author", "Rajapaksaha Thusew nambi Thenuja Hansana (20231300)");
        body.put("contact", "admin@smartcampus.ac.uk");
        body.put("resources", resources);

        return Response.ok(body).build();
    }
}
