package com.adavie.config;

public final class ServerConfig {

  private final String hostname;
  private final int port;
  private final int clientConnectionTimeout;
  private final ThreadPoolConfig threadPoolConfig;
  private final LoggerConfig loggerConfig;

  private ServerConfig(Builder builder) {
    this.hostname = builder.hostname;
    this.port = builder.port;
    this.clientConnectionTimeout = builder.clientConnectionTimeout;
    this.threadPoolConfig = builder.threadPoolConfig;
    this.loggerConfig = builder.loggerConfig;
  }

  public static ServerConfig getDefaultServerConfig() {
    return new ServerConfig.Builder().build();
  }

  public String getHostname() {
    return hostname;
  }

  public int getPort() {
    return port;
  }

  public int getClientConnectionTimeout() {
    return clientConnectionTimeout;
  }

  public ThreadPoolConfig getThreadPoolConfig() {
    return threadPoolConfig;
  }

  public LoggerConfig getLoggerConfig() {return loggerConfig;}

  public static class Builder {
    private String hostname = "localhost";
    private int port = 8081;
    private int clientConnectionTimeout = 30;
    private ThreadPoolConfig threadPoolConfig;
    private LoggerConfig loggerConfig;

    public Builder() {}

    public Builder hostname(String hostname) {
      if (hostname == null || hostname.trim().isEmpty()) {
        throw new IllegalArgumentException("Hostname cannot be null or empty");
      }

      hostname = hostname.trim();

      // Length check (RFC 1035)
      if (hostname.length() > 253) {
        throw new IllegalArgumentException("Hostname too long (max 253 characters)");
      }

      // Whitespace check
      if (hostname.contains(" ")) {
        throw new IllegalArgumentException("Hostname cannot contain spaces");
      }

      // Format validation - allow letters, digits, dots, colons (IPv6), hyphens, underscores
      if (!hostname.matches("^[a-zA-Z0-9.:_-]+$")) {
        throw new IllegalArgumentException("Hostname contains invalid characters");
      }

      this.hostname = hostname;
      return this;
    }

    public Builder port(int port) {

      if (port < 1 || port > 65535) {
        throw new IllegalArgumentException("Port must be between 1 and 65535");
      }

      this.port = port;
      return this;
    }

    public Builder clientConnectionTimeout(int clientConnectionTimeout) {
      if (clientConnectionTimeout < 1) {
        throw new IllegalArgumentException("Client connection timeout must be at least 1");
      }
      this.clientConnectionTimeout = clientConnectionTimeout;
      return this;
    }

    public Builder threadPoolConfig(ThreadPoolConfig threadPoolConfig) {
      this.threadPoolConfig = threadPoolConfig;
      return this;
    }

    public Builder loggerConfig(LoggerConfig loggerConfig) {
      this.loggerConfig = loggerConfig;
      return this;
    }

    public ServerConfig build() {
      if(this.threadPoolConfig == null) {
        this.threadPoolConfig = new ThreadPoolConfig.Builder().build();
      }

      if(this.loggerConfig == null) {
        this.loggerConfig = new LoggerConfig.Builder().build();
      }
      return new ServerConfig(this);
    }
  }
}
