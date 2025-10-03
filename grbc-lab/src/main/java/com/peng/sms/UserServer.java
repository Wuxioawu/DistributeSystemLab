package com.peng.sms;

import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class UserServer {

    private static io.grpc.Server server;

    public static void start() {
        try {
            server = ServerBuilder.forPort(50051).addService(new UserServiceImpl()).build().start();
            System.out.println("{peng wu}: Server started on port 50051");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {
        public void getUser(UserServiceOuterClass.UserRequest request, StreamObserver<UserServiceOuterClass.UserResponse> responseObserver) {

            UserServiceOuterClass.UserResponse.Builder builder = UserServiceOuterClass.UserResponse.newBuilder();
            builder.setEmail("pengwu@gmail.com");
            builder.setUserId(request.getUserId());
            builder.setName("pengwu");
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        }
    }

    public static void stop() {
        if (server != null) {
            server.shutdown();
            System.out.println("{peng wu}: Server stopped");
        }
    }
}
