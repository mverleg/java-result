#!/usr/bin/env bash

set -e -E -o pipefail -u -x

cur="$(pwd)"
ver="$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout 2>/dev/null)"
fd="$(mktemp -d)"
printf 'version = %s, dir = %s\n' "$ver" "$fd"

unzip -q -o "target/result-fat-${ver}-sources.jar" -d "$fd"
unzip -q -o "target/result-fat-${ver}.jar" -d "$fd"

printf 'java file count: %s' "$(find "$fd" -name '*.java' | wc -l)"
find "$fd" -name '*.java' -print -exec touch -d '1 hour ago' {} +
find "$fd" -name '*.class' -exec touch -d 'now' {} +

rm -f "${cur}/result-fat-${ver}.jar"
cd "$fd"
zip -q -r "${cur}/result-fat-${ver}.jar" .

