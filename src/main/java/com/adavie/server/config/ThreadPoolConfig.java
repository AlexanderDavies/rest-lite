package com.adavie.server.config;

public final class ThreadPoolConfig {
  private int minPoolSize;
  private int maxPoolSize;
  private long keepAliveSeconds;
  private int queueSize;

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

  private ThreadPoolConfig(Builder builder) {
    this.minPoolSize = builder.minPoolSize;
    this.maxPoolSize = builder.maxPoolSize;
    this.keepAliveSeconds = builder.keepAliveSeconds;
    this.queueSize = builder.queueSize;
  }

  public static class Builder {
    private int minPoolSize = 50;
    private int maxPoolSize = 150;
    private long keepAliveSeconds = 60L;
    private int queueSize = 20;

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

    public ThreadPoolConfig build() {
      if (minPoolSize > maxPoolSize) {
        throw new IllegalArgumentException(
            "Minimum pool size (" + minPoolSize + ") cannot exceed maximum pool size (" + maxPoolSize + ")");
      }
      return new ThreadPoolConfig(this);
    }
  }
}
