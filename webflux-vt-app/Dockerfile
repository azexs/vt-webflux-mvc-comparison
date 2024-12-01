FROM openjdk:21-jdk-slim

WORKDIR /app

RUN mkdir -p /app/results && chmod 777 /app/results

COPY target/*.jar webflux-vt-app-1.0.0.jar

ENTRYPOINT ["java", "-Dreactor.schedulers.defaultBoundedElasticOnVirtualThreads=true", "-jar", "/app/webflux-vt-app-1.0.0.jar"]

