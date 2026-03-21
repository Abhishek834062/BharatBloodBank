# Stage 1: Build
FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN adduser -D -u 1000 appuser
COPY --from=build /app/target/bharat-blood-bank-1.0.0.jar BharatBloodBank-v1.0.jar
RUN chown appuser:appuser BharatBloodBank-v1.0.jar
USER appuser
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "BharatBloodBank-v1.0.jar"]