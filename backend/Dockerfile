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

CMD ["sh", "-c", "exec java \
    -DidTag_1=${ID_TAG_1} \
    -DcentralSystemUrl_1=${CENTRAL_SYSTEM_URL_1} \
    -DmeterValueSampleInterval_1=${METER_VALUE_SAMPLE_INTERVAL_1} \
    -DmeterValuesSampledData_1=${METER_VALUES_SAMPLED_DATA_1} \
\
    -DidTag_2=${ID_TAG_2} \
    -DcentralSystemUrl_2=${CENTRAL_SYSTEM_URL_2} \
    -DmeterValueSampleInterval_2=${METER_VALUE_SAMPLE_INTERVAL_2} \
    -DmeterValuesSampledData_2=${METER_VALUES_SAMPLED_DATA_2} \
\
    -DidTag_3=${ID_TAG_3} \
    -DcentralSystemUrl_3=${CENTRAL_SYSTEM_URL_3} \
    -DmeterValueSampleInterval_3=${METER_VALUE_SAMPLE_INTERVAL_3} \
    -DmeterValuesSampledData_3=${METER_VALUES_SAMPLED_DATA_3} \
    -jar /app/app.jar"]

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
