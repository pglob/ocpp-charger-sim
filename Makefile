.PHONY: build

docker:
	docker-compose up --build

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