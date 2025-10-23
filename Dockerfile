# === Stage 1: Build the JAR inside Docker ===
FROM maven:3.9.3-eclipse-temurin-17 AS build

# Set the working directory
WORKDIR /app

# Copy Maven configuration and source code
COPY pom.xml .
COPY src ./src

# Build the project (skip tests for faster builds)
RUN mvn clean package -DskipTests

# === Stage 2: Run the application ===
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy only the built JAR from the previous stage
COPY --from=build /app/target/scraper-1.0.0.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
