package com.adavie.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerConfigTest {

    @Test
    void testValidHostnames() {
        // Valid hostnames should not throw exceptions
        assertDoesNotThrow(() -> new ServerConfig.Builder().hostname("localhost").build());
        assertDoesNotThrow(() -> new ServerConfig.Builder().hostname("0.0.0.0").build());
        assertDoesNotThrow(() -> new ServerConfig.Builder().hostname("127.0.0.1").build());
        assertDoesNotThrow(() -> new ServerConfig.Builder().hostname("192.168.1.100").build());
        assertDoesNotThrow(() -> new ServerConfig.Builder().hostname("example.com").build());
        assertDoesNotThrow(() -> new ServerConfig.Builder().hostname("sub.example.com").build());
        assertDoesNotThrow(() -> new ServerConfig.Builder().hostname("my-server.local").build());
        assertDoesNotThrow(() -> new ServerConfig.Builder().hostname("my_server").build());
        assertDoesNotThrow(() -> new ServerConfig.Builder().hostname("::").build());
        assertDoesNotThrow(() -> new ServerConfig.Builder().hostname("::1").build());
        assertDoesNotThrow(() -> new ServerConfig.Builder().hostname("2001:db8::1").build());
    }

    @Test
    void testNullHostname() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ServerConfig.Builder().hostname(null).build()
        );
        assertEquals("Hostname cannot be null or empty", exception.getMessage());
    }

    @Test
    void testEmptyHostname() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ServerConfig.Builder().hostname("").build()
        );
        assertEquals("Hostname cannot be null or empty", exception.getMessage());
    }

    @Test
    void testWhitespaceOnlyHostname() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ServerConfig.Builder().hostname("   ").build()
        );
        assertEquals("Hostname cannot be null or empty", exception.getMessage());
    }

    @Test
    void testHostnameWithSpaces() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ServerConfig.Builder().hostname("my server").build()
        );
        assertEquals("Hostname cannot contain spaces", exception.getMessage());
    }

    @Test
    void testHostnameTooLong() {
        String longHostname = "a".repeat(254);
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ServerConfig.Builder().hostname(longHostname).build()
        );
        assertEquals("Hostname too long (max 253 characters)", exception.getMessage());
    }

    @Test
    void testHostnameWithInvalidCharacters() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ServerConfig.Builder().hostname("example!.com").build()
        );
        assertEquals("Hostname contains invalid characters", exception.getMessage());
    }

    @Test
    void testHostnameTrimsWhitespace() {
        ServerConfig config = new ServerConfig.Builder()
            .hostname("  localhost  ")
            .build();
        assertEquals("localhost", config.getHostname());
    }

    @Test
    void testDefaultHostname() {
        ServerConfig config = new ServerConfig.Builder().build();
        assertEquals("localhost", config.getHostname());
    }

    @Test
    void testValidPortRange() {
        assertDoesNotThrow(() -> new ServerConfig.Builder().port(1).build());
        assertDoesNotThrow(() -> new ServerConfig.Builder().port(8080).build());
        assertDoesNotThrow(() -> new ServerConfig.Builder().port(65535).build());
    }

    @Test
    void testInvalidPortTooLow() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ServerConfig.Builder().port(0).build()
        );
        assertEquals("Port must be between 1 and 65535", exception.getMessage());
    }

    @Test
    void testInvalidPortTooHigh() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ServerConfig.Builder().port(65536).build()
        );
        assertEquals("Port must be between 1 and 65535", exception.getMessage());
    }

    @Test
    void testValidClientTimeout() {
        assertDoesNotThrow(() -> new ServerConfig.Builder().clientConnectionTimeout(1).build());
        assertDoesNotThrow(() -> new ServerConfig.Builder().clientConnectionTimeout(30000).build());
    }

    @Test
    void testInvalidClientTimeout() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ServerConfig.Builder().clientConnectionTimeout(0).build()
        );
        assertEquals("Client connection timeout must be at least 1", exception.getMessage());
    }

    @Test
    void testDefaultServerConfig() {
        ServerConfig config = ServerConfig.getDefaultServerConfig();

        assertEquals("localhost", config.getHostname());
        assertEquals(8081, config.getPort());
        assertEquals(30, config.getClientConnectionTimeout());
    }
}
