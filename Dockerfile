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

# Create log directory and set permissions
RUN mkdir -p /var/log/joycrew && chown -R joycrew:joycrew /var/log/joycrew

# Copy the executable .jar file from the builder stage.
COPY --from=builder /workspace/build/libs/app.jar .

# Change ownership to joycrew user
RUN chown joycrew:joycrew app.jar

USER joycrew

# Set the active Spring profile to 'prod'
ENV SPRING_PROFILES_ACTIVE=prod

# Document the port that the container exposes at runtime
EXPOSE 8082

# The command to run when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]