@echo off
cd /d "%~dp0"
if not exist dist\super-api-bros.jar call build.bat
if errorlevel 1 exit /b 1
java -jar dist\super-api-bros.jar
if errorlevel 1 pause
