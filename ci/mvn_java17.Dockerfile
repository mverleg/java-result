
FROM maven:3.8.3-openjdk-17-slim

WORKDIR /app

COPY pom.xml /app/
RUN mvn dependency:go-offline

COPY src /app
RUN mvn clean package

ENTRYPOINT echo "not a runnable image"

