run:
	docker compose --profile core up

.PHONY: build docker docker_debug integration_test dummy_server

docker:
	docker-compose --profile core up --build

docker_debug:
	DEBUG_STAGE=suspend-debug docker-compose --profile core up --build

integration_test:
	docker-compose --env-file .env.internal --profile integration-test up --build

unit_test:
	docker-compose --profile unit_test up --build

dummy_server:
	NORMAL_MODE=enabled docker compose --profile core --profile dummy-server up --build

dummy_server_debug:
	NORMAL_MODE=enabled DEBUG_STAGE=suspend-debug docker compose --profile core --profile dummy-server up --build

build_backend:
	cd ./backend && mvn spotless:apply && mvn clean && mvn package

build_backend_fast:
	cd ./backend && mvn spotless:apply && mvn clean && mvn -DskipTests=true package

lint_backend:
	cd ./backend && mvn spotless:apply

build_frontend:
	cd ./frontend && npm run format && npm run local

lint_frontend:
	cd ./frontend && npm run lint
