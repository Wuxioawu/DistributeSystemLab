package com.peng.sms;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static com.peng.sms.CommonConstant.LOCAL_HOST;
import static com.peng.sms.CommonConstant.SERVER_PORT;
import static org.junit.Assert.assertThrows;

/**
 * Test connection establishment
 */

@RunWith(JUnit4.class)
public class ServerTest extends TestCase {

    @Test
    public void testServer() {
        new Thread(Server::startServer, "Server").start();
    }

    //Test connection establishment
    @Test
    public void testMain() {
        try {
            new Thread(Server::startServer, "Server").start();
            Thread.sleep(500);
            // Connect to the server
            try (Socket socket = new Socket(LOCAL_HOST, SERVER_PORT)) {
                boolean connected = socket.isConnected();
                System.out.println("Connected: " + connected);
                assertTrue(connected); // JUnit assertion
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testConnectionAndMessage() throws IOException, InterruptedException {
        // Start the server in a new thread
        Thread serverThread = new Thread(Server::startServer, "Server");
        serverThread.start();

        // Give the server some time to start
        Thread.sleep(500);

        // Connect client socket
        try (Socket socket = new Socket(CommonConstant.LOCAL_HOST, CommonConstant.SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send message to server
            String message = "hello world";
            out.println(message);

            // Read response
            String response = in.readLine();
            assertEquals("HELLO WORLD", response);  // Server converts to uppercase
        }

        // Stop the server thread
        serverThread.interrupt();
    }


    //Test error handling for invalid connections
    @Test
    public void testInvalidPort() {
        int invalidPort = 12345;

        Exception exception = assertThrows(IOException.class, () -> {
            new Socket(LOCAL_HOST, invalidPort);
        });
        String expectedMessage = "Connection refused"; // on most systems
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }
}