# ---- Stage 1: Build the JAR ----
FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

# Build JAR (skip tests for faster build)
RUN mvn clean package -DskipTests


# ---- Stage 2: Run the JAR ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user for security (Alpine uses adduser, not useradd)
RUN adduser -D -u 1000 appuser

COPY --from=build /app/target/BharatBloodBank-0.0.1-SNAPSHOT.jar BharatBloodBank-v1.0.jar

# Change ownership
RUN chown appuser:appuser BharatBloodBank-v1.0.jar

# Switch to non-root user
USER appuser

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "BharatBloodBank-v1.0.jar"]