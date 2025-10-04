# Socket Lab - Synchronous Communication

**Student:** Peng Wu  
**Course:** COMP 41720 Distributed Systems

## System Design

This project implements the most fundamental form of synchronous client-server communication using raw TCP sockets. The client establishes a direct connection to the server, sends a message, and blocks while waiting for the response.

### Technology Choice

**Raw TCP Sockets** were chosen for this implementation because:
- **Fundamental Building Block**: Demonstrates the lowest-level synchronous communication
- **Direct Control**: Complete control over connection and data flow
- **Simple Protocol**: No framework overhead, pure request-response pattern
- **Educational Value**: Understanding sockets helps grasp higher-level abstractions (REST, gRPC)

## Architecture

```
Client                                    Server (Port 18007)
  |                                              |
  |─────── TCP Connection ────────>             |
  |                                       Accept Connection
  |                                              |
  |─────── Send Message "wupeng" ──────>        |
  |                                       Read & Process
  |      (Thread BLOCKS waiting)          toUpperCase()
  |                                              |
  |  <────── Response "WUPENG" ───────────────── |
  |                                              |
Continue Execution                        Close Connection
```

### Components

**1. CommonConstant.java** - Configuration
```java
SERVER_PORT = 18007
LOCAL_HOST = "localhost"
```
- Defines shared constants for client and server
- Port 18007 avoids conflicts with common services

**2. Server.java** - Server Implementation
- Creates `ServerSocket` on port **18007**
- Waits for client connections using `accept()` (blocking call)
- Reads incoming message using `BufferedReader`
- **Processes request**: Converts message to uppercase
- Sends response back via `PrintWriter`
- Closes connection after single request

**3. Client.java** - Client Implementation
- Creates `Socket` connection to server
- Sends message: **"wupeng"**
- **Blocks on `readLine()`** waiting for server response
- Measures total request-response time
- Displays server response: "WUPENG"

**4. Main Execution Flow**
- Server starts in separate thread
- Client creates socket connection
- Client sends message and blocks
- Server processes and responds
- Connection closes after single exchange

## How Synchronous Communication is Demonstrated

### Blocking I/O Operations

**Server Blocking:**
```java
Socket socket = serverSocket.accept(); // BLOCKS until client connects
String clientMessage = bufferedReader.readLine(); // BLOCKS until data arrives
```

**Client Blocking:**
```java
Socket socket = new Socket(LOCAL_HOST, SERVER_PORT); // BLOCKS until connected
String response = in.readLine(); // BLOCKS until server responds
```

The client thread completely stops execution at `in.readLine()` and waits until:
1. Server receives the message
2. Server processes it (converts to uppercase)
3. Server sends response back
4. Response arrives at client
5. Only then does execution continue

### Timing Measurement
```java
long start = System.currentTimeMillis();
// ... send message and block waiting for response ...
String response = in.readLine(); // Blocking happens here
long end = System.currentTimeMillis();
System.out.println("time: " + (end - start));
```

This measures the **complete synchronous blocking time**, including:
- Connection establishment
- Message transmission
- Server processing time
- Response transmission
- Network latency

### Why This is Synchronous

✅ Uses blocking I/O streams (`BufferedReader`, `PrintWriter`)  
✅ `readLine()` blocks the thread completely  
✅ No asynchronous callbacks or futures  
✅ Single-threaded sequential execution  
✅ Cannot do other work while waiting  
✅ One request at a time (connection closes after each)

## Running the Application

### Build
```bash
mvn clean compile
```

### Run
```bash
mvn exec:java -Dexec.mainClass="com.peng.sms.Client"
```

### Expected Output
```
------- the socket-lab start -------
Server start successly, Listening on port :18007
Connected to server at localhost: 18007
Enter message: wupeng
Client connected: /127.0.0.1
Received: wupeng
Server response: WUPENG
Socket-lab test: the time is: 5
the socket is closed
Client closed.
------- the socket-lab end -------
```

## Performance Results

- **Response Time**: ~5ms (measured in code)
- **Protocol**: Raw TCP/IP
- **Data Format**: Plain text
- **Overhead**: Minimal (no serialization, no HTTP headers)
- **Blocking**: Client thread blocked for entire duration


**Author:** Peng Wu  
**Connection:** Port 18007 | Message: "wupeng" → "WUPENG"