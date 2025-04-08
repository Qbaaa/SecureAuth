FROM maven:3.9.6-eclipse-temurin-21 AS builder

ARG USER=appuser
ARG ID=1005

RUN groupadd --gid ${ID} ${USER} && userad --gid ${ID} --uid ${ID} -m ${USER}

USER ${USER}:${USER}

WORKDIR /home/appuser/build

COPY pom.xml .
COPY checkstyle ./checkstyle
COPY src ./src

RUN mvn clean package

FROM eclipse-temurin:21-jdk

ARG USER=appuser
ARG ID=1005

RUN groupadd --gid ${ID} ${USER} && userad --gid ${ID} --uid ${ID} -m ${USER}

USER ${USER}:${USER}

WORKDIR /home/appuser/app

COPY --from=builder /home/appuser/build/target/*.jar /home/appuser/app/authsecure.jar