FROM openjdk:23-jdk-slim

WORKDIR /app

COPY target/*.jar webflux-app-1.0.0.jar

ENTRYPOINT ["java", "-jar", "/app/webflux-app-1.0.0.jar"]
