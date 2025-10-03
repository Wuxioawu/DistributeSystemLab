package com.peng.sms;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static com.peng.sms.CommonConstant.SERVER_PORT;

@Slf4j
public class Server {

    public static void startServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            log.info("Server start successly, Listening on port :" + SERVER_PORT);

            // While server is running:
            while (true) {
                // - Accept client connection
                Socket socket = serverSocket.accept();
                log.info("Client connected: {}", socket.getInetAddress());

                // - Create input/output streams
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

                // - Read client message
                String clientMessage = bufferedReader.readLine();
                log.info("Received: {}", clientMessage);

                // - Process request (e.g., convert to uppercase)
                String response = clientMessage.toUpperCase();

                // - Send response back to client
                printWriter.println(response);
                // - Close connection
                socket.close();
                log.info("the socket is closed");
                break;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

