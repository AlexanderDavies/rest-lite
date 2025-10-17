package com.adavie.server;

import com.adavie.config.ServerConfig;
import com.adavie.util.LoggerInitializer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

  private ServerSocket serverSocket;
  private final ServerConfig serverConfig;
  private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

  public Server() {
    this.serverConfig = ServerConfig.getDefaultServerConfig();
    initializeLogger();
  }

  public Server(ServerConfig serverConfig) {
    this.serverConfig = serverConfig;
    initializeLogger();
  }

  public void start() {
    LOGGER.info("Starting server on port:" + serverConfig.getPort());

    try {
      this.serverSocket = createServerSocket();

      ServerHandler socketHandler = new ServerHandler(serverSocket, serverConfig);

      Thread thread = new Thread(socketHandler);
      thread.start();

      while (!serverSocket.isBound() && socketHandler.getBindException() == null) {
        Thread.sleep(10L);
      }

      if(socketHandler.getBindException() != null) {
        LOGGER.severe("Failed to bind to server");
        throw socketHandler.getBindException();
      }

    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }

  }

  private ServerSocket createServerSocket() throws IOException {
    return new ServerSocket();
  }

  private void initializeLogger() {
    LoggerInitializer.configureRootLogger(this.serverConfig.getLoggerConfig());
  }

  public void stop() {
    if (!serverSocket.isClosed()) {
      try {
        serverSocket.close();
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE,"Failed to stop server", e);
        throw new RuntimeException(e);
      }
    }
  }
}
