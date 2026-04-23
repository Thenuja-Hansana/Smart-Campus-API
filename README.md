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

**No Spring Boot. No SQL database. Pure JAX-RS only.**

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
git clone [https://github.com/YOUR_USERNAME/SmartCampusAPI.git](https://github.com/Thenuja-Hansana/Smart-Campus-API.git

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

### Part 1: Service Architecture

**Question:** In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

**Answer:** JAX-RS resource classes are by default comes with Request-scoped, that means when a new instance is created in the project every new incoming HTTP request will be destroyed after the response is completely sent. Because of this reason every instance field class does not have the ability to manage request state. For that I implemented a singleton datastore pattern using ConcurrentHashMap. This ensures that even though the controller is short it will pass the actual data to save memory space.

---

**Question:** Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

**Answer:** So HATEOAS, also known as Hypermedia as the Engine of Application State, make the client form the server URI structure. By doing so Instead of hard coding the entire client navigation we can just use API link provided in response. By doing so developing this application become way easier and more scalable due to its available actions and evolving URI.

---

### Part 2: Room Management

**Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.

**Answer:** In room resource management returning only the IDs reduce its network bandwidth by forcing the client to make addition request such as “N” where it form each room for the N+1 problem, which can severally impact on its client-side processing side. By returning the full objects we can increase the size of their payload and initial the list request by providing all necessary data in a single round-trip.

---

**Question:** Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

**Answer:** Yes, by the operation alone make it idempotent. An operation idempotent will become multiple requests where it has the same effect on the server state as a new single state. In the experience I have implementation of ‘DELETE /rooms/id’ remove the room that is returning the request 204 with No Content. If the exact same request ended up being sent again, it will find no room to delete.

---

### Part 3: Sensor Operations

**Question:** We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

**Answer:** The main reason for that is when we use ‘@Consumes(MediaType.Application_JSON)’, in JAX-RS inspects for the given Content-Type where the request that has been sent will return an HTTP Unsupported media Type where the error is located. This associates the resource with the method that is never executed to protect the application from incompatible or malformed data.

---

**Question:** You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

**Answer:** The Pathe parameters such as ‘/sensors/type/CO2’ imply a rigid, structured hierarchy relationship where it becomes difficult to combine both multiple optional filters. By using Query parameters such as semantically designed for the searching, sorting and filtering I was able to get the flat collections. They are optional but easily combinable. Also, can be applied to any order without changing the fundamental URI.

---

### Part 4: Sub-Resources

**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

**Answer:** As mentioned in the question Sub-Resource locator pattern return the prompt of ‘Separation of Concerns’ where instead of having just one massive class we can have controllers handling each possible path, we delegate the correct nested logic to specialize in smaller resource where classes like ‘SensorReadingResource’ becomes codebase main modular. By following this the code becomes easier to read and easier to make unit tests.

---

### Part 5: Error Handling & Logging

**Question:** Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

**Answer:** As we all know HTTP 404 Not Found implies for the endpoints where the URI itself cannot be found. An HTTP 422 Unprocessable by an Entity. The meaning is when indicate that request URI, it’s correct and the JSON syntax can be perfectly valid. It is correctly separated through between “I cannot seem to find this endpoint routing” and “I can’t process your valid data.”

---

**Question:** From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

**Answer:** Stack trace goes through the implementation, where it includes all the data such as class name, internal file paths and framework libraries and versions and stack trace is where one of these data is leaked. If this information got leaked the attacker can easily gather intelligence to map the server’s internal architecture and identify all the knows CVE expositing vulnerabilities in the specific framework version.

---

**Question:** Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

**Answer:** By using filters, we get the ability to ‘cross-cut concerns centrally without cluttering business logic. By using filters, we can ensure that ‘every’ request and response is automatically logged, including those rejected by JAX-RS before even reaching its resource method. This will enforce the DRY principle also known as Don’t repeat yourself. This makes the overall code clean and scalable.
