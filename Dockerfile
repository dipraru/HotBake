# ════════════════════════════════════════════════
#  Stage 1: Build with Maven
# ════════════════════════════════════════════════
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and pom first (layer caching)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

# Download dependencies (cache layer)
RUN ./mvnw dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ════════════════════════════════════════════════
#  Stage 2: Runtime image
# ════════════════════════════════════════════════
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Create non-root user for security
RUN addgroup -S hotbake && adduser -S hotbake -G hotbake
USER hotbake

# Copy the built jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]
