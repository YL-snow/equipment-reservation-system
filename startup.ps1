<#
============================================================
    设备预约与耗材管理系统 — 一键启动脚本
    启动顺序: MySQL → 后端 → 前端
    使用前确保已安装: MySQL 8.x, Java 21, Maven, Node.js
============================================================
#>

$ErrorActionPreference = "Continue"
$rootDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $rootDir

function Stop-PortListener($Port, $ServiceName) {
    $connections = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    $processIds = @($connections | Select-Object -ExpandProperty OwningProcess -Unique)
    if ($processIds.Count -gt 0) {
        Write-Host "   -> 检测到旧 $ServiceName 正在运行 (PID $($processIds -join ', '))，先停止..." -ForegroundColor Yellow
        foreach ($processId in $processIds) {
            Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
        }
        Start-Sleep -Seconds 2
    }
}

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "    设备预约与耗材管理系统 — 启动中..." -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# MySQL 密码：优先用环境变量，否则提示输入
$mysqlPwd = $env:MYSQL_ROOT_PASSWORD
if (-not $mysqlPwd) {
    $mysqlPwd = Read-Host "请输入 MySQL root 密码" -AsSecureString
    $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($mysqlPwd)
    $mysqlPwd = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
}
$env:MYSQL_PWD = $mysqlPwd

# =====================================================
# 步骤 1: 检查 MySQL 并初始化数据库
# =====================================================
Write-Host "[1/3] 检查 MySQL 数据库..." -ForegroundColor Yellow
try {
    $mysqlAvailable = Get-Service MySQL* -ErrorAction SilentlyContinue
    if (-not $mysqlAvailable) {
        $mysqlAvailable = Get-Process mysqld -ErrorAction SilentlyContinue
    }

    if ($mysqlAvailable) {
        Write-Host "   -> MySQL 服务已运行" -ForegroundColor Green

        # 检查 ers 数据库是否存在
        $dbCheck = mysql -u root --default-character-set=utf8mb4 -e "SHOW DATABASES LIKE 'ers';" 2>$null
        if ($dbCheck -match "ers") {
            Write-Host "   -> 数据库 'ers' 已存在" -ForegroundColor Green
        } else {
            Write-Host "   -> 正在创建数据库 'ers' 并初始化..." -ForegroundColor Yellow
            $dbScript = Join-Path $rootDir "database\schema.sql"
            if (Test-Path $dbScript) {
                Get-Content $dbScript -Encoding UTF8 | mysql -u root --default-character-set=utf8mb4 2>$null
                Write-Host "   [OK] 数据库初始化完成" -ForegroundColor Green
            }
            # 导入种子数据（使用 -Encoding UTF8 防止中文乱码）
            $seedScript = Join-Path $rootDir "database\seed.sql"
            if (Test-Path $seedScript) {
                Get-Content $seedScript -Encoding UTF8 | mysql -u root --default-character-set=utf8mb4 ers 2>$null
                Write-Host "   [OK] 种子数据导入完成" -ForegroundColor Green
            }
        }
    } else {
        Write-Host "  [!] 未检测到 MySQL 运行，请确保 MySQL 已启动" -ForegroundColor Red
        Write-Host "   -> 手动启动: net start MySQL 或打开 MySQL Workbench" -ForegroundColor Gray
    }
} catch {
    Write-Host "  [!] MySQL 检查失败: $_" -ForegroundColor Red
    Write-Host "   -> 请确认 MySQL 已安装并运行" -ForegroundColor Gray
}
Write-Host ""

# =====================================================
# 步骤 2: 启动后端（Spring Boot，端口 8082）
# =====================================================
Write-Host "[2/3] 启动后端服务 (端口 8082)..." -ForegroundColor Yellow
$backendPath = Join-Path $rootDir "backend"
if (Test-Path $backendPath) {
    Stop-PortListener 8082 "后端服务"
    Start-Process powershell -WindowStyle Normal -ArgumentList @"
        `$env:MYSQL_ROOT_PASSWORD = '$mysqlPwd';
        Set-Location '$backendPath';
        Write-Host '=== 设备预约系统 后端服务 (端口 8082) ===' -ForegroundColor Cyan;
        Write-Host '编译并启动后端...' -ForegroundColor Yellow;
        mvn spring-boot:run;
        Read-Host "`n按 Enter 关闭";
"@
    Write-Host "   [OK] 后端服务新窗口已打开" -ForegroundColor Green
    Start-Sleep -Seconds 2
} else {
    Write-Host "   -> 后端目录不存在，跳过" -ForegroundColor DarkYellow
}
Write-Host ""

# =====================================================
# 步骤 3: 启动前端（React + Ant Design + Vite，端口 5174）
# =====================================================
Write-Host "[3/3] 启动前端服务 (端口 5174)..." -ForegroundColor Yellow
$frontendPath = Join-Path $rootDir "frontend"
if (Test-Path $frontendPath) {
    Stop-PortListener 5174 "前端服务"
    # 检查是 pnpm 还是 npm
    $pkgManager = "pnpm"
    if (-not (Get-Command pnpm -ErrorAction SilentlyContinue)) {
        $pkgManager = "npm"
    }
    Start-Process powershell -WindowStyle Normal -ArgumentList @"
        Set-Location '$frontendPath';
        Write-Host '=== 设备预约系统 前端服务 (端口 5174) ===' -ForegroundColor Cyan;
        if (-not (Test-Path 'node_modules')) {
            Write-Host '正在安装前端依赖...' -ForegroundColor Yellow;
            $env:CI = 'true'
            & $pkgManager install
            Remove-Item Env:CI -ErrorAction SilentlyContinue
        } elseif ($pkgManager -eq 'pnpm') {
            Write-Host '正在同步前端依赖...' -ForegroundColor Yellow;
            $env:CI = 'true'
            & $pkgManager install
            Remove-Item Env:CI -ErrorAction SilentlyContinue
        }
        Write-Host '启动开发服务器...' -ForegroundColor Green;
        & $pkgManager run dev;
        Read-Host "`n按 Enter 关闭";
"@
    Write-Host "   [OK] 前端服务新窗口已打开" -ForegroundColor Green
} else {
    Write-Host "   -> 前端目录不存在，跳过" -ForegroundColor DarkYellow
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  系统启动中，请稍候..." -ForegroundColor Cyan
Write-Host "  前端:  http://localhost:5174" -ForegroundColor White
Write-Host "  后端:  http://localhost:8082" -ForegroundColor White
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "提示: 每次启动都会先停止旧服务再重新启动" -ForegroundColor Gray
Write-Host "      各个服务在独立窗口中运行，关闭窗口即停止服务" -ForegroundColor Gray
Write-Host "      启动前请确保 MySQL 已运行" -ForegroundColor Gray
