# ==============================================================================
# NEXUS Backend (Spring Boot / Java 21) - Multi-Stage Production Dockerfile
# ==============================================================================

# 1. Build Stage
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build executable fat jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# 2. Production JRE Runtime Stage
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Install curl for Actuator healthcheck
RUN apk add --no-cache curl

# Non-root unprivileged security context
RUN addgroup -S -g 1001 nexus && \
    adduser -S -u 1001 -G nexus nexus

COPY --from=builder --chown=nexus:nexus /app/target/nexus-backend-*.jar /app/nexus-backend.jar

USER nexus

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=25s --retries=3 \
  CMD curl -f http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/nexus-backend.jar"]
