# Health Check System - Quick Start (PowerShell)
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Health Check System - Quick Start" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

# 1. MySQL
Write-Host "`n[1/3] Checking MySQL..." -ForegroundColor Yellow
$mysql = Get-Service MySQL80 -ErrorAction SilentlyContinue
if ($mysql -and $mysql.Status -eq "Running") {
    Write-Host "  [OK] MySQL already running" -ForegroundColor Green
} else {
    Write-Host "  MySQL not running, trying to start..." -ForegroundColor Gray
    try { Start-Service MySQL80 -ErrorAction Stop; Write-Host "  [OK] MySQL started" -ForegroundColor Green }
    catch { Write-Host "  [WARN] Cannot start MySQL (need Admin). Continuing anyway..." -ForegroundColor Yellow }
}

# 2. Compile
Write-Host "`n[2/3] Compiling..." -ForegroundColor Yellow
$mvn = "$env:USERPROFILE\apache-maven-3.9.16\bin\mvn.cmd"
if (-not (Test-Path $mvn)) {
    Write-Host "  [ERROR] Maven not found: $mvn" -ForegroundColor Red
    pause; exit 1
}
& $mvn compile -q 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "  [ERROR] Compile failed!" -ForegroundColor Red
    & $mvn compile
    pause; exit 1
}
Write-Host "  [OK] Compile success" -ForegroundColor Green

# 3. Launch
Write-Host "`n[3/3] Launching application..." -ForegroundColor Yellow
$cp = (& $mvn -pl health-ui dependency:build-classpath -DincludeScope=runtime -Dmdep.outputFile=/dev/stdout -q) -join ''
$classes = "health-ui\target\classes;health-common\target\classes;health-dao\target\classes;health-service\target\classes"
$fullCp = "$classes;$cp"

Start-Process javaw -ArgumentList "-cp `"$fullCp`"", "com.healthsys.ui.Launcher"

Write-Host "  [OK] Application launched!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Cyan
