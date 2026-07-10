@echo off
setlocal enabledelayedexpansion

echo ============================================
echo   Clean and Rebuild Project
echo ============================================
echo.

REM 配置 Java 环境
set JAVA_HOME=C:\Program Files\Common Files\Oracle\Java\javapath_target_778262046\..
set PATH=%JAVA_HOME%\bin;%PATH%

REM 配置 Maven 环境
set MAVEN_HOME=C:\Users\Lenovo\apache-maven-3.9.6
set PATH=%MAVEN_HOME%\bin;%PATH%

echo Java Home: %JAVA_HOME%
echo Maven Home: %MAVEN_HOME%
echo.

REM 验证环境
java -version 2>&1 | findstr "version"
mvn -version 2>&1 | findstr "Apache Maven"
echo.

echo Cleaning target directories...
if exist "health-ui\target" rmdir /s /q "health-ui\target"
if exist "health-common\target" rmdir /s /q "health-common\target"
if exist "health-dao\target" rmdir /s /q "health-dao\target"
if exist "health-service\target" rmdir /s /q "health-service\target"
echo Clean completed.
echo.

echo Compiling project...
call mvn clean compile -DskipTests
if errorlevel 1 (
    echo.
    echo ============================================
    echo   COMPILE FAILED!
    echo ============================================
    pause
    exit /b 1
)

echo.
echo ============================================
echo   BUILD SUCCESSFUL!
echo ============================================
pause
