# ===================================================================
# Stage 1: Build Stage
# ===================================================================
FROM amazoncorretto:17-alpine-jdk AS builder

WORKDIR /workspace

COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY gradle ./gradle
COPY src ./src

RUN chmod +x ./gradlew && ./gradlew build -x test

RUN mv /workspace/build/libs/*[!plain].jar /workspace/build/libs/app.jar

# ===================================================================
# Stage 2: Final Runtime Stage
# ===================================================================
FROM amazoncorretto:17-alpine

# Set the working directory for the application
WORKDIR /app

# Create a dedicated, non-root user and group for enhanced security.
RUN addgroup -S joycrew && adduser -S joycrew -G joycrew
USER joycrew

# Copy the executable .jar file from the builder stage.
COPY --from=builder /workspace/build/libs/app.jar .

# Set the active Spring profile to 'prod'
ENV SPRING_PROFILES_ACTIVE=prod

# Document the port that the container exposes at runtime
EXPOSE 8082

# Health check to ensure the application is running and healthy.
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost:8082/actuator/health || exit 1

# The command to run when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]