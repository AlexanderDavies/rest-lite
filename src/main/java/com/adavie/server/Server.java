package com.adavie.server;

import com.adavie.config.ServerConfig;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {

  private ServerSocket serverSocket;
  private final ServerConfig serverConfig;

  public Server() {
    this.serverConfig = ServerConfig.getDefaultServerConfig();
  }

  public Server(ServerConfig serverConfig) {
    this.serverConfig = serverConfig;
  }

  public void start() {
    System.out.println("Starting server");

    try {
      this.serverSocket = new ServerSocket();

      ServerHandler socketHandler = new ServerHandler(serverSocket, serverConfig);

      Thread thread = new Thread(socketHandler);
      thread.start();

      while (!serverSocket.isBound() && socketHandler.getBindException() == null) {
        Thread.sleep(10L);
      }

      if(socketHandler.getBindException() != null) {
        System.out.println("Failed to bind to server");
        throw socketHandler.getBindException();
      }

    } catch (IOException | InterruptedException e) {
      System.out.println(e.getMessage());
      throw new RuntimeException(e);
    }

  }

  private ServerSocket createSocket() throws IOException {
    return new ServerSocket();
  }

  public void stop() {
    if (!serverSocket.isClosed()) {
      try {
        serverSocket.close();
      } catch (IOException e) {
        System.out.println("Failed to stop server");
        throw new RuntimeException(e);
      }
    }
  }
}
