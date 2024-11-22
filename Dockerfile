FROM openjdk:17
WORKDIR /app
COPY target/User-service-0.0.1-SNAPSHOT.jar user-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "user-service.jar"]



