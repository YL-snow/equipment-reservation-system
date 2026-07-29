# 数据库脚本

## 说明

此目录存放数据库相关脚本，用于创建设备和预约管理系统的数据库结构及初始数据。

## 数据库信息

- 数据库名：`ers`
- 字符集：`utf8mb4`
- 排序规则：`utf8mb4_unicode_ci`

## 文件说明

| 文件名 | 作用 |
|---|---|
| schema.sql | 建表语句：创建数据库、创建 5 张表 |
| seed.sql | 初始数据：设备分类、设备、用户 |

## 使用方式

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
```

或手动执行：

```bash
mysql -u root -p
```

```sql
CREATE DATABASE IF NOT EXISTS ers DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ers;
SOURCE database/schema.sql;
SOURCE database/seed.sql;
```

## 表结构概览

| 表名 | 说明 |
|---|---|
| `equipment_category` | 设备分类（办公设备、网络设备、实验仪器等） |
| `equipment` | 设备/耗材主表 |
| `user` | 用户表（管理员、学生） |
| `reservation` | 预约单表 |
| `inventory_log` | 库存变更流水表 |
