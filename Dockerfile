FROM eclipse-temurin:21-jdk-alpine as builder
WORKDIR /app
RUN apk add --no-cache git openssh
RUN mkdir -p /root/.ssh
COPY id_rsa_github /root/.ssh/id_rsa
RUN chmod 600 /root/.ssh/id_rsa
RUN ssh-keyscan github.com >> /root/.ssh/known_hosts
ARG GIT_REPO="git@github.com:Modz0/Misbra-BE.git"
RUN git clone -b main $GIT_REPO .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
COPY ./src/main/resources/application.properties /app/config/application.properties
COPY ./src/main/resources/misbra.p12 /app/config/misbra.p12
EXPOSE 8443
ENV SPRING_CONFIG_LOCATION=/app/config/application.properties
ENTRYPOINT ["java", "-jar", "app.jar"]