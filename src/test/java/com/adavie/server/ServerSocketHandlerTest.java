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
        Server firstServer = new Server();
        Thread firstServerThread = new Thread(() -> firstServer.start());
        firstServerThread.start();

        Thread.sleep(500);

        try {
            ServerSocket newSocket = new ServerSocket();
            ServerConfig serverConfig = ServerConfig.getDefaultServerConfig();
            ServerHandler handler = new ServerHandler(newSocket, serverConfig);

            Thread handlerThread = new Thread(handler);
            handlerThread.start();

            Thread.sleep(500);

            IOException bindException = handler.getBindException();
            assertNotNull(bindException, "Bind exception should be captured");
            assertTrue(bindException.getMessage().contains("Address already in use") ||
                            bindException.getMessage().contains("already bound") ||
                            bindException.getMessage().contains("Address in use"),
                    "Exception should indicate port conflict, got: " + bindException.getMessage());

            handlerThread.interrupt();
            handlerThread.join(1000);
        } finally {
            firstServer.stop();
            firstServerThread.join(2000);
        }
    }

    @Test
    void testThreadPoolHandlesMultipleConcurrentClients() throws Exception {
        Server server = new Server();
        Thread serverThread = new Thread(() -> server.start());
        serverThread.start();

        Thread.sleep(500);

        try {
            int clientCount = 10;
            CountDownLatch connectLatch = new CountDownLatch(clientCount);
            List<Socket> clients = Collections.synchronizedList(new ArrayList<>());
            List<Exception> errors = Collections.synchronizedList(new ArrayList<>());

            for (int i = 0; i < clientCount; i++) {
                final int clientId = i;
                new Thread(() -> {
                    try {
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress("localhost", 8081), 2000);
                        clients.add(socket);
                        connectLatch.countDown();
                    } catch (Exception e) {
                        errors.add(e);
                        connectLatch.countDown();
                    }
                }).start();
            }

            boolean allConnected = connectLatch.await(5, TimeUnit.SECONDS);

            assertTrue(allConnected, "All clients should connect within timeout");
            assertEquals(0, errors.size(), "No connection errors should occur");
            assertEquals(clientCount, clients.size(), "All clients should be connected");

            clients.forEach(s -> {
                try {
                    s.close();
                } catch (IOException e) {
                }
            });
        } finally {
            server.stop();
            serverThread.join(2000);
        }
    }
}
