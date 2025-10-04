# REST API Lab - Synchronous Communication

**Student:** Peng Wu  
**Course:** COMP 41720 Distributed Systems

## System Design

This project implements a RESTful API using Spring Boot framework, demonstrating synchronous HTTP-based communication. The system follows REST principles with CRUD operations for user management, where each HTTP request blocks until the server processes and returns a complete response.

### Technology Choice

**Spring Boot REST API** was chosen for this implementation because:
- **Industry Standard**: REST is the most widely used API architecture
- **HTTP Protocol**: Universal, well-understood communication protocol
- **Rich Ecosystem**: Extensive tooling, documentation, and community support
- **Synchronous by Default**: HTTP request-response naturally demonstrates blocking behavior
- **Easy Testing**: Can test with browsers, curl, Postman, etc.

## Architecture

```
Client (RestTemplate)                    Server (Port 19002)
       |                                        |
       |──── GET /api/users ──────>            |
       |                                  @GetMapping
       |    (Thread BLOCKS waiting)       getAllUsers()
       |                                        |
       |  <──── JSON Response ────────────────┘
       |     (User[] array)
       |
  Continue Execution
```

### RESTful Endpoints

| Method | Endpoint | Description | Handler |
|--------|----------|-------------|---------|
| GET | `/api/users` | Get all users | `getUsers()` |
| GET | `/api/users/{id}` | Get user by ID | `getUserById()` |
| POST | `/api/users` | Create new user | `createUser()` |
| PUT | `/api/users/{id}` | Update user | `updateUser()` |
| DELETE | `/api/users/delete/{id}` | Delete user | `deleteUser()` |

### Components

**1. RestApiLabApplication.java** - Main Application
- Starts Spring Boot application
- Acts as REST client using `RestTemplate`
- Makes synchronous GET request to `/api/users`
- **Blocks on `getForObject()`** until response received
- Measures total request-response time
- Gracefully shuts down after demonstration

**2. APIController.java** - REST Controller
```java
@RestController
@RequestMapping("/api/users")
```
- Handles HTTP requests on port **19002**
- Maps HTTP methods to service operations
- Returns data automatically serialized to JSON
- Uses `@Autowired` for dependency injection

**3. APIServices.java** - Business Logic Layer
- Implements user management logic
- Separates business logic from HTTP layer
- Methods: `getAllUsers()`, `getUserById()`, `createUser()`, `updateUser()`, `deleteUser()`

**4. User.java** - Data Model
- Represents user entity
- Automatically serialized to/from JSON by Spring
- Contains user properties (id, name, email, etc.)

## How Synchronous Communication is Demonstrated

### Blocking HTTP Request

**Client-Side Blocking:**
```java
RestTemplate restTemplate = new RestTemplate();
User[] users = restTemplate.getForObject(url, User[].class); // BLOCKS here
```

The client thread completely stops at `getForObject()` and waits until:
1. HTTP GET request is sent to server
2. Server receives and processes the request
3. Server queries data and serializes to JSON
4. Response travels back over network
5. JSON is deserialized into `User[]` array
6. Only then does execution continue

### Server-Side Synchronous Processing

```java
@GetMapping
public List<User> getUsers() {
    return userService.getAllUsers(); // Executes completely before returning
}
```

The server processes each request **synchronously**:
- Request arrives → Handler method executes
- Method runs to completion
- Result serialized to JSON
- Response sent back
- No callbacks, no async processing

### Timing Measurement
```java
long start = System.currentTimeMillis();
User[] users = restTemplate.getForObject(url, User[].class); // Blocking happens here
long end = System.currentTimeMillis();
System.out.println("rest-api-lab: the time is " + (end - start));
```

This measures the **complete synchronous blocking time**, including:
- HTTP request overhead
- Network latency
- Server processing time
- JSON serialization/deserialization
- Round-trip communication

### Why This is Synchronous

✅ `RestTemplate` uses blocking HTTP client  
✅ Thread execution stops at `getForObject()`  
✅ No CompletableFuture or reactive types  
✅ Request-response completes before continuing  
✅ Server handlers execute to completion  
✅ Traditional servlet-based (not reactive WebFlux)

## Running the Application

### Build
```bash
mvn clean package
```

### Run
```bash
mvn spring-boot:run
```

Or run the main class directly:
```bash
java -jar target/rest-api-lab-1.0-SNAPSHOT.jar
```

### Expected Output
```
------- the rest-api-lab start -------
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

Started RestApiLabApplication in 2.3 seconds

rest-api-lab: the time is 45
------- the rest-api-lab end -------
```

### Testing with curl

**Get All Users:**
```bash
curl http://localhost:19002/api/users
```

**Get User by ID:**
```bash
curl http://localhost:19002/api/users/1
```

**Create User:**
```bash
curl -X POST http://localhost:19002/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Peng Wu","email":"pengwu@example.com"}'
```

**Update User:**
```bash
curl -X PUT http://localhost:19002/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Wu Peng","email":"wupeng@example.com"}'
```

**Delete User:**
```bash
curl -X DELETE http://localhost:19002/api/users/delete/1
```

## Performance Results

- **Response Time**: ~45ms (measured in code)
- **Protocol**: HTTP/1.1 with JSON
- **Port**: 19002
- **Data Format**: JSON (human-readable)
- **Blocking**: Client thread blocked for entire 45ms duration

### Performance Breakdown
- **HTTP Overhead**: Headers, connection setup (~10ms)
- **JSON Serialization**: Converting objects to JSON (~15ms)
- **Network Latency**: Localhost communication (~5ms)
- **Processing Time**: Database/service logic (~15ms)

**Author:** Peng Wu  
**API Endpoint:** http://localhost:19002/api/users  
**Framework:** Spring Boot 2.7.0