@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"
if not exist build\classes mkdir build\classes
if not exist build\test-classes mkdir build\test-classes
if not exist dist mkdir dist
(for /r src\main\java %%f in (*.java) do (
  set "source=%%f"
  echo "!source:\=/!"
)) > build\sources.txt
javac --release 17 -encoding UTF-8 -d build\classes @build\sources.txt
if errorlevel 1 exit /b 1
jar --create --file dist\super-api-bros.jar --main-class br.edu.superapi.Main -C build\classes .
if errorlevel 1 exit /b 1
if /i not "%~1"=="test" goto done
(for /r src\test\java %%f in (*.java) do (
  set "source=%%f"
  echo "!source:\=/!"
)) > build\tests.txt
javac --release 17 -encoding UTF-8 -cp build\classes -d build\test-classes @build\tests.txt
if errorlevel 1 exit /b 1
java -Djava.awt.headless=true -cp build\classes;build\test-classes br.edu.superapi.AllTests
if errorlevel 1 exit /b 1
:done
echo Pronto: java -jar dist\super-api-bros.jar
