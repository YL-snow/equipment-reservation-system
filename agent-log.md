# VibeCoding 全栈开发日志

## 一、关键 Prompt 记录

### Prompt 1：生成开发计划
> "你是一名全栈架构师。请阅读当前项目根目录的 PLAN.md（如果不存在则忽略），然后重新生成一份完整的、可执行的开发计划，包含目录结构、数据库设计、API 设计、前端路由设计、开发阶段划分。技术栈为 Spring Boot 3 + JDK 17 + MySQL 8.0 + Flyway + React 18 + Vite + TypeScript + Ant Design 5。"

### Prompt 2：Phase 1 基础设施构建
> "请创建完整的 Docker Compose 配置（MySQL 8.0 + Spring Boot 后端），生成 backend/ 目录下的完整 pom.xml、application.yml、.env，以及 frontend/ 目录下的 Vite + React + TypeScript + Ant Design 空项目，确保前后端能正常启动。"

### Prompt 3：Phase 2 数据库与实体
> "请根据 PLAN.md 中的 schema.sql 设计，生成 Flyway 迁移脚本 V1__init_tables.sql，以及对应的 JPA Entity（Equipment、Reservation、InventoryLog）、Repository 接口，包括冲突查询、悲观锁查询、原子扣库存/归还库存等自定义 SQL。"

### Prompt 4：Phase 3 后端核心 API
> "请实现 ReservationService 的四个核心方法（createReservation、approveReservation、rejectReservation、returnReservation），要求使用 @Transactional 保证原子性；实现 EquipmentController 和 ReservationController 的 REST 接口；实现 GlobalExceptionHandler 统一异常处理；实现 Result 统一响应格式。"

---

## 二、遇到的问题及修复方法

### 问题 1：数据库密码配置导致连接失败

**现象**：后端启动时抛出 `java.sql.SQLException: Access denied for user 'root'@'localhost'`。

**原因**：`application.yml` 中数据库密码通过 `${MYSQL_ROOT_PASSWORD}` 引用环境变量，但未设置该变量或 `.env` 文件未被 Spring Boot 自动加载。

**修复**：在启动前通过 `$env:MYSQL_ROOT_PASSWORD="root"` 设置环境变量，或在 `application.yml` 中提供兜底默认值 `${MYSQL_ROOT_PASSWORD:root}`。

### 问题 2：TINYINT 类型映射异常

**现象**：启动时 Hibernate 报错 `Wrong column type encountered`，期望 `tinyint` 但实际映射为 `boolean`。

**原因**：MySQL 的 `TINYINT(1)` 默认会被 JDBC 驱动映射为 `boolean`/`Boolean`，而非 `Integer`。Equipment 实体中 `status` 字段定义为 `TINYINT` 但无长度指定。

**修复**：在实体字段上添加 `columnDefinition = "TINYINT"` 明确指定类型，并将 Java 类型定义为 `Integer` 而非 `Boolean`，确保 JDBC 按数值类型处理。

### 问题 3：懒加载导致 JSON 序列化 500 错误

**现象**：调用 `GET /api/reservations` 时返回 500 错误，日志显示 `could not initialize proxy - no Session`。

**原因**：Reservation 实体中 `@ManyToOne(fetch = FetchType.LAZY)` 的 `equipment` 字段，在 Service 层返回后 Session 已关闭，Jackson 序列化时尝试访问懒加载代理对象导致异常。

**修复**：在 Repository 中使用 `JOIN FETCH` 提前加载关联：

```java
@Query("SELECT r FROM Reservation r JOIN FETCH r.equipment")
List<Reservation> findAllWithEquipment();
```

同时在 `application.yml` 中配置 `spring.jackson.serialization.fail-on-empty-beans=false` 作为兜底。

---

## 三、人工判断和决策点

### 决策 1：选择本地 MySQL 而非 Docker

- **方案A**：Docker Compose 启动 MySQL，隔离环境、配置简单
- **方案B**：使用本地已安装的 MySQL 8.0，节省资源、无需 Docker 环境

