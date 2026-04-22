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
2.  **Open in IDE**: Open the project folder in Apache NetBeans (File > Open Project). NetBeans will recognize it as a Maven Web Application.
3.  **Clean and Build**: Right-click the project folder in NetBeans and select "Clean and Build". This generates the `smartcampus-api-1.0.war` file in the `target/` directory.
4.  **Deploy and Run**: 
    - Right-click the project and select "Run" (NetBeans will deploy the `.war` to your configured application server like Tomcat or GlassFish).
    - Alternatively, manually copy `target/smartcampus-api-1.0.war` into your server's deployment directory (`webapps` for Tomcat, `autodeploy` for GlassFish).
    *(Note: Depending on your server, the base URL below might include the project name, e.g., `http://localhost:8080/smartcampus-api-1.0/api/v1`).*

---

## 3. Sample curl Commands

### A. API Discovery
```bash
curl -X GET http://localhost:8080/smartcampus-api-1.0/api/v1
```

### B. Create a Room
```bash
curl -X POST http://localhost:8080/smartcampus-api-1.0/api/v1/rooms \
     -H "Content-Type: application/json" \
     -d '{"id":"LIB-301", "name":"Library Quiet Study", "capacity":50}'
```

### C. Register a Sensor
```bash
curl -X POST http://localhost:8080/smartcampus-api-1.0/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"id":"TEMP-001", "type":"Temperature", "roomId":"LIB-301"}'
```

### D. Post a Sensor Reading
```bash
curl -X POST http://localhost:8080/smartcampus-api-1.0/api/v1/sensors/TEMP-001/readings \
     -H "Content-Type: application/json" \
     -d '{"value":22.5}'
```

### E. Filter Sensors by Type
```bash
curl -X GET "http://localhost:8080/smartcampus-api-1.0/api/v1/sensors?type=Temperature"
```

---

## 4. Conceptual Report

### Part 1: Service Architecture

**Question:** In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

**Answer:** JAX-RS resource classes are by default comes with Request-scoped, that means when a new instance is created in the project every new incoming HTTP request will be destroyed after the response is completely sent. Because of this reason every instance field class does not have the ability to manage request state. For that I implemented a singleton datastore pattern using ConcurrentHashMap. This ensures that even though the controller is short it will pass the actual data to save memory space.

<br/>

**Question:** Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

**Answer:** So HATEOAS, also known as Hypermedia as the Engine of Application State, make the client form the server URI structure. By doing so Instead of hard coding the entire client navigation we can just use API link provided in response. By doing so developing this application become way easier and more scalable due to its available actions and evolving URI. 

<br/>

### Part 2: Room Management

**Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.

**Answer:** In room resource management returning only the IDs reduce its network bandwidth by forcing the client to make addition request such as “N” where it form each room for the N+1 problem, which can severally impact on its client-side processing side. By returning the full objects we can increase the size of their payload and initial the list request by providing all necessary data in a single round-trip.  

<br/>

**Question:** Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

**Answer:** Yes, by the operation alone make it idempotent. An operation idempotent will become multiple requests where it has the same effect on the server state as a new single state. In the experience I have implementation of ‘DELETE /rooms/id’ remove the room that is returning the request 204 with No Content. If the exact same request ended up being sent again, it will find no room to delete. 

<br/>

### Part 3: Sensor Operations

**Question:** We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

**Answer:** The main reason for that is when we use ‘@Consumes(MediaType.Application_JSON)’, in JAX-RS inspects for the given Content-Type where the request that has been sent will return an HTTP Unsupported media Type where the error is located. This associates the resource with the method that is never executed to protect the application from incompatible or malformed data. 

<br/>

**Question:** You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

**Answer:** The Pathe parameters such as ‘/sensors/type/CO2’ imply a rigid, structured hierarchy relationship where it becomes difficult to combine both multiple optional filters. By using Query parameters such as semantically designed for the searching, sorting and filtering I was able to get the flat collections. They are optional but easily combinable. Also, can be applied to any order without changing the fundamental URI. 

<br/>

### Part 4: Sub-Resources

**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

**Answer:** As mentioned in the question Sub-Resource locator pattern return the prompt of ‘Separation of Concerns’ where instead of having just one massive class we can have controllers handling each possible path, we delegate the correct nested logic to specialize in smaller resource where classes like ‘SensorReadingResource’ becomes codebase main modular. By following this the code becomes easier to read and easier to make unit tests. 

<br/>

### Part 5: Error Handling & Logging

**Question:** Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

**Answer:** As we all know HTTP 404 Not Found implies for the endpoints where the URI itself cannot be found. An HTTP 422 Unprocessable by an Entity. The meaning is when indicate that request URI, it’s correct and the JSON syntax can be perfectly valid. It is correctly separated through between “I cannot seem to find this endpoint routing” and “I can’t process your valid data.”

<br/>

**Question:** From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

**Answer:** Stack trace goes through the implementation, where it includes all the data such as class name, internal file paths and framework libraries and versions and stack trace is where one of these data is leaked. If this information got leaked the attacker can easily gather intelligence to map the server’s internal architecture and identify all the knows CVE expositing vulnerabilities in the specific framework version. 

<br/>

**Question:** Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

**Answer:** By using filters, we get the ability to ‘cross-cut concerns centrally without cluttering business logic. By using filters, we can ensure that ‘every’ request and response is automatically logged, including those rejected by JAX-RS before even reaching its resource method. This will enforce the DRY principle also known as Don’t repeat yourself. This makes the overall code clean and scalable. 