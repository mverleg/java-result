#!/usr/bin/env bash

set -eEu -o pipefail
pids=()

function logex() {
    echo "$@"
    "$@"
}

logex docker build --quiet -f ./ci/mvn_java15.Dockerfile . &
pids+=($!)
logex docker build --quiet -f ./ci/gradle_java17.Dockerfile . &
pids+=($!)
logex docker build --quiet -f ./ci/mvn_java17.Dockerfile . &
pids+=($!)
logex docker build --quiet -f ./ci/gradle_java15.Dockerfile . &
pids+=($!)

set +x
printf 'waiting for all steps to complete running in parallel\n'
for pid in "${pids[@]}"; do
    wait "$pid"
done
printf 'successful!\n'

