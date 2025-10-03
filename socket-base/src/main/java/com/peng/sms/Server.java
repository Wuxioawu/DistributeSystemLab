package com.peng.sms;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static com.peng.sms.CommonConstant.SERVER_PORT;


public class Server {

    public static void startServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server start successly, Listening on port :" + SERVER_PORT);

            // While server is running:
            while (true) {
                // - Accept client connection
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket.getInetAddress());

                // - Create input/output streams
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

                // - Read client message
                String clientMessage = bufferedReader.readLine();
                System.out.println("Received: " + clientMessage);

                // - Process request (e.g., convert to uppercase)
                String response = clientMessage.toUpperCase();

                // - Send response back to client
                printWriter.println(response);
                // - Close connection
                socket.close();
                System.out.println("the socket is closed");
                break;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

