# Equipment Reservation System · 实验室设备预约与耗材管理系统

> 面向高校实验室的设备资源管理平台 — 预约创建、冲突检测、审批流程、库存追踪

---

## 目录

- [项目简介](#项目简介)
- [产品价值](#产品价值)
- [业务流程](#业务流程)
- [核心功能](#核心功能)
- [角色与权限](#角色与权限)
- [技术架构](#技术架构)
- [API 概览](#api-概览)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [项目管理](#项目管理)
- [License](#license)

---

## 项目简介

**Equipment Reservation System** 是一个面向高校实验室的设备预约与耗材管理系统，解决实验室设备资源管理中的三个核心问题：

1. **设备使用冲突** — 多人同时预约同一设备的冲突检测
2. **审批流程缺失** — 预约需要管理员审核，避免资源滥用
3. **库存管理混乱** — 耗材出入库无记录，库存预警不及时

系统实现了从设备浏览、预约创建、冲突检测、审批管理到库存追踪的完整业务闭环。

### 为什么做这个项目

高校实验室的设备管理长期依赖人工登记，效率低、易出错、无法追溯。本项目的目标是以一个完整的业务系统来规范流程：让每台设备的预约有据可查、每个审批有迹可循、每件耗材有账可对。

---

## 产品价值

| 维度 | 说明 |
|------|------|
| 目标用户 | 高校实验室管理员、在校学生 |
| 核心场景 | 实验室设备预约、管理员审批、耗材库存管理 |
| 解决痛点 | 人工登记效率低、设备使用冲突、库存管理混乱 |
| 业务闭环 | 浏览 → 预约 → 审批 → 使用 → 归还 → 库存更新 |

---

## 业务流程

```
学生登录 → 浏览设备列表 → 选择设备 & 时间 → 提交预约
                                              ↓
                                       管理员审批 → 驳回（退回）
                                              ↓
                                           通过
                                              ↓
                                     学生领用设备 / 耗材
                                              ↓
                                     使用完毕归还设备
                                              ↓
                                       库存自动更新
```

---

## 核心功能

### 设备管理

| 功能 | 描述 |
|------|------|
| 设备浏览 | 展示设备列表，支持按分类筛选、按名称搜索 |
| 设备详情 | 查看设备规格、库存数量、当前预约状态 |
| 分类管理 | 设备分类浏览与筛选（增删改接口预留） |

### 预约管理

| 功能 | 描述 |
|------|------|
| 预约创建 | 选择设备和时间段，系统自动检测时间冲突 |
| 冲突检测 | 基于乐观锁的并发预约处理，确保同一时段不重复预约 |
| 预约审批 | 管理员通过/驳回预约申请 |
| 设备归还 | 确认设备归还，自动恢复库存 |
| 预约记录 | 查看个人/全部预约记录，按状态筛选 |

### 库存管理

| 功能 | 描述 |
|------|------|
| 入库管理 | 耗材入库记录，自动更新库存 |
| 领用管理 | 预约扣减库存，归还恢复库存 |
| 变更流水 | 后端记录 DEDUCT/RETURN 库存日志，支持追溯（管理端展示页待补充） |

### 用户管理

| 功能 | 描述 |
|------|------|
| 注册登录 | JWT 认证，Token 管理 |
| 密码管理 | 修改密码、管理员重置密码 |
| 角色管理 | 管理员修改用户角色（ADMIN / STUDENT） |

---

## 角色与权限

| 角色 | 权限范围 |
|------|----------|
| 管理员 (ADMIN) | 管理设备、审批预约、管理用户、查看所有预约记录、库存管理 |
| 学生 (STUDENT) | 浏览设备、创建预约、查看个人预约记录 |

### 默认账号

| 角色 | 账号 | 密码 |
|------|------|------|
| 管理员 | admin | Ab123456 |
| 学生 | user001 ~ user005 | Ab123456 |

---

## 技术架构

| 层级 | 技术 | 用途 |
|------|------|------|
| 前端 | React 19 + TypeScript + Vite | 用户界面 |
| UI 组件库 | Ant Design 6 | 企业级 UI 组件 |
| 后端 | Spring Boot 3.4 + Java 21 | 业务服务框架 |
| ORM | JPA / Hibernate | 对象关系映射 |
| 数据库 | MySQL 8.x | 关系型数据库 |
| 数据库初始化 | schema.sql + seed.sql | 建表与种子数据（Flyway 脚本保留但未启用） |
| 认证 | JWT (jjwt 0.12.5) | 无状态认证 |
| 密码加密 | BCrypt | 安全哈希 |
| 容器化 | Docker + Docker Compose | 多服务编排 |
| 集群部署 | Kubernetes | 生产级部署（配置就绪） |

---

## API 概览

### 认证接口 `/api/auth`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/auth/login` | 用户登录 | 否 |
| POST | `/api/auth/register` | 用户注册 | 否 |
| PUT | `/api/auth/change-password` | 修改密码 | 是 |
| POST | `/api/auth/logout` | 用户登出 | 是 |

### 设备接口 `/api/equipment`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/equipment` | 获取所有设备列表 | 是 |
| GET | `/api/equipment/search` | 分页搜索设备 | 是 |
| GET | `/api/equipment/{id}` | 获取设备详情 | 是 |
| GET | `/api/equipment/categories` | 获取设备分类 | 是 |

### 预约接口 `/api/reservations`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/reservations` | 获取预约列表 | 是 |
| POST | `/api/reservations` | 创建预约 | 是 |
| GET | `/api/reservations/conflict-check` | 冲突检测 | 是 |
| PUT | `/api/reservations/{id}/approve` | 审批通过 | 是（管理员） |
| PUT | `/api/reservations/{id}/reject` | 驳回预约 | 是（管理员） |
| PUT | `/api/reservations/{id}/return` | 确认归还 | 是（管理员） |

### 用户接口 `/api/users`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/users` | 分页用户列表 | 是（管理员） |
| GET | `/api/users/{id}` | 用户详情 | 是（管理员） |
| PUT | `/api/users/{id}` | 修改用户 | 是（管理员） |
| PUT | `/api/users/{id}/reset-password` | 重置密码 | 是（管理员） |
| PUT | `/api/users/{id}/role` | 修改角色 | 是（管理员） |
| PUT | `/api/users/{id}/blacklist` | 加入/移出失信名单 | 是（管理员） |

---

## 快速开始

### 前置条件

- Node.js 18+
- pnpm (推荐) 或 npm
- Java 21+
- MySQL 8.x
- Maven 3.x

### 1. 配置环境变量

```bash
# 后端环境变量（backend/.env）
MYSQL_ROOT_PASSWORD=your_mysql_password
```

### 2. 数据库初始化

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS ers DEFAULT CHARACTER SET utf8mb4;"

# 导入表结构和种子数据
mysql -u root -p ers < database/schema.sql
mysql -u root -p ers < database/seed.sql
```

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认启动在 `http://localhost:8082`

### 4. 启动前端

```bash
cd frontend
pnpm install
pnpm run dev
```

前端默认启动在 `http://localhost:5174`

### 5. Docker Compose 部署

```bash
# 一键启动所有服务
docker-compose up -d
```

### 6. K8s 部署

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/
```

> 首次部署前，请先修改 `k8s/00-secret.yaml` 中的占位密码。

---

## 项目结构

```
equipment-reservation-system/
├── backend/                          # Spring Boot 后端
│   └── src/main/java/com/ers/
│       ├── controller/               # 4 个 Controller
│       ├── service/                  # 4 个 Service
│       ├── entity/                   # 7 个实体类
│       ├── repository/               # 5 个 Repository
│       ├── dto/                      # 9 个 DTO
│       ├── config/                   # 配置类
│       ├── exception/                # 异常处理
│       └── filter/                   # JWT 认证过滤器
├── frontend/                         # React 前端
│   └── src/
│       ├── pages/                    # 7 个功能页面
│       ├── components/               # 通用组件
│       ├── context/                  # AuthContext
│       └── api/                      # API 调用
├── database/                         # 数据库脚本
│   ├── schema.sql                    # 建表语句
│   └── seed.sql                      # 初始化数据
├── docs/                             # 项目文档
│   ├── requirement.md                # 需求说明
│   ├── api.md                        # API 文档
│   ├── flow-diagrams.md              # 流程图、时序图、状态机
│   └── metrics-and-acceptance.md     # 指标与验收
├── k8s/                              # Kubernetes 配置
├── docker-compose.yml                # Docker 编排
└── README.md
```

---


## 项目管理

### 开发流程

本项目按照软件工程的完整流程推进，从需求分析到验收交付每个阶段都有产出：

```
需求分析 → 接口设计 → 开发实现 → 验收测试
   ↓            ↓           ↓           ↓
 需求文档      API 文档     代码实现     验收报告
```

### 项目里程碑

| 阶段 | 周期 | 交付物 | 说明 |
|------|------|--------|------|
| 需求分析 | Day 1-2 | [需求文档](/docs/requirement.md) | 用户画像、用户故事、MoSCoW 优先级、功能模块定义 |
| 接口设计 | Day 2-3 | [API 文档](/docs/api.md) | RESTful 接口定义、请求/返回格式、状态码约定 |
| 数据库设计 | Day 3 | schema.sql + Flyway 迁移脚本 | 5 张业务表设计、关系建模 |
| 后端开发 | Day 4-7 | Spring Boot 服务端代码 | 4 个 Controller、4 个 Service、JPA Repository、JWT 认证 |
| 前端开发 | Day 5-8 | React 前端代码 | 7 个功能页面、路由守卫、状态管理 |
| 联调测试 | Day 8-9 | 前后端集成 | 接口联调、异常场景覆盖 |
| 验收交付 | Day 10 | [验收报告](/docs/acceptance.md) | 功能验收、边界测试、bug 修复 |

### 技术选型决策

| 决策 | 选项 | 选择理由 |
|------|------|---------|
| 前端框架 | React 19 vs Vue 3 | React 生态成熟、组件库 Ant Design 支持好、TypeScript 集成优秀 |
| UI 组件库 | Ant Design 6 vs Material UI | Ant Design 更适合中后台管理类系统，中文文档完善 |
| 后端框架 | Spring Boot 3 vs Express | Java 生态适合复杂业务系统，JPA 减少 SQL 编写，与 MySQL 配合稳定 |
| 数据库 | MySQL 8 vs PostgreSQL | 高校环境 MySQL 更常见，utf8mb4 完全支持中文 |
| 认证方式 | JWT vs Session | 前后端分离架构下 JWT 天然适合，无需额外 session 同步 |
| 并发控制 | 乐观锁 vs 悲观锁 | 预约场景读多写少，乐观锁性能更好，版本冲突时可友好提示 |

### 范围管理

采用 MoSCoW 方法进行功能优先级管理（详见 [需求文档](/docs/requirement.md#moscow-优先级矩阵)）：

- **Must Have（必须做）**：用户认证、设备浏览、预约创建、冲突检测、审批流程、设备归还 — 构成完整业务闭环
- **Should Have（应该做）**：预约记录查询、用户管理、密码修改 — 提升体验和管理效率
- **Could Have（可以做）**：设备分类增删改、消息通知、数据报表、LDAP 集成 — v1.1+ 迭代范围（黑名单、库存流水已在当前版本实现）
- **Won't Have（本次不做）**：消息通知、数据报表、LDAP 集成

### 质量保障

- **冲突检测正确性**：使用数据库乐观锁（`@Version`）确保并发场景下预约不超卖
- **统一异常处理**：全局 `@ControllerAdvice` 捕获业务异常，统一返回格式
- **密码安全**：BCrypt 加密存储，不保留明文
- **Token 管理**：JWT 黑名单机制实现登出，支持过期校验
- **数据库初始化**：启动时由 `schema.sql` + `seed.sql` 初始化，`db/migration` 保留 Flyway 脚本但未启用

### 文档体系

| 文档 | 作用 | 面向读者 |
|------|------|---------|
| [需求说明](/docs/requirement.md) | 用户画像、用户故事、MoSCoW 优先级 | 产品/项目经理 |
| [API 文档](/docs/api.md) | 接口定义、参数说明、返回格式 | 前后端开发 |
| [验收报告](/docs/acceptance.md) | 功能验收清单、API 验证、边界测试 | 测试/项目经理 |
| [设计决策](/docs/requirement.md#技术选型决策) | 技术选型理由、架构权衡 | 架构师/技术面试 |
## License

Apache 2.0
