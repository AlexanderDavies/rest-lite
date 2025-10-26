package com.adavie.request;

import com.adavie.router.Routes;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {

  private final Socket clientSocket;
  private final Routes routes;
  private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());


  private ClientHandler(Socket clientSocket, Routes routes) {
    this.clientSocket = clientSocket;
    this.routes = routes;
  }

  public static ClientHandler createRequestHandler(Socket clientSocket, Routes routes) {
    return new ClientHandler(clientSocket, routes);
  }

  @Override
  public void run() {
    try {
      //parse the incoming http request via the InputStream
      Request request = new HttpRequest(clientSocket.getInputStream(), clientSocket.getOutputStream());

      request.read();

      //identify the target route

      //map the JSON to the body of the route (for post requests)

      //parse the response from the route to JSON via the OutputStream
      request.write();

    } catch (RestException e) {
      // TO DO need to handle sending REST Exception to the client
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      closeClientConnection();
    }
  }

  private void closeClientConnection() {
    if (!clientSocket.isClosed()) {
      try {
        clientSocket.close();
      } catch (IOException e) {
        LOGGER.warning("Failed to close client connection on port:" + clientSocket.getPort());
      }
    }
  }
}
