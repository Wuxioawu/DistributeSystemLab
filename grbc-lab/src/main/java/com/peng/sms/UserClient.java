package com.peng.sms;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.socket.nio.NioSocketChannel;

public class UserClient {

    public static void startClient() {

        Thread.currentThread().setName("UserClient");
        long start = System.currentTimeMillis();

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        try {
            UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(channel);
            UserServiceOuterClass.UserRequest request = UserServiceOuterClass.UserRequest.newBuilder()
                    .setUserId(12)
                    .build();
            UserServiceOuterClass.UserResponse response = stub.getUser(request);
            System.out.println("User info: " + response.getName() + ", " + response.getEmail());
            long end = System.currentTimeMillis();
            System.out.println("time: " + (end - start));
        } finally {
            channel.shutdown();
        }
    }

}
