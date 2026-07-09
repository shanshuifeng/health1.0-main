# Health Check System - 一键启动脚本
# 自动检测环境、启动MySQL、初始化数据库、构建并运行项目

$ErrorActionPreference = "Continue"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

# ============================================
# 配置区（按需修改）
# ============================================
$MYSQL_SERVICE = "MySQL80"
$MYSQL_PORT = 3307
$MYSQL_USER = "root"
$DB_NAME = "healthsys"
$SQL_INIT_FILE = "$root\sql\init_database.sql"
$MAIN_CLASS = "com.healthsys.ui.Launcher"

# ============================================
# 终端颜色辅助
# ============================================
function Write-Step { Write-Host "`n>>> $args" -ForegroundColor Cyan }
function Write-OK { Write-Host "    [OK] $args" -ForegroundColor Green }
function Write-Warn { Write-Host "    [WARN] $args" -ForegroundColor Yellow }
function Write-Err { Write-Host "    [ERROR] $args" -ForegroundColor Red }

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  健康体检管理系统 - 一键启动" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

# ============================================
# 1. 检测 Java
# ============================================
Write-Step "检测 Java 环境..."
$javaHome = $env:JAVA_HOME
if (-not $javaHome) {
    $java = (Get-Command java -ErrorAction SilentlyContinue).Source
    if (-not $java) { Write-Err "未找到 Java，请安装 JDK 21+"; pause; exit 1 }
} else {
    $java = "$javaHome\bin\java.exe"
}
$javaVer = cmd /c "`"$java`" -version 2>&1" 2>$null | Select-Object -First 1
Write-OK "Java: $javaVer"

# ============================================
# 2. 检测 Maven
# ============================================
Write-Step "检测 Maven..."
$mvn = $null
# 2.1 检查 MAVEN_HOME
if ($env:MAVEN_HOME) { $mvn = "$env:MAVEN_HOME\bin\mvn.cmd" }
# 2.2 检查 PATH
if (-not $mvn -or -not (Test-Path $mvn)) {
    $mvnInPath = (Get-Command mvn.cmd -ErrorAction SilentlyContinue).Source
    if ($mvnInPath) { $mvn = $mvnInPath }
}
# 2.3 搜索常见路径
if (-not $mvn -or -not (Test-Path $mvn)) {
    $commonPaths = @(
        "$env:USERPROFILE\apache-maven-3.9.16\bin\mvn.cmd",
        "$env:USERPROFILE\apache-maven-3.9.9\bin\mvn.cmd",
        "C:\Program Files\apache-maven\bin\mvn.cmd",
        "C:\apache-maven\bin\mvn.cmd"
    )
    foreach ($p in $commonPaths) {
        if (Test-Path $p) { $mvn = $p; break }
    }
}
if (-not $mvn -or -not (Test-Path $mvn)) {
    Write-Err "未找到 Maven，请设置 MAVEN_HOME 环境变量"
    pause; exit 1
}
$mvnDir = Split-Path -Parent (Split-Path -Parent $mvn)
Write-OK "Maven: $mvnDir"

# ============================================
# 3. 检测 / 启动 MySQL
# ============================================
Write-Step "检测 MySQL 服务..."
$mysqlSvc = Get-Service $MYSQL_SERVICE -ErrorAction SilentlyContinue

if (-not $mysqlSvc) {
    Write-Err "未找到 MySQL 服务 '$MYSQL_SERVICE'"
    Write-Host "    请确认 MySQL 已安装且服务名正确。可在脚本顶部修改 `$MYSQL_SERVICE"
    pause; exit 1
}

if ($mysqlSvc.Status -ne "Running") {
    Write-Host "    正在启动 $MYSQL_SERVICE ..."
    try {
        Start-Service $MYSQL_SERVICE -ErrorAction Stop
        Start-Sleep -Seconds 2
    } catch {
        Write-Warn "无法启动 MySQL 服务（可能需要管理员权限）: $_"
        $ans = Read-Host "继续尝试连接？(Y/N)"
        if ($ans -ne 'Y' -and $ans -ne 'y') { exit 1 }
    }
}

