# OCPP Charger Simulator

This is a Portland State Fall/Winter 2024 CS Capstone project.
This project is for the RangerEV sponsor.

## How to build and run

1. Docker must be installed (see here https://docs.docker.com/engine/install/)
2. Ensure Docker is running on your system.
3. In `ocpp-charger-sim/` run `docker-compose up --build`
4. After it is done building, it will run automatically
   - The frontend will default to http://localhost:3030
   - The backend will default to http://localhost:8080
   - Port numbers and URLs are defined in `ocpp-charger-sim/.env`

## Frontend

### Testing

### Linting

Use ESLint to check the code’s quality and style, and Prettier to adjust code formatting.

Before running the linter, install the required dependencies by running the following command in the `frontend` directory:

```bash
npm install
```

View formatting and style issues in all .js and .jsx files:

```bash
npm run lint
```

to automatically fix code formatting issues:

```bash
npm run format
```

## Backend

### Testing

### Linting

Makefile can be used from the root directory for ease-of-use commands.

Run the simulator

```bash
   make
```

Create the docker file

```bash
   make docker
```

Build backend

```bash
   make build_backend
```

Build backend and skip tests

```bash
   make build_backend_fast
```

Use the linter on the backend

```bash
   lint_backend
```

Compose and run docker file

```bash
   make docker
```

Use spotless for backend code formatting. Ensure current directory is at backend.

View formatting and style issues in all .java files:

```bash
   mvn spotless:check
```

Automatically fix code formatting issues:

```bash
   mvn spotless:apply
```
