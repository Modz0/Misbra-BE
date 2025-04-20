FROM eclipse-temurin:21-jdk-alpine as builder
WORKDIR /app

# Install Git & OpenSSH
RUN apk add --no-cache git openssh

# SSH setup for private repo access
RUN mkdir -p /root/.ssh
COPY id_rsa_github /root/.ssh/id_rsa
RUN chmod 600 /root/.ssh/id_rsa
RUN ssh-keyscan github.com >> /root/.ssh/known_hosts

# Clone and build
ARG GIT_REPO="git@github.com:Modz0/Misbra-BE.git"
RUN git clone -b main $GIT_REPO .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# -----------------------------
# Stage 2: Runtime Container
# -----------------------------
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy only the built JAR
COPY --from=builder /app/target/*.jar app.jar

# Create config directory
RUN mkdir -p /app/config

# Expose production HTTPS port
EXPOSE 443

# ✅ Let Spring load embedded configs & also anything from /app/config
ENV SPRING_CONFIG_ADDITIONAL_LOCATION=file:/app/config/

# Start the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
