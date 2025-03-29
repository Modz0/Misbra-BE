# Stage 1: Build the JAR
FROM eclipse-temurin:21-jdk-alpine as builder

WORKDIR /app

# Install required packages (Git & OpenSSH for SSH authentication)
RUN apk add --no-cache git openssh

# Set up SSH key for GitHub authentication
RUN mkdir -p /root/.ssh
COPY id_rsa_github /root/.ssh/id_rsa
RUN chmod 600 /root/.ssh/id_rsa
RUN ssh-keyscan github.com >> /root/.ssh/known_hosts

ARG GIT_REPO="git@github.com:Modz0/Misbra-BE.git"
RUN git clone -b main $GIT_REPO .

# Grant execute permission to Maven wrapper
RUN chmod +x mvnw

# Build the JAR (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the JAR
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Copy application.properties from the local machine (NOT from builder)
COPY ./src/main/resources/application.properties /app/config/application.properties

# Expose port 8080 for Spring Boot
EXPOSE 8080

# Set environment variable for Spring Boot to read from the correct location
ENV SPRING_CONFIG_LOCATION=/app/config/application.properties

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
