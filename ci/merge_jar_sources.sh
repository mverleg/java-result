#!/usr/bin/env bash

set -e -E -o pipefail -u -x

if [ $# != 1 ]; then
    echo 'single argument must be jar base name, e.g. "result-fat"' 1>&2
    exit 1
fi

base="$1"
cur="$(pwd)"
ver="$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout 2>/dev/null)"
fd="$(mktemp -d)"
printf 'base = %s, version = %s, dir = %s\n' "$base" "$ver" "$fd"

unzip -q -o "target/${base}-${ver}-sources.jar" -d "$fd"
unzip -q -o "target/${base}-${ver}.jar" -d "$fd"

printf 'java file count: %s' "$(find "$fd" -name '*.java' | wc -l)"
find "$fd" -name '*.java' -print -exec touch -d '1 hour ago' {} +
find "$fd" -name '*.class' -exec touch -d 'now' {} +

rm -f "${cur}/${base}-${ver}.jar"
cd "$fd"
zip -q -r "${cur}/${base}-${ver}.jar" .