# 等待端口就绪
$portReady = $false
for ($i = 0; $i -lt 15; $i++) {
    try {
        $tcp = Test-NetConnection -ComputerName localhost -Port $MYSQL_PORT -InformationLevel Quiet -WarningAction SilentlyContinue
        if ($tcp) { $portReady = $true; break }
    } catch { }
    Start-Sleep -Seconds 1
}
if (-not $portReady) {
    Write-Err "MySQL 端口 $MYSQL_PORT 在 15 秒内未就绪"
    pause; exit 1
}
Write-OK "MySQL 端口 $MYSQL_PORT 已就绪"

# ============================================
# 4. 检测数据库密码
# ============================================
Write-Step "检测数据库连接..."
$propsFile = "$root\health-common\src\main\resources\db.properties"
if (Test-Path $propsFile) {
    $props = @{}
    Get-Content $propsFile | ForEach-Object {
        if ($_ -match '^db\.(\w+)\s*=\s*(.+)$') { $props[$Matches[1]] = $Matches[2] }
    }
    if ($props['password']) { $MYSQL_PASS = $props['password'] }
    if ($props['user'])     { $MYSQL_USER = $props['user'] }
}

# 尝试连接
$mysqlBin = "$env:ProgramFiles\MySQL\MySQL Server 8.0\bin\mysql.exe"
if (-not (Test-Path $mysqlBin)) { $mysqlBin = "mysql" }

$connTest = & $mysqlBin -u $MYSQL_USER -p"$MYSQL_PASS" -P $MYSQL_PORT -e "SELECT 1" 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Warn "数据库连接失败，尝试交互式输入密码"
    $MYSQL_PASS = Read-Host "请输入 MySQL root 密码" -AsSecureString
    $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($MYSQL_PASS)
    $MYSQL_PASS = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
    $connTest = & $mysqlBin -u $MYSQL_USER -p"$MYSQL_PASS" -P $MYSQL_PORT -e "SELECT 1" 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Err "数据库连接失败，请检查用户名/密码"
        pause; exit 1
    }
}
Write-OK "数据库连接成功"

# ============================================
# 5. 初始化数据库
# ============================================
Write-Step "检查数据库..."
$dbExists = & $mysqlBin -u $MYSQL_USER -p"$MYSQL_PASS" -P $MYSQL_PORT -e "SHOW DATABASES LIKE '$DB_NAME'" 2>&1
if ($dbExists -match $DB_NAME) {
    Write-OK "数据库 '$DB_NAME' 已存在，跳过初始化"
} else {
    Write-Host "    正在初始化数据库..."
    if (Test-Path $SQL_INIT_FILE) {
        Get-Content $SQL_INIT_FILE | & $mysqlBin -u $MYSQL_USER -p"$MYSQL_PASS" -P $MYSQL_PORT 2>&1 | Out-Null
        if ($LASTEXITCODE -eq 0) {
            Write-OK "数据库初始化成功"
        } else {
            Write-Err "数据库初始化失败，请检查 SQL 文件"
            pause; exit 1
        }
    } else {
        Write-Err "未找到初始化脚本: $SQL_INIT_FILE"
        pause; exit 1
    }
}

# ============================================
# 6. 构建项目
# ============================================
Write-Step "构建项目..."
$buildResult = & $mvn clean compile -DskipTests -q 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Err "构建失败！以下为详细错误："
    & $mvn clean compile -DskipTests
    pause; exit 1
}
Write-OK "构建成功"

# ============================================
# 7. 启动应用
# ============================================
Write-Step "启动应用..."
Write-Host "    主类: $MAIN_CLASS"
Write-Host "============================================" -ForegroundColor Cyan

try {
    $execArgs = @(
        '-f', 'health-ui\pom.xml',
        'org.codehaus.mojo:exec-maven-plugin:3.1.0:java',
        "-Dexec.mainClass=$MAIN_CLASS",
        '-Dexec.classpathScope=runtime'
    )
    & $mvn @execArgs
    $exitCode = $LASTEXITCODE
    Write-Host "============================================" -ForegroundColor Cyan
    if ($exitCode -eq 0) {
        Write-OK "应用正常退出"
    } else {
        Write-Warn "应用退出，退出码: $exitCode"
    }
} catch {
    Write-Err "启动失败: $_"
    pause; exit 1
}
