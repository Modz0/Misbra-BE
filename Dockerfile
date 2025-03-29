# Stage 1: Build the JAR
FROM eclipse-temurin:21-jdk-alpine as builder

WORKDIR /app

# Install required packages (Git & OpenSSH for SSH authentication)
RUN apk add --no-cache git openssh

# Set up SSH key for GitHub authentication
RUN mkdir -p /root/.ssh && \
    chmod 700 /root/.ssh
COPY id_rsa_github /root/.ssh/id_rsa
RUN chmod 600 /root/.ssh/id_rsa && \
    ssh-keyscan github.com >> /root/.ssh/known_hosts && \
    chmod 644 /root/.ssh/known_hosts

ARG GIT_REPO="git@github.com:Modz0/Misbra-BE.git"
RUN git clone -b main "$GIT_REPO" .

# Grant execute permission to Maven wrapper and build
RUN chmod +x mvnw && \
    ./mvnw clean package -DskipTests

# Stage 2: Run the JAR
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create config directory and copy application.properties
RUN mkdir -p /app/config
COPY src/main/resources/application.properties /app/config/application.properties

# Expose port 8080 for Spring Boot
EXPOSE 8080

# Set environment variable for Spring Boot configuration
ENV SPRING_CONFIG_LOCATION=file:/app/config/application.properties

# Run the application with better JVM options
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]