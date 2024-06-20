#######################################################
# Stage 1: Build the spring boot maven project
#######################################################
FROM openjdk:21-slim as build
MAINTAINER Konstantinos Filippopolitis <kfilippopolitis@athenarc.gr>

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the project files
COPY pom.xml .
COPY src src/

# Build the project without running tests to speed up the build
RUN mvn clean package -DskipTests

#######################################################
# Stage 2: Setup the running container with the application
#######################################################
FROM openjdk:21-slim

# Timezone setup is optional
ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Application directory setup
WORKDIR /app

# Install Dockerize
ENV DOCKERIZE_VERSION v0.6.1
RUN apt-get update && apt-get install -y wget && \
    wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz && \
    tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz && \
    rm dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz && apt-get clean

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Copy the configuration template
COPY config/application.tmpl /app/application.tmpl

# Expose the application port
EXPOSE 8090

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8090/actuator/health || exit 1

# Use Dockerize to process the configuration template file before starting the application
ENTRYPOINT ["dockerize", "-template", "/app/application.tmpl:/app/application.yml", "java", "-jar", "app.jar"]
