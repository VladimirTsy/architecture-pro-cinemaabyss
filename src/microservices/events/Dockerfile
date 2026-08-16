FROM eclipse-temurin:17-jre-alpine AS builder
WORKDIR /builder
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /builder/target/events-service-1.0.0.jar app.jar

ENV PORT=8082
ENV KAFKA_BROKERS=kafka:9092

EXPOSE ${PORT}

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]
