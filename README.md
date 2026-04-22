# SmartCampus Sensor & Room Management API

**Author:** Rajapaksaha Thusew nambi Thenuja Hansana (20231300)  
**Module:** 5COSC022W Client-Server Architectures (2025/26)

---

## 1. API Design Overview

The **SmartCampusAPI** is a RESTful web service built with JAX-RS (Jersey) to manage campus infrastructure. The design follows a hierarchical resource model:

- **Rooms (`/api/v1/rooms`)**: The primary resource container.
- **Sensors (`/api/v1/sensors`)**: IoT hardware linked to specific rooms.
- **Readings (`/api/v1/sensors/{id}/readings`)**: Nested historical data managed via the **Sub-Resource Locator** pattern.

### Key Features:
- **HATEOAS Discovery**: A root entry point (`/api/v1`) providing resource maps.
- **Thread-Safe Storage**: In-memory data management using `ConcurrentHashMap` and synchronized lists, ensuring data integrity in a multi-threaded request environment.
- **Leak-Proof Error Handling**: Custom `ExceptionMappers` capture business logic violations (e.g., deleting a room with active sensors) and unexpected runtime errors, returning sanitized JSON responses.
- **API Observability**: Global filters log all incoming requests and outgoing responses.

---

## 2. Build & Launch Instructions

### Prerequisites
- Java 11 or higher
- Apache Maven 3.6+

### Execution Steps
1.  **Extract** the project folder.
2.  **Open terminal** in the `SmartCampusAPI` directory.
3.  **Clean and Build**:
    ```bash
    mvn clean install
    ```
4.  **Run the Server**:
    ```bash
    mvn exec:java -Dexec.mainClass="com.smartcampus.Main"
    ```
    *The server will start at `http://localhost:8080/api/v1`*

---

## 3. Sample curl Commands

### A. API Discovery
```bash
curl -X GET http://localhost:8080/api/v1
```

### B. Create a Room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
     -H "Content-Type: application/json" \
     -d '{"id":"LIB-301", "name":"Library Quiet Study", "capacity":50}'
```

### C. Register a Sensor
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"id":"TEMP-001", "type":"Temperature", "roomId":"LIB-301"}'
```

### D. Post a Sensor Reading
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
     -H "Content-Type: application/json" \
     -d '{"value":22.5}'
```

### E. Filter Sensors by Type
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"
```

---

## 4. Conceptual Report

### Part 1: Service Architecture

**Question:** In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

**Answer:** By default, JAX-RS resource classes are **request-scoped**, meaning a new instance is created for every incoming HTTP request and destroyed after the response is sent. Because of this, instance fields in a resource class cannot persist data between requests. To manage state, I implemented a **Singleton DataStore** pattern using `ConcurrentHashMap`. This ensures that even though the controllers are short-lived, the actual data lives in a shared, thread-safe memory space.

<br/>

**Question:** Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

**Answer:** HATEOAS (Hypermedia as the Engine of Application State) decouples the client from the server's URI structure. Instead of hard-coding URLs, clients navigate the API via links provided in responses (like our `/api/v1` discovery document). This allows developers to dynamically discover available actions and evolve the API's URL structure without breaking existing clients, much like how a human navigates a website by clicking links rather than typing every URL manually.

<br/>

### Part 2: Room Management

**Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.

**Answer:** Returning only IDs reduces initial network bandwidth but forces the client to make "N" additional requests to fetch details for each room (the N+1 problem), which can severely impact client-side processing time. Returning full objects increases the payload size of the initial list request but provides all necessary data in a single round-trip, significantly improving performance for high-latency networks.

<br/>

**Question:** Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

**Answer:** Yes, it is **idempotent**. An operation is idempotent if multiple identical requests have the same effect on the server state as a single request. In my implementation, the first `DELETE /rooms/{id}` removes the room (returning 204 No Content). If the exact same request is sent again, it finds no room to delete (returning 404 Not Found). Regardless of how many times the request is sent, the *final state* of the server remains identical: the room does not exist.

<br/>

### Part 3: Sensor Operations

**Question:** We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

**Answer:** Because our methods are annotated with `@Consumes(MediaType.APPLICATION_JSON)`, JAX-RS inspects the `Content-Type` header of every incoming request. If a client sends `text/plain` or `application/xml`, the JAX-RS runtime will automatically intercept the request and return an **HTTP 415 Unsupported Media Type** error. The associated resource method is never executed, which protects the application from trying to process incompatible or malformed data formats.

<br/>

**Question:** You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

**Answer:** Path parameters (e.g., `/sensors/type/CO2`) imply a rigid, structural hierarchical relationship which makes it difficult to combine multiple optional filters (e.g., filtering by both type and status). Query parameters (e.g., `?type=CO2&status=ACTIVE`) are semantically designed for **searching, sorting, and filtering** flat collections. They are optional, easily combinable, and can be applied in any order without changing the fundamental URI that identifies the resource.

<br/>

### Part 4: Sub-Resources

**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

**Answer:** The Sub-Resource Locator pattern promotes **Separation of Concerns**. Instead of having one massive "God Class" controller handling every possible path, we delegate nested logic to specialized, smaller resource classes (like `SensorReadingResource`). This makes the codebase modular, easier to read, much simpler to unit test, and significantly more maintainable as the complexity of the API's resource hierarchy grows.

<br/>

### Part 5: Error Handling & Logging

**Question:** Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

**Answer:** An **HTTP 404 Not Found** semantically implies the endpoint URI itself does not exist. An **HTTP 422 Unprocessable Entity** indicates that the request URI is correct and the JSON syntax is perfectly valid, but the *business logic* cannot be fulfilled (e.g., the `roomId` provided inside the JSON body refers to a room that doesn't exist). It correctly distinguishes between "I can't find this endpoint routing" and "I can't process your valid data."

<br/>

**Question:** From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

**Answer:** Stack traces leak sensitive implementation details, including class names, internal file paths, framework library versions, and the precise mathematical point of failure. This is called "Information Leakage." An attacker can use this gathered intelligence to map the server's internal architecture and identify known CVE (Common Vulnerabilities and Exposures) vulnerabilities in specific framework versions to craft customized, sophisticated exploits against the server.

<br/>

**Question:** Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

**Answer:** Filters allow us to handle **cross-cutting concerns** centrally without cluttering business logic. Using a filter ensures that *every* request and response is automatically logged, including those rejected by the JAX-RS runtime before ever reaching a resource method (such as a 405 Method Not Allowed error). It enforces the DRY (Don't Repeat Yourself) principle, makes the code cleaner, and guarantees logging is applied consistently across the entire API indiscriminately.
