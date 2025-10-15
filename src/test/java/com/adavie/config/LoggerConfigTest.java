package com.adavie.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

class LoggerConfigTest {

    @Test
    void testDefaultConfiguration() {
        LoggerConfig config = new LoggerConfig.Builder().Build();

        assertTrue(config.isEnableFileLogging());
        assertEquals("/logs/app.log", config.isLogFilePath());
        assertEquals(Level.ALL, config.getLogLevel());
        assertEquals(10485760, config.getFileLimitBytes()); // 10MB
        assertEquals(5, config.getFileCount());
    }

    // ===== Log Level Validations =====

    @Test
    void testValidLogLevels() {
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logLevel(Level.ALL).Build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logLevel(Level.FINEST).Build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logLevel(Level.FINER).Build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logLevel(Level.FINE).Build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logLevel(Level.CONFIG).Build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logLevel(Level.INFO).Build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logLevel(Level.WARNING).Build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logLevel(Level.SEVERE).Build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logLevel(Level.OFF).Build());
    }

    @Test
    void testNullLogLevel() {
        // The current implementation doesn't validate null log level
        // This test documents current behavior
        LoggerConfig config = new LoggerConfig.Builder().logLevel(null).Build();
        assertNull(config.getLogLevel());
    }

    // ===== File Path Validations =====

    @Test
    void testValidFilePaths() {
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logFilePath("app.log").Build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logFilePath("/var/log/app.log").Build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logFilePath("./logs/app.log").Build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logFilePath("../logs/app.log").Build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logFilePath("C:\\logs\\app.log").Build());
    }

    @Test
    void testEmptyFilePathThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().logFilePath("").Build()
        );
        assertEquals("Log file path must be specified when file logging is enabled", exception.getMessage());
    }

    @Test
    void testWhitespaceOnlyFilePathThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().logFilePath("   ").Build()
        );
        assertEquals("Log file path must be specified when file logging is enabled", exception.getMessage());
    }

    @Test
    void testFilePathTrimsWhitespace() {
        LoggerConfig config = new LoggerConfig.Builder()
            .logFilePath("  /var/log/app.log  ")
            .Build();
        assertEquals("/var/log/app.log", config.isLogFilePath());
    }

    @Test
    void testFilePathTooLong() {
        String longPath = "a".repeat(256);
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().logFilePath(longPath).Build()
        );
        assertEquals("Log file path exceeds maximum length of 255 characters", exception.getMessage());
    }

    @Test
    void testFilePathExactly255Characters() {
        String exactPath = "a".repeat(255);
        assertDoesNotThrow(() -> new LoggerConfig.Builder().logFilePath(exactPath).Build());
    }

    @Test
    void testFilePathWith254Characters() {
        String exactPath = "a".repeat(254);
        LoggerConfig config = new LoggerConfig.Builder().logFilePath(exactPath).Build();
        assertEquals(254, config.isLogFilePath().length());
    }

    // ===== File Limit Bytes Validations =====

    @Test
    void testValidFileLimitBytes() {
        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileLimitBytes(1024).Build()); // 1KB minimum
        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileLimitBytes(1024 * 1024).Build()); // 1MB
        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileLimitBytes(10 * 1024 * 1024).Build()); // 10MB
        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileLimitBytes(1024 * 1024 * 1024).Build()); // 1GB maximum
    }

    @Test
    void testFileLimitBytesZero() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().fileLimitBytes(0).Build()
        );
        assertEquals("File limit must be positive, got: 0", exception.getMessage());
    }

    @Test
    void testFileLimitBytesNegative() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().fileLimitBytes(-1).Build()
        );
        assertEquals("File limit must be positive, got: -1", exception.getMessage());
    }

    @Test
    void testFileLimitBytesTooSmall() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().fileLimitBytes(1023).Build()
        );
        assertEquals("File limit too small, minimum is 1024 bytes (1KB)", exception.getMessage());
    }

    @Test
    void testFileLimitBytesExactlyMinimum() {
        LoggerConfig config = new LoggerConfig.Builder().fileLimitBytes(1024).Build();
        assertEquals(1024, config.getFileLimitBytes());
    }

    @Test
    void testFileLimitBytesTooLarge() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().fileLimitBytes(1024 * 1024 * 1024 + 1).Build()
        );
        assertEquals("File limit too large, maximum is 1GB", exception.getMessage());
    }

    @Test
    void testFileLimitBytesExactlyMaximum() {
        LoggerConfig config = new LoggerConfig.Builder().fileLimitBytes(1024 * 1024 * 1024).Build();
        assertEquals(1024 * 1024 * 1024, config.getFileLimitBytes());
    }

    // ===== File Count Validations =====

    @Test
    void testValidFileCount() {
        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileCount(1).Build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileCount(5).Build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileCount(50).Build());
        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileCount(100).Build());
    }

    @Test
    void testFileCountZero() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().fileCount(0).Build()
        );
        assertEquals("File count must be positive, got: 0", exception.getMessage());
    }

    @Test
    void testFileCountNegative() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().fileCount(-5).Build()
        );
        assertEquals("File count must be positive, got: -5", exception.getMessage());
    }

    @Test
    void testFileCountTooLarge() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().fileCount(101).Build()
        );
        assertEquals("File count too large, maximum is 100 files", exception.getMessage());
    }

    @Test
    void testFileCountExactlyMaximum() {
        LoggerConfig config = new LoggerConfig.Builder().fileCount(100).Build();
        assertEquals(100, config.getFileCount());
    }

    @Test
    void testFileCountExactlyMinimum() {
        LoggerConfig config = new LoggerConfig.Builder().fileCount(1).Build();
        assertEquals(1, config.getFileCount());
    }

    // ===== File Logging Enable/Disable =====

    @Test
    void testFileLoggingEnabled() {
        LoggerConfig config = new LoggerConfig.Builder()
            .enabledFileLogging(true)
            .Build();
        assertTrue(config.isEnableFileLogging());
    }

    @Test
    void testFileLoggingDisabled() {
        LoggerConfig config = new LoggerConfig.Builder()
            .enabledFileLogging(false)
            .Build();
        assertFalse(config.isEnableFileLogging());
    }

    // ===== Builder Method Chaining =====

    @Test
    void testBuilderMethodChaining() {
        LoggerConfig config = new LoggerConfig.Builder()
            .enabledFileLogging(true)
            .logFilePath("test.log")
            .logLevel(Level.INFO)
            .fileLimitBytes(5 * 1024 * 1024)
            .fileCount(10)
            .Build();

        assertTrue(config.isEnableFileLogging());
        assertEquals("test.log", config.isLogFilePath());
        assertEquals(Level.INFO, config.getLogLevel());
        assertEquals(5 * 1024 * 1024, config.getFileLimitBytes());
        assertEquals(10, config.getFileCount());
    }

    // ===== Edge Cases =====

    @Test
    void testFilePathWithSpecialCharacters() {
        assertDoesNotThrow(() -> new LoggerConfig.Builder()
            .logFilePath("app-log_2024.log")
            .Build()
        );
        assertDoesNotThrow(() -> new LoggerConfig.Builder()
            .logFilePath("/var/log/app-2024-01-01.log")
            .Build()
        );
    }

    @Test
    void testMultipleBuildsFromSameBuilder() {
        LoggerConfig.Builder builder = new LoggerConfig.Builder()
            .logLevel(Level.WARNING)
            .fileLimitBytes(2048);

        LoggerConfig config1 = builder.Build();
        LoggerConfig config2 = builder.Build();

        // Both configs should have the same values
        assertEquals(Level.WARNING, config1.getLogLevel());
        assertEquals(Level.WARNING, config2.getLogLevel());
        assertEquals(2048, config1.getFileLimitBytes());
        assertEquals(2048, config2.getFileLimitBytes());
    }

    @Test
    void testBoundaryValueForFileLimitBytes() {
        // Test just below minimum
        assertThrows(IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().fileLimitBytes(1023).Build()
        );

        // Test at minimum
        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileLimitBytes(1024).Build());

        // Test just above minimum
        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileLimitBytes(1025).Build());

        // Test just below maximum
        assertDoesNotThrow(() -> new LoggerConfig.Builder()
            .fileLimitBytes(1024 * 1024 * 1024 - 1).Build()
        );

        // Test at maximum
        assertDoesNotThrow(() -> new LoggerConfig.Builder()
            .fileLimitBytes(1024 * 1024 * 1024).Build()
        );

        // Test just above maximum
        assertThrows(IllegalArgumentException.class,
            () -> new LoggerConfig.Builder()
                .fileLimitBytes(1024 * 1024 * 1024 + 1).Build()
        );
    }

    @Test
    void testBoundaryValueForFileCount() {
        // Test just below minimum
        assertThrows(IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().fileCount(0).Build()
        );

        // Test at minimum
        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileCount(1).Build());

        // Test just above minimum
        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileCount(2).Build());

        // Test just below maximum
        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileCount(99).Build());

        // Test at maximum
        assertDoesNotThrow(() -> new LoggerConfig.Builder().fileCount(100).Build());

        // Test just above maximum
        assertThrows(IllegalArgumentException.class,
            () -> new LoggerConfig.Builder().fileCount(101).Build()
        );
    }
}
