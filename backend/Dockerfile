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

# Set command line arguments
ENV ID_TAG=temptag
ENV CENTRAL_SYSTEM_URL=ws://host.docker.internal:9000
ENV METER_VALUE_SAMPLE_INTERVAL=30
ENV METER_VALUES_SAMPLED_DATA=ENERGY_ACTIVE_IMPORT_REGISTER

CMD ["sh", "-c", "java -DidTag=${ID_TAG} -DcentralSystemUrl=${CENTRAL_SYSTEM_URL} -DmeterValueSampleInterval=${METER_VALUE_SAMPLE_INTERVAL} -DmeterValuesSampledData=${METER_VALUES_SAMPLED_DATA} -jar /app/app.jar"]

# Stage 3: Run with debugging
FROM build AS run-debug

# Copy the packaged jar file from the build stage
COPY --from=build /app/target/ocpp-charger-sim-backend-1.0-SNAPSHOT.jar /app/app.jar

# Expose the backend port and debug port
EXPOSE ${BACKEND_PORT}
EXPOSE ${DEBUG_PORT:-5005}

CMD ["sh", "-c", "java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${DEBUG_PORT:-5005} -jar /app/app.jar"]

# Stage 4: Run with debugging, start suspended
FROM build AS suspend-debug

# Copy the packaged jar file from the build stage
COPY --from=build /app/target/ocpp-charger-sim-backend-1.0-SNAPSHOT.jar /app/app.jar

# Expose the backend port and debug port
EXPOSE ${BACKEND_PORT}
EXPOSE ${DEBUG_PORT:-5005}

CMD ["sh", "-c", "java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:${DEBUG_PORT:-5005} -jar /app/app.jar"]

# Stage 5: Run Tests
FROM build AS test

# Set the working directory
WORKDIR /app

CMD ["mvn", "clean", "test"]