**选择**：**方案B（本地 MySQL）**。原因：开发机上已安装 MySQL 8.0，无需额外安装 Docker；Flyway 可自动建表，无需手动维护容器生命周期。但保留了 `docker-compose.yml` 方案供生产环境使用。

### 决策 2：使用 JOIN FETCH 修复懒加载

- **方案A**：保持 LAZY 加载，在 Controller 层使用 `@Transactional` 保持 Session 打开（Open Session in View）
- **方案B**：在 Repository 层使用 `JOIN FETCH` 或 `@EntityGraph` 主动加载关联

**选择**：**方案B（JOIN FETCH）**。原因：OSIV 模式会将事务延长到视图层，在高并发场景下容易引发连接池耗尽。JOIN FETCH 明确、可预测，符合 DDD 仓储模式的最佳实践。

### 决策 3：冲突预检使用 GET 而非 POST

- **方案A**：POST 请求，将参数放在 Request Body 中，语义上更接近"查询"
- **方案B**：GET 请求，参数通过 Query String 传递，符合 REST 语义

**选择**：**方案B（GET）**。原因：冲突预检是只读查询、幂等操作，符合 GET 语义；便于前端缓存和条件预取，且无副作用。

### 决策 4：使用枚举而非字符串表示状态

- **方案A**：数据库中存储字符串（如 `VARCHAR(20)`），Java 中也使用 String
- **方案B**：数据库存字符串，Java 中使用枚举（`ReservationStatus`），通过 `@Enumerated(EnumType.STRING)` 映射

**选择**：**方案B（枚举）**。原因：类型安全，编译期即可发现非法状态值；代码可读性好，IDE 自动补全；与数据库字符串值兼容，不影响查询。

---

## 四、开发反思与总结

### 1. Prompt 粒度的重要性

初期尝试使用一个超大 Prompt 生成整个项目，但 AI 输出的质量不稳定，容易遗漏细节。后续拆分为 Plan → Phase 1 → Phase 2 → Phase 3 → Phase 4 逐步推进，每个 Prompt 聚焦一个明确目标，输出质量和一致性显著提升。

### 2. 代码一致性检查

AI 在多次对话中容易产生不一致，例如 Controller 中引用的 DTO 类名与实际文件不匹配、实体中字段名与数据库列名不对应等。每次生成后需要人工或通过编译验证一致性，并及时修正。

### 3. 事务与并发是核心复杂度

本系统的最大复杂度不在于 CRUD，而在于库存扣减的原子性和时间冲突检测的并发安全。最终方案：
- 库存扣减使用 `UPDATE ... WHERE available_qty >= ?` 行级原子操作
- 冲突检测在事务内使用 `PESSIMISTIC_WRITE` 锁行 + 再次检测兜底
- 乐观锁（`@Version`）作为最后防线

三层保障确保在并发场景下不会超卖。

### 4. VibeCoding 的适用边界

VibeCoding 在快速原型、建表建项目、搭建 CRUD 层时效率极高，但在以下场景需要人工介入：
- **复杂事务逻辑**：多步原子操作的条件判断顺序
- **性能优化**：懒加载策略、N+1 查询问题
- **安全配置**：CORS 跨域、密码环境变量
- **工具链调试**：Flyway 迁移冲突、Maven 依赖版本兼容

### 5. 文档伴随开发

AI 生成代码的同时同步生成文档（schema.sql、api.md、seed.sql），确保文档与代码始终保持一致，避免了传统开发中"代码写完了但文档没人更新"的问题。

### 6. 改进方向

- 增加审批/归还操作的权限控制（目前无认证鉴权）
- 增加前端 loading 和错误提示的精细化处理
- 补充集成测试覆盖核心事务场景（并发扣库存、时间冲突边界）
- 考虑使用 Redisson 分布式锁替代数据库悲观锁，提升扩展性
