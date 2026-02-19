FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY src src

ARG MAVEN_PROFILE=prod
RUN mvn -B -DskipTests -P${MAVEN_PROFILE} clean package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS="-Xms256m -Xmx512m"
USER nobody
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
