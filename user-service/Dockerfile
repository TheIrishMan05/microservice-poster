# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY shared-contracts ./shared-contracts
COPY user-service ./user-service

RUN chmod +x ./gradlew && ./gradlew :user-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
RUN apk add --no-cache curl

COPY --from=builder /app/user-service/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
