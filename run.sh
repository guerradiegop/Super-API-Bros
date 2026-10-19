#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
if [ ! -f dist/super-api-bros.jar ]; then sh build.sh; fi
java -jar dist/super-api-bros.jar
