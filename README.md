# OCPP Charger Simulator
This is a Portland State Fall/Winter 2024 CS Capstone project.
This project is for the RangerEV sponsor.

## How to build and run
1. Docker must be installed (see here https://docs.docker.com/engine/install/)
2. In `ocpp-charger-sim/` run `docker-compose up --build`
3. After it is done building, it will run automatically 
   * The frontend will default to http://localhost:3030
   * The backend will default to  http://localhost:8080
   * Port numbers and URLs are defined in `ocpp-charger-sim/.env`
