package com.adavie.config;

import java.io.File;
import java.util.logging.Level;

public class LoggerConfig {
  private static final boolean DEFAULT_ENABLE_FILE_LOGGING = true;
  private static final String DEFAULT_LOG_FILE_PATH = "/logs/app.log";
  private static final Level DEFAULT_LOG_LEVEL = Level.ALL;
  private static final int DEFAULT_FILE_LIMIT_BYTES = 10485760;
  private static final int DEFAULT_FILE_COUNT = 5;

  private final boolean enableFileLogging;
  private final String logFilePath;
  private final Level logLevel;
  private final int fileLimitBytes;
  private final int fileCount;

  private LoggerConfig(Builder builder) {
    this.enableFileLogging = builder.enableFileLogging;
    this.logFilePath = builder.logFilePath;
    this.logLevel = builder.logLevel;
    this.fileLimitBytes = builder.fileLimitBytes;
    this.fileCount = builder.fileCount;
  }

  public boolean isEnableFileLogging() {
    return enableFileLogging;
  }

  public String isLogFilePath() {
    return logFilePath;
  }

  public Level getLogLevel() {
    return logLevel;
  }

  public int getFileLimitBytes() {
    return fileLimitBytes;
  }

  public int getFileCount() {
    return fileCount;
  }

  public static class Builder {
    private boolean enableFileLogging = DEFAULT_ENABLE_FILE_LOGGING;
    private String logFilePath = DEFAULT_LOG_FILE_PATH;
    private Level logLevel = DEFAULT_LOG_LEVEL;
    private int fileLimitBytes = DEFAULT_FILE_LIMIT_BYTES;
    private int fileCount = DEFAULT_FILE_COUNT;

    public Builder enabledFileLogging(boolean enableFileLogging) {
      this.enableFileLogging = enableFileLogging;
      return this;
    }

    public Builder logFilePath(String logFilePath) {
      if (logFilePath.trim().isEmpty()) {
        throw new IllegalArgumentException("Log file path must be specified when file logging is enabled");
      }
      if (logFilePath.length() > 255) {
        throw new IllegalArgumentException("Log file path exceeds maximum length of 255 characters");
      }

      this.logFilePath = logFilePath.trim();
      return this;
    }

    public Builder logLevel(Level logLevel) {
      this.logLevel = logLevel;
      return this;
    }

    public Builder fileLimitBytes(int fileLimitBytes) {

      if (fileLimitBytes <= 0) {
        throw new IllegalArgumentException("File limit must be positive, got: " + fileLimitBytes);
      }

      // Reasonable minimum (e.g., 1KB)
      if (fileLimitBytes < 1024) {
        throw new IllegalArgumentException("File limit too small, minimum is 1024 bytes (1KB)");
      }

      // Reasonable maximum (e.g., 1GB to prevent huge files)
      if (fileLimitBytes > 1024 * 1024 * 1024) {
        throw new IllegalArgumentException("File limit too large, maximum is 1GB");
      }

      this.fileLimitBytes = fileLimitBytes;
      return this;
    }

    public Builder fileCount(int fileCount) {

      if (fileCount <= 0) {
        throw new IllegalArgumentException("File count must be positive, got: " + fileCount);
      }

      if (fileCount > 100) {
        throw new IllegalArgumentException("File count too large, maximum is 100 files");
      }

      this.fileCount = fileCount;
      return this;
    }

    public LoggerConfig Build() {
      return new LoggerConfig(this);
    }
  }
}
