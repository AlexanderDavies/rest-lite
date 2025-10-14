package com.adavie.server;

import com.adavie.request.ClientHandler;
import com.adavie.util.ThreadPoolFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class ServerHandler implements Runnable {
  private final ServerSocket serverSocket;
  private final ServerConfig serverConfig;
  private IOException bindException;

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

    ThreadPoolExecutor threadPoolExecutor = ThreadPoolFactory.newThreadPool();

    try {
      while (!serverSocket.isClosed()) {
        try {
          Socket clientSocket = serverSocket.accept();

          clientSocket.setSoTimeout(serverConfig.getClientConnectionTimeout());

          ClientHandler requestHandler =  ClientHandler.createRequestHandler(clientSocket);

          threadPoolExecutor.execute(requestHandler);

        } catch (IOException ex) {
          System.out.println("Exception accepting client connection.");
        }
      }
    } finally {
      shutdownThreadPool(threadPoolExecutor);
    }
  }

  private void shutdownThreadPool(ThreadPoolExecutor threadPoolExecutor) {

    threadPoolExecutor.shutdown();
    try {
      if (!threadPoolExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
        System.out.println("Threads didn't finish in 30s, forcing shutdown");
        threadPoolExecutor.shutdownNow();
      }
    } catch (InterruptedException ex) {
      threadPoolExecutor.shutdownNow();
    }
  }
}
