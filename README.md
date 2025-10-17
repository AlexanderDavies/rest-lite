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

### Planned
- Loading properties from config file
- Parsing of headers
- Route registration and routing
- JSON parsing and mapping
- Path parameters
- HTTPS support
- Cookie handling

## Installation

This project uses Gradle as its build system. Clone the repository and build the project:

```bash
git clone <repository-url>
cd rest-lite
./gradlew build
```

## Usage

### Basic Server

Start a server with default configuration (localhost:8081):

```java
import com.adavie.server.Server;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        server.start();

        // Server is now running on localhost:8081

        // To stop the server:
        // server.stop();
    }
}
```

### Custom Configuration

Configure hostname, port, and timeout:

```java
import com.adavie.server.Server;
import com.adavie.server.config.ServerConfig;

public class Main {
    public static void main(String[] args) {
        ServerConfig config = new ServerConfig.Builder()
            .hostname("localhost")
            .port(9090)
            .clientConnectionTimeout(60)
            .build();

        Server server = new Server(config);
        server.start();

        System.out.println("Server running on localhost:9090");
    }
}
```

### Custom Thread Pool Configuration

Configure the server's thread pool for handling client connections:

```java
import com.adavie.server.Server;
import com.adavie.server.config.ServerConfig;
import com.adavie.server.config.ThreadPoolConfig;

public class Main {
    public static void main(String[] args) {
        // Create custom thread pool configuration
        ThreadPoolConfig threadPoolConfig = new ThreadPoolConfig.Builder()
            .minPoolSize(25)
            .maxPoolSize(200)
            .keepAliveSeconds(120L)
            .queueSize(50)
            .build();

        // Create server config with custom thread pool
        ServerConfig config = new ServerConfig.Builder()
            .hostname("localhost")
            .port(9090)
            .threadPoolConfig(threadPoolConfig)
            .build();

        Server server = new Server(config);
        server.start();

        System.out.println("Server running on localhost:9090 with custom thread pool");
    }
}
```

### Custom Logger Configuration

Configure the root logger with custom settings for file logging:

```java
import com.adavie.server.Server;
import com.adavie.server.config.ServerConfig;
import com.adavie.config.LoggerConfig;
import java.util.logging.Level;

public class Main {
    public static void main(String[] args) {
        // Create custom logger configuration
        LoggerConfig loggerConfig = new LoggerConfig.Builder()
            .enabledFileLogging(true)
            .logFilePath("/var/log/myapp/server.log")
            .logLevel(Level.INFO)
            .fileLimitBytes(5242880)  // 5MB per file
            .fileCount(10)            // Keep 10 log files
            .build();

        // Create server config with custom logger
        ServerConfig config = new ServerConfig.Builder()
            .hostname("localhost")
            .port(9090)
            .loggerConfig(loggerConfig)
            .build();

        Server server = new Server(config);
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