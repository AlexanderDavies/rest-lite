package com.adavie.server;

import com.adavie.config.ServerConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ServerSocketHandlerTest {

    @Test
    void testBindExceptionCapturedWhenPortAlreadyInUse() throws Exception {
        // Start the first server to occupy port 8081
        Server firstServer = new Server();
        Thread firstServerThread = new Thread(() -> firstServer.start());
        firstServerThread.start();

        // Wait for first server to bind
        Thread.sleep(500);

        try {
            // Try to bind another server to the same port
            ServerSocket newSocket = new ServerSocket();
            ServerConfig serverConfig = ServerConfig.getDefaultServerConfig();
            ServerHandler handler = new ServerHandler(newSocket, serverConfig);

            Thread handlerThread = new Thread(handler);
            handlerThread.start();

            // Give it time to attempt binding
            Thread.sleep(500);

            // Verify the bind exception was captured
            IOException bindException = handler.getBindException();
            assertNotNull(bindException, "Bind exception should be captured");
            assertTrue(bindException.getMessage().contains("Address already in use") ||
                            bindException.getMessage().contains("already bound") ||
                            bindException.getMessage().contains("Address in use"),
                    "Exception should indicate port conflict, got: " + bindException.getMessage());

            // Cleanup
            handlerThread.interrupt();
            handlerThread.join(1000);
        } finally {
            // Stop first server
            firstServer.stop();
            firstServerThread.join(2000);
        }
    }

    @Test
    void testThreadPoolHandlesMultipleConcurrentClients() throws Exception {
        // Start a server first
        Server server = new Server();
        Thread serverThread = new Thread(() -> server.start());
        serverThread.start();

        // Give server time to start
        Thread.sleep(500);

        try {
            int clientCount = 10;
            CountDownLatch connectLatch = new CountDownLatch(clientCount);
            List<Socket> clients = Collections.synchronizedList(new ArrayList<>());
            List<Exception> errors = Collections.synchronizedList(new ArrayList<>());

            // Connect clients concurrently
            for (int i = 0; i < clientCount; i++) {
                final int clientId = i;
                new Thread(() -> {
                    try {
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress("localhost", 8081), 2000);
                        clients.add(socket);
                        System.out.println("Client " + clientId + " connected");
                        connectLatch.countDown();
                    } catch (Exception e) {
                        errors.add(e);
                        connectLatch.countDown();
                    }
                }).start();
            }

            // Wait for all connections
            boolean allConnected = connectLatch.await(5, TimeUnit.SECONDS);

            assertTrue(allConnected, "All clients should connect within timeout");
            assertEquals(0, errors.size(), "No connection errors should occur");
            assertEquals(clientCount, clients.size(), "All clients should be connected");

            // Cleanup
            clients.forEach(s -> {
                try {
                    s.close();
                } catch (IOException e) {
                }
            });
        } finally {
            // Stop server
            server.stop();
            serverThread.join(2000);
        }
    }
}
