FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY main-service/main-service-server/target/main-service-server-*.jar app.jar

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]