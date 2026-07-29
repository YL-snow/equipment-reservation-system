# AGENTS.md

## 项目目标

实验室设备预约与耗材管理系统（Equipment Reservation and Consumables Management System）。

- 前后端分离架构
- MySQL 持久化存储
- JWT 认证
- 支持设备预约、冲突检测、审批流转

## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | React ^19.2 + TypeScript + Vite ^8.1 + Ant Design ^6.5 |
| 后端 | Spring Boot 3.4.1 + Java 21 + JPA/Hibernate |
| 数据库 | MySQL 8.x |
| 构建 | Maven + npm |
| 认证 | JWT (jjwt 0.12.5) |

## 构建与启动

### 后端

```bash
cd backend
mvn clean package -DskipTests   # 打包
mvn spring-boot:run             # 直接启动
java -jar target/backend-0.0.1-SNAPSHOT.jar  # 运行JAR
```

### 前端

```bash
cd frontend
npm install     # 安装依赖
npm run dev     # 开发模式启动（默认 5173 端口）
npm run build   # 生产构建
```

### 数据库

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
```

## 入口点

| 组件 | 路径 |
|---|---|
| 前端入口 | `frontend/src/main.tsx` |
| 路由配置 | `frontend/src/App.tsx` |
| 后端入口 | `backend/src/main/java/com/ers/Application.java` |
| 控制器 | `backend/src/main/java/com/ers/controller/` |
| 实体类 | `backend/src/main/java/com/ers/entity/` |
| 数据库初始化 | `database/schema.sql`, `database/seed.sql` |

## 项目边界

- `database/` - 数据库 DDL 和 DML 脚本
- `docs/` - 需求、接口、验收文档
- `k8s/` - Kubernetes 部署配置文件（课程拓展）
- `deploy_via_ssh.py` - 远程 SSH 部署脚本（课程拓展）

## 框架特性与注意事项

- Spring Boot 使用 `spring.jpa.hibernate.ddl-auto=validate`，表结构需提前通过 SQL 脚本创建
- Flyway 已禁用（`spring.flyway.enabled=false`），不进行自动迁移
- JWT token 存储在 localStorage，请求时通过 axios 拦截器自动注入 Authorization header
- 前端通过 Vite proxy 将 `/api` 请求代理到 `http://localhost:8080`
- 设备表使用乐观锁（`@Version`）处理并发预约
