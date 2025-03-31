# DESCRIPTION
## Prerequisite
- maven
- java: 21
- docker

## Running Application Locally

## Swagger
http://localhost:8080/swagger-ui/index.html

## Run Test Cases Command
[Infrastructure is build in docker by TestContainers Automatically]
```sh
mvn clean test
```
## Run Application Locally in Testing Mode
[Infrastructure is build by TestContainers Automatically]
```sh
mvn clean spring-boot:test-run -Dspring-boot.run.profiles=test
```
## Run Application Locally
[Infrastructure is build by docker-composer: docker/SecureAuth/docker-compose.yml]
```sh
mvn clean spring-boot:run
```
