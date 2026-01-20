FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY target/Homebank-0.0.1-SNAPSHOT.jar app.jar 

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/app.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Xms256m -Xmx512m"
USER nobody
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
