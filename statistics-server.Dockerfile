FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY statistics/statistics-server/target/statistics-server-*.jar app.jar

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]