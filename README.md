#Lab2

[![Build](https://github.com/JuanjEC/JuanJeronimoEcheverry-ArqdeSoft-Laboratorios/actions/workflows/build.yml/badge.svg)](https://github.com/JuanjEC/JuanJeronimoEcheverry-ArqdeSoft-Laboratorios/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=JuanjEC_JuanJeronimoEcheverry-ArqdeSoft-Laboratorios&metric=alert_status)](https://sonarcloud.io/project/overview?id=JuanjEC_JuanJeronimoEcheverry-ArqdeSoft-Laboratorios)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=JuanjEC_JuanJeronimoEcheverry-ArqdeSoft-Laboratorios&metric=coverage)](https://sonarcloud.io/project/overview?id=JuanjEC_JuanJeronimoEcheverry-ArqdeSoft-Laboratorios)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=JuanjEC_JuanJeronimoEcheverry-ArqdeSoft-Laboratorios&metric=ncloc)](https://sonarcloud.io/project/overview?id=JuanjEC_JuanJeronimoEcheverry-ArqdeSoft-Laboratorios)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=JuanjEC_JuanJeronimoEcheverry-ArqdeSoft-Laboratorios&metric=sqale_index)](https://sonarcloud.io/project/overview?id=JuanjEC_JuanJeronimoEcheverry-ArqdeSoft-Laboratorios)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=JuanjEC_JuanJeronimoEcheverry-ArqdeSoft-Laboratorios&metric=reliability_rating)](https://sonarcloud.io/project/overview?id=JuanjEC_JuanJeronimoEcheverry-ArqdeSoft-Laboratorios)
[![Snyk](https://snyk.io/test/github/JuanjEC/JuanJeronimoEcheverry-ArqdeSoft-Laboratorios/badge.svg)](https://snyk.io/test/github/JuanjEC/JuanJeronimoEcheverry-ArqdeSoft-Laboratorios)
[![Docker Image](https://img.shields.io/badge/Docker-banco%3Alatest-blue?logo=docker)](https://hub.docker.com/r/JuanjEC/banco)
[![Deploy on Render](https://img.shields.io/badge/Render-Deployed-46E3B7?logo=render&logoColor=black)](https://render.com/)

# Banco UdeA

Spring Boot REST API for customers and money transfers, with a React frontend in `banco-frontend`.

## Documentacion

El paso a paso del laboratorio, con las evidencias del pipeline y del despliegue, está en [PASOS_SEGUIDOS.md](PASOS_SEGUIDOS.md).

## Despliegue

URL del servicio: https://banco-latest.onrender.com

## Endpoints

| Method | Path                              | Description                  |
|--------|-----------------------------------|------------------------------|
| GET    | `/api/customers`                  | List customers               |
| GET    | `/api/customers/{id}`             | Get customer by id           |
| POST   | `/api/customers`                  | Create customer              |
| POST   | `/api/transactions`               | Transfer money               |
| GET    | `/api/transactions/{account}`     | Transactions of an account   |

## Stack

Java 17, Spring Boot 4.1.1, Spring Data JPA, H2 (in-memory), MapStruct, Lombok, JUnit 5, JaCoCo, SonarCloud, GitHub Actions, Docker Hub, Render.

## Configuration

The application uses an in-memory H2 database, so data is lost when the service restarts. A MySQL database can still be used by setting these environment variables.

| Variable      | Description                              |
|---------------|------------------------------------------|
| `DB_URL`      | JDBC URL (default: in-memory H2)         |
| `DB_USER`     | Database user (default: `sa`)            |
| `DB_PASSWORD` | Database password (default: empty)       |
| `PORT`        | HTTP port (default 8080)                 |

## Run locally

```shell
./mvnw spring-boot:run
```

## Test and coverage

Tests run against the in-memory H2 database.

```shell
./mvnw clean verify
```

The JaCoCo report is generated at `target/site/jacoco/index.html`.

## Docker

```shell
./mvnw package -DskipTests
docker build -t banco .
docker run -p 8080:8080 banco
```

## Pipeline

Jobs: tests, sonar, build, docker, deploy. The docker and deploy jobs only run on pushes to `main`.
