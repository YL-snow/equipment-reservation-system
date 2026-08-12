@echo off
chcp 65001 >nul
title 设备预约与耗材管理系统 - 一键启动
cd /d "%~dp0"

REM 自动读取 backend\.env 中的 MYSQL_ROOT_PASSWORD
if "%MYSQL_ROOT_PASSWORD%"=="" (
    for /f "usebackq tokens=1,* delims==" %%a in ("%~dp0backend\.env") do (
        if /i "%%a"=="MYSQL_ROOT_PASSWORD" set "MYSQL_ROOT_PASSWORD=%%b"
    )
)

if "%MYSQL_ROOT_PASSWORD%"=="" (
    echo 请先设置 MySQL 密码：
    echo    set MYSQL_ROOT_PASSWORD=你的密码
    echo    startup.bat
    pause
    exit /b
)

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0startup.ps1"
pause
