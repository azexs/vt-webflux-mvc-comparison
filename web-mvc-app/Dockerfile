FROM openjdk:21-jdk-slim

WORKDIR /app

RUN mkdir -p /app/results && chmod 777 /app/results

COPY target/*.jar virtual-threads-1.0.0.jar

ENTRYPOINT ["sh", "-c", "mkdir -p /app/results && chmod 777 /app/results && java -jar virtual-threads-1.0.0.jar"]

