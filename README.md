# Datacatalogue [![Maintainability Badge](https://api.codeclimate.com/v1/badges/48216c43e4acff2fd7eb/maintainability)](https://codeclimate.com/github/madgik/exareme2/maintainability) [![Test Coverage](https://api.codeclimate.com/v1/badges/48216c43e4acff2fd7eb/test_coverage)](https://codeclimate.com/github/madgik/exareme2/test_coverage)
Based on the new example you've provided, let's create a README template for a Spring Boot project. This template will incorporate elements like badges for visual representation of project status, prerequisites for running the application, deployment instructions using Docker, and configuration details necessary for connecting to external services and databases.

---

[![CHUV](https://img.shields.io/badge/CHUV-LREN-AF4C64.svg)](https://www.unil.ch/lren/en/home.html) [![License](https://img.shields.io/badge/license-AGPL--3.0-blue.svg)](https://www.gnu.org/licenses/agpl-3.0.html)
[![DockerHub](https://img.shields.io/badge/docker-yourproject%2Fyourbackend-008bb8.svg)](https://hub.docker.com/r/yourproject/yourbackend/)

# Backend for Your Project

This project serves as the backend for Your Project, facilitating data management, authentication, and interaction with external services.

## Prerequisites

- **Java 21 SDK**: Ensure Java 21 SDK is installed for compiling and running the application. [Download Java 21](https://jdk.java.net/21/).
- **Docker**: Required for building and deploying the application as a container. [Get Docker](https://www.docker.com/get-started).
- **PostgreSQL**: A running instance is needed for database operations. [Install PostgreSQL](https://www.postgresql.org/download/).
- **IntelliJ IDEA** (Optional): Recommended for development. [Download IntelliJ IDEA](https://www.jetbrains.com/idea/download/).

## Development Deployment

To run the backend for development purposes:

1. **Ensure PostgreSQL is Running**: A PostgreSQL instance must be available.
2. **Configure Environment Variables**: Set necessary environment variables for database connectivity and external services.
3. **Run the Application**: Use IntelliJ or your preferred IDE to launch the application.

## Deployment Using Docker

Build and run the Docker image with the following commands:

1. **Build the Image**

    ```bash
    docker build -t yourproject/yourbackend:latest .
    ```

2. **Run the Container**

   Replace the environment variable placeholders with your actual configuration.

    ```bash
    docker run -d \
      -e PORTAL_DB_URL=jdbc:postgresql://<HOST>:<PORT>/<DB_NAME> \
      -e PORTAL_DB_USER=<USER> \
      -e PORTAL_DB_PASSWORD=<PASSWORD> \
      -e EXAREME_URL=http://<EXAREME_HOST>:<EXAREME_PORT> \
      -e KEYCLOAK_AUTH_URL=http://<KEYCLOAK_HOST>/auth \
      -e KEYCLOAK_REALM=<REALM> \
      -e KEYCLOAK_CLIENT_ID=<CLIENT_ID> \
      -e KEYCLOAK_CLIENT_SECRET=<CLIENT_SECRET> \
      --name yourbackend yourproject/yourbackend:latest
    ```

## Configuration

Detail of environment variables for configuring the application:

- **LOG_LEVEL**: Developer logs level. Default: "ERROR".
- **DATABASE CONFIGURATION**: Including `PORTAL_DB_URL`, `PORTAL_DB_USER`, and `PORTAL_DB_PASSWORD`.
- **EXTERNAL SERVICES**: URLs for Exareme and other services.
- **AUTHENTICATION**: Details for Keycloak configuration.

Refer to the Deployment section for more detailed instructions on using these variables.

# Acknowledgements

This project/research received funding from the European Union’s Horizon 2020 Framework Programme for Research and Innovation under the Framework Partnership Agreement No. 650003 (HBP FPA).

