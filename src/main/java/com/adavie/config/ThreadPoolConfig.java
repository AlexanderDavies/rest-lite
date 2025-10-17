package com.adavie.config;

public final class ThreadPoolConfig {
  public static final int DEFAULT_MIN_POOL_SIZE = 50;
  public static final int DEFAULT_MAX_POOL_SIZE = 150;
  public static final int DEFAULT_QUEUE_SIZE = 20;
  public static final long DEFAULT_KEEPALIVE_SECONDS = 60L;
  public static final boolean DEFAULT_VIRTUAL_THREADS = true;

  private final int minPoolSize;
  private final int maxPoolSize;
  private final long keepAliveSeconds;
  private final int queueSize;
  private final boolean virtualThreads;

  public int getMinPoolSize() {
    return minPoolSize;
  }

  public int getMaxPoolSize() {
    return maxPoolSize;
  }

  public long getKeepAliveSeconds() {
    return keepAliveSeconds;
  }

  public int getQueueSize() {
    return queueSize;
  }

  public boolean isVirtualThreads() {
    return virtualThreads;
  }

  public boolean isDefault() {
    return minPoolSize != ThreadPoolConfig.DEFAULT_MIN_POOL_SIZE
      || maxPoolSize != ThreadPoolConfig.DEFAULT_MAX_POOL_SIZE
      || queueSize != ThreadPoolConfig.DEFAULT_QUEUE_SIZE
      || keepAliveSeconds != ThreadPoolConfig.DEFAULT_KEEPALIVE_SECONDS;
  }

  private ThreadPoolConfig(Builder builder) {
    this.minPoolSize = builder.minPoolSize;
    this.maxPoolSize = builder.maxPoolSize;
    this.keepAliveSeconds = builder.keepAliveSeconds;
    this.queueSize = builder.queueSize;
    this.virtualThreads = builder.virtualThreads;
  }

  public static class Builder {
    private int minPoolSize = DEFAULT_MIN_POOL_SIZE;
    private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;
    private long keepAliveSeconds = DEFAULT_KEEPALIVE_SECONDS;
    private int queueSize = DEFAULT_QUEUE_SIZE;
    private boolean virtualThreads = DEFAULT_VIRTUAL_THREADS;

    public Builder minPoolSize(int minPoolSize) {
      if (minPoolSize < 1) {
        throw new IllegalArgumentException("Minimum pool size must be at least 1");
      }
      if (minPoolSize > 10000) {
        throw new IllegalArgumentException("Minimum pool size cannot exceed 10000");
      }
      this.minPoolSize = minPoolSize;
      return this;
    }

    public Builder maxPoolSize(int maxPoolSize) {
      if (maxPoolSize < 1) {
        throw new IllegalArgumentException("Maximum pool size must be at least 1");
      }
      if (maxPoolSize > 10000) {
        throw new IllegalArgumentException("Maximum pool size cannot exceed 10000");
      }
      this.maxPoolSize = maxPoolSize;
      return this;
    }

    public Builder keepAliveSeconds(long keepAliveSeconds) {
      if (keepAliveSeconds < 0) {
        throw new IllegalArgumentException("Keep alive seconds cannot be negative");
      }
      if (keepAliveSeconds > 86400) {
        throw new IllegalArgumentException("Keep alive seconds cannot exceed 86400 (24 hours)");
      }
      this.keepAliveSeconds = keepAliveSeconds;
      return this;
    }

    public Builder queueSize(int queueSize) {
      if (queueSize < 0 && queueSize != -1) {
        throw new IllegalArgumentException("Queue size must be non-negative or -1 for unbounded");
      }
      if (queueSize > 100000) {
        throw new IllegalArgumentException("Queue size cannot exceed 100000");
      }
      this.queueSize = queueSize;
      return this;
    }

    public Builder virtualThreads(boolean virtualThreads) {
      this.virtualThreads = virtualThreads;
      return this;
    }

    public ThreadPoolConfig build() {
      if (minPoolSize > maxPoolSize) {
        throw new IllegalArgumentException(
            "Minimum pool size (" + minPoolSize + ") cannot exceed maximum pool size (" + maxPoolSize + ")");
      }
      return new ThreadPoolConfig(this);
    }
  }
}
