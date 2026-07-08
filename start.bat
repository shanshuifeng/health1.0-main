@echo off
chcp 936 >nul
title Health Check System

echo ============================================
echo   Health Check System - Quick Start
echo ============================================
echo.

:: 1. MySQL
echo [1/3] Checking MySQL...
sc query MySQL80 | find "RUNNING" >nul
if %errorlevel% neq 0 (
    echo   MySQL not running, trying to start...
    net start MySQL80 >nul 2>&1
    if %errorlevel% neq 0 (
        echo   [WARN] Cannot start MySQL (need Admin rights)
        echo   If MySQL is already running, ignore this.
    ) else (
        echo   [OK] MySQL started
    )
) else (
    echo   [OK] MySQL already running
)
echo.

:: 2. Compile
echo [2/3] Compiling...
set MVN_HOME=%USERPROFILE%\apache-maven-3.9.16
if not exist "%MVN_HOME%\bin\mvn.cmd" (
    echo   [ERROR] Maven not found: %MVN_HOME%
    echo   Please install Maven 3.9.16 to: %MVN_HOME%
    pause
    exit /b 1
)
call "%MVN_HOME%\bin\mvn.cmd" compile -q
if %errorlevel% neq 0 (
    echo   [ERROR] Compile failed! Trying full compile...
    call "%MVN_HOME%\bin\mvn.cmd" compile
    pause
    exit /b 1
)
echo   [OK] Compile success
echo.

:: 3. Launch
echo [3/3] Launching application...
for /f "tokens=*" %%i in ('"%MVN_HOME%\bin\mvn.cmd" -pl health-ui dependency:build-classpath -DincludeScope=runtime -Dmdep.outputFile=/dev/stdout -q') do set CP=%%i
set CLASSES=health-ui\target\classes;health-common\target\classes;health-dao\target\classes;health-service\target\classes
set FULL_CP=%CLASSES%;%CP%

start "HealthCheck" javaw -cp "%FULL_CP%" com.healthsys.ui.Launcher

echo   [OK] Application launched!
echo   You can close this window now.
echo ============================================