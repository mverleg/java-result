
ARG GRADLE_VERSION
FROM gradle:${GRADLE_VERSION}

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts /app/
RUN gradle build

COPY src /app/src
RUN gradle build test

ENTRYPOINT echo "not a runnable image"

