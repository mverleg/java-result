
FROM gradle:6.8.3-jdk15

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts /app/
RUN gradle build

COPY src /app
RUN gradle build

ENTRYPOINT echo "not a runnable image"

