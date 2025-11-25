FROM ubuntu:latest
LABEL authors="Admin"

ENTRYPOINT ["top", "-b"]


FROM maven:4.0.0-eclipse-temurin-17 AS build
COPY . .
RUN mvn clean package

FROM eclipse-temurin:17-jdk-alpine
COPY --from=build /target/carcharging-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]