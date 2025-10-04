# gRPC Lab - Synchronous Communication

**Student:** Peng Wu  
**Course:** COMP 41720 Distributed Systems

## System Design

This project implements a synchronous client-server communication system using gRPC and Protocol Buffers. The system demonstrates a blocking request-response pattern where the client waits for the server's complete response before continuing execution.

### Technology Choice

**gRPC** was chosen for this implementation because:
- **Strong Type Safety**: Protocol Buffers provide compile-time type checking
- **High Performance**: Binary serialization is more efficient than JSON
- **Synchronous by Default**: BlockingStub naturally demonstrates synchronous communication
- **Clear Contract**: `.proto` files define a clear API contract between client and server

## Architecture

```
Client (Blocking)  ──────RPC Call────────>  Server (Port 50051)
      |                                            |
      |                                     Process Request
      |                                            |
      |  <──────UserResponse (blocks here)────────┘
      |
   Continue Execution
```

### Components

**1. UserService.proto** - Service Definition
```protobuf

service UserService {
  rpc GetUser (UserRequest) returns (UserResponse);
}
```
- Defines the RPC interface contract
- UserRequest contains `user_id`
- UserResponse returns `user_id`, `name`, `email`

**2. UserServer.java** - Server Implementation
- Listens on port **50051**
- Implements `UserServiceImpl` extending `UserServiceImplBase`
- Processes `getUser()` requests synchronously
- Returns hardcoded user data: "pengwu" / "pengwu@gmail.com"

**3. UserClient.java** - Client Implementation
- Creates a **BlockingStub** (key for synchronous behavior)
- Sends UserRequest with userId=12
- **Blocks and waits** for server response
- Measures total request-response time

**4. Main.java** - Orchestration
- Starts server in daemon thread
- Waits 2 seconds for server initialization
- Executes client call
- Gracefully shuts down server

## How Synchronous Communication is Demonstrated

### Blocking Behavior
```java
UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(channel);
UserResponse response = stub.getUser(request); // Thread BLOCKS here
```

The client thread stops execution at `stub.getUser()` and waits until:
1. Request is sent to server
2. Server processes the request
3. Response is received back
4. Only then does execution continue

### Timing Measurement
```java
long start = System.currentTimeMillis();
// ... gRPC call blocks here ...
long end = System.currentTimeMillis();
System.out.println("time: " + (end - start)); // Shows blocking duration
```

This measures the **complete synchronous blocking time**, proving the client waits for the full round-trip.

### Why This is Synchronous (Not Asynchronous)

✅ Uses `BlockingStub` (not `FutureStub` or `AsyncStub`)  
✅ Thread execution stops at RPC call  
✅ No callbacks or futures  
✅ Sequential execution flow  
✅ Direct return value, not a promise

## Running the Application

### Build
```bash
mvn clean compile
```

### Run
```bash
mvn exec:java -Dexec.mainClass="com.peng.sms.Main"
```

### Expected Output
```
------- the grpc-lab start -------
Waiting for server to start...
{peng wu}: Server started on port 50051
Starting client...
User info: pengwu, pengwu@gmail.com
grpc-lab test: the time is: 15
Client completed!
{peng wu}: Server stopped
------- the grpc-lab end -------
```

## Performance Results

- **Response Time**: ~15ms (measured in code)
- **Protocol**: HTTP/2 with binary Protocol Buffers
- **Payload Size**: ~50 bytes (much smaller than JSON)
- **Blocking**: Client thread blocked for entire 15ms duration

**Author:** Peng Wu (pengwu@gmail.com)