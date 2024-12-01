FROM openjdk:21-jdk-slim

WORKDIR /app

RUN mkdir -p /app/results && chmod 777 /app/results

COPY target/*.jar webflux-app-1.0.0.jar

ENTRYPOINT ["java", "-jar", "/app/webflux-app-1.0.0.jar"]
