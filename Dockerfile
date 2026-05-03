FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY settings.gradle .
COPY build.gradle .

COPY shipment-service shipment-service

RUN chmod +x ./gradlew
RUN ./gradlew :shipment-service:clean :shipment-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/shipment-service/build/libs/*.jar app.jar

EXPOSE 19087
ENTRYPOINT ["java", "-jar", "app.jar"]
