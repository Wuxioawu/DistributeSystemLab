package com.peng.sms;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static com.peng.sms.CommonConstant.LOCAL_HOST;
import static com.peng.sms.CommonConstant.SERVER_PORT;

public class Client {

    public static void main(String[] args) {
        System.out.println("--------------------------------------- the socket-lab start ---------------------------------------");
        Thread.currentThread().setName("client");

        long start = System.currentTimeMillis();

        new Thread(() -> {
            Server.startServer();
        }, "Server").start();

        try (Socket socket = new Socket(LOCAL_HOST, SERVER_PORT);
             // Create input/output streams
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            // Wait for response
            System.out.println("Connected to server at " + LOCAL_HOST + ": " + SERVER_PORT);

            // Print server response
            System.out.print("Enter message: ");
            String message = "wupeng";

            out.println(message);

            String response = in.readLine();
            System.out.println("Server response: " + response);
            long end = System.currentTimeMillis();
            System.out.println("Socket-lab test: the time is: " + (end - start));

        } catch (IOException e) {
            e.printStackTrace();
        }
        // Create Socket connection to server
        System.out.println("Client closed.");
        System.out.println("--------------------------------------- the socket-lab end ---------------------------------------");
    }
}
