# OCPP Charger Simulator

This is a Portland State Fall/Winter 2024 CS Capstone project.
This project is for the RangerEV sponsor.

## How to build and run

Make can be used from the root directory for ease-of-use commands.

1. Docker must be installed (see here https://docs.docker.com/engine/install/)
2. Ensure Docker is running on your system.
3. In `ocpp-charger-sim/`, run `make`
4. After it is done building, it will run automatically
   - The frontend will default to http://localhost:3030
   - The backend will default to http://localhost:8080
   - Port numbers and URLs are defined in `ocpp-charger-sim/.env`

To start an already built container: use `make` \
To stop a running container: use `make stop` \
To load in configuration variables: use `make run_load` \
If changes were made to the Simulator code: use `make docker`

`make run_load` and `make docker` will revert unspecified configurations to their default.

### Loading configuration variables

Certain configurations can be loaded from the command line for ease of use. Supported configurations are found in `ocpp-charger-sim/backend/Dockerfile` and `ocpp-charger-sim/docker-compose.yml`. Use the suffix "_n", where n is the number of the charger you want. If you do not provide any from the command line, they can be modified when the simulator is running.

Once configurations are loaded in, they will persist as long as the backend container is not recreated. That is, if you do not run `make run_load`, or `make docker`, the configuration will persist.

Example:
```
ID_TAG_1=charger1 CENTRAL_SYSTEM_URL_1=ws://example.com \
ID_TAG_2=charger2 CENTRAL_SYSTEM_URL_2=ws://example.com \
ID_TAG_3=charger3 CENTRAL_SYSTEM_URL_3=ws://example.com \
make run_load
```

## Frontend

### Linting

Use ESLint to check the code’s quality and style, and Prettier to adjust code formatting.

Before running the linter, install the required dependencies by running the following command in the `ocpp-charger-sim/frontend` directory:

```bash
npm install
```

To automatically fix formatting and style issues in all .js and .jsx files:

```bash
npm run lint
```

## Backend

Build backend

```bash
   make build_backend
```

Build backend and skip tests

```bash
   make build_backend_fast
```

Use the linter on the backend (using spotless). Ensure current directory is at backend.

```bash
   make lint_backend
```

View formatting and style issues in all .java files:

```bash
   mvn spotless:check
```

Automatically fix code formatting issues:

```bash
   mvn spotless:apply
```

## Testing

Unit tests can be ran with:

```
   make unit_test
```

Integration tests can be ran with:

```
   make integration_test
```

The simulator can be ran with a dummy central system with:

```
   make dummy_server
```

## Debugging
The following commands open up port 5005 for backend debugging:
* `docker_debug`
* `dummy_server_debug`

For Visual Studio Code users, a `launch.json` is provided for connecting to the debugger.
