
ARG MVN_VERSION
FROM maven:${MVN_VERSION}

WORKDIR /app

COPY pom.xml /app/
RUN mvn dependency:go-offline

COPY src /app/src
RUN mvn package

ENTRYPOINT echo "not a runnable image"

