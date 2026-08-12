# 核心流程与状态图

本文用图说明设备预约系统的页面流转、审批时序与预约状态机，供评审、文档查阅和面试讲解使用。

## 1. 用户页面流程图

```mermaid
flowchart TD
    A[登录 / 注册] --> B[设备列表]
    B --> C[新建预约]
    C --> D{时间冲突 / 库存校验}
    D -->|冲突或库存不足| C
    D -->|校验通过| E[预约记录 PENDING]
    B --> F[预约记录]
    E --> F
    G[管理员] --> H[审批管理]
    H -->|通过| I[APPROVED 库存扣减]
    H -->|驳回| J[REJECTED]
    I --> K[确认归还]
    K --> L[RETURNED 库存加回]
    G --> M[用户管理]
```

## 2. 预约审批时序图

```mermaid
sequenceDiagram
    participant U as 用户
    participant FE as 前端
    participant BE as 后端
    participant DB as 数据库
    participant AD as 管理员

    U->>FE: 填写设备、数量、时间
    FE->>BE: POST /api/reservations
    BE->>DB: 查询设备可用库存
    BE->>DB: 查询时间冲突预约
    alt 库存不足或时间冲突
        BE-->>FE: 400 当前可用库存不足 / 时间冲突
        FE-->>U: 提示重新选择
    else 校验通过
        BE->>DB: 保存预约 PENDING
        DB-->>BE: 预约 ID
        BE-->>FE: 提交成功
        FE-->>U: 跳转预约记录
    end

    AD->>FE: 打开审批管理
    AD->>FE: 点击通过 / 驳回
    FE->>BE: PUT /api/reservations/{id}/approve
    BE->>DB: 行锁查询设备
    BE->>DB: 原子扣减库存 availableQty >= quantity
    alt 扣减失败
        BE-->>FE: 400 库存已被其他预约占用
    else 扣减成功
        BE->>DB: 状态更新 APPROVED 并写库存日志
        BE-->>FE: 审批通过
    end

    AD->>FE: 确认归还
    FE->>BE: PUT /api/reservations/{id}/return
    BE->>DB: 校验归还数量不超出总库存
    BE->>DB: 状态更新 RETURNED 并加回库存
```

## 3. 预约状态机

```mermaid
stateDiagram-v2
    [*] --> PENDING: 提交预约
    PENDING --> APPROVED: 审批通过
    PENDING --> REJECTED: 审批驳回
    APPROVED --> RETURNED: 确认归还
    PENDING --> CANCELLED: 取消预约（枚举预留）
    RETURNED --> [*]
```

| 当前状态 | 触发动作 | 条件 | 目标状态 | 系统处理 |
|---|---|---|---|---|
| PENDING | 提交 | 时间不冲突、数量 <= 可用库存 | PENDING | 保存预约 |
| PENDING | 审批通过 | 无冲突、库存足够 | APPROVED | 原子扣减库存、写库存日志 |
| PENDING | 审批驳回 | 管理员操作 | REJECTED | 更新状态 |
| APPROVED | 确认归还 | 归还后库存不超总库存 | RETURNED | 库存加回、写库存日志 |
| PENDING | 取消 | 预留能力 | CANCELLED | 当前接口未开放 |

## 4. 关键设计点

- 库存扣减使用数据库原子更新，避免并发审批超卖。
- PENDING 阶段只做校验不扣库存，避免未审批预约长期占用库存。
- 审批时再次进行冲突与库存校验，防止审批前数据变化。
- 归还时校验可用库存 + 归还数量 <= 总库存，防止重复归还。
