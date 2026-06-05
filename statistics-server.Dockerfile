FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY statistics/statistics-server/target/statistics-server-*.jar app.jar

# Install wait-for-it script (using bash /dev/tcp)
RUN apk add --no-cache bash
COPY docker/wait-for-statistics.sh /wait-for-statistics.sh
RUN chmod +x /wait-for-statistics.sh

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 9090

ENTRYPOINT ["/wait-for-statistics.sh", "statistics-db", "5432", "--", "java", "-jar", "app.jar"]
