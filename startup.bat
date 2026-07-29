@echo off
chcp 65001 >nul
title 设备预约与耗材管理系统 - 一键启动
cd /d "%~dp0"

REM 请先设置 MySQL 密码环境变量，或直接修改下方密码
set MYSQL_ROOT_PASSWORD=%MYSQL_ROOT_PASSWORD%

if "%MYSQL_ROOT_PASSWORD%"=="" (
    echo 请先设置 MySQL 密码：
    echo    set MYSQL_ROOT_PASSWORD=你的密码
    echo    startup.bat
    pause
    exit /b
)

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0startup.ps1"
pause