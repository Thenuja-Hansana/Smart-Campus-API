# 🏫 Smart Campus Sensor & Room Management API
A RESTful API built with JAX-RS (Jersey) for managing campus rooms and IoT sensors.  
Module: 5COSC022W – Client-Server Architectures | University of Westminster

---

## 📋 Table of Contents
1. [API Design Overview](#api-design-overview)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Build & Run Instructions](#build--run-instructions)
5. [API Endpoints Reference](#api-endpoints-reference)
6. [Sample curl Commands](#sample-curl-commands)
7. [Conceptual Report – Question Answers](#conceptual-report--question-answers)

---

## API Design Overview
The Smart Campus API follows REST architectural principles to expose three core resources:

| Resource | Base Path | Description |
|---|---|---|
| Discovery | `GET /api/v1` | API metadata and HATEOAS links |
| Rooms | `/api/v1/rooms` | Campus room management |
| Sensors | `/api/v1/sensors` | IoT sensor registration and management |
| Readings | `/api/v1/sensors/{id}/readings` | Historical sensor reading logs |

**Design Decisions**
- In-memory storage using `ConcurrentHashMap` (thread-safe, no database required)
- Per-request JAX-RS lifecycle with a singleton `DataStore` for safe shared state
- Nested sub-resources via the Sub-Resource Locator pattern for sensor readings
- Exception Mappers for every error scenario — no raw stack traces ever returned
- Cross-cutting logging via JAX-RS filters applied globally to all endpoints

---

## Technology Stack
| Component | Technology |
|---|---|
| Language | Java 11 |
| Framework | JAX-RS 2.1 (Jersey 2.41) |
| JSON | Jackson Databind 2.15 |
| Build Tool | Maven 3.x |
| IDE | NetBeans (Web Application project) |
| Server | Apache Tomcat (bundled with NetBeans) |
| Storage | In-memory `ConcurrentHashMap` / `ArrayList` |

> ⚠️ **No Spring Boot. No SQL database. Pure JAX-RS only.**

---

## Project Structure
```
SmartCampusAPI/
├── pom.xml
└── src/
    └── main/
        ├── java/com/smartcampus/
        │   ├── SmartCampusApplication.java     ← @ApplicationPath("/api/v1")
        │   ├── DataStore.java                  ← Singleton in-memory data store
        │   ├── model/
        │   │   ├── Room.java
        │   │   ├── Sensor.java
        │   │   └── SensorReading.java
        │   ├── resource/
        │   │   ├── DiscoveryResource.java       ← GET /api/v1
        │   │   ├── RoomResource.java            ← /api/v1/rooms
        │   │   ├── SensorResource.java          ← /api/v1/sensors
        │   │   └── SensorReadingResource.java   ← /api/v1/sensors/{id}/readings
        │   ├── exception/
        │   │   ├── RoomNotEmptyException.java
        │   │   ├── RoomNotEmptyExceptionMapper.java
        │   │   ├── LinkedResourceNotFoundException.java
        │   │   ├── LinkedResourceNotFoundExceptionMapper.java
        │   │   ├── SensorUnavailableException.java
        │   │   ├── SensorUnavailableExceptionMapper.java
        │   │   └── GlobalExceptionMapper.java
        │   └── filter/
        │       └── LoggingFilter.java
```

---

## Build & Run Instructions

**Prerequisites**
- Java JDK 11 or higher installed
- Apache Maven 3.6+ installed
- NetBeans IDE (with Tomcat/GlassFish bundled)

**Option A: Run in NetBeans (Recommended)**
1. Open NetBeans and go to `File → Open Project`
2. Navigate to the `SmartCampusAPI` folder and open it
3. Right-click the project → Clean and Build
4. Right-click the project → Run
5. NetBeans will deploy the WAR to the bundled Tomcat server
6. The API will be available at: `http://localhost:8080/Smart-Campus-API-main/api/v1`

**Option B: Build with Maven CLI**
```bash
# 1. Clone the repository
git clone https://github.com/YOUR_USERNAME/SmartCampusAPI.git
cd SmartCampusAPI

# 2. Build the WAR file
mvn clean package

# 3. Deploy to Tomcat
# Copy target/smartcampus-api-1.0.war to your Tomcat webapps/ directory
cp target/smartcampus-api-1.0.war /path/to/tomcat/webapps/

# 4. Start Tomcat
/path/to/tomcat/bin/startup.sh   # Linux/Mac
/path/to/tomcat/bin/startup.bat  # Windows

# 5. Access the API
curl http://localhost:8080/Smart-Campus-API-main/api/v1
```

Verify the server is running
```bash
curl -s http://localhost:8080/Smart-Campus-API-main/api/v1 | python -m json.tool
```
You should see the discovery response with version info and resource links.

---

## API Endpoints Reference

**Discovery**
| Method | Path | Description |
|---|---|---|
| GET | `/api/v1` | API metadata and HATEOAS navigation links |

**Rooms**
| Method | Path | Description | Success Code |
|---|---|---|---|
| GET | `/api/v1/rooms` | List all rooms | 200 |
| POST | `/api/v1/rooms` | Create a new room | 201 |
| GET | `/api/v1/rooms/{roomId}` | Get room by ID | 200 |
| DELETE | `/api/v1/rooms/{roomId}` | Delete room (blocked if sensors exist) | 200 |

**Sensors**
| Method | Path | Description | Success Code |
|---|---|---|---|
| GET | `/api/v1/sensors` | List all sensors | 200 |
| GET | `/api/v1/sensors?type=CO2` | Filter sensors by type | 200 |
| POST | `/api/v1/sensors` | Register a new sensor | 201 |
| GET | `/api/v1/sensors/{sensorId}` | Get sensor by ID | 200 |
| DELETE | `/api/v1/sensors/{sensorId}` | Remove a sensor | 200 |

**Sensor Readings (Sub-Resource)**
| Method | Path | Description | Success Code |
|---|---|---|---|
| GET | `/api/v1/sensors/{sensorId}/readings` | Get all readings for a sensor | 200 |
| POST | `/api/v1/sensors/{sensorId}/readings` | Append a new reading | 201 |
| GET | `/api/v1/sensors/{sensorId}/readings/{readingId}` | Get a specific reading | 200 |

**Error Responses**
| Scenario | Status Code |
|---|---|
| Room deleted with sensors assigned | 409 Conflict |
| Sensor created with non-existent roomId | 422 Unprocessable Entity |
| Reading posted to MAINTENANCE sensor | 403 Forbidden |
| Resource not found | 404 Not Found |
| Duplicate resource ID | 409 Conflict |
| Any unexpected server error | 500 Internal Server Error |

---

## Sample curl Commands
> Base URL: `http://localhost:8080/Smart-Campus-API-main/api/v1`  

---

**1. Discover the API (GET /api/v1)**
```bash
curl -X GET http://localhost:8080/Smart-Campus-API-main/api/v1 \
  -H "Accept: application/json"
```
Expected: 200 OK with API metadata and resource links.

---

**2. List all Rooms (GET /api/v1/rooms)**
```bash
curl -X GET http://localhost:8080/Smart-Campus-API-main/api/v1/rooms \
  -H "Accept: application/json"
```
Expected: 200 OK with array of all room objects including sensor IDs.

---

**3. Create a new Room (POST /api/v1/rooms)**
```bash
curl -X POST http://localhost:8080/Smart-Campus-API-main/api/v1/rooms \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{"id": "SCI-205", "name": "Science Lab 205", "capacity": 40}'
```
Expected: 201 Created with the new room object.

---

**4. Get a specific Room (GET /api/v1/rooms/{roomId})**
```bash
curl -X GET http://localhost:8080/Smart-Campus-API-main/api/v1/rooms/LIB-301 \
  -H "Accept: application/json"
```
Expected: 200 OK with room details including assigned sensor IDs.

---

**5. Attempt to Delete a Room with Sensors (409 Conflict)**
```bash
curl -X DELETE http://localhost:8080/Smart-Campus-API-main/api/v1/rooms/LIB-301 \
  -H "Accept: application/json"
```
Expected: 409 Conflict — room has sensors assigned, deletion blocked.

---

**6. Delete an empty Room (DELETE /api/v1/rooms/{roomId})**
```bash
# First create a room with no sensors
curl -X POST http://localhost:8080/Smart-Campus-API-main/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id": "EMPTY-01", "name": "Empty Test Room", "capacity": 10}'

# Then delete it successfully
curl -X DELETE http://localhost:8080/Smart-Campus-API-main/api/v1/rooms/EMPTY-01 \
  -H "Accept: application/json"
```
Expected: 200 OK — room deleted successfully.

---

**7. List all Sensors (GET /api/v1/sensors)**
```bash
curl -X GET http://localhost:8080/Smart-Campus-API-main/api/v1/sensors \
  -H "Accept: application/json"
```
Expected: 200 OK with all sensors.

---

**8. Filter Sensors by type (GET /api/v1/sensors?type=Temperature)**
```bash
curl -X GET "http://localhost:8080/Smart-Campus-API-main/api/v1/sensors?type=Temperature" \
  -H "Accept: application/json"
```
Expected: 200 OK with only Temperature-type sensors.

---

**9. Register a new Sensor (POST /api/v1/sensors)**
```bash
curl -X POST http://localhost:8080/Smart-Campus-API-main/api/v1/sensors \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{"id": "LIGHT-001", "type": "Light", "status": "ACTIVE", "currentValue": 350.0, "roomId": "LIB-301"}'
```
Expected: 201 Created with new sensor details.

---

**10. Register Sensor with invalid roomId (422 Unprocessable Entity)**
```bash
curl -X POST http://localhost:8080/Smart-Campus-API-main/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "BAD-001", "type": "Temperature", "status": "ACTIVE", "currentValue": 0.0, "roomId": "NONEXISTENT-ROOM"}'
```
Expected: 422 Unprocessable Entity — roomId does not exist.

---

**11. Get readings history for a sensor (GET /api/v1/sensors/{sensorId}/readings)**
```bash
curl -X GET http://localhost:8080/Smart-Campus-API-main/api/v1/sensors/TEMP-001/readings \
  -H "Accept: application/json"
```
Expected: 200 OK with reading history array.

---

**12. Post a new reading to an ACTIVE sensor (POST /api/v1/sensors/{sensorId}/readings)**
```bash
curl -X POST http://localhost:8080/Smart-Campus-API-main/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{"value": 24.7}'
```
Expected: 201 Created. Note that `currentValue` on the parent sensor is also updated.

---

**13. Post a reading to a MAINTENANCE sensor (403 Forbidden)**
```bash
curl -X POST http://localhost:8080/Smart-Campus-API-main/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 15.0}'
```
Expected: 403 Forbidden — sensor OCC-001 is under MAINTENANCE.

---

**14. Trigger a 404 Not Found error**
```bash
curl -X GET http://localhost:8080/Smart-Campus-API-main/api/v1/rooms/FAKE-ROOM \
  -H "Accept: application/json"
```
Expected: 404 Not Found with descriptive JSON error body.

---

# Conceptual Report – Question Answers

---

### Part 1 – Setup & Discovery

**Q1: JAX-RS Resource Class Lifecycle**
By default, JAX-RS follows a per-request lifecycle: a new instance of each resource class is created for every incoming HTTP request and discarded after the response is sent. This means instance fields are never shared between requests.
This architectural decision has direct implications for in-memory state management. If data were stored as instance fields in a resource class (e.g., `private Map<String, Room> rooms = new HashMap<>()`), each request would get its own fresh, empty map — meaning any data created in one request would be invisible to the next. This is data loss.
Solution used in this project: A singleton `DataStore` class backed by `ConcurrentHashMap` holds all application data. Since it is a single shared instance (via `DataStore.getInstance()`), all per-request resource instances access the same underlying maps. `ConcurrentHashMap` is used instead of `HashMap` because multiple request threads may read and write concurrently — `ConcurrentHashMap` provides thread-safe operations at the bucket level without requiring `synchronized` blocks on every method call, preventing race conditions while maintaining high throughput.

---

**Q2: Why HATEOAS is a Hallmark of Advanced RESTful Design**
HATEOAS (Hypermedia As The Engine Of Application State) is Richardson Maturity Level 3 — the highest level of REST maturity. In a HATEOAS-compliant API, each response includes hyperlinks that tell the client what actions are available next and where to find related resources.
Benefits over static documentation:
- **Self-discovery:** Clients navigate the API by following links in responses rather than hard-coding URLs. A client hitting `GET /api/v1` receives links to `/api/v1/rooms` and `/api/v1/sensors` directly in the JSON — no external docs required.
- **Reduced coupling:** If the server reorganises its URL structure (e.g., `/api/v2/rooms`), HATEOAS clients automatically follow the new links without code changes. Clients relying on static docs would break.
- **Discoverability:** Client developers can explore the API interactively. Each response acts as a menu of next possible actions.
- **Evolvability:** New capabilities can be added by introducing new link relations. Old clients that don't understand the new links simply ignore them — backwards compatibility is preserved.
In this project, the Discovery endpoint (`GET /api/v1`) returns a `links` object with navigation pointers to all primary resource collections, embodying the HATEOAS principle.

---

### Part 2 – Room Management

**Q3: Returning IDs vs Full Room Objects in a List**
| Approach | Pros | Cons |
|---|---|---|
| IDs only | Minimal payload, fast for large collections | Requires N+1 follow-up GET requests for details |
| Full objects | Single request returns everything needed | Larger payload, wastes bandwidth if client only needs IDs |

Implications:
- **IDs only** (`["LIB-301", "LAB-101"]`): Reduces initial response size but causes the N+1 problem — for a list of 100 rooms, the client must fire 100 individual `GET /rooms/{id}` requests to get names and capacities. This multiplies network latency and server load.
- **Full objects** (this project's approach): The client receives everything in one call. More appropriate for collections of moderate size. For very large datasets, pagination (e.g., `?page=1&size=20`) is the standard mitigation to control response size while still returning full objects per page.
Best practice is to return full objects with pagination for most APIs, with optional field projection (`?fields=id,name`) for clients that genuinely only need a subset.

---

**Q4: Is DELETE Idempotent?**
Yes, DELETE is idempotent in this implementation.
Idempotency means that making the same request multiple times produces the same server state, regardless of how many times it is called.
- First DELETE on `ROOM-X`: Room is removed. Server state = room absent. Response = 200 OK.
- Second DELETE on `ROOM-X`: Room is already gone. Server state = room still absent (unchanged). Response = 404 Not Found.
The server state is identical after both calls — the room does not exist in either case. The response status code differs (200 vs 404), but idempotency is defined by state, not by response code. Therefore this implementation is fully idempotent.
The business constraint (blocking deletion of rooms with sensors) does not affect idempotency — it is a pre-condition check, not a violation of the idempotency property.

---

### Part 3 – Sensor Operations & Linking

**Q5: Consequences of @Consumes(APPLICATION_JSON) Mismatch**
The `@Consumes(MediaType.APPLICATION_JSON)` annotation declares that the POST method only accepts requests with `Content-Type: application/json`.
If a client sends data with `Content-Type: text/plain` or `Content-Type: application/xml`, JAX-RS performs content negotiation before the method is invoked:
- The runtime scans all resource methods matching the request path and HTTP method.
- It filters candidates by matching the request's `Content-Type` against each method's `@Consumes` annotation.
- If no method accepts the submitted content type, JAX-RS automatically returns HTTP 415 Unsupported Media Type — before any application code runs.
The developer does not need to write any validation code for this. The framework enforces it declaratively. The client receives a `415` response indicating they must change their `Content-Type` header to `application/json`.

---

**Q6: @QueryParam vs Path-based Filtering**
`@QueryParam` approach (used): `GET /api/v1/sensors?type=CO2`  
Path-based alternative: `GET /api/v1/sensors/type/CO2`
Why @QueryParam is superior for filtering:
- **Optional by design:** Query parameters are inherently optional. Omitting `?type=` returns all sensors; including it filters the results. Path segments cannot be optional without defining multiple `@Path` patterns.
- **No routing conflicts:** `/sensors/{sensorId}` already uses a path parameter for sensor IDs. Adding `/sensors/type/{value}` creates ambiguity — JAX-RS cannot tell whether `type` is a sensor ID or a keyword.
- **REST semantics:** Path segments should identify resources (specific rooms, sensors). Query parameters are the conventional REST idiom for modifying the representation — filtering, sorting, pagination, and searching. `?type=CO2` says "give me the sensors collection, filtered by type", not "navigate to a 'type' resource".
- **Composability:** Multiple filters compose naturally: `?type=CO2&status=ACTIVE`. Path-based approaches cannot stack without deeply nested and brittle URL designs.
- **Industry standard:** All major APIs (GitHub, Google, Twitter) use query parameters for filtering collections.

---

### Part 4 – Sub-Resources

**Q7: Architectural Benefits of the Sub-Resource Locator Pattern**
The Sub-Resource Locator pattern delegates the handling of a nested path segment to a dedicated resource class. In this project, `SensorResource` has no `@GET`/`@POST` methods for `/readings` — it simply returns a `SensorReadingResource` instance:
```java
@Path("/{sensorId}/readings")
public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
    return new SensorReadingResource(sensorId);
}
```
Benefits over a monolithic controller:
- **Separation of concerns:** `SensorResource` manages sensor CRUD; `SensorReadingResource` manages readings. Neither knows about the other's internal logic.
- **Manageable complexity:** A single class handling `/sensors`, `/sensors/{id}`, `/sensors/{id}/readings`, and `/sensors/{id}/readings/{rid}` would grow to hundreds of lines. Sub-resources keep each class focused and readable.
- **Independent testing:** `SensorReadingResource` can be unit-tested in isolation by instantiating it directly with a `sensorId`, without needing to invoke the full request pipeline.
- **Lazy instantiation:** JAX-RS only instantiates `SensorReadingResource` when the `/readings` path is actually requested — no overhead for requests that don't need it.
- **Scalability:** The pattern scales to arbitrary nesting depth. Adding `/readings/{readingId}/annotations` simply requires another locator in `SensorReadingResource`.

---

### Part 5 – Error Handling & Logging

**Q8: Why HTTP 422 is More Semantically Accurate Than 404 for Missing References**
When a client POSTs a new sensor with `"roomId": "NONEXISTENT-ROOM"`:
- The endpoint `/api/v1/sensors` was found (no 404 there).
- The JSON payload is syntactically valid.
- The semantic problem is that a field value inside the payload references a resource that doesn't exist.
404 Not Found means the requested URL was not found. Using 404 here would mislead the client into thinking the `/sensors` endpoint doesn't exist.
422 Unprocessable Entity means: "I understand your request, I can parse your JSON, but the semantic content of the payload is invalid." It was designed precisely for this scenario — when the server understands the request structure but cannot process it due to business rule violations or invalid references within the data.
RFC 4918 defines 422 as: "The server understands the content type of the request entity, and the syntax of the request entity is correct, but it was unable to process the contained instructions."
Using 422 gives the client a precise, actionable signal: the problem is inside the payload data, specifically an invalid reference — not a wrong URL.

---

**Q9: Security Risks of Exposing Java Stack Traces**
Exposing raw stack traces to API consumers is a CWE-209 (Information Exposure Through Error Messages) vulnerability. An attacker can extract:
- **Framework and library versions:** Stack traces reveal exact class names like `org.glassfish.jersey.server.ServerRuntime` or `com.fasterxml.jackson.databind.JsonMappingException`. The attacker looks up the version in Maven coordinates and searches CVE databases for known exploits targeting that specific version.
- **Internal package structure:** Full class paths reveal the application's internal architecture — package names, class names, and line numbers — making it far easier to reason about the codebase.
- **Technology fingerprinting:** A trace touching `org.apache.tomcat`, `jersey`, and `jackson` in sequence reveals the full stack: Tomcat + Jersey + Jackson. Each is a known attack surface.
- **Business logic leakage:** Variable values, SQL query fragments (if any), file paths, and internal IP addresses may appear in exception messages embedded in the trace.
- **Exploit targeting:** Line numbers make it trivial to correlate with public decompiled code or GitHub repos, helping an attacker craft a precise payload that triggers a specific code path.
Mitigation (implemented): The `GlobalExceptionMapper` logs the full trace server-side via `java.util.logging.Logger` where only authorised admins can see it, while returning only a generic `500 Internal Server Error` message to the client — no internal details whatsoever.

---

**Q10: Why JAX-RS Filters Are Better Than Manual Logger Calls for Cross-Cutting Concerns**
Logging is a cross-cutting concern — it applies to every endpoint regardless of business logic. The problems with manual `Logger.info()` calls in every resource method:
- **DRY violation:** The same boilerplate is copy-pasted into dozens of methods. Any change to the log format requires touching every single method.
- **Inconsistency risk:** Developers forget to add logging to new methods. Some methods log before processing, others after — the log output becomes unreliable.
- **Exception blindspot:** If a resource method throws an exception before reaching the `Logger.info()` call, the request is never logged. Filters execute regardless of whether the method throws.
- **Separation of concerns violated:** Business logic methods are polluted with infrastructure concerns.

Advantages of JAX-RS Filters:
- **Automatic application:** Registered once, applies to every request/response across the entire application — including future endpoints added by other developers.
- **Guaranteed execution:** `ContainerRequestFilter` fires before any resource method; `ContainerResponseFilter` fires after, even if an exception was mapped.
- **Composable:** Multiple filters can be chained (e.g., logging + authentication + CORS) without modifying resource classes.
- **Testable in isolation:** The filter can be unit-tested independently of any resource.

This is the AOP (Aspect-Oriented Programming) philosophy applied to JAX-RS: infrastructure concerns are separated into dedicated components that wrap business logic transparently.