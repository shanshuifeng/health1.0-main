# Clean and Rebuild Project
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Clean and Rebuild Project" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# 配置 Java 环境
$javaBinPath = "C:\Program Files\Common Files\Oracle\Java\javapath_target_778262046"
# javac.exe 在 bin 目录下，所以 JAVA_HOME 应该是其父目录
$env:JAVA_HOME = Split-Path $javaBinPath
$env:PATH = "$javaBinPath;$env:PATH"

# 配置 Maven 环境
$mavenHome = "$env:USERPROFILE\apache-maven-3.9.6"
$env:MAVEN_HOME = $mavenHome
$env:PATH = "$mavenHome\bin;$env:PATH"

Write-Host "Java Home: $env:JAVA_HOME" -ForegroundColor Green
Write-Host "Maven Home: $mavenHome" -ForegroundColor Green
Write-Host ""

# 验证环境
Write-Host "Checking environment..." -ForegroundColor Yellow
java -version 2>&1 | Select-String "version"
mvn -version 2>&1 | Select-String "Apache Maven"
Write-Host ""

# 清理 target 目录
Write-Host "Cleaning target directories..." -ForegroundColor Yellow
if (Test-Path "health-ui\target") { Remove-Item -Recurse -Force "health-ui\target" }
if (Test-Path "health-common\target") { Remove-Item -Recurse -Force "health-common\target" }
if (Test-Path "health-dao\target") { Remove-Item -Recurse -Force "health-dao\target" }
if (Test-Path "health-service\target") { Remove-Item -Recurse -Force "health-service\target" }
Write-Host "Clean completed." -ForegroundColor Green
Write-Host ""

# 编译项目
Write-Host "Compiling project..." -ForegroundColor Yellow
mvn clean compile -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Red
    Write-Host "  COMPILE FAILED!" -ForegroundColor Red
    Write-Host "============================================" -ForegroundColor Red
    pause
    exit 1
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "  BUILD SUCCESSFUL!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
pause
