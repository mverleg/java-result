
FROM gradle:7.2.0-jdk17

WORKDIR /app

COPY build.gradle settings.gradle /app/
RUN gradle build

COPY src /app
RUN gradle build

ENTRYPOINT echo "not a runnable image"

