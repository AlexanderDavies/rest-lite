# Rest Lite

A lightweight and simple Java HTTP server and RESTful framework with zero dependencies.

## Features

### Implemented

- ✅ Base server with client connection support
- ✅ Configurable server settings:
  - Server address/hostname
  - Port (default: 8081)
  - Client connection timeout
  - Thread pool configuration
  - Logging
- ✅ Server shutdown with connection cleanup
- ✅ Thread-based request handling
- ✅ Logging
- ✅ Virtual thread support
- ✅ Handle clients requests
- ✅ Parse Headers
- ✅ Route registration with type safety
  - Support for 0-4 parameter routes
  - Explicit type declarations
  - Generic type preservation with TypeToken
  - Runtime type validation

### Planned
- Extend support for routes with > four params
- Handle query params
- Route requests to registered handlers
- JSON response parsing
- Handle path parameters (registration and routing)
- Handle post requests
  - parse JSON body and map to request object
  - Handle generics
- Handle Cookies
- Provide HTTPS support
- Load properties from config file


## Installation

This project uses Gradle as its build system. Clone the repository and build the project:

```bash
git clone <repository-url>
cd rest-lite
./gradlew build
```

## Usage

### Basic Server with Routes

Start a server with route registration:

```java
import com.adavie.server.Server;
import com.adavie.router.Routes;

public class Main {
    public static void main(String[] args) {
        Routes routes = new Routes();

        routes.add("/hello", () -> "Hello World");
        routes.add("/greet", (String name) -> "Hello, " + name);

        Server server = new Server(routes);
        server.start();

        // Server is now running on localhost:8081
    }
}
```

### Route Registration

#### Simple Route Registration

Register routes with lambdas (types inferred as Object):

```java
Routes routes = new Routes();

routes.add("/hello", () -> "Hello World");

routes.add("/greet", (String name) -> "Hello, " + name);

routes.add("/add", (Integer a, Integer b) -> a + b);

routes.add("/fullname",
    (String first, String middle, String last) ->
        first + " " + middle + " " + last);
```

#### Explicit Type Registration with TypeToken

Register routes with explicit type information for better type safety:

```java
import com.adavie.router.TypeToken;

Routes routes = new Routes();

routes.add("/hello",
    () -> "Hello World",
    new TypeToken<String>() {});

routes.add("/greet",
    (String name) -> "Hello, " + name,
    new TypeToken<String>() {},
    new TypeToken<String>() {});

routes.add("/concat",
    (String a, String b) -> a + " " + b,
    new TypeToken<String>() {},
    new TypeToken<String>() {},
    new TypeToken<String>() {});
```

#### Generic Type Preservation

Use TypeToken to preserve generic types like `List<String>` or `Map<String, Integer>`:

```java
import java.util.List;
import java.util.Map;
import java.util.Arrays;

Routes routes = new Routes();

routes.add("/users",
    () -> Arrays.asList("Alice", "Bob", "Charlie"),
    new TypeToken<List<String>>() {});

routes.add("/config",
    () -> Map.of("version", "1.0", "name", "MyApp"),
    new TypeToken<Map<String, String>>() {});

routes.add("/filter",
    (List<Integer> numbers) -> numbers.stream()
        .filter(n -> n > 10)
        .collect(Collectors.toList()),
    new TypeToken<List<Integer>>() {},
    new TypeToken<List<Integer>>() {});
```

#### Invoking Routes

Invoke registered routes directly:

```java
Routes routes = new Routes();
routes.add("/greet", (String name) -> "Hello, " + name);

Object result = routes.invoke("/greet", "Alice");
System.out.println(result);

String typedResult = routes.invokeTyped("/greet", String.class, "Bob");
System.out.println(typedResult);
```

### Custom Configuration

Configure hostname, port, and timeout with routes:

```java
import com.adavie.server.Server;
import com.adavie.config.ServerConfig;
import com.adavie.router.Routes;

public class Main {
    public static void main(String[] args) {
        Routes routes = new Routes();
        routes.add("/hello", () -> "Hello World");

        ServerConfig config = new ServerConfig.Builder()
            .hostname("localhost")
            .port(9090)
            .clientConnectionTimeout(60)
            .build();

        Server server = new Server(routes, config);
        server.start();

        System.out.println("Server running on localhost:9090");
    }
}
```

### Custom Thread Pool Configuration

Configure the server's thread pool for handling client connections:

```java
import com.adavie.server.Server;
import com.adavie.config.ServerConfig;
import com.adavie.config.ThreadPoolConfig;
import com.adavie.router.Routes;

public class Main {
    public static void main(String[] args) {
        Routes routes = new Routes();
        routes.add("/hello", () -> "Hello World");

        ThreadPoolConfig threadPoolConfig = new ThreadPoolConfig.Builder()
            .minPoolSize(25)
            .maxPoolSize(200)
            .keepAliveSeconds(120L)
            .queueSize(50)
            .build();

        ServerConfig config = new ServerConfig.Builder()
            .hostname("localhost")
            .port(9090)
            .threadPoolConfig(threadPoolConfig)
            .build();

        Server server = new Server(routes, config);
        server.start();

        System.out.println("Server running on localhost:9090 with custom thread pool");
    }
}
```

### Custom Logger Configuration

Configure the root logger with custom settings for file logging:

```java
import com.adavie.server.Server;
import com.adavie.config.ServerConfig;
import com.adavie.config.LoggerConfig;
import com.adavie.router.Routes;
import java.util.logging.Level;

public class Main {
    public static void main(String[] args) {
        Routes routes = new Routes();
        routes.add("/hello", () -> "Hello World");

        LoggerConfig loggerConfig = new LoggerConfig.Builder()
            .enabledFileLogging(true)
            .logFilePath("/var/log/myapp/server.log")
            .logLevel(Level.INFO)
            .fileLimitBytes(5242880)
            .fileCount(10)
            .build();

        ServerConfig config = new ServerConfig.Builder()
            .hostname("localhost")
            .port(9090)
            .loggerConfig(loggerConfig)
            .build();

        Server server = new Server(routes, config);
        server.start();

        System.out.println("Server running with custom logging configuration");
    }
}
```

### Configuration Options

#### ServerConfig
- **hostname**: Server hostname (default: `localhost`)
- **port**: Server port between 1-65535 (default: `8081`)
- **clientConnectionTimeout**: Connection timeout in seconds (default: `30`)
- **threadPoolConfig**: Custom thread pool configuration (default: uses ThreadPoolConfig defaults)

#### ThreadPoolConfig
- **minPoolSize**: Minimum number of threads in the pool, range 1-10000 (default: `50`)
- **maxPoolSize**: Maximum number of threads in the pool, range 1-10000 (default: `150`)
- **keepAliveSeconds**: Time in seconds that idle threads stay alive, range 0-86400 (default: `60`)
- **queueSize**: Size of the work queue, range 0-100000 or -1 for unbounded (default: `20`)

#### LoggerConfig
- **enableFileLogging**: Enable or disable file logging (default: `true`)
- **logFilePath**: Path to the log file, max 255 characters (default: `/logs/app.log`)
- **logLevel**: Logging level (default: `Level.ALL`)
- **fileLimitBytes**: Maximum size of each log file in bytes, range 1KB-1GB (default: `10485760` - 10MB)
- **fileCount**: Number of log files to rotate through, range 1-100 (default: `5`)

## Running the Project

### Build and Test

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Clean build artifacts
./gradlew clean
```

### Running Tests

```bash
./gradlew test --tests ServerTest
```

## Requirements

- Java 8 or higher
- Gradle 7.0 or higher (wrapper included)