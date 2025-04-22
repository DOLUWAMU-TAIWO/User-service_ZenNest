# Use an official OpenJDK runtime as a base image
FROM openjdk:21-slim

# Create a non-root user for security
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

# Set working directory
WORKDIR /app

# Copy the JAR file to the container
COPY target/User-service-0.0.1-SNAPSHOT.jar user-service.jar

# Change ownership of the app directory
RUN chown -R appuser:appgroup /app

# Expose the application port
EXPOSE 8085

# Switch to non-root user
USER appuser

# Add a health check for Docker to monitor container status
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8085/actuator/health || exit 1

# Run the application with the active profile set to "prod"
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "user-service.jar"]