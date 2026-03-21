# Base image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Non-root user
RUN adduser -D -u 1000 appuser

# Copy local JAR
COPY target/BharatBloodBank-0.0.1-SNAPSHOT.jar BharatBloodBank-v1.0.jar

# Set ownership
RUN chown appuser:appuser BharatBloodBank-v1.0.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Environment
ENV SPRING_PROFILES_ACTIVE=prod

# Run app
ENTRYPOINT ["java", "-jar", "BharatBloodBank-v1.0.jar"]