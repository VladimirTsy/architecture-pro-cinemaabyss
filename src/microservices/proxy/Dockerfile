FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /builder
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /builder/target/proxy-1.0.0.jar app.jar

ENV PORT=8000
ENV MONOLITH_URL=http://monolith:8080
ENV MOVIES_SERVICE_URL=http://movies-service:8081
ENV EVENTS_SERVICE_URL=http://events-service:8082
ENV GRADUAL_MIGRATION=true
ENV MOVIES_MIGRATION_PERCENT=50

EXPOSE ${PORT}

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]