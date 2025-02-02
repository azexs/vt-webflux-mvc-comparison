FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/*.jar web-mvc-app-1.0.0.jar.jar

ENTRYPOINT ["sh", "-c", "java -jar web-mvc-app-1.0.0.jar"]
