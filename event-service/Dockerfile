FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew ./
COPY gradle/ gradle/
COPY settings.gradle.kts build.gradle.kts ./

COPY eureka-server/build.gradle.kts eureka-server/
COPY config-server/build.gradle.kts config-server/
COPY api-gateway/build.gradle.kts api-gateway/
COPY user-service/build.gradle.kts user-service/
COPY event-service/build.gradle.kts event-service/
COPY order-service/build.gradle.kts order-service/
COPY shared-contracts/build.gradle.kts shared-contracts/

RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon || true

COPY eureka-server/src eureka-server/src
COPY config-server/src config-server/src
COPY api-gateway/src api-gateway/src
COPY user-service/src user-service/src
COPY event-service/src event-service/src
COPY order-service/src order-service/src
COPY shared-contracts/src shared-contracts/src

ARG MODULE_NAME=event-service
RUN ./gradlew :${MODULE_NAME}:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
RUN apk add --no-cache curl

ARG MODULE_NAME=event-service
COPY --from=builder /app/${MODULE_NAME}/build/libs/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
