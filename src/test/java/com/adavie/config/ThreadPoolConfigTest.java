package com.adavie.config;

import com.adavie.config.ThreadPoolConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThreadPoolConfigTest {

    @Test
    void testDefaultValues() {
        ThreadPoolConfig config = new ThreadPoolConfig.Builder().build();
        assertNotNull(config);
    }

    @Test
    void testValidMinPoolSize() {
        assertDoesNotThrow(() -> new ThreadPoolConfig.Builder().minPoolSize(1).build());
        assertDoesNotThrow(() -> new ThreadPoolConfig.Builder().minPoolSize(50).build());
        assertDoesNotThrow(() -> new ThreadPoolConfig.Builder().minPoolSize(10000).maxPoolSize(10000).build());
    }

    @Test
    void testInvalidMinPoolSizeTooLow() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ThreadPoolConfig.Builder().minPoolSize(0).build()
        );
        assertEquals("Minimum pool size must be at least 1", exception.getMessage());
    }

    @Test
    void testInvalidMinPoolSizeNegative() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ThreadPoolConfig.Builder().minPoolSize(-1).build()
        );
        assertEquals("Minimum pool size must be at least 1", exception.getMessage());
    }

    @Test
    void testInvalidMinPoolSizeTooHigh() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ThreadPoolConfig.Builder().minPoolSize(10001).build()
        );
        assertEquals("Minimum pool size cannot exceed 10000", exception.getMessage());
    }

    @Test
    void testValidMaxPoolSize() {
        assertDoesNotThrow(() -> new ThreadPoolConfig.Builder().minPoolSize(1).maxPoolSize(1).build());
        assertDoesNotThrow(() -> new ThreadPoolConfig.Builder().maxPoolSize(150).build());
        assertDoesNotThrow(() -> new ThreadPoolConfig.Builder().maxPoolSize(10000).build());
    }

    @Test
    void testInvalidMaxPoolSizeTooLow() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ThreadPoolConfig.Builder().maxPoolSize(0).build()
        );
        assertEquals("Maximum pool size must be at least 1", exception.getMessage());
    }

    @Test
    void testInvalidMaxPoolSizeNegative() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ThreadPoolConfig.Builder().maxPoolSize(-1).build()
        );
        assertEquals("Maximum pool size must be at least 1", exception.getMessage());
    }

    @Test
    void testInvalidMaxPoolSizeTooHigh() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ThreadPoolConfig.Builder().maxPoolSize(10001).build()
        );
        assertEquals("Maximum pool size cannot exceed 10000", exception.getMessage());
    }

    @Test
    void testValidKeepAliveSeconds() {
        assertDoesNotThrow(() -> new ThreadPoolConfig.Builder().keepAliveSeconds(0L).build());
        assertDoesNotThrow(() -> new ThreadPoolConfig.Builder().keepAliveSeconds(60L).build());
        assertDoesNotThrow(() -> new ThreadPoolConfig.Builder().keepAliveSeconds(86400L).build());
    }

    @Test
    void testInvalidKeepAliveSecondsNegative() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ThreadPoolConfig.Builder().keepAliveSeconds(-1L).build()
        );
        assertEquals("Keep alive seconds cannot be negative", exception.getMessage());
    }

    @Test
    void testInvalidKeepAliveSecondsTooHigh() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ThreadPoolConfig.Builder().keepAliveSeconds(86401L).build()
        );
        assertEquals("Keep alive seconds cannot exceed 86400 (24 hours)", exception.getMessage());
    }

    @Test
    void testValidQueueSize() {
        assertDoesNotThrow(() -> new ThreadPoolConfig.Builder().queueSize(0).build());
        assertDoesNotThrow(() -> new ThreadPoolConfig.Builder().queueSize(20).build());
        assertDoesNotThrow(() -> new ThreadPoolConfig.Builder().queueSize(100000).build());
    }

    @Test
    void testValidQueueSizeUnbounded() {
        assertDoesNotThrow(() -> new ThreadPoolConfig.Builder().queueSize(-1).build());
    }

    @Test
    void testInvalidQueueSizeNegative() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ThreadPoolConfig.Builder().queueSize(-2).build()
        );
        assertEquals("Queue size must be non-negative or -1 for unbounded", exception.getMessage());
    }

    @Test
    void testInvalidQueueSizeTooHigh() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ThreadPoolConfig.Builder().queueSize(100001).build()
        );
        assertEquals("Queue size cannot exceed 100000", exception.getMessage());
    }

    @Test
    void testMinPoolSizeExceedsMaxPoolSize() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ThreadPoolConfig.Builder()
                .minPoolSize(200)
                .maxPoolSize(100)
                .build()
        );
        assertEquals("Minimum pool size (200) cannot exceed maximum pool size (100)", exception.getMessage());
    }

    @Test
    void testMinPoolSizeEqualsMaxPoolSize() {
        assertDoesNotThrow(() -> new ThreadPoolConfig.Builder()
            .minPoolSize(100)
            .maxPoolSize(100)
            .build()
        );
    }

    @Test
    void testBoundaryMinPoolSizeEqualsOne() {
        assertDoesNotThrow(() -> new ThreadPoolConfig.Builder()
            .minPoolSize(1)
            .maxPoolSize(1)
            .build()
        );
    }

    @Test
    void testBoundaryMaxPoolSizeAtLimit() {
        assertDoesNotThrow(() -> new ThreadPoolConfig.Builder()
            .minPoolSize(10000)
            .maxPoolSize(10000)
            .build()
        );
    }

    @Test
    void testCrossFieldValidationOrder() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ThreadPoolConfig.Builder()
                .maxPoolSize(50)
                .minPoolSize(100)
                .build()
        );
        assertEquals("Minimum pool size (100) cannot exceed maximum pool size (50)", exception.getMessage());
    }

    @Test
    void testBuilderChaining() {
        assertDoesNotThrow(() -> new ThreadPoolConfig.Builder()
            .minPoolSize(10)
            .maxPoolSize(100)
            .keepAliveSeconds(120L)
            .queueSize(50)
            .build()
        );
    }

    @Test
    void testMultipleBuildCalls() {
        ThreadPoolConfig.Builder builder = new ThreadPoolConfig.Builder()
            .minPoolSize(10)
            .maxPoolSize(100);

        ThreadPoolConfig config1 = builder.build();
        ThreadPoolConfig config2 = builder.build();

        assertNotNull(config1);
        assertNotNull(config2);
        assertNotSame(config1, config2);
    }

    @Test
    void testVirtualThreadsDefaultTrue() {
        ThreadPoolConfig config = new ThreadPoolConfig.Builder().build();
        assertTrue(config.isVirtualThreads());
    }

    @Test
    void testVirtualThreadsSetToFalse() {
        ThreadPoolConfig config = new ThreadPoolConfig.Builder()
            .virtualThreads(false)
            .build();
        assertFalse(config.isVirtualThreads());
    }

    @Test
    void testVirtualThreadsSetToTrue() {
        ThreadPoolConfig config = new ThreadPoolConfig.Builder()
            .virtualThreads(true)
            .build();
        assertTrue(config.isVirtualThreads());
    }

    @Test
    void testVirtualThreadsWithCustomPoolConfig() {
        ThreadPoolConfig config = new ThreadPoolConfig.Builder()
            .minPoolSize(25)
            .maxPoolSize(200)
            .keepAliveSeconds(120L)
            .queueSize(50)
            .virtualThreads(true)
            .build();

        assertEquals(25, config.getMinPoolSize());
        assertEquals(200, config.getMaxPoolSize());
        assertEquals(120L, config.getKeepAliveSeconds());
        assertEquals(50, config.getQueueSize());
        assertTrue(config.isVirtualThreads());
    }

    @Test
    void testVirtualThreadsDisabledWithCustomPoolConfig() {
        ThreadPoolConfig config = new ThreadPoolConfig.Builder()
            .minPoolSize(25)
            .maxPoolSize(200)
            .keepAliveSeconds(120L)
            .queueSize(50)
            .virtualThreads(false)
            .build();

        assertEquals(25, config.getMinPoolSize());
        assertEquals(200, config.getMaxPoolSize());
        assertEquals(120L, config.getKeepAliveSeconds());
        assertEquals(50, config.getQueueSize());
        assertFalse(config.isVirtualThreads());
    }

    @Test
    void testBuilderChainingWithVirtualThreads() {
        assertDoesNotThrow(() -> new ThreadPoolConfig.Builder()
            .minPoolSize(10)
            .maxPoolSize(100)
            .keepAliveSeconds(120L)
            .queueSize(50)
            .virtualThreads(false)
            .build()
        );
    }

    @Test
    void testIsDefaultWithAllDefaultValues() {
        ThreadPoolConfig config = new ThreadPoolConfig.Builder().build();
        assertFalse(config.isDefault());
    }

    @Test
    void testIsDefaultWithMultipleCustomValues() {
        ThreadPoolConfig config = new ThreadPoolConfig.Builder()
            .minPoolSize(100)
            .maxPoolSize(200)
            .queueSize(100)
            .keepAliveSeconds(120L)
            .build();
        assertTrue(config.isDefault());
    }
}
