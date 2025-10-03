package com.peng.sms;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import junit.framework.TestCase;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class UserServerTest extends TestCase {

    // Service method invocation tests
    @Test
    public void testGetUserWithRealServer() throws Exception {

        io.grpc.Server server = io.grpc.ServerBuilder.forPort(50051)
                .addService(new UserServer.UserServiceImpl())
                .build()
                .start();

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(channel);

        UserServiceOuterClass.UserRequest request =
                UserServiceOuterClass.UserRequest.newBuilder().setUserId(123).build();

        UserServiceOuterClass.UserResponse response = stub.getUser(request);

        assertEquals(123, response.getUserId());
        assertEquals("pengwu", response.getName());
        assertEquals("pengwu@gmail.com", response.getEmail());

        channel.shutdownNow();
        server.shutdownNow();
    }

    // Data serialization/deserialization tests
    @Test
    public void testSerializationAndDeserialization() throws Exception {
        UserServiceOuterClass.UserRequest request =
                UserServiceOuterClass.UserRequest.newBuilder()
                        .setUserId(345)
                        .build();

        byte[] serialized = request.toByteArray();
        UserServiceOuterClass.UserRequest deserialized =
                UserServiceOuterClass.UserRequest.parseFrom(serialized);

        assertEquals(345, deserialized.getUserId());
    }
}
