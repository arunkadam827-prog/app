# ---- Build stage: compile the Spring Boot fat jar ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy wrapper + build files first so dependency download is cached
COPY gradlew ./
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

# Copy sources and build (skip tests for faster deploys)
COPY src src
RUN ./gradlew --no-daemon bootJar -x test

# ---- Runtime stage: slim JRE image ----
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Render injects PORT (default 10000); Spring reads server.port from it
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["/entrypoint.sh"]
