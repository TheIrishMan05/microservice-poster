# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY shared-contracts ./shared-contracts
COPY event-service ./event-service

RUN chmod +x ./gradlew && ./gradlew :event-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
RUN apk add --no-cache curl

COPY --from=builder /app/event-service/build/libs/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
