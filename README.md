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

## 4. Conceptual Report (Answers to Questions)

### Part 1: Service Architecture
**Q: Explain the default lifecycle of a JAX-RS Resource class. How do you manage in-memory synchronization?**
**A:** By default, JAX-RS resource classes are **request-scoped**, meaning a new instance is created for every incoming HTTP request and destroyed after the response is sent. Because of this, instance fields in a resource class cannot persist data between requests. To manage state, I implemented a **Singleton DataStore** pattern using `ConcurrentHashMap`. This ensures that even though the controllers are short-lived, the actual data lives in a shared, thread-safe memory space.

**Q: Why is "Hypermedia" (HATEOAS) considered a hallmark of advanced RESTful design?**
**A:** HATEOAS (Hypermedia as the Engine of Application State) decouples the client from the server's URI structure. Instead of hard-coding URLs, clients navigate the API via links provided in responses (like our `/api/v1` discovery document). This allows developers to evolve the API's URL structure without breaking existing clients, much like how a human navigates a website by clicking links rather than typing every URL manually.

### Part 2: Room Management
**Q: Implications of returning only IDs versus full room objects in a list?**
**A:** Returning only IDs reduces initial network bandwidth but forces the client to make "N" additional requests to fetch details for each room (the N+1 problem). Returning full objects increases the payload size of the list but provides all necessary data in a single round-trip, significantly improving performance for mobile clients or high-latency networks.

**Q: Is the DELETE operation idempotent in your implementation? Justify.**
**A:** Yes, it is **idempotent**. An operation is idempotent if multiple identical requests have the same effect on the server state as a single request. In my implementation, the first `DELETE /rooms/LIB-301` removes the room (returning 204). Subsequent calls find no room to delete (returning 404), but the *final state* of the server is identical: the room does not exist.

### Part 3: Sensor Operations
**Q: Technical consequences if a client sends data in a format other than JSON?**
**A:** Because our methods are annotated with `@Consumes(MediaType.APPLICATION_JSON)`, JAX-RS will check the `Content-Type` header of the request. If a client sends `text/plain`, the runtime will automatically return an **HTTP 415 Unsupported Media Type** error. The resource method code is never even executed, protecting the application from processing incompatible data formats.

**Q: Why is the query parameter approach superior for filtering collections vs path parameters?**
**A:** Path parameters (e.g., `/sensors/type/CO2`) imply a rigid hierarchical relationship, making it difficult to combine filters (e.g., filtering by both type and status). Query parameters (e.g., `?type=CO2&status=ACTIVE`) are semantically designed for **searching, sorting, and filtering** non-hierarchical traits. They are optional and can be combined in any order without changing the resource identifier.

### Part 4: Sub-Resources
**Q: Discuss the architectural benefits of the Sub-Resource Locator pattern.**
**A:** The pattern promotes **Separation of Concerns**. Instead of one massive "God Class" handling every possible path, we delegate nested logic to specialized classes (e.g., `SensorReadingResource`). This makes the codebase modular, easier to unit test, and more maintainable as the complexity of the resource hierarchy grows.

### Part 5: Error Handling & Logging
**Q: Why is HTTP 422 often more semantically accurate than 404 for missing references?**
**A:** An **HTTP 404** implies the endpoint URI itself does not exist. An **HTTP 422 (Unprocessable Entity)** indicates that the request URI is correct and the JSON syntax is valid, but the *business logic* cannot be fulfilled (e.g., the `roomId` provided inside the JSON body refers to a non-existent entity). It distinguishes between "I can't find this page" and "I can't process this specific data."

**Q: Cybersecurity risks of exposing internal Java stack traces?**
**A:** Stack traces leak sensitive implementation details: class names, internal file paths, library versions, and the precise point of failure. An attacker can use this "Information Leakage" to identify known vulnerabilities in specific library versions or to map the server's internal architecture to craft more sophisticated exploits.

**Q: Why use JAX-RS filters for logging rather than Logger.info() in every method?**
**A:** Filters handle **cross-cutting concerns** centrally. Using a filter ensures that *every* request and response is logged, including those rejected by the JAX-RS runtime before reaching a resource method (like 405 Method Not Allowed). It enforces the DRY (Don't Repeat Yourself) principle, making the code cleaner and ensuring logging logic is applied consistently across the entire API.
