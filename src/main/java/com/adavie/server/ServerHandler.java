package com.adavie.server;

import com.adavie.request.ClientHandler;
import com.adavie.config.ServerConfig;
import com.adavie.util.ThreadPoolFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

class ServerHandler implements Runnable {
  private final ServerSocket serverSocket;
  private final ServerConfig serverConfig;
  private IOException bindException;
  private static final Logger LOGGER = Logger.getLogger(ServerHandler.class.getName());

  public ServerHandler(ServerSocket serverSocket, ServerConfig serverConfig) {
    this.serverSocket = serverSocket;
    this.serverConfig = serverConfig;
  }

  public IOException getBindException() {
    return bindException;
  }

  @Override
  public void run() {
    SocketAddress socketAddress = new InetSocketAddress(serverConfig.getHostname(), serverConfig.getPort());
    try {
      serverSocket.bind(socketAddress);
    } catch (IOException e) {
      this.bindException = e;
    }

    ExecutorService executorService = ThreadPoolFactory.newExecutorService(serverConfig.getThreadPoolConfig());

    try {
      while (!serverSocket.isClosed()) {
        try {
          Socket clientSocket = serverSocket.accept();

          clientSocket.setSoTimeout(serverConfig.getClientConnectionTimeout());

          ClientHandler requestHandler = ClientHandler.createRequestHandler(clientSocket);

          executorService.execute(requestHandler);

        } catch (IOException e) {
          LOGGER.warning("Exception accepting client connection.");
        }
      }
    } finally {
      shutdownExecutorService(executorService);
    }
  }

  private void shutdownExecutorService(ExecutorService executorService) {

    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
        LOGGER.warning("Threads didn't finish in 30s, forcing shutdown");
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow();
    }
  }
}
