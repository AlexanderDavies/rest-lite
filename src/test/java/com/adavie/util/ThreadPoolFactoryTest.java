package com.adavie.util;

import com.adavie.config.ThreadPoolConfig;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class ThreadPoolFactoryTest {

    @Test
    void testCreateDefaultThreadPool() {
        ThreadPoolConfig config = new ThreadPoolConfig.Builder()
            .virtualThreads(false)
            .build();

        ExecutorService executor = ThreadPoolFactory.newExecutorService(config);

        assertNotNull(executor);
        assertTrue(executor instanceof ThreadPoolExecutor);

        ThreadPoolExecutor tpe = (ThreadPoolExecutor) executor;
        assertEquals(50, tpe.getCorePoolSize());
        assertEquals(150, tpe.getMaximumPoolSize());
        assertEquals(60L, tpe.getKeepAliveTime(java.util.concurrent.TimeUnit.SECONDS));

        executor.shutdown();
    }

    @Test
    void testCreateCustomThreadPool() {
        ThreadPoolConfig config = new ThreadPoolConfig.Builder()
            .minPoolSize(25)
            .maxPoolSize(200)
            .keepAliveSeconds(120L)
            .queueSize(50)
            .virtualThreads(false)
            .build();

        ExecutorService executor = ThreadPoolFactory.newExecutorService(config);

        assertNotNull(executor);
        assertTrue(executor instanceof ThreadPoolExecutor);

        ThreadPoolExecutor tpe = (ThreadPoolExecutor) executor;
        assertEquals(25, tpe.getCorePoolSize());
        assertEquals(200, tpe.getMaximumPoolSize());
        assertEquals(120L, tpe.getKeepAliveTime(java.util.concurrent.TimeUnit.SECONDS));

        executor.shutdown();
    }

    @Test
    void testVirtualThreadsEnabledWithDefaults() {
        ThreadPoolConfig config = new ThreadPoolConfig.Builder()
            .virtualThreads(true)
            .build();

        ExecutorService executor = ThreadPoolFactory.newExecutorService(config);

        assertNotNull(executor);
        // On Java 21+, this should be a virtual thread executor
        // On earlier versions, it falls back to ThreadPoolExecutor
        // We just verify it's created successfully

        executor.shutdown();
    }

    @Test
    void testVirtualThreadsEnabledWithCustomConfig() {
        ThreadPoolConfig config = new ThreadPoolConfig.Builder()
            .minPoolSize(25)
            .maxPoolSize(200)
            .keepAliveSeconds(120L)
            .queueSize(50)
            .virtualThreads(true)
            .build();

        // Capture log warnings
        TestLogHandler logHandler = new TestLogHandler();
        Logger logger = Logger.getLogger(ThreadPoolFactory.class.getName());
        logger.addHandler(logHandler);

        ExecutorService executor = ThreadPoolFactory.newExecutorService(config);

        assertNotNull(executor);

        // If running on Java 21+ with virtual threads available, we should see a warning
        // because custom pool config is being ignored
        if (isJava21OrHigher()) {
            assertTrue(logHandler.hasWarning(), "Expected warning about ignored pool configuration");
            assertTrue(logHandler.getWarningMessage().contains("Pool configuration"),
                "Warning should mention pool configuration");
            assertTrue(logHandler.getWarningMessage().contains("will be ignored"),
                "Warning should mention configuration will be ignored");
        }

        logger.removeHandler(logHandler);
        executor.shutdown();
    }

    @Test
    void testVirtualThreadsDisabledUsesThreadPoolExecutor() {
        ThreadPoolConfig config = new ThreadPoolConfig.Builder()
            .minPoolSize(10)
            .maxPoolSize(100)
            .virtualThreads(false)
            .build();

        ExecutorService executor = ThreadPoolFactory.newExecutorService(config);

        assertNotNull(executor);
        assertTrue(executor instanceof ThreadPoolExecutor,
            "When virtual threads are disabled, should always use ThreadPoolExecutor");

        ThreadPoolExecutor tpe = (ThreadPoolExecutor) executor;
        assertEquals(10, tpe.getCorePoolSize());
        assertEquals(100, tpe.getMaximumPoolSize());

        executor.shutdown();
    }

    @Test
    void testNoWarningWithDefaultConfigAndVirtualThreads() {
        ThreadPoolConfig config = new ThreadPoolConfig.Builder()
            .virtualThreads(true)
            .build();

        TestLogHandler logHandler = new TestLogHandler();
        Logger logger = Logger.getLogger(ThreadPoolFactory.class.getName());
        logger.addHandler(logHandler);

        ExecutorService executor = ThreadPoolFactory.newExecutorService(config);

        assertNotNull(executor);

        // No warning should be logged when using default config
        assertFalse(logHandler.hasWarning(),
            "No warning should be logged when using default pool configuration");

        logger.removeHandler(logHandler);
        executor.shutdown();
    }

    @Test
    void testExecutorCanExecuteTasks() throws Exception {
        ThreadPoolConfig config = new ThreadPoolConfig.Builder()
            .minPoolSize(2)
            .maxPoolSize(5)
            .virtualThreads(false)
            .build();

        ExecutorService executor = ThreadPoolFactory.newExecutorService(config);

        // Submit a simple task
        java.util.concurrent.Future<String> future = executor.submit(() -> "Task completed");

        assertEquals("Task completed", future.get());

        executor.shutdown();
    }

    private boolean isJava21OrHigher() {
        return Runtime.version().feature() >= 21;
    }

    // Helper class to capture log messages during tests
    private static class TestLogHandler extends Handler {
        private LogRecord lastWarning;

        @Override
        public void publish(LogRecord record) {
            if (record.getLevel().intValue() >= java.util.logging.Level.WARNING.intValue()) {
                lastWarning = record;
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        public boolean hasWarning() {
            return lastWarning != null;
        }

        public String getWarningMessage() {
            return lastWarning != null ? lastWarning.getMessage() : null;
        }
    }
}
