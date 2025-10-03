FROM openjdk:25-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 19001
CMD ["java", "-jar", "app.jar"]
