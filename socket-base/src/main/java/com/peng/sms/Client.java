package com.peng.sms;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import static com.peng.sms.CommonConstant.LOCAL_HOST;

@Slf4j
public class Client {

    public static void main(String[] args) {

        Thread.currentThread().setName("client");

        new Thread(() -> {
            Server.startServer();
        },"Server").start();

        Scanner scanner = new Scanner(System.in);
        // Create Socket connection to server
        System.out.print("Enter the port: ");

        // Test error handling for invalid connections
        int serverPort = Integer.parseInt(scanner.nextLine());
        if (serverPort != CommonConstant.SERVER_PORT) {
            log.error("the port is wrong");
        }

        try (Socket socket = new Socket(LOCAL_HOST, serverPort);
             // Create input/output streams
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            // Wait for response
            log.info("Connected to server at " + LOCAL_HOST + ":{}", serverPort);

            // Print server response
            System.out.print("Enter message: ");
            String message = userInput.readLine();

            out.println(message);

            String response = in.readLine();
            log.info("Server response: {}", response);

        } catch (IOException e) {
            e.printStackTrace();
        }
        // Create Socket connection to server
        System.out.println("Client closed.");
    }
}
