# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY eureka-server ./eureka-server

RUN chmod +x ./gradlew && ./gradlew :eureka-server:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
RUN apk add --no-cache curl

COPY --from=builder /app/eureka-server/build/libs/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
