@echo off
chcp 65001 >nul
title 设备预约与耗材管理系统 - 一键启动
cd /d "%~dp0"

REM 设置 MySQL 密码（供 startup.ps1 和 backend 使用）
set MYSQL_ROOT_PASSWORD=YLxue615

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0startup.ps1"
pause