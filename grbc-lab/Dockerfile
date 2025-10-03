FROM openjdk:25-jdk-slim
WORKDIR /app
COPY target/grbc-lab-all.jar app.jar
EXPOSE 50051
ENTRYPOINT ["java", "-Dio.netty.transport.noNative=true", "-Djava.net.preferIPv4Stack=true", "-jar", "app.jar"]