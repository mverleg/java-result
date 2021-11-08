#!/usr/bin/env bash

set -eEu -o pipefail
pids=()

function logrun() {
    echo ">> $@"
    "$@"
}

logrun docker build --quiet --build-arg MVN_VERSION=3.8.1-openjdk-15-slim -f ./ci/mvn.Dockerfile . &
pids+=($!)
logrun docker build --quiet --build-arg MVN_VERSION=3.8.3-openjdk-17-slim -f ./ci/mvn.Dockerfile . &
pids+=($!)
logrun docker build --quiet --build-arg GRADLE_VERSION=6.8.3-jdk15 -f ./ci/gradle.Dockerfile . &
pids+=($!)
logrun docker build --quiet --build-arg GRADLE_VERSION=7.2.0-jdk17 -f ./ci/gradle.Dockerfile . &
pids+=($!)

set +x
printf 'waiting for all steps to complete running in parallel\n'
for pid in "${pids[@]}"; do
    wait "$pid"
done
printf 'successful!\n'

