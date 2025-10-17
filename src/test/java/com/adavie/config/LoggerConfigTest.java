package com.adavie.config;

import org.junit.jupiter.api.Test;

import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

class LoggerConfigTest {

    @Test
    void testDefaultConfiguration() {
        LoggerConfig config = new LoggerConfig.Builder().build();

        assertTrue(config.isEnableFileLogging());
        assertEquals("/logs/app.log", config.isLogFilePath());
        assertEquals(Level.ALL, config.getLogLevel());
        assertEquals(10485760, config.getFileLimitBytes()); // 10MB
        assertEquals(5, config.getFileCount());
    }

    @Test
    void testValidLogLevels() {
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logLevel(Level.ALL).build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logLevel(Level.FINEST).build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logLevel(Level.FINER).build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logLevel(Level.FINE).build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logLevel(Level.CONFIG).build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logLevel(Level.INFO).build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logLevel(Level.WARNING).build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logLevel(Level.SEVERE).build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logLevel(Level.OFF).build());
    }

    @Test
    void testNullLogLevel() {
        LoggerConfig config = new LoggerConfig.Builder().logLevel(null).build();
        assertNull(config.getLogLevel());
    }

    @Test
    void testValidFilePaths() {
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logFilePath("app.log").build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logFilePath("/var/log/app.log").build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logFilePath("./logs/app.log").build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logFilePath("../logs/app.log").build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logFilePath("C:\\logs\\app.log").build());
    }

    @Test
    void testEmptyFilePathThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().logFilePath("").build()
        );
        assertEquals("Log file path must be specified when file logging is enabled", exception.getMessage());
    }

    @Test
    void testWhitespaceOnlyFilePathThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().logFilePath("   ").build()
        );
        assertEquals("Log file path must be specified when file logging is enabled", exception.getMessage());
    }

    @Test
    void testFilePathTrimsWhitespace() {
        LoggerConfig config = new LoggerConfig.Builder()
            .logFilePath("  /var/log/app.log  ")
            .build();
        assertEquals("/var/log/app.log", config.isLogFilePath());
    }

    @Test
    void testFilePathTooLong() {
        String longPath = "a".repeat(256);
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().logFilePath(longPath).build()
        );
        assertEquals("Log file path exceeds maximum length of 255 characters", exception.getMessage());
    }

    @Test
    void testFilePathExactly255Characters() {
        String exactPath = "a".repeat(255);
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logFilePath(exactPath).build());
    }

    @Test
    void testFilePathWith254Characters() {
        String exactPath = "a".repeat(254);
        LoggerConfig config = new LoggerConfig.Builder().logFilePath(exactPath).build();
        assertEquals(254, config.isLogFilePath().length());
    }

    @Test
    void testValidFileLimitBytes() {
        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileLimitBytes(1024).build()); // 1KB minimum
        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileLimitBytes(1024 * 1024).build()); // 1MB
        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileLimitBytes(10 * 1024 * 1024).build()); // 10MB
        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileLimitBytes(1024 * 1024 * 1024).build()); // 1GB maximum
    }

    @Test
    void testFileLimitBytesZero() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().fileLimitBytes(0).build()
        );
        assertEquals("File limit must be positive, got: 0", exception.getMessage());
    }

    @Test
    void testFileLimitBytesNegative() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().fileLimitBytes(-1).build()
        );
        assertEquals("File limit must be positive, got: -1", exception.getMessage());
    }

    @Test
    void testFileLimitBytesTooSmall() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().fileLimitBytes(1023).build()
        );
        assertEquals("File limit too small, minimum is 1024 bytes (1KB)", exception.getMessage());
    }

    @Test
    void testFileLimitBytesExactlyMinimum() {
        LoggerConfig config = new LoggerConfig.Builder().fileLimitBytes(1024).build();
        assertEquals(1024, config.getFileLimitBytes());
    }

    @Test
    void testFileLimitBytesTooLarge() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().fileLimitBytes(1024 * 1024 * 1024 + 1).build()
        );
        assertEquals("File limit too large, maximum is 1GB", exception.getMessage());
    }

    @Test
    void testFileLimitBytesExactlyMaximum() {
        LoggerConfig config = new LoggerConfig.Builder().fileLimitBytes(1024 * 1024 * 1024).build();
        assertEquals(1024 * 1024 * 1024, config.getFileLimitBytes());
    }

    @Test
    void testValidFileCount() {
        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileCount(1).build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileCount(5).build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileCount(50).build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileCount(100).build());
    }

    @Test
    void testFileCountZero() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().fileCount(0).build()
        );
        assertEquals("File count must be positive, got: 0", exception.getMessage());
    }

    @Test
    void testFileCountNegative() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().fileCount(-5).build()
        );
        assertEquals("File count must be positive, got: -5", exception.getMessage());
    }

    @Test
    void testFileCountTooLarge() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().fileCount(101).build()
        );
        assertEquals("File count too large, maximum is 100 files", exception.getMessage());
    }

    @Test
    void testFileCountExactlyMaximum() {
        LoggerConfig config = new LoggerConfig.Builder().fileCount(100).build();
        assertEquals(100, config.getFileCount());
    }

    @Test
    void testFileCountExactlyMinimum() {
        LoggerConfig config = new LoggerConfig.Builder().fileCount(1).build();
        assertEquals(1, config.getFileCount());
    }

    @Test
    void testFileLoggingEnabled() {
        LoggerConfig config = new LoggerConfig.Builder()
            .enabledFileLogging(true)
            .build();
        assertTrue(config.isEnableFileLogging());
    }

    @Test
    void testFileLoggingDisabled() {
        LoggerConfig config = new LoggerConfig.Builder()
            .enabledFileLogging(false)
            .build();
        assertFalse(config.isEnableFileLogging());
    }

    @Test
    void testBuilderMethodChaining() {
        LoggerConfig config = new LoggerConfig.Builder()
            .enabledFileLogging(true)
            .logFilePath("test.log")
            .logLevel(Level.INFO)
            .fileLimitBytes(5 * 1024 * 1024)
            .fileCount(10)
            .build();

        assertTrue(config.isEnableFileLogging());
        assertEquals("test.log", config.isLogFilePath());
        assertEquals(Level.INFO, config.getLogLevel());
        assertEquals(5 * 1024 * 1024, config.getFileLimitBytes());
        assertEquals(10, config.getFileCount());
    }

    @Test
    void testFilePathWithSpecialCharacters() {
        assertDoesNotThrow(() -> new LoggerConfig.Builder()
            .logFilePath("app-log_2024.log")
            .build()
        );
        assertDoesNotThrow(() -> new LoggerConfig.Builder()
            .logFilePath("/var/log/app-2024-01-01.log")
            .build()
        );
    }

    @Test
    void testMultipleBuildsFromSameBuilder() {
        LoggerConfig.Builder builder = new LoggerConfig.Builder()
            .logLevel(Level.WARNING)
            .fileLimitBytes(2048);

        LoggerConfig config1 = builder.build();
        LoggerConfig config2 = builder.build();

        assertEquals(Level.WARNING, config1.getLogLevel());
        assertEquals(Level.WARNING, config2.getLogLevel());
        assertEquals(2048, config1.getFileLimitBytes());
        assertEquals(2048, config2.getFileLimitBytes());
    }

    @Test
    void testBoundaryValueForFileLimitBytes() {
        assertThrows(IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().fileLimitBytes(1023).build()
        );

        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileLimitBytes(1024).build());

        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileLimitBytes(1025).build());

        assertDoesNotThrow(() -> new LoggerConfig.Builder()
            .fileLimitBytes(1024 * 1024 * 1024 - 1).build()
        );

        assertDoesNotThrow(() -> new LoggerConfig.Builder()
            .fileLimitBytes(1024 * 1024 * 1024).build()
        );

        assertThrows(IllegalArgumentException.class,
            () -> new LoggerConfig.Builder()
                .fileLimitBytes(1024 * 1024 * 1024 + 1).build()
        );
    }

    @Test
    void testBoundaryValueForFileCount() {
        assertThrows(IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().fileCount(0).build()
        );

        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileCount(1).build());

        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileCount(2).build());

        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileCount(99).build());

        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileCount(100).build());

        assertThrows(IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().fileCount(101).build()
        );
    }
}
