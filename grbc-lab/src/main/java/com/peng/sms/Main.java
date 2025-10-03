package com.peng.sms;

public class Main {
    public static void main(String[] args) {
        System.out.println("--------------------------------------- the grpc-lab start ---------------------------------------");
        Thread serverThread = new Thread(() -> {
            UserServer.start();
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                System.out.println("Server thread interrupted");
            }
        }, "UserServer");

        serverThread.setDaemon(true);
        serverThread.start();

        try {
            System.out.println("Waiting for server to start...");
            Thread.sleep(2000);
            System.out.println("Starting client...");
            UserClient.startClient();
            System.out.println("Client completed!");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            UserServer.stop();
            System.out.println("Program exiting...");
        }
        System.out.println("--------------------------------------- the grpc-lab end ---------------------------------------");
    }
}