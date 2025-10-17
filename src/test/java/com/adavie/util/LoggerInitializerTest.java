package com.adavie.util;

import com.adavie.config.LoggerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.logging.*;

import static org.junit.jupiter.api.Assertions.*;

class LoggerInitializerTest {

    private Logger rootLogger;
    private Handler[] originalHandlers;

    @BeforeEach
    void setUp() {
        rootLogger = Logger.getLogger("");
        originalHandlers = rootLogger.getHandlers().clone();
    }

    @AfterEach
    void tearDown() {
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            handler.close();
            rootLogger.removeHandler(handler);
        }


        for (Handler handler : originalHandlers) {
            rootLogger.addHandler(handler);
        }
    }

    @Test
    void testConfigureRootLoggerWithConsoleOnly() {
        LoggerConfig config = new LoggerConfig.Builder()
            .enabledFileLogging(false)
            .logLevel(Level.INFO)
            .build();

        LoggerInitializer.configureRootLogger(config);

        assertEquals(Level.INFO, rootLogger.getLevel());

        Handler[] handlers = rootLogger.getHandlers();
        assertTrue(handlers.length > 0, "At least one handler should be present");

        boolean hasConsoleHandler = false;
        for (Handler handler : handlers) {
            if (handler instanceof ConsoleHandler) {
                hasConsoleHandler = true;
                assertEquals(Level.INFO, handler.getLevel());
            }
        }
        assertTrue(hasConsoleHandler, "ConsoleHandler should be present");
    }

    @Test
    void testConfigureRootLoggerRemovesExistingHandlers() {
        ConsoleHandler dummyHandler = new ConsoleHandler();
        rootLogger.addHandler(dummyHandler);

        int handlerCountBefore = rootLogger.getHandlers().length;
        assertTrue(handlerCountBefore > 0, "Should have handlers before configuration");

        LoggerConfig config = new LoggerConfig.Builder()
            .enabledFileLogging(false)
            .logLevel(Level.INFO)
            .build();

        LoggerInitializer.configureRootLogger(config);

        Handler[] handlers = rootLogger.getHandlers();
        assertFalse(contains(handlers, dummyHandler), "Old handler should be removed");
    }

    @Test
    void testConfigureRootLoggerWithDifferentLevels() {
        Level[] levels = {Level.ALL, Level.FINEST, Level.INFO, Level.WARNING, Level.SEVERE, Level.OFF};

        for (Level level : levels) {
            LoggerConfig config = new LoggerConfig.Builder()
                .enabledFileLogging(false)
                .logLevel(level)
                .build();

            LoggerInitializer.configureRootLogger(config);

            assertEquals(level, rootLogger.getLevel(), "Root logger level should be " + level);

            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                if (handler instanceof ConsoleHandler) {
                    assertEquals(level, handler.getLevel(), "Console handler level should be " + level);
                }
            }
        }
    }

    @Test
    void testConfigureRootLoggerWithFileLogging(@TempDir Path tempDir) {
        Path logFile = tempDir.resolve("test.log");

        LoggerConfig config = new LoggerConfig.Builder()
            .enabledFileLogging(true)
            .logFilePath(logFile.toString())
            .logLevel(Level.INFO)
            .fileLimitBytes(1024 * 1024) // 1MB
            .fileCount(3)
            .build();

        LoggerInitializer.configureRootLogger(config);

        boolean hasConsoleHandler = false;
        boolean hasFileHandler = false;

        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            if (handler instanceof ConsoleHandler) {
                hasConsoleHandler = true;
            }
            if (handler instanceof FileHandler) {
                hasFileHandler = true;
                assertEquals(Level.INFO, handler.getLevel());
            }
        }

        assertTrue(hasConsoleHandler, "ConsoleHandler should be present");
        assertTrue(hasFileHandler, "FileHandler should be present when file logging is enabled");

        Logger testLogger = Logger.getLogger(LoggerInitializerTest.class.getName());
        testLogger.info("Test message");

        assertTrue(Files.exists(logFile) || Files.exists(Path.of(logFile + ".0")),
            "Log file should be created");
    }

    @Test
    void testConfigureRootLoggerWithInvalidFilePath() {
        LoggerConfig config = new LoggerConfig.Builder()
            .enabledFileLogging(true)
            .logFilePath("/invalid/path/that/does/not/exist/test.log")
            .logLevel(Level.INFO)
            .build();

        assertDoesNotThrow(() -> LoggerInitializer.configureRootLogger(config));

        Handler[] handlers = rootLogger.getHandlers();
        boolean hasConsoleHandler = false;
        for (Handler handler : handlers) {
            if (handler instanceof ConsoleHandler) {
                hasConsoleHandler = true;
                break;
            }
        }
        assertTrue(hasConsoleHandler, "ConsoleHandler should still be present even if file logging fails");
    }

    @Test
    void testFileLoggingDisabled() {
        LoggerConfig config = new LoggerConfig.Builder()
            .enabledFileLogging(false)
            .logLevel(Level.INFO)
            .build();

        LoggerInitializer.configureRootLogger(config);

        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            assertFalse(handler instanceof FileHandler,
                "FileHandler should not be present when file logging is disabled");
        }
    }


    @Test
    void testLogFormatterFormat() {
        LoggerConfig config = new LoggerConfig.Builder()
            .enabledFileLogging(false)
            .logLevel(Level.INFO)
            .build();

        LoggerInitializer.configureRootLogger(config);

        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            if (handler instanceof ConsoleHandler) {
                Formatter formatter = handler.getFormatter();
                assertNotNull(formatter, "Formatter should be set");

                LogRecord record = new LogRecord(Level.INFO, "Test message");
                record.setLoggerName("com.adavie.test");
                record.setInstant(Instant.now());

                String formatted = formatter.format(record);

                assertTrue(formatted.contains("INFO"), "Formatted message should contain log level");
                assertTrue(formatted.contains("com.adavie.test"), "Formatted message should contain logger name");
                assertTrue(formatted.contains("Test message"), "Formatted message should contain the message");
                assertTrue(formatted.matches("(?s).*\\[\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}].*"),
                    "Formatted message should contain date in ISO format, got: " + formatted);
            }
        }
    }

    @Test
    void testLogFormatterWithParameters() {
        LoggerConfig config = new LoggerConfig.Builder()
            .enabledFileLogging(false)
            .logLevel(Level.INFO)
            .build();

        LoggerInitializer.configureRootLogger(config);

        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            if (handler instanceof ConsoleHandler) {
                Formatter formatter = handler.getFormatter();

                LogRecord record = new LogRecord(Level.WARNING, "User {0} failed login attempt {1}");
                record.setParameters(new Object[]{"john", 3});
                record.setLoggerName("com.adavie.auth");
                record.setInstant(Instant.now());

                String formatted = formatter.format(record);

                assertTrue(formatted.contains("john"), "Should contain parameter 1");
                assertTrue(formatted.contains("3"), "Should contain parameter 2");
            }
        }
    }

    @Test
    void testReconfigureRootLogger() {
        LoggerConfig config1 = new LoggerConfig.Builder()
            .enabledFileLogging(false)
            .logLevel(Level.WARNING)
            .build();

        LoggerInitializer.configureRootLogger(config1);
        assertEquals(Level.WARNING, rootLogger.getLevel());

        LoggerConfig config2 = new LoggerConfig.Builder()
            .enabledFileLogging(false)
            .logLevel(Level.SEVERE)
            .build();

        LoggerInitializer.configureRootLogger(config2);
        assertEquals(Level.SEVERE, rootLogger.getLevel());
    }

    @Test
    void testChildLoggersInheritConfiguration() {
        LoggerConfig config = new LoggerConfig.Builder()
            .enabledFileLogging(false)
            .logLevel(Level.WARNING)
            .build();

        LoggerInitializer.configureRootLogger(config);

        Logger childLogger1 = Logger.getLogger("com.adavie.server");
        Logger childLogger2 = Logger.getLogger("com.adavie.client");
        Logger childLogger3 = Logger.getLogger("com.adavie.util.helper");

        assertDoesNotThrow(() -> childLogger1.warning("Test warning"));
        assertDoesNotThrow(() -> childLogger2.warning("Test warning"));
        assertDoesNotThrow(() -> childLogger3.warning("Test warning"));
    }

    private boolean contains(Handler[] handlers, Handler target) {
        for (Handler handler : handlers) {
            if (handler == target) {
                return true;
            }
        }
        return false;
    }
}
