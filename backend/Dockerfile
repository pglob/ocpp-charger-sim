# Backend Dockerfile

# Stage 1: Build the jar
FROM maven:3.9.4-eclipse-temurin-21 AS build

# Copy the pom.xml and source code
COPY pom.xml /app/pom.xml
COPY src /app/src

# Set the working directory
WORKDIR /app

# Package the application
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM build AS run

# Copy the packaged jar file from the build stage
COPY --from=build /app/target/ocpp-charger-sim-backend-1.0-SNAPSHOT.jar /app/app.jar

# Expose the backend port
EXPOSE ${BACKEND_PORT}

# Run the application
CMD ["java", "-jar", "/app/app.jar"]

# Run tests
FROM build AS test

# Set the working directory
WORKDIR /app

CMD ["mvn", "clean", "test"]