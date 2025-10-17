package com.adavie.util;

import com.adavie.config.LoggerConfig;

import java.io.IOException;
import java.util.logging.*;

public class LoggerInitializer {

  public static void configureRootLogger(LoggerConfig loggerConfig) {
    Logger rootLogger = Logger.getLogger("");

    Handler[] rootHandlers = rootLogger.getHandlers();
    for (Handler handler : rootHandlers) {
      rootLogger.removeHandler(handler);
    }

    rootLogger.setLevel(loggerConfig.getLogLevel());

    addConsoleHandler(rootLogger, loggerConfig.getLogLevel());

    if (loggerConfig.isEnableFileLogging()) {
      addFileHandler(rootLogger, loggerConfig);
    }
  }

  private static void addConsoleHandler(Logger rootLogger, Level logLevel) {
    ConsoleHandler consoleHandler = new ConsoleHandler();
    consoleHandler.setLevel(logLevel);
    consoleHandler.setFormatter(new LogFormatter());
    rootLogger.addHandler(consoleHandler);
  }

  private static void addFileHandler(Logger rootLogger, LoggerConfig loggerConfig) {
    try {
      FileHandler fileHandler = new FileHandler(loggerConfig.getLogFilePath(), loggerConfig.getFileLimitBytes(), loggerConfig.getFileCount(), true);
      fileHandler.setLevel(loggerConfig.getLogLevel());
      fileHandler.setFormatter(new LogFormatter());
      rootLogger.addHandler(fileHandler);
    } catch (IOException e) {
      rootLogger.log(Level.WARNING, "Failed to configure logger to output to file: " + loggerConfig.getLogFilePath(), e);
    }
  }

  private static class LogFormatter extends Formatter {
    public String format(LogRecord record) {
      return String.format("[%1$tF %1$tT] [%2$-7s] %3$s - %4$s%n",
        new java.util.Date(record.getMillis()),
        record.getLevel(),
        record.getLoggerName(),
        formatMessage(record)
      );
    }
  }
}
