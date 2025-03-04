FROM openjdk:21-slim
WORKDIR /app
COPY target/User-service-0.0.1-SNAPSHOT.jar user-service.jar
EXPOSE 8085
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "user-service.jar"]