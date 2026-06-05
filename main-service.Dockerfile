FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY main-service/main-service-server/target/main-service-server-*.jar app.jar

# Install wait-for-it script (using bash /dev/tcp)
RUN apk add --no-cache bash
COPY docker/wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8080

ENTRYPOINT ["/wait-for-it.sh", "main-db", "5432", "--", "java", "-jar", "app.jar"]
