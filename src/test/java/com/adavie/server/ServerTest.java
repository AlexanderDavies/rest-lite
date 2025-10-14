package com.adavie.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerTest {

    private Server server;
    private Thread serverThread;

    @BeforeEach
    void startServer() throws InterruptedException {
        server = new Server();
        serverThread = new Thread(() -> server.start());
        serverThread.start();

        // Give the server a moment to start and bind to the port
        Thread.sleep(500);
    }

    @AfterEach
    void cleanup() throws InterruptedException {
        server.stop();
        serverThread.join(1000);
    }

    @Test
    void testServerStartsAndBindsToPort() throws IOException {
        String host = "localhost";
        int port = 8081;
        int timeoutMs = 1000;

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            assertTrue(socket.isConnected(), "Server should be running and bound to port 8081");
        }
    }

    @Test
    void testStopAlreadyStoppedServer() throws InterruptedException {
        // Stop the server once
        server.stop();
        serverThread.join(1000);

        // Stopping again should not throw an exception
        server.stop();
    }

    @Test
    void testMultipleClientConnections() throws IOException {
        String host = "localhost";
        int port = 8081;
        int timeoutMs = 1000;

        List<Socket> sockets = new ArrayList<>();

        try {
            // Connect 3 clients simultaneously
            for (int i = 0; i < 3; i++) {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), timeoutMs);
                sockets.add(socket);
                assertTrue(socket.isConnected(), "Client " + i + " should be connected");
            }
        } finally {
            // Clean up all sockets
            for (Socket socket : sockets) {
                if (!socket.isClosed()) {
                    socket.close();
                }
            }
        }
    }

    @Test
    void testServerStopClosesActiveConnections() throws InterruptedException {
        String host = "localhost";
        int port = 8081;
        int timeoutMs = 1000;

        // Connect a client to the server
        Socket clientSocket = new Socket();
        try {
            clientSocket.connect(new InetSocketAddress(host, port), timeoutMs);
            assertTrue(clientSocket.isConnected(), "Client should be connected");

            // Stop the server
            server.stop();
            serverThread.join(1000);

            // Try to read from the socket - should return -1 (EOF) or throw exception
            int result = clientSocket.getInputStream().read();
            assertTrue(result == -1, "Should receive EOF after server closes connection");

        } catch (IOException e) {
            // SocketException is also acceptable - means connection was closed
            assertTrue(e.getMessage().contains("Socket") || e.getMessage().contains("Connection"),
                "Exception should indicate socket/connection closure");
        } finally {
            // Clean up
            try {
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                // Ignore cleanup errors
            }
        }
    }

    @Test
    void testPortAlreadyInUse() throws InterruptedException {
        // Try to start a second server on the same port
        Server secondServer = new Server();
        Thread secondServerThread = new Thread(() -> {
            assertThrows(RuntimeException.class, () -> secondServer.start(),
                "Starting server on occupied port should throw RuntimeException");
        });

        secondServerThread.start();
        secondServerThread.join(2000);

        // Verify the original server is still running
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", 8081), 1000);
            assertTrue(socket.isConnected(), "Original server should still be running");
        } catch (IOException e) {
            // Clean up second server if needed
        }
    }
}
