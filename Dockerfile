# Use official Maven + OpenJDK image to build and run
FROM maven:3.9.3-eclipse-temurin-17 AS build

WORKDIR /app

# Copy all files
COPY pom.xml .
COPY src ./src

# Build the project (skip tests)
RUN mvn clean package -DskipTests

# Second stage: minimal runtime
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/scraper-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
