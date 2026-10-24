@echo off
REM Run the JavaFX app "Recording Studio" (Windows).
REM PostgreSQL with the KR1 database must be running first.
cd /d "%~dp0"
call mvn -q javafx:run
if errorlevel 1 pause
