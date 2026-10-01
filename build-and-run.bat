@echo off
chcp 866 >nul
setlocal

echo ============================================
echo   RECORDING STUDIO - build and run
echo ============================================
echo.

REM --- Kill any running java process holding the jar ---
taskkill /F /IM java.exe >nul 2>nul

REM --- Check Maven ---
where mvn >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Maven not found in PATH.
    echo Install Maven and add its bin folder to PATH.
    pause
    exit /b 1
)

REM --- Build ---
echo [1/2] Building project via Maven...
echo.
call mvn clean package
if errorlevel 1 (
    echo.
    echo [ERROR] Build failed. Run cancelled.
    pause
    exit /b 1
)

REM --- Run ---
echo.
echo [2/2] Running application...
echo.
java -jar target\recording-studio-1.0.0.jar

echo.
echo Application finished.
pause