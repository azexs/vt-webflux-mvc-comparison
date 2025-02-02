FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/*.jar web-mvc-vt-app-1.0.0.jar

ENTRYPOINT ["sh", "-c", "java -Djdk.tracePinnedThreads=full -Djdk.virtualThreadScheduler.maxPoolSize=256 -Djdk.virtualThreadScheduler.parallelism=12 -jar web-mvc-vt-app-1.0.0.jar"]

