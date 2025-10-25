package com.adavie.request;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {

  private final Socket clientSocket;
  private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());


  private ClientHandler(Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  public static ClientHandler createRequestHandler(Socket clientSocket) {
    return new ClientHandler(clientSocket);
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
