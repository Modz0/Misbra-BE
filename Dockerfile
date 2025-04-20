FROM eclipse-temurin:21-jdk-alpine as builder
WORKDIR /app

# Install Git & OpenSSH (for repo access)
RUN apk add --no-cache git openssh

# Set up SSH key for GitHub authentication
RUN mkdir -p /root/.ssh
COPY id_rsa_github /root/.ssh/id_rsa
RUN chmod 600 /root/.ssh/id_rsa
RUN ssh-keyscan github.com >> /root/.ssh/known_hosts

# Clone the repo & build the JAR
ARG GIT_REPO="git@github.com:Modz0/Misbra-BE.git"
RUN git clone -b main $GIT_REPO .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Stage 2: Final Image
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy built JAR
COPY --from=builder /app/target/*.jar app.jar

# Copy application properties
COPY ./src/main/resources/application.properties /app/config/application.properties


COPY /etc/letsencrypt/live/misbra-api.org/keystore.p12 /app/config/keystore.p12

# Expose HTTPS Port
EXPOSE 443

# Set Environment Variables
ENV SPRING_CONFIG_LOCATION=file:/app/config/

# Run Spring Boot App
ENTRYPOINT ["java", "-jar", "app.jar"]
