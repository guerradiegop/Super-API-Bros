#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p build/classes build/test-classes dist
find src/main/java -name '*.java' > build/sources.txt
javac --release 17 -encoding UTF-8 -d build/classes @build/sources.txt
jar --create --file dist/super-api-bros.jar --main-class br.edu.superapi.Main -C build/classes .
if [ "${1:-}" = "test" ]; then
  find src/test/java -name '*.java' > build/tests.txt
  javac --release 17 -encoding UTF-8 -cp build/classes -d build/test-classes @build/tests.txt
  java -Djava.awt.headless=true -cp build/classes:build/test-classes br.edu.superapi.AllTests
fi
echo 'Pronto: java -jar dist/super-api-bros.jar'
