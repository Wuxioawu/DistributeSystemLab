package com.peng.sms;

import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class UserServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        io.grpc.Server server = ServerBuilder.forPort(50051).addService(new UserServiceImpl()).build().start();
        System.out.println("{peng wu}: Server started on port 50051");
        server.awaitTermination();
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
}
