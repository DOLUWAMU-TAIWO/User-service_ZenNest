FROM eclipse-temurin:21-jdk-alpine

# Use a slim, secure base image
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the fat JAR into the container
COPY target/User-service-0.0.1-SNAPSHOT.jar app.jar

# Set ownership and permissions
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8085


HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --spider -q http://localhost:8085/actuator/health || exit 1

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]