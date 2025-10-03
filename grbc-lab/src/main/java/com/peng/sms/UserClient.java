package com.peng.sms;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class UserClient {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(channel);
        UserServiceOuterClass.UserRequest request = UserServiceOuterClass.UserRequest.newBuilder().setUserId(12).build();
        UserServiceOuterClass.UserResponse response = stub.getUser(request);
        System.out.println("User info: " + response.getName() + ", " + response.getEmail());
        channel.shutdown();
    }
}
