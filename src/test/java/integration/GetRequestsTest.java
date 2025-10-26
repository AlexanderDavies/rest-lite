package integration;

import com.adavie.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static java.net.http.HttpClient.Version.*;
import static org.junit.jupiter.api.Assertions.*;

public class GetRequestsTest {

  private static Server server;
  private static final URI uri = URI.create("http://localhost:8081/");
  private static final HttpClient client = HttpClient.newBuilder()
    .version(HTTP_1_1)
    .connectTimeout(Duration.ofSeconds(60L))
    .build();

  @BeforeAll
  static void setUp() {
    server = new Server();
    server.start();
  }

  @AfterAll
  static void tearDown() {
    if (server != null) {
      server.stop();
    }
  }

  @Test
  void testGetRequestReturns200Success() throws Exception {

    HttpRequest request = HttpRequest.newBuilder()
      .uri(uri)
      .GET()
      .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(200, response.statusCode());
    assertEquals("Hello, World!", response.body());
  }
}
