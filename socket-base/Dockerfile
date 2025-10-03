FROM openjdk:25-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 50051
CMD ["java", "-jar", "app.jar"]
