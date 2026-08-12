# 接口文档

## 接口说明

所有接口采用 RESTful 风格，基路径为 `http://localhost:8082/api`。

统一返回格式：

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "操作成功",
  "data": {}
}
```

错误返回格式：

```json
{
  "success": false,
  "code": "ERROR",
  "message": "错误描述"
}
```

认证方式：在 HTTP Header 中添加 `Authorization: Bearer <token>`。

## 接口列表

### 认证接口 `/api/auth`

| 方法 | 路径 | 请求参数 | 返回说明 | 认证 |
|---|---|---|---|---|
| POST | `/api/auth/login` | `{"userId":"admin","password":"Ab123456"}` | `{token, user信息}` | 否 |
| POST | `/api/auth/register` | `{"userId":"xxx","name":"xxx","password":"xxx"}` | 注册成功消息 | 否 |
| PUT | `/api/auth/change-password` | `{"oldPassword":"xxx","newPassword":"xxx"}` | 修改成功消息 | 是 |
| POST | `/api/auth/logout` | 无 | 登出成功消息 | 是 |

### 设备接口 `/api/equipment`

| 方法 | 路径 | 请求参数 | 返回说明 | 认证 |
|---|---|---|---|---|
| GET | `/api/equipment` | 无 | 所有设备列表 | 是 |
| GET | `/api/equipment/search` | `page, size, name, categoryId` | 分页设备列表 | 是 |
| GET | `/api/equipment/{id}` | 路径参数 | 设备详情 | 是 |
| GET | `/api/equipment/categories` | 无 | 分类列表 | 是 |

### 预约接口 `/api/reservations`

| 方法 | 路径 | 请求参数 | 返回说明 | 认证 |
|---|---|---|---|---|
| GET | `/api/reservations` | 无 | 预约列表（管理员看全部，学生看自己的） | 是 |
| POST | `/api/reservations` | `{"equipmentId":1,"quantity":1,"startTime":"...","endTime":"..."}` | 预约ID和状态 | 是 |
| GET | `/api/reservations/conflict-check` | `equipmentId, startTime, endTime` | 冲突检测结果 | 是 |
| PUT | `/api/reservations/{id}/approve` | 路径参数 | 审批成功消息 | 是 |
| PUT | `/api/reservations/{id}/reject` | 路径参数 | 驳回成功消息 | 是 |
| PUT | `/api/reservations/{id}/return` | 路径参数 | 归还成功消息 | 是 |

### 用户接口 `/api/users`

| 方法 | 路径 | 请求参数 | 返回说明 | 认证 |
|---|---|---|---|---|
| GET | `/api/users` | `page, size, name, userId` | 分页用户列表 | 是 |
| GET | `/api/users/{id}` | 路径参数 | 用户详情 | 是 |
| PUT | `/api/users/{id}` | `{"name":"新姓名"}` | 修改成功消息 | 是 |
| PUT | `/api/users/{id}/reset-password` | 路径参数 | 重置成功（密码为 Ab123456） | 是 |
| PUT | `/api/users/{id}/role` | `{"role":"ADMIN"}` | 修改成功消息 | 是 |

## 接口设计原则

- 使用 RESTful 风格
- 提供统一的错误返回格式
- 接口路径以 `/api/` 开头
- 敏感操作需要 JWT 认证
- 预约时间冲突由后端检测
