# 后端项目

## 说明

后端基于 **Spring Boot 3.4.1** + **Java 21**，使用 **JPA/Hibernate** 进行数据库操作，**JWT** 进行认证鉴权。

## 启动方式

```bash
cd backend
mvn spring-boot:run
```

或打包后运行：

```bash
mvn clean package -DskipTests
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

默认启动在 `http://localhost:8080`

## 配置文件

`src/main/resources/application.yml`

| 配置项 | 说明 |
|---|---|
| 数据库 | MySQL `localhost:3306/ers`，用户 `root`，密码从环境变量 `MYSQL_ROOT_PASSWORD` 读取 |
| 服务器端口 | 8080 |
| JPA DDL | `validate`（仅校验，不自动建表） |
| 时区 | Asia/Shanghai |

## 接口清单

| 方法 | 路径 | 说明 | 认证 |
|---|---|---|---|
| POST | `/api/auth/login` | 登录 | 否 |
| POST | `/api/auth/register` | 注册 | 否 |
| PUT | `/api/auth/change-password` | 修改密码 | 是 |
| POST | `/api/auth/logout` | 退出登录 | 是 |
| GET | `/api/equipment` | 获取全部设备列表 | 是 |
| GET | `/api/equipment/search` | 分页搜索设备 | 是 |
| GET | `/api/equipment/{id}` | 获取设备详情 | 是 |
| GET | `/api/equipment/categories` | 获取设备分类列表 | 是 |
| GET | `/api/reservations` | 获取预约列表 | 是 |
| POST | `/api/reservations` | 创建预约 | 是 |
| GET | `/api/reservations/conflict-check` | 冲突检测 | 是 |
| PUT | `/api/reservations/{id}/approve` | 审批通过 | 是 |
| PUT | `/api/reservations/{id}/reject` | 驳回预约 | 是 |
| PUT | `/api/reservations/{id}/return` | 归还设备 | 是 |
| GET | `/api/users` | 分页查询用户列表 | 是 |
| GET | `/api/users/{id}` | 查询用户详情 | 是 |
| PUT | `/api/users/{id}` | 修改用户信息 | 是 |
| PUT | `/api/users/{id}/reset-password` | 重置密码 | 是 |
| PUT | `/api/users/{id}/role` | 修改用户角色 | 是 |
